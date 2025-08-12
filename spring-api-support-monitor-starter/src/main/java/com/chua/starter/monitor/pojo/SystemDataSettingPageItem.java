package com.chua.starter.monitor.pojo;

import com.chua.starter.monitor.entity.SystemDataSetting;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 数据源分页项，携带能力信息
 *
 * @author CH
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "数据源分页项，包含能力标识")
public class SystemDataSettingPageItem extends SystemDataSetting {

    /** 是否支持备份 */
    @Schema(description = "是否支持备份")
    private boolean capabilitiesBackup;

    /** 是否支持文档 */
    @Schema(description = "是否支持文档")
    private boolean capabilitiesDocument;

    /** 是否文件型 */
    @Schema(description = "是否文件型数据源")
    private boolean capabilitiesFile;
}


