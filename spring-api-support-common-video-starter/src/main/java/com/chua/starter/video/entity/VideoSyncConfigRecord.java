package com.chua.starter.video.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chua.starter.mybatis.pojo.SysBase;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 视频同步记录
 *
 * @author CH
 * @since 2025/9/15 9:38
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "video_sync_config_record")
@Schema(description = "视频同步记录")
public class VideoSyncConfigRecord extends SysBase {
    /**
     * 记录ID
     */
    @TableId
    @Schema(description = "记录ID")
    private String videoSyncConfigRecordId;

    /**
     * 同步ID
     */
    @TableField("video_sync_config_id")
    @Schema(description = "同步ID")
    private String videoSyncConfigId;

    /**
     * 同步结果
     */
    @TableField("video_sync_config_result")
    @Schema(description = "同步结果")
    private String videoSyncConfigResult;

    /**
     * 同步时间
     */
    @TableField("video_sync_config_record_time")
    @Schema(description = "同步时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime videoSyncConfigRecordTime;

    /**
     * 同步错误信息
     */
    @TableField("video_sync_config_error_message")
    @Schema(description = "同步错误信息")
    private String videoSyncConfigErrorMessage;
}
