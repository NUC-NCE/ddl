package com.datacheck.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 索引定义
 */
public class IndexDdl {
    private String indexName;
    private String tableName;
    private List<String> columns = new ArrayList<>();
    private String indexType;
    private boolean unique;
    private String tablespace;

    public IndexDdl() {
    }

    public IndexDdl(String indexName, String tableName) {
        this.indexName = indexName;
        this.tableName = tableName;
    }

    public String getIndexName() {
        return indexName;
    }

    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public List<String> getColumns() {
        return columns;
    }

    public void setColumns(List<String> columns) {
        this.columns = columns;
    }

    public void addColumn(String column) {
        this.columns.add(column);
    }

    public String getIndexType() {
        return indexType;
    }

    public void setIndexType(String indexType) {
        this.indexType = indexType;
    }

    public boolean isUnique() {
        return unique;
    }

    public void setUnique(boolean unique) {
        this.unique = unique;
    }

    public String getTablespace() {
        return tablespace;
    }

    public void setTablespace(String tablespace) {
        this.tablespace = tablespace;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("INDEX ").append(indexName);
        if (unique) {
            sb.append(" UNIQUE");
        }
        sb.append(" ON ").append(tableName).append(" (");
        sb.append(String.join(", ", columns));
        sb.append(")");
        if (indexType != null && !indexType.isEmpty()) {
            sb.append(" TYPE ").append(indexType);
        }
        if (tablespace != null && !tablespace.isEmpty()) {
            sb.append(" TABLESPACE ").append(tablespace);
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IndexDdl indexDdl = (IndexDdl) o;
        return unique == indexDdl.unique &&
                Objects.equals(indexName, indexDdl.indexName) &&
                Objects.equals(tableName, indexDdl.tableName) &&
                Objects.equals(columns, indexDdl.columns) &&
                Objects.equals(indexType, indexDdl.indexType) &&
                Objects.equals(tablespace, indexDdl.tablespace);
    }

    @Override
    public int hashCode() {
        return Objects.hash(indexName, tableName, columns, indexType, unique, tablespace);
    }
}
