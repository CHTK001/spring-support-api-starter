package com.chua.starter.entity.system;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chua.common.support.base.validator.group.AddGroup;
import com.chua.common.support.base.validator.group.UpdateGroup;
import com.chua.common.support.base.validator.group.UploadGroup;
import com.chua.starter.mybatis.pojo.SysBase;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 系统组件表
 *
 * @author CH
 * @since 2024/10/24
 */
@Schema(description = "系统组件表")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_sfc")
public class SysSfc extends SysBase implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 组件ID
     */
    @TableId(value = "sys_sfc_id", type = IdType.AUTO)
    @Schema(description = "组件ID")
    @NotNull(message = "组件ID不能为空", groups = {UpdateGroup.class, UploadGroup.class})
    private Integer sysSfcId;

    /**
     * 组件名称(英文)
     */
    @TableField(value = "sys_sfc_name")
    @Schema(description = "组件名称(英文)")
    @Size(max = 255, message = "组件名称最大长度需小于255", groups = {AddGroup.class})
    private String sysSfcName;

    /**
     * 组件中文名称
     */
    @TableField(value = "sys_sfc_chinese_name")
    @Schema(description = "组件中文名称")
    @Size(max = 255, message = "组件中文名称最大长度需小于255", groups = {AddGroup.class})
    private String sysSfcChineseName;

    /**
     * 缓存模式
     */
    @TableField(value = "sys_sfc_model_cache")
    @Schema(description = "缓存模式")
    private String sysSfcModelCache;

    /**
     * 组件功能描述(多个逗号分隔)
     */
    @TableField(value = "sys_sfc_function")
    @Schema(description = "组件功能描述(多个逗号分隔)")
    @Size(max = 255, message = "组件功能描述最大长度需小于255", groups = {AddGroup.class})
    private String sysSfcFunction;

    /**
     * 组件图标
     */
    @TableField(value = "sys_sfc_icon")
    @Schema(description = "组件图标")
    @Size(max = 255, message = "组件图标最大长度需小于255", groups = {AddGroup.class})
    private String sysSfcIcon;

    /**
     * 组件分类，如: HOME
     */
    @TableField(value = "sys_sfc_category")
    @Schema(description = "组件分类，如: HOME")
    @Size(max = 255, message = "组件分类最大长度需小于255", groups = {AddGroup.class})
    private String sysSfcCategory;

    /**
     * 组件存放路径
     */
    @TableField(value = "sys_sfc_path")
    @Schema(description = "组件存放路径")
    @Size(max = 255, message = "组件存放路径最大长度需小于255")
    private String sysSfcPath;

    /**
     * 组件代理地址
     */
    @TableField(value = "sys_sfc_proxy")
    @Schema(description = "组件代理地址")
    @Size(max = 255, message = "组件代理地址最大长度需小于255")
    private String sysSfcProxy;

    /**
     * 组件类型: 0:文件 1:文本
     */
    @TableField(value = "sys_sfc_type")
    @Schema(description = "组件类型: 0:文件 1:文本")
    private Integer sysSfcType;

    /**
     * 组件版本号
     */
    @TableField(value = "sys_sfc_version")
    @Schema(description = "组件版本号")
    private String sysSfcVersion;

    /**
     * 组件内容
     */
    @TableField(value = "sys_sfc_content")
    @Schema(description = "组件内容")
    private String sysSfcContent;

    /**
     * 组件状态: 0:未启用 1:已启用
     */
    @TableField(value = "sys_sfc_status")
    @Schema(description = "组件状态: 0:未启用 1:已启用")
    private Integer sysSfcStatus;

    /**
     * 延迟时间(毫秒)
     */
    @TableField(value = "sys_sfc_delay")
    @Schema(description = "延迟时间(毫秒)")
    private Integer sysSfcDelay;

    /**
     * 超时时间(毫秒)
     */
    @TableField(value = "sys_sfc_timeout")
    @Schema(description = "超时时间(毫秒)")
    private Integer sysSfcTimeout;

    /**
     * 组件描述
     */
    @TableField(value = "sys_sfc_desc")
    @Schema(description = "组件描述")
    @Size(max = 255, message = "组件描述最大长度需小于255")
    private String sysSfcDesc;

    /**
     * 是否已安装(非数据库字段)
     */
    @TableField(exist = false)
    @Schema(description = "是否已安装(非数据库字段)")
    private Integer sysSfcInstall;
}
