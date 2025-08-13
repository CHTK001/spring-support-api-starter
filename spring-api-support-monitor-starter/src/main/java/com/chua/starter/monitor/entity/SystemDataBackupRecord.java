package com.chua.starter.monitor.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 备份事件记录（用于 RedisSearch 持久化）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemDataBackupRecord {

    @Schema(description = "记录ID")
    private String id;

    @Schema(description = "配置ID")
    private Integer settingId;

    @Schema(description = "事件类型")
    private String event;

    @Schema(description = "时间戳")
    private Long timestamp;

    @Schema(description = "名称（文件名）")
    private String name;

    @Schema(description = "备份文件路径/地址")
    private String path;

    @Schema(description = "大小（字节），未知为-1")
    private Long size;

    @Schema(description = "描述/消息")
    private String message;
}


