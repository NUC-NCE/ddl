package com.datacheck.sdk;

import com.datacheck.Config;
import com.datacheck.compare.DdlComparatorImpl;
import com.datacheck.model.DdlCompareResult;
import com.datacheck.sdk.config.DatabaseConfig;
import com.datacheck.sdk.config.SdkOptions;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * DDL 校验器 Builder
 */
public class DdlComparatorBuilder {
    private DatabaseConfig oracleConfig;
    private DatabaseConfig gaussConfig;
    private SdkOptions options;
    private Connection oracleConnection;
    private Connection gaussConnection;

    private DdlComparatorBuilder() {
    }

    public static DdlComparatorBuilder builder() {
        return new DdlComparatorBuilder();
    }

    /**
     * 配置 Oracle 数据库
     */
    public OracleConfigStep oracle() {
        return new OracleConfigStep(this);
    }

    /**
     * 配置 Gauss 数据库
     */
    public GaussConfigStep gauss() {
        return new GaussConfigStep(this);
    }

    /**
     * 配置选项
     */
    public OptionsStep options() {
        return new OptionsStep(this);
    }

    /**
     * 使用已有的连接
     */
    public DdlComparatorBuilder withConnections(Connection oracleConnection, Connection gaussConnection) {
        this.oracleConnection = oracleConnection;
        this.gaussConnection = gaussConnection;
        return this;
    }

    /**
     * 构建并执行校验
     */
    public DdlCompareResult buildAndCompare(List<String> tables) throws SQLException {
        DdlComparatorImpl comparator = build();
        try {
            return comparator.compare(tables);
        } finally {
            comparator.close();
        }
    }

    /**
     * 构建校验器
     */
    public DdlComparatorImpl build() throws SQLException {
        Config config = createConfig();

        if (oracleConnection != null && gaussConnection != null) {
            return new DdlComparatorImpl(oracleConnection, gaussConnection, config);
        }

        return new DdlComparatorImpl(config);
    }

    private Config createConfig() {
        Config config = new Config();

        Config.OracleConfig oracle = new Config.OracleConfig();
        if (oracleConfig != null) {
            oracle.setJdbcUrl(oracleConfig.getJdbcUrl());
            oracle.setUsername(oracleConfig.getUsername());
            oracle.setPassword(oracleConfig.getPassword());
        }
        config.setOracle(oracle);

        Config.GaussConfig gauss = new Config.GaussConfig();
        if (gaussConfig != null) {
            gauss.setJdbcUrl(gaussConfig.getJdbcUrl());
            gauss.setUsername(gaussConfig.getUsername());
            gauss.setPassword(gaussConfig.getPassword());
        }
        config.setGauss(gauss);

        Config.Options opts = new Config.Options();
        if (options != null) {
            opts.setCompareTablespace(options.isCompareTablespace());
            opts.setCompareIndex(options.isCompareIndex());
            opts.setComparePrimaryKey(options.isComparePrimaryKey());
            opts.setCompareColumn(options.isCompareColumn());
            config.setThreadCount(options.getThreadCount());
            config.setOutputDir(options.getOutputDir());
        }
        config.setOptions(opts);

        return config;
    }

    // Builder 步骤接口
    public interface BuildStep {
        DdlComparatorBuilder build();
    }

    public static class OracleConfigStep {
        private final DdlComparatorBuilder builder;

        public OracleConfigStep(DdlComparatorBuilder builder) {
            this.builder = builder;
        }

        public GaussConfigStep jdbcUrl(String jdbcUrl) {
            DatabaseConfig config = new DatabaseConfig();
            config.setJdbcUrl(jdbcUrl);
            builder.oracleConfig = config;
            return new GaussConfigStep(builder, config);
        }

        public OracleConfigStep config(DatabaseConfig config) {
            builder.oracleConfig = config;
            return this;
        }
    }

    public static class GaussConfigStep {
        private final DdlComparatorBuilder builder;
        private DatabaseConfig oracleConfig;

        public GaussConfigStep(DdlComparatorBuilder builder) {
            this.builder = builder;
        }

        public GaussConfigStep(DdlComparatorBuilder builder, DatabaseConfig oracleConfig) {
            this.builder = builder;
            this.oracleConfig = oracleConfig;
        }

        public OptionsStep jdbcUrl(String jdbcUrl) {
            DatabaseConfig config = new DatabaseConfig();
            config.setJdbcUrl(jdbcUrl);
            builder.gaussConfig = config;
            return new OptionsStep(builder);
        }

        public GaussConfigStep config(DatabaseConfig config) {
            builder.gaussConfig = config;
            return this;
        }

        public BuildStep username(String username) {
            if (oracleConfig != null) {
                oracleConfig.setUsername(username);
            }
            return builder;
        }

        public BuildStep password(String password) {
            if (oracleConfig != null) {
                oracleConfig.setPassword(password);
            }
            return builder;
        }
    }

    public static class OptionsStep {
        private final DdlComparatorBuilder builder;

        public OptionsStep(DdlComparatorBuilder builder) {
            this.builder = builder;
            builder.options = new SdkOptions();
        }

        public OptionsStep compareTablespace(boolean compare) {
            builder.options.setCompareTablespace(compare);
            return this;
        }

        public OptionsStep compareIndex(boolean compare) {
            builder.options.setCompareIndex(compare);
            return this;
        }

        public OptionsStep comparePrimaryKey(boolean compare) {
            builder.options.setComparePrimaryKey(compare);
            return this;
        }

        public OptionsStep compareColumn(boolean compare) {
            builder.options.setCompareColumn(compare);
            return this;
        }

        public OptionsStep threadCount(int count) {
            builder.options.setThreadCount(count);
            return this;
        }

        public DdlComparatorBuilder build() {
            return builder;
        }
    }
}
