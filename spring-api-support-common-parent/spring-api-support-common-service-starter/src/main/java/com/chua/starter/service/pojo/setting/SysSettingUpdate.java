package com.chua.starter.service.pojo.setting;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
* 系统设置
* @since 2024/8/9
* @author CH
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
    /** SYS设置分组 */
    private String sysSettingGroup;

    /**
    * 配置编码（查询条件）
     */
    @Schema(description = "配置编码")
    /** SYS设置代码 */
    private String sysSettingCode;
}
