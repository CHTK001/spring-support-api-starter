package com.chua.starter.school.dto;

import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.school.entity.SchoolSyncConfig;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 学校同步配置查询DTO
 *
 * @author CH
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "学校同步配置查询DTO")
public class SchoolSyncConfigDTO extends Query<SchoolSyncConfig> {

    @Schema(description = "学校编码")
    private String schoolSyncConfigSchoolCode;

    @Schema(description = "学校名称")
    private String schoolSyncConfigSchoolName;

    @Schema(description = "同步类型")
    private String schoolSyncConfigSyncType;

    @Schema(description = "同步状态")
    private String schoolSyncConfigSyncStatus;
} 