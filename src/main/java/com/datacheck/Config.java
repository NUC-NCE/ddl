package com.datacheck;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;

public class Config {
    private OracleConfig oracle;
    private GaussConfig gauss;
    private Options options;
    private int threadCount;
    private String outputDir;

    public OracleConfig getOracle() {
        return oracle;
    }

    public void setOracle(OracleConfig oracle) {
        this.oracle = oracle;
    }

    public GaussConfig getGauss() {
        return gauss;
    }

    public void setGauss(GaussConfig gauss) {
        this.gauss = gauss;
    }

    public Options getOptions() {
        return options;
    }

    public void setOptions(Options options) {
        this.options = options;
    }

    public int getThreadCount() {
        return threadCount;
    }

    public void setThreadCount(int threadCount) {
        this.threadCount = threadCount;
    }

    public String getOutputDir() {
        return outputDir;
    }

    public void setOutputDir(String outputDir) {
        this.outputDir = outputDir;
    }

    public static class OracleConfig {
        private String jdbcUrl;
        private String username;
        private String password;

        public String getJdbcUrl() {
            return jdbcUrl;
        }

        public void setJdbcUrl(String jdbcUrl) {
            this.jdbcUrl = jdbcUrl;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    public static class GaussConfig {
        private String jdbcUrl;
        private String username;
        private String password;

        public String getJdbcUrl() {
            return jdbcUrl;
        }

        public void setJdbcUrl(String jdbcUrl) {
            this.jdbcUrl = jdbcUrl;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    public static class Options {
        private boolean compareTablespace = true;
        private boolean compareIndex = true;
        private boolean comparePrimaryKey = true;
        private boolean compareColumn = true;

        public boolean isCompareTablespace() {
            return compareTablespace;
        }

        public void setCompareTablespace(boolean compareTablespace) {
            this.compareTablespace = compareTablespace;
        }

        public boolean isCompareIndex() {
            return compareIndex;
        }

        public void setCompareIndex(boolean compareIndex) {
            this.compareIndex = compareIndex;
        }

        public boolean isComparePrimaryKey() {
            return comparePrimaryKey;
        }

        public void setComparePrimaryKey(boolean comparePrimaryKey) {
            this.comparePrimaryKey = comparePrimaryKey;
        }

        public boolean isCompareColumn() {
            return compareColumn;
        }

        public void setCompareColumn(boolean compareColumn) {
            this.compareColumn = compareColumn;
        }
    }

    public static Config load(String configPath) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(new File(configPath), Config.class);
    }

    public String getOracleJdbcUrl() {
        if (oracle != null && oracle.getJdbcUrl() != null) {
            return oracle.getJdbcUrl();
        }
        return null;
    }

    public String getGaussJdbcUrl() {
        if (gauss != null && gauss.getJdbcUrl() != null) {
            return gauss.getJdbcUrl();
        }
        return null;
    }
}
