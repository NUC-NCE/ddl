package com.datacheck.compare;

import com.datacheck.model.*;

import java.util.*;

/**
 * 表结构比对器
 */
public class TableDdlComparator {

    /**
     * 比较两个表的 DDL 差异
     */
    public List<DdlDifference> compare(TableDdl oracleTable, TableDdl gaussTable) {
        List<DdlDifference> differences = new ArrayList<>();
        String tableName = oracleTable != null ? oracleTable.getTableName() : gaussTable.getTableName();

        if (oracleTable == null) {
            differences.add(new DdlDifference(DifferenceType.GAUSS_ONLY, tableName, "TABLE", tableName));
            return differences;
        }

        if (gaussTable == null) {
            differences.add(new DdlDifference(DifferenceType.ORACLE_ONLY, tableName, "TABLE", tableName));
            return differences;
        }

        // 比较列
        differences.addAll(compareColumns(oracleTable.getColumns(), gaussTable.getColumns(), tableName));

        return differences;
    }

    /**
     * 比较列定义
     */
    private List<DdlDifference> compareColumns(List<ColumnDdl> oracleColumns, List<ColumnDdl> gaussColumns, String tableName) {
        List<DdlDifference> differences = new ArrayList<>();

        // 构建列名到列定义的映射
        Map<String, ColumnDdl> oracleColMap = new LinkedHashMap<>();
        Map<String, ColumnDdl> gaussColMap = new LinkedHashMap<>();

        for (ColumnDdl col : oracleColumns) {
            oracleColMap.put(col.getColumnName().toUpperCase(), col);
        }
        for (ColumnDdl col : gaussColumns) {
            gaussColMap.put(col.getColumnName().toUpperCase(), col);
        }

        // 检查 Oracle 独有的列
        for (ColumnDdl oracleCol : oracleColumns) {
            String colName = oracleCol.getColumnName().toUpperCase();
            if (!gaussColMap.containsKey(colName)) {
                DdlDifference diff = new DdlDifference(DifferenceType.ORACLE_ONLY, tableName, "COLUMN", colName);
                diff.setOracleValue(oracleCol.toString());
                differences.add(diff);
            }
        }

        // 检查 Gauss 独有的列
        for (ColumnDdl gaussCol : gaussColumns) {
            String colName = gaussCol.getColumnName().toUpperCase();
            if (!oracleColMap.containsKey(colName)) {
                DdlDifference diff = new DdlDifference(DifferenceType.GAUSS_ONLY, tableName, "COLUMN", colName);
                diff.setGaussValue(gaussCol.toString());
                differences.add(diff);
            }
        }

        // 比较共同的列
        for (ColumnDdl oracleCol : oracleColumns) {
            String colName = oracleCol.getColumnName().toUpperCase();
            ColumnDdl gaussCol = gaussColMap.get(colName);
            if (gaussCol != null) {
                differences.addAll(compareColumn(oracleCol, gaussCol, tableName));
            }
        }

        return differences;
    }

    /**
     * 比较单个列的差异
     */
    private List<DdlDifference> compareColumn(ColumnDdl oracleCol, ColumnDdl gaussCol, String tableName) {
        List<DdlDifference> differences = new ArrayList<>();
        String colName = oracleCol.getColumnName();

        // 比较数据类型
        String oracleType = normalizeDataType(oracleCol.getDataType());
        String gaussType = normalizeDataType(gaussCol.getDataType());
        if (!oracleType.equals(gaussType)) {
            DdlDifference diff = new DdlDifference(DifferenceType.DIFFERENT, tableName, "COLUMN_TYPE", colName);
            diff.setOracleValue(oracleCol.getDataType() + formatLength(oracleCol));
            diff.setGaussValue(gaussCol.getDataType() + formatLength(gaussCol));
            differences.add(diff);
        } else if (!Objects.equals(oracleCol.getDataLength(), gaussCol.getDataLength()) ||
                   !Objects.equals(oracleCol.getDataPrecision(), gaussCol.getDataPrecision()) ||
                   !Objects.equals(oracleCol.getDataScale(), gaussCol.getDataScale())) {
            // 数据类型相同但长度/精度不同
            DdlDifference diff = new DdlDifference(DifferenceType.DIFFERENT, tableName, "COLUMN_LENGTH", colName);
            diff.setOracleValue(formatLength(oracleCol));
            diff.setGaussValue(formatLength(gaussCol));
            differences.add(diff);
        }

        // 比较可空性
        if (oracleCol.isNullable() != gaussCol.isNullable()) {
            DdlDifference diff = new DdlDifference(DifferenceType.DIFFERENT, tableName, "COLUMN_NULLABLE", colName);
            diff.setOracleValue(oracleCol.isNullable() ? "NULL" : "NOT NULL");
            diff.setGaussValue(gaussCol.isNullable() ? "NULL" : "NOT NULL");
            differences.add(diff);
        }

        // 比较默认值
        String oracleDefault = normalizeDefault(oracleCol.getDefaultValue());
        String gaussDefault = normalizeDefault(gaussCol.getDefaultValue());
        if (!Objects.equals(oracleDefault, gaussDefault)) {
            DdlDifference diff = new DdlDifference(DifferenceType.DIFFERENT, tableName, "COLUMN_DEFAULT", colName);
            diff.setOracleValue(oracleCol.getDefaultValue());
            diff.setGaussValue(gaussCol.getDefaultValue());
            differences.add(diff);
        }

        return differences;
    }

    /**
     * 标准化数据类型
     */
    private String normalizeDataType(String dataType) {
        if (dataType == null) return "";
        dataType = dataType.toUpperCase();
        // Oracle 与 GaussDB 类型映射
        if (dataType.equals("NUMBER")) return "NUMERIC";
        if (dataType.equals("VARCHAR2")) return "VARCHAR";
        if (dataType.equals("CLOB")) return "TEXT";
        if (dataType.equals("BLOB")) return "BYTEA";
        if (dataType.equals("DATE")) return "TIMESTAMP";
        return dataType;
    }

    /**
     * 格式化长度/精度信息
     */
    private String formatLength(ColumnDdl col) {
        if (col.getDataLength() != null && col.getDataLength() > 0) {
            return "(" + col.getDataLength() + ")";
        }
        if (col.getDataPrecision() != null) {
            if (col.getDataScale() != null && col.getDataScale() > 0) {
                return "(" + col.getDataPrecision() + "," + col.getDataScale() + ")";
            }
            return "(" + col.getDataPrecision() + ")";
        }
        return "";
    }

    /**
     * 标准化默认值
     */
    private String normalizeDefault(String defaultValue) {
        if (defaultValue == null || defaultValue.trim().isEmpty()) {
            return null;
        }
        return defaultValue.trim().toUpperCase();
    }
}
