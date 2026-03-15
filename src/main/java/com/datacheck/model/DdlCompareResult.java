package com.datacheck.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DDL 校验结果
 */
public class DdlCompareResult {
    private int totalTables;
    private int matchedTables;
    private int tablesWithDifferences;
    private Map<String, List<DdlDifference>> tableDifferences = new HashMap<>();
    private List<DdlDifference> allDifferences = new ArrayList<>();

    public int getTotalTables() {
        return totalTables;
    }

    public void setTotalTables(int totalTables) {
        this.totalTables = totalTables;
    }

    public int getMatchedTables() {
        return matchedTables;
    }

    public void setMatchedTables(int matchedTables) {
        this.matchedTables = matchedTables;
    }

    public int getTablesWithDifferences() {
        return tablesWithDifferences;
    }

    public void setTablesWithDifferences(int tablesWithDifferences) {
        this.tablesWithDifferences = tablesWithDifferences;
    }

    public Map<String, List<DdlDifference>> getTableDifferences() {
        return tableDifferences;
    }

    public void setTableDifferences(Map<String, List<DdlDifference>> tableDifferences) {
        this.tableDifferences = tableDifferences;
    }

    public List<DdlDifference> getAllDifferences() {
        return allDifferences;
    }

    public void addDifference(DdlDifference difference) {
        allDifferences.add(difference);
        String tableName = difference.getTableName();
        if (tableName != null) {
            tableDifferences.computeIfAbsent(tableName, k -> new ArrayList<>()).add(difference);
        }
    }

    public void addDifferences(List<DdlDifference> differences) {
        for (DdlDifference diff : differences) {
            addDifference(diff);
        }
    }

    public List<DdlDifference> getDifferencesForTable(String tableName) {
        return tableDifferences.getOrDefault(tableName, new ArrayList<>());
    }

    public boolean hasDifferences() {
        return !allDifferences.isEmpty();
    }

    public String getSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("Total Tables: ").append(totalTables).append("\n");
        sb.append("Matched: ").append(matchedTables).append("\n");
        sb.append("Tables with Differences: ").append(tablesWithDifferences).append("\n");
        sb.append("Total Differences: ").append(allDifferences.size());
        return sb.toString();
    }
}
