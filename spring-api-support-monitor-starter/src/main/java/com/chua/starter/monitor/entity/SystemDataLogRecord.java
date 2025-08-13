package com.chua.starter.monitor.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemDataLogRecord {

    @Schema(description = "记录ID")
    private String id;

    @Schema(description = "配置ID")
    private Integer settingId;

    @Schema(description = "日志级别")
    private String level;

    @Schema(description = "时间戳")
    private Long timestamp;

    @Schema(description = "内容")
    private String message;

    @Schema(description = "来源")
    private String from;
}


