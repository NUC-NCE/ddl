package com.datacheck.sdk;

import com.datacheck.Config;
import com.datacheck.compare.DdlComparatorImpl;
import com.datacheck.model.DdlCompareResult;
import com.datacheck.sdk.config.DatabaseConfig;
import com.datacheck.sdk.config.SdkOptions;
import com.datacheck.sdk.model.ComparisonSummary;

import java.sql.SQLException;
import java.util.List;

/**
 * DDL 校验 SDK 主入口
 */
public class DdlComparator {
    private final DdlComparatorImpl comparator;

    private DdlComparator(DdlComparatorImpl comparator) {
        this.comparator = comparator;
    }

    /**
     * 执行校验
     */
    public DdlCompareResult compare(List<String> tables) {
        return comparator.compare(tables);
    }

    /**
     * 执行校验并返回汇总
     */
    public ComparisonSummary compareAndSummarize(List<String> tables) {
        DdlCompareResult result = compare(tables);
        return toSummary(result);
    }

    /**
     * 关闭连接
     */
    public void close() {
        comparator.close();
    }

    private ComparisonSummary toSummary(DdlCompareResult result) {
        return new ComparisonSummary(
                result.getTotalTables(),
                result.getMatchedTables(),
                result.getTablesWithDifferences(),
                result.getAllDifferences().size()
        );
    }

    /**
     * 创建 Builder
     */
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private DatabaseConfig oracleConfig;
        private DatabaseConfig gaussConfig;
        private SdkOptions options;

        public Builder oracle(DatabaseConfig config) {
            this.oracleConfig = config;
            return this;
        }

        public Builder gauss(DatabaseConfig config) {
            this.gaussConfig = config;
            return this;
        }

        public Builder options(SdkOptions options) {
            this.options = options;
            return this;
        }

        public DdlComparator build() throws SQLException {
            DdlComparatorImpl impl = new DdlComparatorImpl(createConfig());
            return new DdlComparator(impl);
        }

        public ComparisonSummary compare(List<String> tables) throws SQLException {
            try (DdlComparator comp = build()) {
                return comp.compareAndSummarize(tables);
            }
        }

        public DdlCompareResult compareDetail(List<String> tables) throws SQLException {
            try (DdlComparator comp = build()) {
                return comp.compare(tables);
            }
        }

        private Config createConfig() {
            Config config = new Config();

            Config.OracleConfig oracle = new Config.OracleConfig();
            if (oracleConfig != null) {
                oracle.setJdbcUrl(oracleConfig.getJdbcUrl());
                oracle.setUsername(oracleConfig.getUsername());
                oracle.setPassword(oracleConfig.getPassword());
            }
            config.setOracle(oracle);

            Config.GaussConfig gauss = new Config.GaussConfig();
            if (gaussConfig != null) {
                gauss.setJdbcUrl(gaussConfig.getJdbcUrl());
                gauss.setUsername(gaussConfig.getUsername());
                gauss.setPassword(gaussConfig.getPassword());
            }
            config.setGauss(gauss);

            Config.Options opts = new Config.Options();
            if (options != null) {
                opts.setCompareTablespace(options.isCompareTablespace());
                opts.setCompareIndex(options.isCompareIndex());
                opts.setComparePrimaryKey(options.isComparePrimaryKey());
                opts.setCompareColumn(options.isCompareColumn());
                config.setThreadCount(options.getThreadCount());
                config.setOutputDir(options.getOutputDir());
            }
            config.setOptions(opts);

            return config;
        }
    }
}
