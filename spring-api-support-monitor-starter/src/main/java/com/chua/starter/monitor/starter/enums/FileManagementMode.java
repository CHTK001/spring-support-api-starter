package com.chua.starter.monitor.starter.enums;

import io.swagger.annotations.ApiModel;

/**
 * 文件管理模式枚举
 * 
 * @author CH
 * @since 2024/12/13
 */
@ApiModel(description = "文件管理模式")
public enum FileManagementMode {

    /**
     * 不启用文件管理
     */
    NONE("NONE", "不启用", "不提供文件管理功能"),

    /**
     * 本地模式 - 直接访问本地文件系统
     */
    LOCAL("LOCAL", "本地连接", "直接访问服务器本地文件系统，仅适用于本地服务器"),

    /**
     * SSH模式 - 通过SSH/SFTP协议进行文件管理
     */
    SSH("SSH", "SSH模式", "通过SSH/SFTP协议直接访问服务器文件系统"),

    /**
     * NODE模式 - 通过在线客户端进行文件管理（不支持手动输入地址）
     */
    NODE("NODE", "NODE（在线）", "通过在线客户端代理进行文件管理，自动检测客户端地址"),

    /**
     * API模式 - 通过手动配置的HTTP API进行文件管理
     */
    API("API", "API模式", "通过手动配置的HTTP API地址进行文件管理");

    private final String code;
    private final String name;
    private final String description;

    FileManagementMode(String code, String name, String description) {
        this.code = code;
        this.name = name;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 根据代码获取枚举值
     */
    public static FileManagementMode fromCode(String code) {
        if (code == null) {
            return NONE;
        }

        for (FileManagementMode mode : values()) {
            if (mode.code.equals(code)) {
                return mode;
            }
        }
        return NONE;
    }

    /**
     * 根据名称获取枚举值
     */
    public static FileManagementMode fromName(String name) {
        if (name == null) {
            return NONE;
        }

        for (FileManagementMode mode : values()) {
            if (mode.name.equals(name)) {
                return mode;
            }
        }
        return NONE;
    }

    /**
     * 判断是否支持文件管理功能
     */
    public boolean supportsFileManagement() {
        return this != NONE;
    }

    /**
     * 判断是否需要本地文件系统访问
     */
    public boolean requiresLocalFileSystem() {
        return this == LOCAL;
    }

    /**
     * 判断是否需要SSH连接
     */
    public boolean requiresSshConnection() {
        return this == SSH;
    }

    /**
     * 判断是否需要API连接
     */
    public boolean requiresApiConnection() {
        return this == API || this == NODE;
    }

    /**
     * 判断是否需要客户端在线
     */
    public boolean requiresClientOnline() {
        return this == NODE;
    }

    /**
     * 判断是否需要手动配置API地址
     */
    public boolean requiresManualApiConfig() {
        return this == API;
    }

    /**
     * 判断是否支持自动客户端检测
     */
    public boolean supportsAutoClientDetection() {
        return this == NODE;
    }

    /**
     * 判断是否支持实时文件操作
     */
    public boolean supportsRealtimeOperations() {
        return this == LOCAL || this == SSH || this == API || this == NODE;
    }

    /**
     * 判断是否支持大文件传输
     */
    public boolean supportsLargeFileTransfer() {
        return this == LOCAL || this == SSH || this == API || this == NODE;
    }

    /**
     * 判断是否支持文件权限管理
     */
    public boolean supportsPermissionManagement() {
        return this == LOCAL || this == SSH;
    }

    /**
     * 判断是否支持文件压缩/解压
     */
    public boolean supportsArchiveOperations() {
        return this == LOCAL || this == SSH || this == API || this == NODE;
    }

    /**
     * 判断是否支持文件搜索
     */
    public boolean supportsFileSearch() {
        return this == LOCAL || this == SSH || this == API || this == NODE;
    }

    /**
     * 判断是否支持文件预览
     */
    public boolean supportsFilePreview() {
        return this == LOCAL || this == SSH || this == API || this == NODE;
    }

    /**
     * 获取支持的操作列表
     */
    public String[] getSupportedOperations() {
        switch (this) {
            case LOCAL:
                return new String[]{"LIST", "TREE", "UPLOAD", "DOWNLOAD", "DELETE", "RENAME", "MKDIR", "COPY", "MOVE",
                        "CHMOD", "CHOWN", "SEARCH", "PREVIEW", "INFO", "COMPRESS", "EXTRACT"};
            case SSH:
                return new String[]{"LIST", "TREE", "UPLOAD", "DOWNLOAD", "DELETE", "RENAME", "MKDIR", "COPY", "MOVE",
                        "CHMOD", "CHOWN", "SEARCH", "PREVIEW", "INFO"};
            case NODE:
                return new String[]{"LIST", "TREE", "UPLOAD", "DOWNLOAD", "DELETE", "RENAME", "MKDIR", "COPY", "MOVE",
                        "SEARCH", "PREVIEW", "INFO", "COMPRESS", "EXTRACT"};
            case API:
                return new String[]{"LIST", "TREE", "UPLOAD", "DOWNLOAD", "DELETE", "RENAME", "MKDIR", "COPY", "MOVE",
                        "SEARCH", "PREVIEW", "INFO", "COMPRESS", "EXTRACT"};
            case NONE:
            default:
                return new String[0];
        }
    }

    /**
     * 判断是否支持指定操作
     */
    public boolean supportsOperation(String operation) {
        if (operation == null) {
            return false;
        }

        String[] supportedOps = getSupportedOperations();
        for (String op : supportedOps) {
            if (op.equalsIgnoreCase(operation)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取默认超时时间（秒）
     */
    public int getDefaultTimeout() {
        switch (this) {
            case LOCAL:
                return 30; // 本地操作默认30秒超时
            case SSH:
                return 60; // SSH操作默认60秒超时
            case NODE:
                return 30; // NODE操作默认30秒超时
            case API:
                return 30; // API操作默认30秒超时
            case NONE:
            default:
                return 0;
        }
    }

    /**
     * 获取默认重试次数
     */
    public int getDefaultMaxRetries() {
        switch (this) {
            case LOCAL:
                return 1; // 本地操作默认重试1次
            case SSH:
                return 2; // SSH操作默认重试2次
            case NODE:
                return 3; // NODE操作默认重试3次
            case API:
                return 3; // API操作默认重试3次
            case NONE:
            default:
                return 0;
        }
    }

    /**
     * 判断是否需要操作系统限制
     */
    public boolean hasOsRestriction() {
        return this == SSH; // SSH模式仅支持Linux系统
    }

    /**
     * 获取支持的操作系统列表
     */
    public String[] getSupportedOperatingSystems() {
        switch (this) {
            case SSH:
                return new String[]{"Linux", "Unix"};
            case API:
                return new String[]{"Linux", "Unix", "Windows", "MacOS"};
            case NONE:
            default:
                return new String[0];
        }
    }

    /**
     * 判断是否支持指定操作系统
     */
    public boolean supportsOperatingSystem(String os) {
        if (os == null) {
            return false;
        }

        String[] supportedOs = getSupportedOperatingSystems();
        for (String supportedOsName : supportedOs) {
            if (supportedOsName.equalsIgnoreCase(os)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取配置验证规则
     */
    public String getConfigValidationRules() {
        switch (this) {
            case SSH:
                return "需要配置SSH连接信息：主机地址、端口、用户名、密码或私钥";
            case API:
                return "需要配置API连接信息：客户端地址、端口、认证方式等";
            case NONE:
            default:
                return "无需配置";
        }
    }

    /**
     * 获取使用建议
     */
    public String getUsageRecommendation() {
        switch (this) {
            case SSH:
                return "适用于Linux/Unix服务器，提供完整的文件系统访问权限，支持权限管理";
            case API:
                return "适用于需要客户端代理的环境，支持跨平台，需要客户端保持在线状态";
            case NONE:
            default:
                return "不提供文件管理功能";
        }
    }

    @Override
    public String toString() {
        return String.format("%s(%s): %s", name, code, description);
    }
}
