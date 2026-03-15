package com.datacheck.model;

/**
 * DDL 差异项
 */
public class DdlDifference {
    private DifferenceType type;
    private String tableName;
    private String category;    // TABLESPACE, COLUMN, INDEX, PRIMARY_KEY
    private String objectName;  // 具体的列名、索引名等
    private String oracleValue;
    private String gaussValue;
    private String message;

    public DdlDifference() {
    }

    public DdlDifference(DifferenceType type, String tableName, String category, String objectName) {
        this.type = type;
        this.tableName = tableName;
        this.category = category;
        this.objectName = objectName;
    }

    public DifferenceType getType() {
        return type;
    }

    public void setType(DifferenceType type) {
        this.type = type;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getObjectName() {
        return objectName;
    }

    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }

    public String getOracleValue() {
        return oracleValue;
    }

    public void setOracleValue(String oracleValue) {
        this.oracleValue = oracleValue;
    }

    public String getGaussValue() {
        return gaussValue;
    }

    public void setGaussValue(String gaussValue) {
        this.gaussValue = gaussValue;
    }

    public String getMessage() {
        if (message != null) {
            return message;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(type.name()).append("] ");
        if (tableName != null && !tableName.isEmpty()) {
            sb.append("Table: ").append(tableName).append(", ");
        }
        if (category != null) {
            sb.append(category).append(": ");
        }
        if (objectName != null) {
            sb.append(objectName);
        }
        if (type == DifferenceType.DIFFERENT) {
            sb.append(" | Oracle=").append(oracleValue);
            sb.append(" vs Gauss=").append(gaussValue);
        }
        return sb.toString();
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return getMessage();
    }
}
