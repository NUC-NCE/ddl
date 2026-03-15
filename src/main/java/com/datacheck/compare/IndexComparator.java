package com.datacheck.compare;

import com.datacheck.model.*;

import java.util.*;

/**
 * 索引比对器
 */
public class IndexComparator {

    /**
     * 比较两个表的索引差异
     */
    public List<DdlDifference> compare(List<IndexDdl> oracleIndexes, List<IndexDdl> gaussIndexes, String tableName) {
        List<DdlDifference> differences = new ArrayList<>();

        // 构建索引名到索引的映射
        Map<String, IndexDdl> oracleIdxMap = new LinkedHashMap<>();
        Map<String, IndexDdl> gaussIdxMap = new LinkedHashMap<>();

        for (IndexDdl idx : oracleIndexes) {
            oracleIdxMap.put(idx.getIndexName().toUpperCase(), idx);
        }
        for (IndexDdl idx : gaussIndexes) {
            gaussIdxMap.put(idx.getIndexName().toUpperCase(), idx);
        }

        // 检查 Oracle 独有的索引
        for (IndexDdl oracleIdx : oracleIndexes) {
            String idxName = oracleIdx.getIndexName().toUpperCase();
            if (!gaussIdxMap.containsKey(idxName)) {
                DdlDifference diff = new DdlDifference(DifferenceType.ORACLE_ONLY, tableName, "INDEX", idxName);
                diff.setOracleValue(oracleIdx.toString());
                differences.add(diff);
            }
        }

        // 检查 Gauss 独有的索引
        for (IndexDdl gaussIdx : gaussIndexes) {
            String idxName = gaussIdx.getIndexName().toUpperCase();
            if (!oracleIdxMap.containsKey(idxName)) {
                DdlDifference diff = new DdlDifference(DifferenceType.GAUSS_ONLY, tableName, "INDEX", idxName);
                diff.setGaussValue(gaussIdx.toString());
                differences.add(diff);
            }
        }

        // 比较共同的索引
        for (IndexDdl oracleIdx : oracleIndexes) {
            String idxName = oracleIdx.getIndexName().toUpperCase();
            IndexDdl gaussIdx = gaussIdxMap.get(idxName);
            if (gaussIdx != null) {
                differences.addAll(compareIndex(oracleIdx, gaussIdx, tableName));
            }
        }

        return differences;
    }

    /**
     * 比较单个索引的差异
     */
    private List<DdlDifference> compareIndex(IndexDdl oracleIdx, IndexDdl gaussIdx, String tableName) {
        List<DdlDifference> differences = new ArrayList<>();
        String idxName = oracleIdx.getIndexName();

        // 比较索引列
        List<String> oracleCols = oracleIdx.getColumns();
        List<String> gaussCols = gaussIdx.getColumns();
        if (!listEqualsIgnoreCase(oracleCols, gaussCols)) {
            DdlDifference diff = new DdlDifference(DifferenceType.DIFFERENT, tableName, "INDEX_COLUMN", idxName);
            diff.setOracleValue(String.join(", ", oracleCols));
            diff.setGaussValue(String.join(", ", gaussCols));
            differences.add(diff);
        }

        // 比较索引类型
        String oracleType = normalizeIndexType(oracleIdx.getIndexType());
        String gaussType = normalizeIndexType(gaussIdx.getIndexType());
        if (!Objects.equals(oracleType, gaussType)) {
            DdlDifference diff = new DdlDifference(DifferenceType.DIFFERENT, tableName, "INDEX_TYPE", idxName);
            diff.setOracleValue(oracleIdx.getIndexType());
            diff.setGaussValue(gaussIdx.getIndexType());
            differences.add(diff);
        }

        // 比较唯一性
        if (oracleIdx.isUnique() != gaussIdx.isUnique()) {
            DdlDifference diff = new DdlDifference(DifferenceType.DIFFERENT, tableName, "INDEX_UNIQUE", idxName);
            diff.setOracleValue(oracleIdx.isUnique() ? "UNIQUE" : "NON-UNIQUE");
            diff.setGaussValue(gaussIdx.isUnique() ? "UNIQUE" : "NON-UNIQUE");
            differences.add(diff);
        }

        // 比较表空间
        if (!Objects.equals(oracleIdx.getTablespace(), gaussIdx.getTablespace())) {
            DdlDifference diff = new DdlDifference(DifferenceType.DIFFERENT, tableName, "INDEX_TABLESPACE", idxName);
            diff.setOracleValue(oracleIdx.getTablespace());
            diff.setGaussValue(gaussIdx.getTablespace());
            differences.add(diff);
        }

        return differences;
    }

    /**
     * 标准化索引类型
     */
    private String normalizeIndexType(String indexType) {
        if (indexType == null || indexType.isEmpty()) return "BTREE";
        indexType = indexType.toUpperCase();
        if (indexType.contains("BTREE") || indexType.equals("B-TREE")) return "BTREE";
        if (indexType.contains("HASH")) return "HASH";
        if (indexType.contains("BITMAP")) return "BITMAP";
        if (indexType.contains("GIST")) return "GIST";
        if (indexType.contains("GIN")) return "GIN";
        return indexType;
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
