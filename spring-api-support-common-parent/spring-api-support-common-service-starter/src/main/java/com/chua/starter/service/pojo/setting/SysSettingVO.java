package com.chua.starter.service.pojo.setting;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.Version;
import com.chua.common.support.base.validator.group.AddGroup;
import com.chua.common.support.base.validator.group.UpdateGroup;
import com.chua.starter.common.support.api.annotations.ApiFieldIgnore;
import com.chua.common.support.base.validator.group.IgnoreGroup;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 系统设置
 *
 * @since 2024/8/7
 * @author CH
 */
@Schema(description = "系统设置")
@Data
public class SysSettingVO {
    @TableId(value = "sys_setting_id", type = IdType.AUTO)
    @ApiModelProperty(value = "")
    @Schema(description = "")
    @NotNull(message = "配置ID不能为null", groups = {UpdateGroup.class})
    @ApiFieldIgnore(IgnoreGroup.class)
    /**
     * SYS设置标识
    */
    private Integer sysSettingId;
    /**
     * 配置参数
     */
    @TableField(value = "sys_setting_config")
    @ApiModelProperty(value = "配置参数")
    @Schema(description = "配置参数")
    @Size(max = 255, message = "配置参数最大长度要小于 255")
    /**
     * SYS设置配置
    */
    private String sysSettingConfig;
    /**
     * 是否启用; 0:不启用
     */
    @TableField(value = "sys_setting_version")
    @ApiModelProperty(value = "版本")
    @Schema(description = "版本")
    @Version
    @ApiFieldIgnore(IgnoreGroup.class)
    /**
     * SYS设置版本
    */
    private Integer sysSettingVersion;
    /**
     * 配置备注
     */
    @TableField(value = "sys_setting_remark")
    @ApiModelProperty(value = "配置备注")
    @Schema(description = "配置备注")
    @Size(max = 255, message = "配置备注最大长度要小于 255")
    /**
     * SYS设置评论
    */
    private String sysSettingRemark;
    /**
     * 配置类型; 用于前端组件判断; e.g.BOOL：布尔
     */
    @TableField(value = "sys_setting_value_type")
    @ApiModelProperty(value = "配置类型; 用于前端组件判断; e.g.BOOL：布尔")
    @Schema(description = "配置类型; 用于前端组件判断; e.g.BOOL：布尔")
    @Size(max = 255, message = "配置类型; 用于前端组件判断; e.g.BOOL：布尔最大长度要小于 255")
    @ApiFieldIgnore(IgnoreGroup.class)
    /**
     * SYS设置值类型
    */
    private String sysSettingValueType;
    /**
     * 配置名称
     */
    @TableField(value = "sys_setting_name")
    @ApiModelProperty(value = "配置名称")
    @Schema(description = "配置名称")
    @Size(max = 255, message = "配置名称最大长度要小于 255")
    @NotNull(message = "配置名称不能为null", groups = {AddGroup.class})
    /**
     * SYS设置名称
    */
    private String sysSettingName;

    /**
     * 配置编码（唯一标识码，如 login.title）
     */
    @TableField(value = "sys_setting_code")
    @ApiModelProperty(value = "配置编码")
    @Schema(description = "配置编码")
    @Size(max = 255, message = "配置编码最大长度要小于 255")
    /**
     * SYS设置代码
    */
    private String sysSettingCode;

    /**
     * 配置值
     */
    @TableField(value = "sys_setting_value")
    @ApiModelProperty(value = "配置值")
    @Schema(description = "配置值")
    @Size(max = 255, message = "配置值最大长度要小于 255")
    /**
     * SYS设置值
    */
    private String sysSettingValue;

    /**
     * 出厂默认值（与当前运行值分离，用于“恢复默认”）
     */
    @TableField(value = "sys_setting_default_value")
    @ApiModelProperty(value = "出厂默认值")
    @Schema(description = "出厂默认值")
    @Size(max = 255, message = "出厂默认值最大长度要小于 255")
    private String sysSettingDefaultValue;

    /**
     * 配置所属分组
     */
    @TableField(value = "sys_setting_group")
    @ApiModelProperty(value = "配置所属分组")
    @Schema(description = "配置所属分组")
    @Size(max = 255, message = "配置所属分组最大长度要小于 255")
    @NotNull(message = "配置名称不能为null", groups = {AddGroup.class})
    @ApiFieldIgnore(IgnoreGroup.class)
    /**
     * SYS设置分组
    */
    private String sysSettingGroup;

    /**
     * 是否系统配置
     */
    @TableField(value = "sys_setting_in_system")
    @ApiModelProperty(value = "是否系统配置")
    @Schema(description = "是否系统配置")
    /**
     * SYS设置insystem
    */
    private Integer sysSettingInSystem;

    /**
     * 排序优先级
     */
    @TableField(value = "sys_setting_sort")
    @ApiModelProperty(value = "排序优先级")
    @Schema(description = "排序优先级")
    /**
     * SYS设置排序
    */
    private Integer sysSettingSort;

    /**
     * 选项配置（Dict/选择 类型用，JSON 字符串）
     */
    @Schema(description = "选项配置")
    /**
     * SYS设置期权
    */
    private String sysSettingOptions;
}
