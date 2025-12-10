package com.chua.starter.monitor.pojo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 文件管理API配置
 * 
 * @author CH
 * @since 2024/12/13
 */
@ApiModel(description = "文件管理API配置")
@Schema(description = "文件管理API配置")
@Data
public class FileManagementApiConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * API主机地址
     */
    @ApiModelProperty(value = "API主机地址", required = true)
    @Schema(description = "API主机地址")
    private String apiHost;

    /**
     * API端口号
     */
    @ApiModelProperty(value = "API端口号", required = true)
    @Schema(description = "API端口号")
    private Integer apiPort;

    /**
     * API基础路径
     */
    @ApiModelProperty(value = "API基础路径")
    @Schema(description = "API基础路径")
    private String basePath = "/api/file";

    /**
     * 是否使用HTTPS
     */
    @ApiModelProperty(value = "是否使用HTTPS")
    @Schema(description = "是否使用HTTPS")
    private Boolean useHttps = false;

    /**
     * 认证类型
     */
    @ApiModelProperty(value = "认证类型")
    @Schema(description = "认证类型")
    private String authType = "NONE";

    /**
     * 用户名（Basic认证）
     */
    @ApiModelProperty(value = "用户名")
    @Schema(description = "用户名（Basic认证）")
    private String username;

    /**
     * 密码（Basic认证）
     */
    @ApiModelProperty(value = "密码")
    @Schema(description = "密码（Basic认证）")
    private String password;

    /**
     * Token（Token认证）
     */
    @ApiModelProperty(value = "Token")
    @Schema(description = "Token（Token认证）")
    private String token;

    /**
     * API密钥（API Key认证）
     */
    @ApiModelProperty(value = "API密钥")
    @Schema(description = "API密钥（API Key认证）")
    private String apiKey;

    /**
     * API密钥请求头名称
     */
    @ApiModelProperty(value = "API密钥请求头名称")
    @Schema(description = "API密钥请求头名称")
    private String apiKeyHeader = "X-API-Key";

    /**
     * 连接超时时间（秒）
     */
    @ApiModelProperty(value = "连接超时时间")
    @Schema(description = "连接超时时间（秒）")
    private Integer connectionTimeout = 30;

    /**
     * 读取超时时间（秒）
     */
    @ApiModelProperty(value = "读取超时时间")
    @Schema(description = "读取超时时间（秒）")
    private Integer readTimeout = 60;

    /**
     * 写入超时时间（秒）
     */
    @ApiModelProperty(value = "写入超时时间")
    @Schema(description = "写入超时时间（秒）")
    private Integer writeTimeout = 60;

    /**
     * 最大重试次数
     */
    @ApiModelProperty(value = "最大重试次数")
    @Schema(description = "最大重试次数")
    private Integer maxRetries = 3;

    /**
     * 重试间隔时间（毫秒）
     */
    @ApiModelProperty(value = "重试间隔时间")
    @Schema(description = "重试间隔时间（毫秒）")
    private Long retryInterval = 1000L;

    /**
     * 自定义请求头
     */
    @ApiModelProperty(value = "自定义请求头")
    @Schema(description = "自定义请求头（JSON格式）")
    private String customHeaders;

    /**
     * 是否启用压缩
     */
    @ApiModelProperty(value = "是否启用压缩")
    @Schema(description = "是否启用压缩")
    private Boolean compressionEnabled = true;

    /**
     * 是否验证SSL证书
     */
    @ApiModelProperty(value = "是否验证SSL证书")
    @Schema(description = "是否验证SSL证书")
    private Boolean sslVerificationEnabled = true;

    /**
     * 是否使用客户端地址自动检测
     */
    @ApiModelProperty(value = "是否使用客户端地址自动检测")
    @Schema(description = "是否使用客户端地址自动检测")
    private Boolean useClientAddress = true;

    /**
     * 是否使用客户端端口自动检测
     */
    @ApiModelProperty(value = "是否使用客户端端口自动检测")
    @Schema(description = "是否使用客户端端口自动检测")
    private Boolean useClientPort = true;

    /**
     * 认证类型枚举
     */
    public enum AuthType {
        NONE("NONE", "无认证"),
        BASIC("BASIC", "Basic认证"),
        TOKEN("TOKEN", "Token认证"),
        API_KEY("API_KEY", "API Key认证");

        private final String code;
        private final String desc;

        AuthType(String code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public String getCode() {
            return code;
        }

        public String getDesc() {
            return desc;
        }

        public static AuthType fromCode(String code) {
            for (AuthType type : values()) {
                if (type.getCode().equals(code)) {
                    return type;
                }
            }
            return NONE;
        }
    }

    /**
     * 获取认证类型枚举
     */
    public AuthType getAuthTypeEnum() {
        return AuthType.fromCode(authType);
    }

    /**
     * 设置认证类型枚举
     */
    public void setAuthTypeEnum(AuthType authType) {
        this.authType = authType.getCode();
    }

    /**
     * 获取完整的API基础URL
     */
    public String getBaseUrl() {
        String protocol = useHttps ? "https" : "http";
        return String.format("%s://%s:%d%s", protocol, apiHost, apiPort, basePath);
    }

    /**
     * 构建API URL
     */
    public String buildApiUrl(String endpoint) {
        String baseUrl = getBaseUrl();
        if (endpoint.startsWith("/")) {
            return baseUrl + endpoint;
        } else {
            return baseUrl + "/" + endpoint;
        }
    }

    /**
     * 构建文件操作API URL
     */
    public String buildFileOperationUrl(String operation) {
        return buildApiUrl("/file/" + operation);
    }

    /**
     * 获取自定义请求头Map
     */
    public Map<String, String> getCustomHeadersOrDefault() {
        if (customHeaders == null || customHeaders.trim().isEmpty()) {
            return new HashMap<>();
        }
        
        try {
            // 这里应该解析JSON字符串为Map
            // 简化实现，实际应该使用JSON解析器
            return new HashMap<>();
        } catch (Exception e) {
            return new HashMap<>();
        }
    }

    /**
     * 设置自定义请求头Map
     */
    public void setCustomHeadersMap(Map<String, String> headers) {
        if (headers == null || headers.isEmpty()) {
            this.customHeaders = null;
        } else {
            // 这里应该将Map转换为JSON字符串
            // 简化实现，实际应该使用JSON序列化器
            this.customHeaders = "{}";
        }
    }

    /**
     * 获取连接超时时间（带默认值）
     */
    public int getConnectionTimeoutOrDefault() {
        return connectionTimeout != null ? connectionTimeout : 30;
    }

    /**
     * 获取读取超时时间（带默认值）
     */
    public int getReadTimeoutOrDefault() {
        return readTimeout != null ? readTimeout : 60;
    }

    /**
     * 获取写入超时时间（带默认值）
     */
    public int getWriteTimeoutOrDefault() {
        return writeTimeout != null ? writeTimeout : 60;
    }

    /**
     * 获取最大重试次数（带默认值）
     */
    public int getMaxRetriesOrDefault() {
        return maxRetries != null ? maxRetries : 3;
    }

    /**
     * 获取重试间隔时间（带默认值）
     */
    public long getRetryIntervalOrDefault() {
        return retryInterval != null ? retryInterval : 1000L;
    }

    /**
     * 判断是否启用压缩（带默认值）
     */
    public boolean isCompressionEnabledOrDefault() {
        return compressionEnabled != null ? compressionEnabled : true;
    }

    /**
     * 判断是否验证SSL证书（带默认值）
     */
    public boolean isSslVerificationEnabledOrDefault() {
        return sslVerificationEnabled != null ? sslVerificationEnabled : true;
    }

    /**
     * 判断是否使用客户端地址自动检测（带默认值）
     */
    public boolean useClientAddress() {
        return useClientAddress != null ? useClientAddress : true;
    }

    /**
     * 判断是否使用客户端端口自动检测（带默认值）
     */
    public boolean useClientPort() {
        return useClientPort != null ? useClientPort : true;
    }

    /**
     * 验证配置是否有效
     */
    public boolean isValid() {
        if (apiHost == null || apiHost.trim().isEmpty()) {
            return false;
        }
        if (apiPort == null || apiPort <= 0 || apiPort > 65535) {
            return false;
        }
        
        AuthType authTypeEnum = getAuthTypeEnum();
        switch (authTypeEnum) {
            case BASIC:
                return username != null && !username.trim().isEmpty() &&
                       password != null && !password.trim().isEmpty();
            case TOKEN:
                return token != null && !token.trim().isEmpty();
            case API_KEY:
                return apiKey != null && !apiKey.trim().isEmpty() &&
                       apiKeyHeader != null && !apiKeyHeader.trim().isEmpty();
            case NONE:
            default:
                return true;
        }
    }

    /**
     * 获取配置验证错误信息
     */
    public String getValidationError() {
        if (apiHost == null || apiHost.trim().isEmpty()) {
            return "API主机地址不能为空";
        }
        if (apiPort == null || apiPort <= 0 || apiPort > 65535) {
            return "API端口号必须在1-65535范围内";
        }
        
        AuthType authTypeEnum = getAuthTypeEnum();
        switch (authTypeEnum) {
            case BASIC:
                if (username == null || username.trim().isEmpty()) {
                    return "Basic认证需要配置用户名";
                }
                if (password == null || password.trim().isEmpty()) {
                    return "Basic认证需要配置密码";
                }
                break;
            case TOKEN:
                if (token == null || token.trim().isEmpty()) {
                    return "Token认证需要配置Token";
                }
                break;
            case API_KEY:
                if (apiKey == null || apiKey.trim().isEmpty()) {
                    return "API Key认证需要配置API密钥";
                }
                if (apiKeyHeader == null || apiKeyHeader.trim().isEmpty()) {
                    return "API Key认证需要配置请求头名称";
                }
                break;
        }
        
        return null;
    }

    /**
     * 获取健康检查URL
     */
    public String getHealthCheckUrl() {
        return buildApiUrl("/health");
    }

    /**
     * 创建默认配置
     */
    public static FileManagementApiConfig createDefault() {
        FileManagementApiConfig config = new FileManagementApiConfig();
        config.setApiHost("localhost");
        config.setApiPort(8080);
        config.setBasePath("/api/file");
        config.setUseHttps(false);
        config.setAuthType("NONE");
        config.setConnectionTimeout(30);
        config.setReadTimeout(60);
        config.setWriteTimeout(60);
        config.setMaxRetries(3);
        config.setRetryInterval(1000L);
        config.setCompressionEnabled(true);
        config.setSslVerificationEnabled(true);
        config.setUseClientAddress(true);
        config.setUseClientPort(true);
        return config;
    }

    @Override
    public String toString() {
        return String.format("FileManagementApiConfig{baseUrl=%s, authType=%s, timeout=%ds}", 
                getBaseUrl(), authType, getConnectionTimeoutOrDefault());
    }
}
