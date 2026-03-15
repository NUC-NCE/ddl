package com.datacheck.compare;

import com.datacheck.model.*;

import java.util.*;

/**
 * 主键比对器
 */
public class PrimaryKeyComparator {

    /**
     * 比较两个表的主键差异
     */
    public List<DdlDifference> compare(PrimaryKey oraclePk, PrimaryKey gaussPk, String tableName) {
        List<DdlDifference> differences = new ArrayList<>();

        if (oraclePk == null && gaussPk == null) {
            return differences;
        }

        if (oraclePk == null) {
            DdlDifference diff = new DdlDifference(DifferenceType.GAUSS_ONLY, tableName, "PRIMARY_KEY",
                    gaussPk.getConstraintName());
            diff.setGaussValue("PRIMARY KEY (" + String.join(", ", gaussPk.getColumns()) + ")");
            differences.add(diff);
            return differences;
        }

        if (gaussPk == null) {
            DdlDifference diff = new DdlDifference(DifferenceType.ORACLE_ONLY, tableName, "PRIMARY_KEY",
                    oraclePk.getConstraintName());
            diff.setOracleValue("PRIMARY KEY (" + String.join(", ", oraclePk.getColumns()) + ")");
            differences.add(diff);
            return differences;
        }

        // 比较主键列
        List<String> oracleCols = oraclePk.getColumns();
        List<String> gaussCols = gaussPk.getColumns();
        if (!listEqualsIgnoreCase(oracleCols, gaussCols)) {
            DdlDifference diff = new DdlDifference(DifferenceType.DIFFERENT, tableName, "PRIMARY_KEY",
                    oraclePk.getConstraintName());
            diff.setOracleValue(String.join(", ", oracleCols));
            diff.setGaussValue(String.join(", ", gaussCols));
            diff.setMessage("[" + DifferenceType.DIFFERENT + "] Table: " + tableName + ", PRIMARY_KEY: Column mismatch | Oracle=(" +
                    diff.getOracleValue() + ") vs Gauss=(" + diff.getGaussValue() + ")");
            differences.add(diff);
        }

        return differences;
    }

    /**
     * 比较两个列表是否相等（忽略大小写）
     */
    private boolean listEqualsIgnoreCase(List<String> list1, List<String> list2) {
        if (list1 == list2) return true;
        if (list1 == null || list2 == null) return false;
        if (list1.size() != list2.size()) return false;

        Iterator<String> it1 = list1.iterator();
        Iterator<String> it2 = list2.iterator();
        while (it1.hasNext() && it2.hasNext()) {
            if (!it1.next().equalsIgnoreCase(it2.next())) {
                return false;
            }
        }
        return true;
    }
}
