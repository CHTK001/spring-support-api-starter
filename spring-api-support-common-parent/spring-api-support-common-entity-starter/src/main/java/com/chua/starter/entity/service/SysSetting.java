package com.chua.starter.entity.service;

import com.baomidou.mybatisplus.annotation.*;
import com.chua.common.support.base.validator.group.AddGroup;
import com.chua.common.support.base.validator.group.UpdateGroup;
import com.chua.starter.mybatis.pojo.SysBase;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 系统设置表
 *
 * @since 2024/8/9
 * @author CH
 */
@Schema(description = "系统设置表")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_setting")
public class SysSetting extends SysBase implements Serializable {
    /**
     * 串行版本UID
    */
    private static final long serialVersionUID = 1L;

    /**
     * 配置标识
     */
    @TableId(value = "sys_setting_id", type = IdType.AUTO)
    @Schema(description = "配置ID")
    @NotNull(message = "配置ID不能为空", groups = {UpdateGroup.class})
    /**
     * SYS设置标识
    */
    private Integer sysSettingId;

    /**
     * 配置名称
     */
    @TableField(value = "sys_setting_name")
    @Schema(description = "配置名称")
    @Size(max = 255, message = "配置名称最大长度需小于255")
    @NotNull(message = "配置名称不能为空", groups = {AddGroup.class})
    /**
     * SYS设置名称
    */
    private String sysSettingName;

    /**
     * 配置编码（唯一标识码，如 login.title）
     */
    @TableField(value = "sys_setting_code")
    @Schema(description = "配置编码")
    @Size(max = 255, message = "配置编码最大长度需小于255")
    /**
     * SYS设置代码
    */
    private String sysSettingCode;

    /**
     * 配置值
     */
    @TableField(value = "sys_setting_value")
    @Schema(description = "配置值")
    @Size(max = 65535, message = "配置值最大长度需小于65535")
    /**
     * SYS设置值
    */
    private String sysSettingValue;

    /**
     * 出厂默认值（与当前运行值 sysSettingValue 分离，用于“恢复默认”）
     */
    @TableField(value = "sys_setting_default_value")
    private String sysSettingDefaultValue;

    /**
     * 配置参数
     */
    @TableField(value = "sys_setting_config")
    @Schema(description = "配置参数")
    @Size(max = 255, message = "配置参数最大长度需小于255")
    /**
     * SYS设置配置
    */
    private String sysSettingConfig;

    /**
     * 配置备注
     */
    @TableField(value = "sys_setting_remark")
    @Schema(description = "配置备注")
    @Size(max = 255, message = "配置备注最大长度需小于255")
    /**
     * SYS设置评论
    */
    private String sysSettingRemark;

    /**
     * 排序优先级
     */
    @TableField(value = "sys_setting_sort")
    @Schema(description = "排序优先级")
    /**
     * SYS设置排序
    */
    private Integer sysSettingSort;

    /**
     * 版本号(乐观锁)
     */
    @TableField(value = "sys_setting_version")
    @Schema(description = "版本号")
    @Version
    /**
     * SYS设置版本
    */
    private Integer sysSettingVersion;

    /**
     * 是否启用: 0:不启用 1:启用
     */
    @TableField(value = "sys_setting_status")
    @Schema(description = "是否启用: 0:不启用 1:启用")
    /**
     * SYS设置状态
    */
    private Integer sysSettingStatus;

    /**
     * 是否仅管理员生效: 0:否 1:是
     */
    @TableField(value = "sys_setting_admin")
    @Schema(description = "是否仅管理员生效: 0:否 1:是")
    /**
     * SYS设置admin
    */
    private Integer sysSettingAdmin;

    /**
     * 配置值类型(前端组件判断用): BOOL-布尔 文本-文本 等
     */
    @TableField(value = "sys_setting_value_type")
    @Schema(description = "配置值类型(前端组件判断用): BOOL-布尔 TEXT-文本 等")
    @Size(max = 255, message = "配置值类型最大长度需小于255")
    /**
     * SYS设置值类型
    */
    private String sysSettingValueType;

    /**
     * 配置所属分组
     */
    @TableField(value = "sys_setting_group")
    @Schema(description = "配置所属分组")
    @Size(max = 255, message = "配置所属分组最大长度需小于255")
    @NotNull(message = "配置所属分组不能为空", groups = {AddGroup.class})
    /**
     * SYS设置分组
    */
    private String sysSettingGroup;

    /**
     * 是否是配置集合: 0:单个 1:集合
     */
    @TableField(value = "sys_setting_collect")
    @Schema(description = "是否是配置集合: 0:单个 1:集合")
    /**
     * SYS设置collect
    */
    private Integer sysSettingCollect;

    /**
     * 是否系统配置: 0:否 1:是
     */
    @TableField(value = "sys_setting_in_system")
    @Schema(description = "是否系统配置: 0:否 1:是")
    /**
     * SYS设置insystem
    */
    private Integer sysSettingInSystem;

    /**
     * 是否作用于系统启动: 0:否 1:是
     */
    @TableField(value = "sys_setting_app_inner")
    @Schema(description = "是否作用于系统启动: 0:否 1:是")
    /**
     * SYS设置appinner
    */
    private Integer sysSettingAppInner;

    /**
     * 是否敏感配置: 0:否 1:是（列表/详情返回时掩码，明文需通过解密接口获取）
     */
    @TableField(value = "sys_setting_is_sensitive")
    @Schema(description = "是否敏感配置: 0:否 1:是")
    /**
     * SYS设置issensitive
    */
    private Integer sysSettingIsSensitive;

    /**
     * 校验规则（正则表达式，可为空）。配置值非空时，更新/新增需匹配该正则
     */
    @TableField(value = "sys_setting_validate")
    @Schema(description = "校验规则(正则表达式，可为空)")
    @Size(max = 512, message = "校验规则最大长度需小于512")
    /**
     * SYS设置校验
    */
    private String sysSettingValidate;
}
