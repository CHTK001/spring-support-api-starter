package com.chua.starter.monitor.pojo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Map;

/**
 * 远程操作请求
 *
 * @author CH
 * @since 2024/12/30
 */
@Data
@ApiModel(value = "RemoteOperationRequest", description = "远程操作请求")
@Schema(description = "远程操作请求")
public class RemoteOperationRequest {

    /**
     * 目标节点ID
     */
    @NotBlank(message = "节点ID不能为空")
    @ApiModelProperty(value = "目标节点ID", required = true)
    @Schema(description = "目标节点ID")
    private String nodeId;

    /**
     * 操作类型：FILE_UPLOAD, FILE_DOWNLOAD, SCRIPT_EXECUTE, SYSTEM_MONITOR, TERMINAL_CONNECT
     */
    @NotBlank(message = "操作类型不能为空")
    @ApiModelProperty(value = "操作类型", required = true)
    @Schema(description = "操作类型：FILE_UPLOAD, FILE_DOWNLOAD, SCRIPT_EXECUTE, SYSTEM_MONITOR, TERMINAL_CONNECT")
    private String operationType;

    /**
     * 操作参数
     */
    @ApiModelProperty(value = "操作参数")
    @Schema(description = "操作参数")
    private Map<String, Object> parameters;

    /**
     * 超时时间（秒）
     */
    @ApiModelProperty(value = "超时时间（秒）", example = "30")
    @Schema(description = "超时时间（秒）")
    private Integer timeout = 30;

    /**
     * 是否异步执行
     */
    @ApiModelProperty(value = "是否异步执行", example = "false")
    @Schema(description = "是否异步执行")
    private Boolean async = false;

    /**
     * 操作描述
     */
    @ApiModelProperty(value = "操作描述")
    @Schema(description = "操作描述")
    private String description;

    /**
     * 文件上传请求
     */
    @Data
    @ApiModel(value = "FileUploadRequest", description = "文件上传请求")
    @Schema(description = "文件上传请求")
    public static class FileUploadRequest {
        
        /**
         * 本地文件路径
         */
        @NotBlank(message = "本地文件路径不能为空")
        @ApiModelProperty(value = "本地文件路径", required = true)
        @Schema(description = "本地文件路径")
        private String localFilePath;

        /**
         * 远程文件路径
         */
        @NotBlank(message = "远程文件路径不能为空")
        @ApiModelProperty(value = "远程文件路径", required = true)
        @Schema(description = "远程文件路径")
        private String remoteFilePath;

        /**
         * 是否覆盖已存在文件
         */
        @ApiModelProperty(value = "是否覆盖已存在文件", example = "false")
        @Schema(description = "是否覆盖已存在文件")
        private Boolean overwrite = false;

        /**
         * 文件权限（Linux/Unix）
         */
        @ApiModelProperty(value = "文件权限", example = "755")
        @Schema(description = "文件权限（Linux/Unix）")
        private String permissions;
    }

    /**
     * 文件下载请求
     */
    @Data
    @ApiModel(value = "FileDownloadRequest", description = "文件下载请求")
    @Schema(description = "文件下载请求")
    public static class FileDownloadRequest {
        
        /**
         * 远程文件路径
         */
        @NotBlank(message = "远程文件路径不能为空")
        @ApiModelProperty(value = "远程文件路径", required = true)
        @Schema(description = "远程文件路径")
        private String remoteFilePath;

        /**
         * 本地保存路径
         */
        @ApiModelProperty(value = "本地保存路径")
        @Schema(description = "本地保存路径")
        private String localSavePath;

        /**
         * 是否压缩传输
         */
        @ApiModelProperty(value = "是否压缩传输", example = "true")
        @Schema(description = "是否压缩传输")
        private Boolean compress = true;
    }

    /**
     * 脚本执行请求
     */
    @Data
    @ApiModel(value = "ScriptExecuteRequest", description = "脚本执行请求")
    @Schema(description = "脚本执行请求")
    public static class ScriptExecuteRequest {
        
        /**
         * 脚本内容或脚本文件路径
         */
        @NotBlank(message = "脚本内容不能为空")
        @ApiModelProperty(value = "脚本内容或脚本文件路径", required = true)
        @Schema(description = "脚本内容或脚本文件路径")
        private String script;

        /**
         * 脚本类型：SHELL, BATCH, PYTHON, POWERSHELL
         */
        @ApiModelProperty(value = "脚本类型", example = "SHELL")
        @Schema(description = "脚本类型：SHELL, BATCH, PYTHON, POWERSHELL")
        private String scriptType = "SHELL";

        /**
         * 工作目录
         */
        @ApiModelProperty(value = "工作目录")
        @Schema(description = "工作目录")
        private String workingDirectory;

        /**
         * 环境变量
         */
        @ApiModelProperty(value = "环境变量")
        @Schema(description = "环境变量")
        private Map<String, String> environment;

        /**
         * 脚本参数
         */
        @ApiModelProperty(value = "脚本参数")
        @Schema(description = "脚本参数")
        private String[] arguments;

        /**
         * 是否以管理员权限执行
         */
        @ApiModelProperty(value = "是否以管理员权限执行", example = "false")
        @Schema(description = "是否以管理员权限执行")
        private Boolean runAsAdmin = false;
    }

    /**
     * 系统监控请求
     */
    @Data
    @ApiModel(value = "SystemMonitorRequest", description = "系统监控请求")
    @Schema(description = "系统监控请求")
    public static class SystemMonitorRequest {
        
        /**
         * 监控类型：CPU, MEMORY, DISK, NETWORK, PROCESS, SERVICE
         */
        @ApiModelProperty(value = "监控类型", example = "CPU")
        @Schema(description = "监控类型：CPU, MEMORY, DISK, NETWORK, PROCESS, SERVICE")
        private String monitorType;

        /**
         * 监控间隔（秒）
         */
        @ApiModelProperty(value = "监控间隔（秒）", example = "5")
        @Schema(description = "监控间隔（秒）")
        private Integer interval = 5;

        /**
         * 监控持续时间（秒）
         */
        @ApiModelProperty(value = "监控持续时间（秒）", example = "60")
        @Schema(description = "监控持续时间（秒）")
        private Integer duration = 60;

        /**
         * 是否包含详细信息
         */
        @ApiModelProperty(value = "是否包含详细信息", example = "false")
        @Schema(description = "是否包含详细信息")
        private Boolean includeDetails = false;
    }

    /**
     * 终端连接请求
     */
    @Data
    @ApiModel(value = "TerminalConnectRequest", description = "终端连接请求")
    @Schema(description = "终端连接请求")
    public static class TerminalConnectRequest {
        
        /**
         * 终端类型：SSH, TELNET, LOCAL
         */
        @ApiModelProperty(value = "终端类型", example = "SSH")
        @Schema(description = "终端类型：SSH, TELNET, LOCAL")
        private String terminalType = "SSH";

        /**
         * 用户名
         */
        @ApiModelProperty(value = "用户名")
        @Schema(description = "用户名")
        private String username;

        /**
         * 密码
         */
        @ApiModelProperty(value = "密码")
        @Schema(description = "密码")
        private String password;

        /**
         * 私钥文件路径
         */
        @ApiModelProperty(value = "私钥文件路径")
        @Schema(description = "私钥文件路径")
        private String privateKeyPath;

        /**
         * 终端大小（行数）
         */
        @ApiModelProperty(value = "终端大小（行数）", example = "24")
        @Schema(description = "终端大小（行数）")
        private Integer rows = 24;

        /**
         * 终端大小（列数）
         */
        @ApiModelProperty(value = "终端大小（列数）", example = "80")
        @Schema(description = "终端大小（列数）")
        private Integer cols = 80;
    }
}
