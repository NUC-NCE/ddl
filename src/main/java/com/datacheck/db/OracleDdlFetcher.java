package com.datacheck.db;

import com.datacheck.Config;
import com.datacheck.model.*;

import java.sql.*;
import java.util.*;

/**
 * Oracle DDL 信息获取器
 */
public class OracleDdlFetcher {
    private final Connection connection;
    private final Config config;

    public OracleDdlFetcher(Connection connection, Config config) {
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
        String sql = "SELECT column_name, data_type, data_length, data_precision, data_scale, " +
                     "nullable, data_default " +
                     "FROM all_tab_columns " +
                     "WHERE owner = UPPER(?) AND table_name = UPPER(?) " +
                     "ORDER BY column_id";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, config.getOracle().getUsername());
            stmt.setString(2, tableName);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                ColumnDdl column = new ColumnDdl();
                column.setColumnName(rs.getString("column_name"));
                column.setDataType(rs.getString("data_type"));
                column.setDataLength(rs.getObject("data_length") != null ? rs.getInt("data_length") : null);
                column.setDataPrecision(rs.getObject("data_precision") != null ? rs.getInt("data_precision") : null);
                column.setDataScale(rs.getObject("data_scale") != null ? rs.getInt("data_scale") : null);
                column.setNullable("Y".equals(rs.getString("nullable")));
                column.setDefaultValue(rs.getString("data_default"));
                columns.add(column);
            }
        }
        return columns;
    }

    /**
     * 获取表的主键信息
     */
    public PrimaryKey getPrimaryKey(String tableName) throws SQLException {
        String sql = "SELECT cons.constraint_name " +
                     "FROM all_constraints cons, all_cons_columns cols " +
                     "WHERE cons.constraint_name = cols.constraint_name " +
                     "AND cons.constraint_type = 'P' " +
                     "AND cols.owner = UPPER(?) " +
                     "AND cols.table_name = UPPER(?) " +
                     "ORDER BY cols.position";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, config.getOracle().getUsername());
            stmt.setString(2, tableName);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                PrimaryKey pk = new PrimaryKey(rs.getString("constraint_name"), tableName);
                // 获取主键列
                String pkColSql = "SELECT column_name " +
                                 "FROM all_cons_columns " +
                                 "WHERE constraint_name = ? " +
                                 "AND owner = UPPER(?) " +
                                 "AND table_name = UPPER(?) " +
                                 "ORDER BY position";
                try (PreparedStatement pkStmt = connection.prepareStatement(pkColSql)) {
                    pkStmt.setString(1, pk.getConstraintName());
                    pkStmt.setString(2, config.getOracle().getUsername());
                    pkStmt.setString(3, tableName);
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
        String sql = "SELECT tablespace_name FROM all_tables " +
                     "WHERE owner = UPPER(?) AND table_name = UPPER(?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, config.getOracle().getUsername());
            stmt.setString(2, tableName);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("tablespace_name");
            }
        }
        return null;
    }

    /**
     * 获取表的所有索引
     */
    public List<IndexDdl> getIndexes(String tableName) throws SQLException {
        List<IndexDdl> indexes = new ArrayList<>();
        String sql = "SELECT index_name, index_type, uniqueness, tablespace_name " +
                     "FROM all_indexes " +
                     "WHERE owner = UPPER(?) AND table_name = UPPER(?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, config.getOracle().getUsername());
            stmt.setString(2, tableName);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                IndexDdl index = new IndexDdl(rs.getString("index_name"), tableName);
                index.setIndexType(rs.getString("index_type"));
                index.setUnique("UNIQUE".equals(rs.getString("uniqueness")));
                index.setTablespace(rs.getString("tablespace_name"));

                // 获取索引列
                String colSql = "SELECT column_name, column_position " +
                               "FROM all_ind_columns " +
                               "WHERE index_owner = UPPER(?) AND index_name = ? " +
                               "ORDER BY column_position";
                try (PreparedStatement colStmt = connection.prepareStatement(colSql)) {
                    colStmt.setString(1, config.getOracle().getUsername());
                    colStmt.setString(2, index.getIndexName());
                    ResultSet colRs = colStmt.executeQuery();
                    while (colRs.next()) {
                        index.addColumn(colRs.getString("column_name"));
                    }
                }
                indexes.add(index);
            }
        }
        return indexes;
    }

    /**
     * 获取所有表的表名列表
     */
    public List<String> getAllTables() throws SQLException {
        List<String> tables = new ArrayList<>();
        String sql = "SELECT table_name FROM all_tables " +
                     "WHERE owner = UPPER(?) ORDER BY table_name";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, config.getOracle().getUsername());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                tables.add(rs.getString("table_name"));
            }
        }
        return tables;
    }
}
