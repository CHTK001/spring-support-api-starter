package com.chua.starter.system.pojo.setting;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author CH
 * @since 2024/8/9
 */
@Data
@Schema(description = "系统设置")
public class SysSettingUpdate {

    /**
     * 系统设置分组
     */
    @Schema(description = "系统设置分组")
    private String sysSettingGroup;
}
