package com.chua.starter.monitor.pojo.agent;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Agent 文件句柄 DTO
 * <p>
 * 用于存储和展示 Agent 上报的文件句柄信息
 * </p>
 *
 * @author CH
 * @version 1.0.0
 * @since 2025/01/17
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Agent 文件句柄信息")
public class AgentFileHandleDTO {

    /**
     * 文件路径
     */
    @Schema(description = "文件路径")
    private String filePath;

    /**
     * 文件描述符
     */
    @Schema(description = "文件描述符")
    private Integer fd;

    /**
     * 句柄类型
     */
    @Schema(description = "句柄类型: FILE, SOCKET, PIPE 等")
    private String handleType;

    /**
     * 打开模式
     */
    @Schema(description = "打开模式: READ, WRITE, READ_WRITE")
    private String mode;

    /**
     * 进程ID
     */
    @Schema(description = "进程ID")
    private Long pid;

    /**
     * 应用名称
     */
    @Schema(description = "应用名称")
    private String applicationName;

    /**
     * IP地址
     */
    @Schema(description = "IP地址")
    private String ipAddress;

    /**
     * 端口
     */
    @Schema(description = "端口")
    private Integer port;

    /**
     * 打开时间
     */
    @Schema(description = "打开时间（毫秒时间戳）")
    private Long openTime;

    /**
     * 最后访问时间
     */
    @Schema(description = "最后访问时间（毫秒时间戳）")
    private Long lastAccessTime;

    /**
     * 读取字节数
     */
    @Schema(description = "读取字节数")
    private Long readBytes;

    /**
     * 写入字节数
     */
    @Schema(description = "写入字节数")
    private Long writeBytes;
}
