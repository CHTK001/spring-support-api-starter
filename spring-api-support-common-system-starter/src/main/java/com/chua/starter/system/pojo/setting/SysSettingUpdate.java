package com.chua.starter.system.pojo.setting;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author CH
 * @since 2024/8/9
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "系统设置")
public class SysSettingUpdate {

    /**
     * 系统设置分组
     */
    @Schema(description = "系统设置分组")
    private String sysSettingGroup;
}
