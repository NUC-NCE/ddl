package com.datacheck.sdk.config;

/**
 * SDK 选项配置
 */
public class SdkOptions {
    private boolean compareTablespace = true;
    private boolean compareIndex = true;
    private boolean comparePrimaryKey = true;
    private boolean compareColumn = true;
    private int threadCount = 4;
    private String outputDir = "./output";

    public boolean isCompareTablespace() {
        return compareTablespace;
    }

    public SdkOptions setCompareTablespace(boolean compareTablespace) {
        this.compareTablespace = compareTablespace;
        return this;
    }

    public boolean isCompareIndex() {
        return compareIndex;
    }

    public SdkOptions setCompareIndex(boolean compareIndex) {
        this.compareIndex = compareIndex;
        return this;
    }

    public boolean isComparePrimaryKey() {
        return comparePrimaryKey;
    }

    public SdkOptions setComparePrimaryKey(boolean comparePrimaryKey) {
        this.comparePrimaryKey = comparePrimaryKey;
        return this;
    }

    public boolean isCompareColumn() {
        return compareColumn;
    }

    public SdkOptions setCompareColumn(boolean compareColumn) {
        this.compareColumn = compareColumn;
        return this;
    }

    public int getThreadCount() {
        return threadCount;
    }

    public SdkOptions setThreadCount(int threadCount) {
        this.threadCount = threadCount;
        return this;
    }

    public String getOutputDir() {
        return outputDir;
    }

    public SdkOptions setOutputDir(String outputDir) {
        this.outputDir = outputDir;
        return this;
    }
}
