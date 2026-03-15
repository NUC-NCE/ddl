package com.datacheck.db;

import com.datacheck.Config;
import com.datacheck.model.*;

import java.sql.*;
import java.util.*;

/**
 * GaussDB DDL 信息获取器
 */
public class GaussDdlFetcher {
    private final Connection connection;
    private final Config config;

    public GaussDdlFetcher(Connection connection, Config config) {
        this.connection = connection;
        this.config = config;
    }

    /**
     * 获取表的完整 DDL
     */
    public TableDdl getTableDdl(String tableName) throws SQLException {
        TableDdl tableDdl = new TableDdl(tableName);
        tableDdl.setColumns(getColumns(tableName));
        tableDdl.setPrimaryKey(getPrimaryKey(tableName));
        tableDdl.setTablespace(getTablespace(tableName));
        return tableDdl;
    }

    /**
     * 获取表的列信息
     */
    public List<ColumnDdl> getColumns(String tableName) throws SQLException {
        List<ColumnDdl> columns = new ArrayList<>();
        String sql = "SELECT column_name, data_type, character_maximum_length, " +
                     "numeric_precision, numeric_scale, is_nullable, column_default " +
                     "FROM information_schema.columns " +
                     "WHERE table_name = ? AND table_schema = current_schema() " +
                     "ORDER BY ordinal_position";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, tableName);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                ColumnDdl column = new ColumnDdl();
                column.setColumnName(rs.getString("column_name"));
                column.setDataType(rs.getString("data_type"));
                column.setDataLength(rs.getObject("character_maximum_length") != null ?
                        rs.getInt("character_maximum_length") : null);
                column.setDataPrecision(rs.getObject("numeric_precision") != null ?
                        rs.getInt("numeric_precision") : null);
                column.setDataScale(rs.getObject("numeric_scale") != null ?
                        rs.getInt("numeric_scale") : null);
                column.setNullable("YES".equals(rs.getString("is_nullable")));
                column.setDefaultValue(rs.getString("column_default"));
                columns.add(column);
            }
        }
        return columns;
    }

    /**
     * 获取表的主键信息
     */
    public PrimaryKey getPrimaryKey(String tableName) throws SQLException {
        String sql = "SELECT tc.constraint_name " +
                     "FROM information_schema.table_constraints tc " +
                     "JOIN information_schema.key_column_usage kcu " +
                     "ON tc.constraint_name = kcu.constraint_name " +
                     "AND tc.table_schema = kcu.table_schema " +
                     "WHERE tc.constraint_type = 'PRIMARY KEY' " +
                     "AND tc.table_name = ? " +
                     "AND tc.table_schema = current_schema()";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, tableName);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                PrimaryKey pk = new PrimaryKey(rs.getString("constraint_name"), tableName);
                // 获取主键列
                String pkColSql = "SELECT kcu.column_name " +
                                 "FROM information_schema.table_constraints tc " +
                                 "JOIN information_schema.key_column_usage kcu " +
                                 "ON tc.constraint_name = kcu.constraint_name " +
                                 "WHERE tc.constraint_type = 'PRIMARY KEY' " +
                                 "AND tc.table_name = ? " +
                                 "AND tc.table_schema = current_schema() " +
                                 "ORDER BY kcu.ordinal_position";
                try (PreparedStatement pkStmt = connection.prepareStatement(pkColSql)) {
                    pkStmt.setString(1, tableName);
                    ResultSet pkRs = pkStmt.executeQuery();
                    while (pkRs.next()) {
                        pk.addColumn(pkRs.getString("column_name"));
                    }
                }
                return pk;
            }
        }
        return null;
    }

    /**
     * 获取表的表空间
     */
    public String getTablespace(String tableName) throws SQLException {
        // GaussDB/PostgreSQL 使用 tablespace
        String sql = "SELECT tablespace FROM pg_tables WHERE tablename = ? AND schemaname = current_schema()";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, tableName);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("tablespace");
            }
        }
        return null;
    }

    /**
     * 获取表的所有索引
     */
    public List<IndexDdl> getIndexes(String tableName) throws SQLException {
        List<IndexDdl> indexes = new ArrayList<>();
        String sql = "SELECT indexname, indexdef " +
                     "FROM pg_indexes " +
                     "WHERE tablename = ? AND schemaname = current_schema()";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, tableName);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String indexName = rs.getString("indexname");
                String indexDef = rs.getString("indexdef");

                // 解析 indexdef 获取索引类型和列
                IndexDdl index = parseIndexDef(indexName, tableName, indexDef);
                indexes.add(index);
            }
        }
        return indexes;
    }

    private IndexDdl parseIndexDef(String indexName, String tableName, String indexDef) {
        IndexDdl index = new IndexDdl(indexName, tableName);

        // 解析索引定义
        // 格式: CREATE [UNIQUE] INDEX index_name ON table_name USING btree (columns)
        boolean unique = indexDef.toUpperCase().contains("UNIQUE");
        index.setUnique(unique);

        // 解析索引类型
        if (indexDef.toUpperCase().contains("USING HASH")) {
            index.setIndexType("HASH");
        } else if (indexDef.toUpperCase().contains("USING BTREE")) {
            index.setIndexType("BTREE");
        } else if (indexDef.toUpperCase().contains("USING GIST")) {
            index.setIndexType("GIST");
        } else if (indexDef.toUpperCase().contains("USING GIN")) {
            index.setIndexType("GIN");
        }

        // 解析列名 - 简单解析括号内的内容
        int start = indexDef.indexOf('(');
        int end = indexDef.lastIndexOf(')');
        if (start > 0 && end > start) {
            String columnsStr = indexDef.substring(start + 1, end);
            String[] cols = columnsStr.split(",");
            for (String col : cols) {
                // 去除空格和可能的排序方向
                col = col.trim().split(" ")[0];
                index.addColumn(col);
            }
        }

        return index;
    }

    /**
     * 获取所有表的表名列表
     */
    public List<String> getAllTables() throws SQLException {
        List<String> tables = new ArrayList<>();
        String sql = "SELECT tablename FROM pg_tables " +
                     "WHERE schemaname = current_schema() ORDER BY tablename";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                tables.add(rs.getString("tablename"));
            }
        }
        return tables;
    }
}
