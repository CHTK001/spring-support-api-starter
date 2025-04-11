package com.chua.starter.school.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.chua.starter.common.support.annotations.ApiIgnore;
import com.chua.starter.common.support.group.IgnoreGroup;
import com.chua.starter.mybatis.pojo.SysBase;
import com.chua.starter.school.enums.SyncType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 学校同步配置
 *
 * @author CH
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("school_sync_config")
@Schema(description = "学校同步配置")
public class SchoolSyncConfig extends SysBase {

    /**
     * 主键ID
     */
    @TableId(value = "school_sync_config_id", type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long schoolSyncConfigId;
    /**
     * 配置名称
     */
    @TableField("school_sync_config_name")
    @Schema(description = "配置名称")
    private String schoolSyncConfigName;
    /**
     * 配置类型
     */
    @TableField("school_sync_config_type")
    @Schema(description = "配置类型")
    private String schoolSyncConfigType;

    /**
     * 同步类型
     */
    @TableField("school_sync_config_sync_type")
    @Schema(description = "同步类型")
    private SyncType schoolSyncConfigSyncType;

    /**
     * cookie
     */
    @TableField("school_sync_config_cookie")
    @Schema(description = "cookie")
    private String schoolSyncConfigCookie;

    /**
     * 同步状态（0-未同步，1-同步中，2-同步成功，3-同步失败）
     */
    @TableField("school_sync_config_sync_status")
    @Schema(description = "同步状态（0-未同步，1-同步中，2-同步成功，3-同步失败）")
    private Integer schoolSyncConfigSyncStatus;
    /**
     * 是否启用
     */
    @TableField("school_sync_config_enabled")
    @Schema(description = "是否启用")
    private Boolean schoolSyncConfigEnabled;

    /**
     * 同步时间
     */
    @TableField("school_sync_config_sync_time")
    @Schema(description = "同步时间")
    private LocalDateTime schoolSyncConfigSyncTime;

    /**
     * 删除标志（0-存在，1-删除）
     */
    @TableField("school_sync_config_deleted")
    @TableLogic
    @Schema(description = "删除标志（0-存在，1-删除）")
    @ApiIgnore(IgnoreGroup.class)
    private Integer schoolSyncConfigDeleted;
}