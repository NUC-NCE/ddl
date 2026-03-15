package com.datacheck.compare;

import com.datacheck.model.*;

import java.util.*;

/**
 * 表空间比对器
 */
public class TablespaceComparator {

    /**
     * 比较两个表的表空间差异
     */
    public List<DdlDifference> compare(String oracleTablespace, String gaussTablespace, String tableName) {
        List<DdlDifference> differences = new ArrayList<>();

        if (oracleTablespace == null && gaussTablespace == null) {
            return differences;
        }

        if (oracleTablespace == null) {
            DdlDifference diff = new DdlDifference(DifferenceType.GAUSS_ONLY, tableName, "TABLESPACE", tableName);
            diff.setGaussValue(gaussTablespace);
            differences.add(diff);
            return differences;
        }

        if (gaussTablespace == null) {
            DdlDifference diff = new DdlDifference(DifferenceType.ORACLE_ONLY, tableName, "TABLESPACE", tableName);
            diff.setOracleValue(oracleTablespace);
            differences.add(diff);
            return differences;
        }

        // 比较表空间
        if (!oracleTablespace.equalsIgnoreCase(gaussTablespace)) {
            DdlDifference diff = new DdlDifference(DifferenceType.DIFFERENT, tableName, "TABLESPACE", tableName);
            diff.setOracleValue(oracleTablespace);
            diff.setGaussValue(gaussTablespace);
            differences.add(diff);
        }

        return differences;
    }
}
