package com.chua.starter.school.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 学校同步配置VO
 *
 * @author CH
 */
@Data
@Schema(description = "学校同步配置VO")
public class SchoolSyncConfigVO {

    @Schema(description = "配置ID")
    private Long schoolSyncConfigId;

    @Schema(description = "学校编码")
    private String schoolSyncConfigSchoolCode;

    @Schema(description = "学校名称")
    private String schoolSyncConfigSchoolName;

    @Schema(description = "同步类型")
    private String schoolSyncConfigSyncType;

    @Schema(description = "同步状态")
    private String schoolSyncConfigSyncStatus;

    @Schema(description = "最后同步时间")
    private LocalDateTime schoolSyncConfigLastSyncTime;

    @Schema(description = "失败原因")
    private String schoolSyncConfigFailureReason;

    @Schema(description = "创建时间")
    private LocalDateTime schoolSyncConfigCreateTime;

    @Schema(description = "更新时间")
    private LocalDateTime schoolSyncConfigUpdateTime;

    @Schema(description = "备注")
    private String schoolSyncConfigRemark;
} 