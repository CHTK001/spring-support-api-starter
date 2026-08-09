package com.chua.starter.service.pojo.setting;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 系统设置
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

    /**
     * 配置编码（查询条件）
     */
    @Schema(description = "配置编码")
    private String sysSettingCode;
}
