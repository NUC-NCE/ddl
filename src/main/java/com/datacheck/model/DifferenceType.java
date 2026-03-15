package com.datacheck.model;

/**
 * DDL 差异类型
 */
public enum DifferenceType {
    ORACLE_ONLY,   // Oracle 独有
    GAUSS_ONLY,    // Gauss 独有
    DIFFERENT      // 两者不同
}
