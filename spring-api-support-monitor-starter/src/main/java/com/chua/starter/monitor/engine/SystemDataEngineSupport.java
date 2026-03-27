package com.chua.starter.monitor.engine;

import com.chua.starter.monitor.entity.SystemDataSetting;

import java.util.Locale;

/**
 * Engine 识别辅助工具。
 */
public final class SystemDataEngineSupport {

    private static final String[] JDBC_TOKENS = {
            "jdbc", "mysql", "mariadb", "postgres", "postgresql", "oracle",
            "sqlserver", "mssql", "sqlite", "tidb", "clickhouse", "h2", "dm",
            "kingbase", "kingbase8", "gaussdb", "oscar", "oceanbase", "polardb",
            "tdsql"
    };

    private static final String[] PARTITION_TOKENS = {
            "mysql", "mariadb", "tidb", "postgres", "postgresql", "oracle",
            "sqlserver", "mssql", "clickhouse", "oceanbase", "polardb"
    };

    private static final String[] CYPHER_TOKENS = {
            "neo4j", "cypher"
    };

    private static final String[] LUCENE_TOKENS = {
            "elasticsearch", "elastic", "lucene", "solr", "redissearch",
            "redis-search", "redis_search"
    };

    private SystemDataEngineSupport() {
    }

    public static String signature(SystemDataSetting setting) {
        if (setting == null) {
            return "";
        }
        return String.join(" ",
                safe(setting.getSystemDataSettingType()),
                safe(setting.getSystemDataSettingProtocol()),
                safe(setting.getSystemDataSettingServer()),
                safe(setting.getSystemDataSettingDatabase()),
                safe(setting.getSystemDataSettingMode())
        ).toLowerCase(Locale.ROOT);
    }

    public static boolean containsAny(SystemDataSetting setting, String... tokens) {
        String signature = signature(setting);
        for (String token : tokens) {
            if (signature.contains(token.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }

    public static boolean isJdbc(SystemDataSetting setting) {
        return containsAny(setting, JDBC_TOKENS);
    }

    public static boolean isRedis(SystemDataSetting setting) {
        return containsAny(setting, "redis", "redissearch", "redis-search", "redis_search");
    }

    public static boolean isMongodb(SystemDataSetting setting) {
        return containsAny(setting, "mongodb", "mongo");
    }

    public static boolean isZookeeper(SystemDataSetting setting) {
        return containsAny(setting, "zookeeper", "zk");
    }

    public static boolean isInfluxdb(SystemDataSetting setting) {
        return containsAny(setting, "influxdb", "influx");
    }

    public static boolean isKafka(SystemDataSetting setting) {
        return containsAny(setting, "kafka");
    }

    public static boolean isElasticsearch(SystemDataSetting setting) {
        return containsAny(setting, "elasticsearch", "elastic");
    }

    public static boolean isHttp(SystemDataSetting setting) {
        return containsAny(setting, "http", "https");
    }

    public static boolean supportsPartitions(SystemDataSetting setting) {
        return containsAny(setting, PARTITION_TOKENS);
    }

    public static SystemDataOrmDialect resolveOrmDialect(SystemDataSetting setting) {
        if (isJdbc(setting)) {
            return SystemDataOrmDialect.SQL;
        }
        if (containsAny(setting, CYPHER_TOKENS)) {
            return SystemDataOrmDialect.CYPHER;
        }
        if (containsAny(setting, LUCENE_TOKENS)) {
            return SystemDataOrmDialect.LUCENE;
        }
        return null;
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
