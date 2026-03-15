package com.datacheck.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 主键定义
 */
public class PrimaryKey {
    private String constraintName;
    private String tableName;
    private List<String> columns = new ArrayList<>();

    public PrimaryKey() {
    }

    public PrimaryKey(String constraintName, String tableName) {
        this.constraintName = constraintName;
        this.tableName = tableName;
    }

    public String getConstraintName() {
        return constraintName;
    }

    public void setConstraintName(String constraintName) {
        this.constraintName = constraintName;
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

    @Override
    public String toString() {
        return "PRIMARY KEY " + constraintName + " (" + String.join(", ", columns) + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PrimaryKey that = (PrimaryKey) o;
        return Objects.equals(constraintName, that.constraintName) &&
                Objects.equals(tableName, that.tableName) &&
                Objects.equals(columns, that.columns);
    }

    @Override
    public int hashCode() {
        return Objects.hash(constraintName, tableName, columns);
    }
}
