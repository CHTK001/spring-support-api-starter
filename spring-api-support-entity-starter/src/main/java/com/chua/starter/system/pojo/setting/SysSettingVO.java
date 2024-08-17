package com.chua.starter.system.pojo.setting;

import com.baomidou.mybatisplus.annotation.TableField;
import com.chua.common.support.validator.group.AddGroup;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 系统设置
 * @author CH
 */
@Schema(description = "系统设置")
@Data
public class SysSettingVO {

    /**
     * 配置名称
     */
    @TableField(value = "sys_setting_name")
    @ApiModelProperty(value = "配置名称")
    @Schema(description = "配置名称")
    @Size(max = 255, message = "配置名称最大长度要小于 255")
    @NotNull(message = "配置名称不能为null", groups = {AddGroup.class})
    private String sysSettingName;

    /**
     * 配置值
     */
    @TableField(value = "sys_setting_value")
    @ApiModelProperty(value = "配置值")
    @Schema(description = "配置值")
    @Size(max = 255, message = "配置值最大长度要小于 255")
    private String sysSettingValue;
}
