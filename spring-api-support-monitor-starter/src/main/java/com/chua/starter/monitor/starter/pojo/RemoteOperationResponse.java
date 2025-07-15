package com.chua.starter.monitor.starter.pojo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 远程操作响应
 *
 * @author CH
 * @since 2024/12/30
 */
@Data
@ApiModel(value = "RemoteOperationResponse", description = "远程操作响应")
@Schema(description = "远程操作响应")
public class RemoteOperationResponse {

    /**
     * 操作ID
     */
    @ApiModelProperty(value = "操作ID")
    @Schema(description = "操作ID")
    private String operationId;

    /**
     * 节点ID
     */
    @ApiModelProperty(value = "节点ID")
    @Schema(description = "节点ID")
    private String nodeId;

    /**
     * 操作类型
     */
    @ApiModelProperty(value = "操作类型")
    @Schema(description = "操作类型")
    private String operationType;

    /**
     * 执行状态：SUCCESS, FAILED, RUNNING, TIMEOUT, CANCELLED
     */
    @ApiModelProperty(value = "执行状态")
    @Schema(description = "执行状态：SUCCESS, FAILED, RUNNING, TIMEOUT, CANCELLED")
    private String status;

    /**
     * 执行结果
     */
    @ApiModelProperty(value = "执行结果")
    @Schema(description = "执行结果")
    private Object result;

    /**
     * 错误信息
     */
    @ApiModelProperty(value = "错误信息")
    @Schema(description = "错误信息")
    private String errorMessage;

    /**
     * 开始时间
     */
    @ApiModelProperty(value = "开始时间")
    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    @ApiModelProperty(value = "结束时间")
    @Schema(description = "结束时间")
    private LocalDateTime endTime;

    /**
     * 执行耗时（毫秒）
     */
    @ApiModelProperty(value = "执行耗时（毫秒）")
    @Schema(description = "执行耗时（毫秒）")
    private Long duration;

    /**
     * 进度百分比（0-100）
     */
    @ApiModelProperty(value = "进度百分比", example = "100")
    @Schema(description = "进度百分比（0-100）")
    private Integer progress = 0;

    /**
     * 额外信息
     */
    @ApiModelProperty(value = "额外信息")
    @Schema(description = "额外信息")
    private Map<String, Object> metadata;

    /**
     * 文件上传响应
     */
    @Data
    @ApiModel(value = "FileUploadResponse", description = "文件上传响应")
    @Schema(description = "文件上传响应")
    public static class FileUploadResponse {
        
        /**
         * 上传的文件路径
         */
        @ApiModelProperty(value = "上传的文件路径")
        @Schema(description = "上传的文件路径")
        private String filePath;

        /**
         * 文件大小（字节）
         */
        @ApiModelProperty(value = "文件大小（字节）")
        @Schema(description = "文件大小（字节）")
        private Long fileSize;

        /**
         * 文件MD5校验值
         */
        @ApiModelProperty(value = "文件MD5校验值")
        @Schema(description = "文件MD5校验值")
        private String md5Hash;

        /**
         * 传输速度（字节/秒）
         */
        @ApiModelProperty(value = "传输速度（字节/秒）")
        @Schema(description = "传输速度（字节/秒）")
        private Long transferSpeed;

        /**
         * 是否覆盖了已存在文件
         */
        @ApiModelProperty(value = "是否覆盖了已存在文件")
        @Schema(description = "是否覆盖了已存在文件")
        private Boolean overwritten;
    }

    /**
     * 文件下载响应
     */
    @Data
    @ApiModel(value = "FileDownloadResponse", description = "文件下载响应")
    @Schema(description = "文件下载响应")
    public static class FileDownloadResponse {
        
        /**
         * 下载的文件路径
         */
        @ApiModelProperty(value = "下载的文件路径")
        @Schema(description = "下载的文件路径")
        private String filePath;

        /**
         * 本地保存路径
         */
        @ApiModelProperty(value = "本地保存路径")
        @Schema(description = "本地保存路径")
        private String localPath;

        /**
         * 文件大小（字节）
         */
        @ApiModelProperty(value = "文件大小（字节）")
        @Schema(description = "文件大小（字节）")
        private Long fileSize;

        /**
         * 文件MD5校验值
         */
        @ApiModelProperty(value = "文件MD5校验值")
        @Schema(description = "文件MD5校验值")
        private String md5Hash;

        /**
         * 传输速度（字节/秒）
         */
        @ApiModelProperty(value = "传输速度（字节/秒）")
        @Schema(description = "传输速度（字节/秒）")
        private Long transferSpeed;

        /**
         * 是否压缩传输
         */
        @ApiModelProperty(value = "是否压缩传输")
        @Schema(description = "是否压缩传输")
        private Boolean compressed;
    }

    /**
     * 脚本执行响应
     */
    @Data
    @ApiModel(value = "ScriptExecuteResponse", description = "脚本执行响应")
    @Schema(description = "脚本执行响应")
    public static class ScriptExecuteResponse {
        
        /**
         * 退出码
         */
        @ApiModelProperty(value = "退出码")
        @Schema(description = "退出码")
        private Integer exitCode;

        /**
         * 标准输出
         */
        @ApiModelProperty(value = "标准输出")
        @Schema(description = "标准输出")
        private String stdout;

        /**
         * 标准错误输出
         */
        @ApiModelProperty(value = "标准错误输出")
        @Schema(description = "标准错误输出")
        private String stderr;

        /**
         * 进程ID
         */
        @ApiModelProperty(value = "进程ID")
        @Schema(description = "进程ID")
        private Long processId;

        /**
         * 执行的命令
         */
        @ApiModelProperty(value = "执行的命令")
        @Schema(description = "执行的命令")
        private String command;

        /**
         * 工作目录
         */
        @ApiModelProperty(value = "工作目录")
        @Schema(description = "工作目录")
        private String workingDirectory;
    }

    /**
     * 系统监控响应
     */
    @Data
    @ApiModel(value = "SystemMonitorResponse", description = "系统监控响应")
    @Schema(description = "系统监控响应")
    public static class SystemMonitorResponse {
        
        /**
         * 监控类型
         */
        @ApiModelProperty(value = "监控类型")
        @Schema(description = "监控类型")
        private String monitorType;

        /**
         * 监控数据
         */
        @ApiModelProperty(value = "监控数据")
        @Schema(description = "监控数据")
        private List<Map<String, Object>> data;

        /**
         * 采样时间戳
         */
        @ApiModelProperty(value = "采样时间戳")
        @Schema(description = "采样时间戳")
        private List<LocalDateTime> timestamps;

        /**
         * 统计信息
         */
        @ApiModelProperty(value = "统计信息")
        @Schema(description = "统计信息")
        private Map<String, Object> statistics;
    }

    /**
     * 终端连接响应
     */
    @Data
    @ApiModel(value = "TerminalConnectResponse", description = "终端连接响应")
    @Schema(description = "终端连接响应")
    public static class TerminalConnectResponse {
        
        /**
         * 会话ID
         */
        @ApiModelProperty(value = "会话ID")
        @Schema(description = "会话ID")
        private String sessionId;

        /**
         * WebSocket连接地址
         */
        @ApiModelProperty(value = "WebSocket连接地址")
        @Schema(description = "WebSocket连接地址")
        private String websocketUrl;

        /**
         * 终端类型
         */
        @ApiModelProperty(value = "终端类型")
        @Schema(description = "终端类型")
        private String terminalType;

        /**
         * 连接状态
         */
        @ApiModelProperty(value = "连接状态")
        @Schema(description = "连接状态")
        private String connectionStatus;

        /**
         * 终端大小
         */
        @ApiModelProperty(value = "终端大小")
        @Schema(description = "终端大小")
        private Map<String, Integer> terminalSize;
    }

    /**
     * 创建成功响应
     */
    public static RemoteOperationResponse success(String operationId, String nodeId, String operationType, Object result) {
        RemoteOperationResponse response = new RemoteOperationResponse();
        response.setOperationId(operationId);
        response.setNodeId(nodeId);
        response.setOperationType(operationType);
        response.setStatus("SUCCESS");
        response.setResult(result);
        response.setProgress(100);
        response.setEndTime(LocalDateTime.now());
        return response;
    }

    /**
     * 创建失败响应
     */
    public static RemoteOperationResponse failed(String operationId, String nodeId, String operationType, String errorMessage) {
        RemoteOperationResponse response = new RemoteOperationResponse();
        response.setOperationId(operationId);
        response.setNodeId(nodeId);
        response.setOperationType(operationType);
        response.setStatus("FAILED");
        response.setErrorMessage(errorMessage);
        response.setProgress(0);
        response.setEndTime(LocalDateTime.now());
        return response;
    }

    /**
     * 创建运行中响应
     */
    public static RemoteOperationResponse running(String operationId, String nodeId, String operationType, int progress) {
        RemoteOperationResponse response = new RemoteOperationResponse();
        response.setOperationId(operationId);
        response.setNodeId(nodeId);
        response.setOperationType(operationType);
        response.setStatus("RUNNING");
        response.setProgress(progress);
        return response;
    }
}
