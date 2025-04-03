package com.chua.starter.school.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 学校同步配置
 *
 * @author CH
 */
@Data
@TableName("school_sync_config")
@Schema(description = "学校同步配置")
public class SchoolSyncConfig {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long configId;

    /**
     * 配置名称
     */
    @Schema(description = "配置名称")
    private String configName;

    /**
     * 同步目标（如学校信息、专业信息等）
     */
    @Schema(description = "同步目标（如学校信息、专业信息等）")
    private String syncTarget;

    /**
     * 同步URL
     */
    @Schema(description = "同步URL")
    private String syncUrl;

    /**
     * 同步类型
     */
    @Schema(description = "同步类型")
    private String syncType;

    /**
     * 同步Cookie
     */
    @Schema(description = "同步Cookie")
    private String syncCookie;

    /**
     * 同步周期（单位：小时）
     */
    @Schema(description = "同步周期（单位：小时）")
    private Integer syncPeriod;

    /**
     * 上次同步时间
     */
    @Schema(description = "上次同步时间")
    private LocalDateTime lastSyncTime;

    /**
     * 同步状态（0-未同步，1-同步中，2-同步成功，3-同步失败）
     */
    @Schema(description = "同步状态（0-未同步，1-同步中，2-同步成功，3-同步失败）")
    private Integer syncStatus;

    /**
     * 备注
     */
    @Schema(description = "备注")
    private String syncRemark;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
} 