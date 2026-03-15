package com.datacheck.compare;

import com.datacheck.Config;
import com.datacheck.db.DatabaseConnector;
import com.datacheck.db.GaussDdlFetcher;
import com.datacheck.db.OracleDdlFetcher;
import com.datacheck.model.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * DDL 校验主类
 */
public class DdlComparatorImpl implements AutoCloseable {
    private final Config config;
    private final DatabaseConnector connector;
    private final OracleDdlFetcher oracleFetcher;
    private final GaussDdlFetcher gaussFetcher;

    private final TableDdlComparator tableDdlComparator = new TableDdlComparator();
    private final IndexComparator indexComparator = new IndexComparator();
    private final PrimaryKeyComparator primaryKeyComparator = new PrimaryKeyComparator();
    private final TablespaceComparator tablespaceComparator = new TablespaceComparator();

    public DdlComparatorImpl(Config config) throws SQLException {
        this.config = config;
        this.connector = new DatabaseConnector(config);
        connector.connectAll();

        this.oracleFetcher = new OracleDdlFetcher(connector.getOracleConnection(), config);
        this.gaussFetcher = new GaussDdlFetcher(connector.getGaussConnection(), config);
    }

    /**
     * 使用自定义连接构造
     */
    public DdlComparatorImpl(Connection oracleConnection, Connection gaussConnection, Config config) {
        this.config = config;
        this.connector = null;
        this.oracleFetcher = new OracleDdlFetcher(oracleConnection, config);
        this.gaussFetcher = new GaussDdlFetcher(gaussConnection, config);
    }

    /**
     * 执行 DDL 校验
     */
    public DdlCompareResult compare(List<String> tables) {
        DdlCompareResult result = new DdlCompareResult();
        result.setTotalTables(tables.size());

        int threadCount = config.getThreadCount() > 0 ? config.getThreadCount() : 4;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        List<Future<List<DdlDifference>>> futures = new ArrayList<>();

        for (String tableName : tables) {
            futures.add(executor.submit(() -> compareTable(tableName)));
        }

        int matched = 0;
        for (Future<List<DdlDifference>> future : futures) {
            try {
                List<DdlDifference> diffs = future.get();
                if (diffs.isEmpty()) {
                    matched++;
                }
                result.addDifferences(diffs);
            } catch (InterruptedException | ExecutionException e) {
                System.err.println("Error comparing table: " + e.getMessage());
            }
        }

        executor.shutdown();
        result.setMatchedTables(matched);
        result.setTablesWithDifferences(result.getTotalTables() - matched);

        return result;
    }

    /**
     * 比较单个表
     */
    private List<DdlDifference> compareTable(String tableName) {
        List<DdlDifference> differences = new ArrayList<>();

        try {
            Config.Options options = config.getOptions();
            if (options == null) {
                options = new Config.Options();
            }

            // 获取表结构
            TableDdl oracleTable = oracleFetcher.getTableDdl(tableName);
            TableDdl gaussTable = gaussFetcher.getTableDdl(tableName);

            // 比较表结构
            if (options.isCompareColumn()) {
                differences.addAll(tableDdlComparator.compare(oracleTable, gaussTable));
            }

            // 比较主键
            if (options.isComparePrimaryKey()) {
                differences.addAll(primaryKeyComparator.compare(
                        oracleTable != null ? oracleTable.getPrimaryKey() : null,
                        gaussTable != null ? gaussTable.getPrimaryKey() : null,
                        tableName));
            }

            // 比较表空间
            if (options.isCompareTablespace()) {
                String oracleTs = oracleTable != null ? oracleTable.getTablespace() : null;
                String gaussTs = gaussTable != null ? gaussTable.getTablespace() : null;
                differences.addAll(tablespaceComparator.compare(oracleTs, gaussTs, tableName));
            }

            // 比较索引
            if (options.isCompareIndex()) {
                List<IndexDdl> oracleIndexes = oracleFetcher.getIndexes(tableName);
                List<IndexDdl> gaussIndexes = gaussFetcher.getIndexes(tableName);
                differences.addAll(indexComparator.compare(oracleIndexes, gaussIndexes, tableName));
            }

        } catch (SQLException e) {
            System.err.println("Error fetching DDL for table " + tableName + ": " + e.getMessage());
            DdlDifference diff = new DdlDifference(DifferenceType.DIFFERENT, tableName, "ERROR", tableName);
            diff.setMessage("Error: " + e.getMessage());
            differences.add(diff);
        }

        return differences;
    }

    /**
     * 关闭连接
     */
    public void close() {
        if (connector != null) {
            connector.closeAll();
        }
    }
}
