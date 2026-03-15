package com.datacheck;

import com.datacheck.compare.DdlComparatorImpl;
import com.datacheck.model.DdlCompareResult;
import com.datacheck.output.DdlResultWriter;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DDL 校验工具 CLI 主程序
 */
public class DdlChecker {

    private static final String VERSION = "1.0.0";

    public static void main(String[] args) {
        try {
            // 解析命令行参数
            CmdArgs cmdArgs = parseArgs(args);
            if (cmdArgs == null) {
                printUsage();
                System.exit(1);
            }

            if (cmdArgs.help) {
                printUsage();
                System.exit(0);
            }

            if (cmdArgs.version) {
                printVersion();
                System.exit(0);
            }

            // 加载配置
            Config config = Config.load(cmdArgs.configPath);

            // 加载表名列表
            List<String> tables = loadTables(cmdArgs.tablesPath);

            System.out.println("Starting DDL comparison...");
            System.out.println("Total tables to compare: " + tables.size());

            // 执行校验
            try (DdlComparatorImpl comparator = new DdlComparatorImpl(config)) {
                DdlCompareResult result = comparator.compare(tables);

                // 输出结果
                if (cmdArgs.outputPath != null) {
                    DdlResultWriter.write(result, cmdArgs.outputPath);
                    System.out.println("Result written to: " + cmdArgs.outputPath);
                } else {
                    DdlResultWriter.printToConsole(result);
                }

                // 返回状态码
                if (result.hasDifferences()) {
                    System.out.println("\nComparison completed with differences found!");
                    System.exit(1);
                } else {
                    System.out.println("\nComparison completed successfully - no differences!");
                    System.exit(0);
                }
            }

        } catch (SQLException e) {
            System.err.println("Database connection error: " + e.getMessage());
            System.exit(2);
        } catch (IOException e) {
            System.err.println("IO error: " + e.getMessage());
            System.exit(3);
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            System.exit(4);
        }
    }

    private static CmdArgs parseArgs(String[] args) {
        CmdArgs cmdArgs = new CmdArgs();

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            switch (arg) {
                case "-c":
                case "--config":
                    if (i + 1 < args.length) {
                        cmdArgs.configPath = args[++i];
                    }
                    break;
                case "-t":
                case "--tables":
                    if (i + 1 < args.length) {
                        cmdArgs.tablesPath = args[++i];
                    }
                    break;
                case "-o":
                case "--output":
                    if (i + 1 < args.length) {
                        cmdArgs.outputPath = args[++i];
                    }
                    break;
                case "-h":
                case "--help":
                    cmdArgs.help = true;
                    break;
                case "-v":
                case "--version":
                    cmdArgs.version = true;
                    break;
                default:
                    System.err.println("Unknown option: " + arg);
                    return null;
            }
        }

        return cmdArgs;
    }

    private static List<String> loadTables(String tablesPath) throws IOException {
        List<String> tables = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(tablesPath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty() && !line.startsWith("#")) {
                    tables.add(line);
                }
            }
        }
        return tables;
    }

    private static void printUsage() {
        System.out.println("DDL Checker - Compare DDL between Oracle and GaussDB");
        System.out.println();
        System.out.println("Usage: java -jar ddl-checker.jar [options]");
        System.out.println();
        System.out.println("Options:");
        System.out.println("  -c, --config <path>    Configuration file path (required)");
        System.out.println("  -t, --tables <path>   Tables list file path (default: tables.txt)");
        System.out.println("  -o, --output <path>   Output file path (default: console)");
        System.out.println("  -h, --help            Show this help message");
        System.out.println("  -v, --version         Show version information");
        System.out.println();
        System.out.println("Example:");
        System.out.println("  java -jar ddl-checker.jar -c config.json -t tables.txt -o result.txt");
    }

    private static void printVersion() {
        System.out.println("DDL Checker version " + VERSION);
    }

    private static class CmdArgs {
        String configPath = "config.json";
        String tablesPath = "tables.txt";
        String outputPath = null;
        boolean help = false;
        boolean version = false;
    }
}
