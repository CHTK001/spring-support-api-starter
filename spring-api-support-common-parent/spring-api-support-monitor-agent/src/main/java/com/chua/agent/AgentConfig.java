package com.chua.agent;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Agent 运行配置
 * <p>
 * 来源分两层：构建期注入的版本号（{@code agent.properties}）与运行期的 agent.yml。
 * yml 采用 {@code key: value} 扁平写法，与平台侧部署脚本生成的格式一致。
 * </p>
 *
 * @author CH
 * @since 4.0.0.42
 */
public final class AgentConfig {

    /** 版本号缺省值（资源缺失时不至于显示 null） */
    private static final String UNKNOWN_VERSION = "unknown";

    /** 构建期产物 */
    private final Properties buildInfo;

    /** 运行期配置 */
    private final Properties runtime;

    /** 监听端口 */
    private final int port;

    /** 监听地址 */
    private final String bind;

    /** 主机注册密钥（平台下发，用于回写平台时标识主机） */
    private final String agentKey;

    /** 接口访问令牌；为空表示不校验 */
    private final String token;

    /**
     * 构造运行配置。
     *
     * @param buildInfo 构建期属性
     * @param runtime   运行期属性
     * @param port      监听端口
     * @param bind      监听地址
     * @param agentKey  主机注册密钥
     * @param token     接口访问令牌
     */
    private AgentConfig(Properties buildInfo, Properties runtime, int port, String bind, String agentKey, String token) {
        this.buildInfo = buildInfo;
        this.runtime = runtime;
        this.port = port;
        this.bind = bind;
        this.agentKey = agentKey;
        this.token = token;
    }

    /**
     * 从配置文件装载运行配置。
     *
     * @param cfgPath        配置文件路径，可为 null
     * @param defaultPort    默认端口
     * @param defaultBind    默认监听地址
     * @return 运行配置
     */
    public static AgentConfig load(Path cfgPath, int defaultPort, String defaultBind) {
        Properties runtime = AgentMain.loadYamlLike(cfgPath);
        int port = parseInt(runtime.getProperty("agent.port"), defaultPort);
        String bind = nonBlank(runtime.getProperty("agent.bind"), defaultBind);
        String agentKey = nonBlank(runtime.getProperty("agent.agentKey"), "");
        String token = nonBlank(runtime.getProperty("agent.token"), "");
        return new AgentConfig(loadBuildInfo(), runtime, port, bind, agentKey, token);
    }

    /**
     * 读取构建期写入的 jar 内属性。
     *
     * @return 构建期属性，读不到时返回空集
     */
    private static Properties loadBuildInfo() {
        Properties props = new Properties();
        try (InputStream in = AgentConfig.class.getClassLoader().getResourceAsStream("agent.properties")) {
            if (in != null) {
                props.load(in);
            }
        } catch (Exception e) {
            // 版本信息只用于展示与升级比对，缺失不应阻断启动
        }
        return props;
    }

    /**
     * 监听端口。
     *
     * @return 端口
     */
    public int port() {
        return port;
    }

    /**
     * 监听地址。
     *
     * @return 地址
     */
    public String bind() {
        return bind;
    }

    /**
     * 主机注册密钥。
     *
     * @return 密钥，未配置时为空串
     */
    public String agentKey() {
        return agentKey;
    }

    /**
     * 接口访问令牌。
     *
     * @return 令牌，空串表示不校验
     */
    public String token() {
        return token;
    }

    /**
     * Agent 版本号（构建期注入）。
     *
     * @return 版本号
     */
    public String version() {
        return nonBlank(buildInfo.getProperty("version"), UNKNOWN_VERSION);
    }

    /**
     * 构建时间戳。
     *
     * @return 构建时间，缺失时返回 unknown
     */
    public String buildTime() {
        return nonBlank(buildInfo.getProperty("buildTime"), UNKNOWN_VERSION);
    }

    /**
     * 取任意原始配置项（供 /info 透出排查信息）。
     *
     * @param key 键
     * @return 值，未配置时为 null
     */
    public String raw(String key) {
        return runtime.getProperty(key);
    }

    /**
     * 宽松取整数。
     *
     * @param text     文本
     * @param fallback 缺省值
     * @return 整数，非法或越界时返回缺省值
     */
    private static int parseInt(String text, int fallback) {
        String value = nonBlank(text, null);
        if (value == null) {
            return fallback;
        }
        try {
            int parsed = Integer.parseInt(value);
            return parsed > 0 && parsed < 65536 ? parsed : fallback;
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    /**
     * 取非空白文本。
     *
     * @param text     文本
     * @param fallback 缺省值
     * @return 文本
     */
    private static String nonBlank(String text, String fallback) {
        if (text == null) {
            return fallback;
        }
        String value = text.trim();
        return value.isEmpty() ? fallback : value;
    }
}
