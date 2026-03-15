package com.datacheck.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * 表结构定义
 */
public class TableDdl {
    private String tableName;
    private List<ColumnDdl> columns = new ArrayList<>();
    private PrimaryKey primaryKey;
    private String tablespace;
    private String comment;
    private Map<String, String> columnComments = new HashMap<>();

    public TableDdl() {
    }

    public TableDdl(String tableName) {
        this.tableName = tableName;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public List<ColumnDdl> getColumns() {
        return columns;
    }

    public void setColumns(List<ColumnDdl> columns) {
        this.columns = columns;
    }

    public void addColumn(ColumnDdl column) {
        this.columns.add(column);
    }

    public PrimaryKey getPrimaryKey() {
        return primaryKey;
    }

    public void setPrimaryKey(PrimaryKey primaryKey) {
        this.primaryKey = primaryKey;
    }

    public String getTablespace() {
        return tablespace;
    }

    public void setTablespace(String tablespace) {
        this.tablespace = tablespace;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Map<String, String> getColumnComments() {
        return columnComments;
    }

    public void setColumnComments(Map<String, String> columnComments) {
        this.columnComments = columnComments;
    }

    public ColumnDdl getColumn(String columnName) {
        for (ColumnDdl column : columns) {
            if (column.getColumnName().equalsIgnoreCase(columnName)) {
                return column;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE TABLE ").append(tableName).append(" (\n");
        for (int i = 0; i < columns.size(); i++) {
            sb.append("  ").append(columns.get(i));
            if (i < columns.size() - 1) {
                sb.append(",");
            }
            sb.append("\n");
        }
        if (primaryKey != null && primaryKey.getColumns().size() > 0) {
            sb.append("  PRIMARY KEY (");
            sb.append(String.join(", ", primaryKey.getColumns()));
            sb.append(")\n");
        }
        sb.append(")");
        if (tablespace != null && !tablespace.isEmpty()) {
            sb.append(" TABLESPACE ").append(tablespace);
        }
        return sb.toString();
    }
}
