package com.datacheck.sdk.model;

/**
 * 校验汇总信息
 */
public class ComparisonSummary {
    private int totalTables;
    private int matched;
    private int tablesWithDifferences;
    private int totalDifferences;

    public ComparisonSummary() {
    }

    public ComparisonSummary(int totalTables, int matched, int tablesWithDifferences, int totalDifferences) {
        this.totalTables = totalTables;
        this.matched = matched;
        this.tablesWithDifferences = tablesWithDifferences;
        this.totalDifferences = totalDifferences;
    }

    public int getTotalTables() {
        return totalTables;
    }

    public void setTotalTables(int totalTables) {
        this.totalTables = totalTables;
    }

    public int getMatched() {
        return matched;
    }

    public void setMatched(int matched) {
        this.matched = matched;
    }

    public int getTablesWithDifferences() {
        return tablesWithDifferences;
    }

    public void setTablesWithDifferences(int tablesWithDifferences) {
        this.tablesWithDifferences = tablesWithDifferences;
    }

    public int getDifferences() {
        return totalDifferences;
    }

    public void setDifferences(int differences) {
        this.totalDifferences = differences;
    }

    @Override
    public String toString() {
        return "ComparisonSummary{" +
                "totalTables=" + totalTables +
                ", matched=" + matched +
                ", tablesWithDifferences=" + tablesWithDifferences +
                ", totalDifferences=" + totalDifferences +
                '}';
    }
}
