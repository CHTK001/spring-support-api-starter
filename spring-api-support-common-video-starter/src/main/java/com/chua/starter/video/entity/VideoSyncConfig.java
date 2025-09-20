package com.chua.starter.video.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chua.starter.mybatis.pojo.SysBase;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 *
 * @author CH
 * @since 2025/9/18 21:44
 */

/**
 * 视频同步配置表
 */
@ApiModel(description = "视频同步配置表")
@Schema(description = "视频同步配置表")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "video_sync_config")
public class VideoSyncConfig extends SysBase {
    /**
     * 配置ID
     */
    @TableId(value = "video_sync_config_id", type = IdType.AUTO)
    @ApiModelProperty(value = "配置ID")
    @Schema(description = "配置ID")
    private String videoSyncConfigId;

    /**
     * 同步名称
     */
    @TableField(value = "video_sync_config_name")
    @ApiModelProperty(value = "同步名称")
    @Schema(description = "同步名称")
    private String videoSyncConfigName;

    /**
     * 数据来源ID
     */
    @TableField(value = "video_source_id")
    @ApiModelProperty(value = "数据来源ID")
    @Schema(description = "数据来源ID")
    private String videoSourceId;

    /**
     * 是否启用
     */
    @TableField(value = "video_sync_config_enabled")
    @ApiModelProperty(value = "是否启用")
    @Schema(description = "是否启用")
    private Boolean videoSyncConfigEnabled;

    /**
     * 同步周期(分钟)
     */
    @TableField(value = "video_sync_interval")
    @ApiModelProperty(value = "同步周期(分钟)")
    @Schema(description = "同步周期(分钟)")
    private Integer videoSyncInterval;

    /**
     * 最后同步时间
     */
    @TableField(value = "video_sync_config_last_async_time")
    @ApiModelProperty(value = "最后同步时间")
    @Schema(description = "最后同步时间")
    private LocalDateTime videoSyncConfigLastAsyncTime;

    /**
     * 请求头
     */
    @TableField(value = "video_sync_config_headers")
    @ApiModelProperty(value = "请求头")
    @Schema(description = "请求头")
    private String videoSyncConfigHeaders;

    /**
     * 备注
     */
    @TableField(value = "video_sync_config_remark")
    @ApiModelProperty(value = "备注")
    @Schema(description = "备注")
    private String videoSyncConfigRemark;

    /**
     * 同步状态
     */
    @TableField(value = "video_sync_config_status")
    @ApiModelProperty(value = "同步状态")
    @Schema(description = "同步状态")
    private String videoSyncConfigStatus;
    /**
     * 同步消息
     */
    @TableField(value = "video_sync_config_message")
    @ApiModelProperty(value = "同步消息")
    @Schema(description = "同步消息")
    private String videoSyncConfigMessage;

    /**
     * 最后一次的位置
     */
    @TableField(value = "video_sync_config_last_offset")
    @ApiModelProperty(value = "最后一次的位置")
    @Schema(description = "最后一次的位置")
    private String videoSyncConfigLastOffset;
    /**
     * 最后一次的日志
     */
    @TableField(value = "video_sync_config_last_log")
    @ApiModelProperty(value = "最后一次的日志")
    @Schema(description = "最后一次的日志")
    private String videoSyncConfigLastLog;
    /**
     * 同步次数
     */
    @TableField(exist = false)
    @ApiModelProperty(value = "同步次数")
    private Integer syncCount;
    /**
     * 同步视频数量
     */
    @TableField(exist = false)
    @ApiModelProperty(value = "同步视频数量")
    private Long syncVideoCount;
}