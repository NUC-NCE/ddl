package com.datacheck.output;

import com.datacheck.model.DdlCompareResult;
import com.datacheck.model.DdlDifference;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * DDL 校验结果输出器
 */
public class DdlResultWriter {

    private static final String SEPARATOR = "----------------------------------------";
    private static final String LINE = "========================================";

    /**
     * 写入结果到文件
     */
    public static void write(DdlCompareResult result, String outputPath) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(outputPath))) {
            writeHeader(writer);
            writeSummary(writer, result);
            writeDifferences(writer, result);
        }
    }

    /**
     * 写入结果到输出流
     */
    public static void write(DdlCompareResult result, OutputStream out) {
        PrintWriter writer = new PrintWriter(out);
        writeHeader(writer);
        writeSummary(writer, result);
        writeDifferences(writer, result);
        writer.flush();
    }

    private static void writeHeader(PrintWriter writer) {
        writer.println(LINE);
        writer.println("DDL Compare Report");
        writer.println(LINE);
        writer.println("Generated: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        writer.println();
    }

    private static void writeSummary(PrintWriter writer, DdlCompareResult result) {
        writer.println("Summary:");
        writer.println(SEPARATOR);
        writer.println("Total Tables: " + result.getTotalTables());
        writer.println("Matched: " + result.getMatchedTables());
        writer.println("Tables with Differences: " + result.getTablesWithDifferences());
        writer.println("Total Differences: " + result.getAllDifferences().size());
        writer.println();
    }

    private static void writeDifferences(PrintWriter writer, DdlCompareResult result) {
        Map<String, List<DdlDifference>> tableDiffs = result.getTableDifferences();

        if (tableDiffs.isEmpty()) {
            writer.println("No differences found!");
            return;
        }

        writer.println("Detailed Differences:");
        writer.println(SEPARATOR);

        for (Map.Entry<String, List<DdlDifference>> entry : tableDiffs.entrySet()) {
            String tableName = entry.getKey();
            List<DdlDifference> diffs = entry.getValue();

            writer.println();
            writer.println("Table: " + tableName);

            for (DdlDifference diff : diffs) {
                writer.println("  " + diff.getMessage());
            }
        }
    }

    /**
     * 控制台输出
     */
    public static void printToConsole(DdlCompareResult result) {
        write(result, System.out);
    }
}
