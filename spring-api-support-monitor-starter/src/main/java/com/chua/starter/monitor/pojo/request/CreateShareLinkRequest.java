package com.chua.starter.monitor.pojo.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 创建分享链接请求
 *
 * @author CH
 * @since 2025-12-08
 */
@Data
@Schema(description = "创建分享链接请求")
public class CreateShareLinkRequest {

    /**
     * 服务器ID
     */
    @Schema(description = "服务器ID")
    private Long serverId;

    /**
     * 服务器基础 URL（如 http://192.168.1.100:8080）
     */
    @Schema(description = "服务器基础URL")
    private String baseUrl;

    /**
     * 存储桶
     */
    @Schema(description = "存储桶")
    private String bucket;

    /**
     * 文件路径
     */
    @Schema(description = "文件路径")
    private String filePath;

    /**
     * 文件名
     */
    @Schema(description = "文件名")
    private String fileName;

    /**
     * 过期时间（分钟），0 或 null 表示永不过期
     */
    @Schema(description = "过期时间（分钟）")
    private Integer expireMinutes;
}
