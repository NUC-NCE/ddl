package com.datacheck.model;

/**
 * 列定义
 */
public class ColumnDdl {
    private String columnName;
    private String dataType;
    private Integer dataLength;
    private Integer dataPrecision;
    private Integer dataScale;
    private boolean nullable;
    private String defaultValue;
    private String comment;

    public ColumnDdl() {
    }

    public ColumnDdl(String columnName, String dataType) {
        this.columnName = columnName;
        this.dataType = dataType;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public Integer getDataLength() {
        return dataLength;
    }

    public void setDataLength(Integer dataLength) {
        this.dataLength = dataLength;
    }

    public Integer getDataPrecision() {
        return dataPrecision;
    }

    public void setDataPrecision(Integer dataPrecision) {
        this.dataPrecision = dataPrecision;
    }

    public Integer getDataScale() {
        return dataScale;
    }

    public void setDataScale(Integer dataScale) {
        this.dataScale = dataScale;
    }

    public boolean isNullable() {
        return nullable;
    }

    public void setNullable(boolean nullable) {
        this.nullable = nullable;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(columnName).append(" ").append(dataType);
        if (dataLength != null && dataLength > 0) {
            if (dataPrecision != null && dataScale != null && dataScale > 0) {
                sb.append("(").append(dataPrecision).append(",").append(dataScale).append(")");
            } else if (dataLength > 0) {
                sb.append("(").append(dataLength).append(")");
            }
        }
        if (!nullable) {
            sb.append(" NOT NULL");
        }
        if (defaultValue != null && !defaultValue.isEmpty()) {
            sb.append(" DEFAULT ").append(defaultValue);
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ColumnDdl columnDdl = (ColumnDdl) o;
        return nullable == columnDdl.nullable &&
                java.util.Objects.equals(columnName, columnDdl.columnName) &&
                java.util.Objects.equals(dataType, columnDdl.dataType) &&
                java.util.Objects.equals(dataLength, columnDdl.dataLength) &&
                java.util.Objects.equals(dataPrecision, columnDdl.dataPrecision) &&
                java.util.Objects.equals(dataScale, columnDdl.dataScale) &&
                java.util.Objects.equals(defaultValue, columnDdl.defaultValue);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(columnName, dataType, dataLength, dataPrecision, dataScale, nullable, defaultValue);
    }
}
