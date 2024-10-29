package com.chua.starter.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chua.common.support.validator.group.AddGroup;
import com.chua.common.support.validator.group.UpdateGroup;
import com.chua.common.support.validator.group.UploadGroup;
import com.chua.starter.mybatis.pojo.SysBase;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 *
 * @since 2024/10/24
 * @author CH    
 */

/**
 * 组件库
 */
@ApiModel(description = "组件库")
@Schema(description = "组件库")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_sfc")
public class SysSfc extends SysBase implements Serializable {
    private static final long serialVersionUID = 1L;
    @TableId(value = "sys_sfc_id", type = IdType.AUTO)
    @ApiModelProperty(value = "")
    @Schema(description = "")
    @NotNull(message = "不能为null", groups = {UpdateGroup.class, UploadGroup.class})
    private Integer sysSfcId;

    /**
     * 组件名称
     */
    @TableField(value = "sys_sfc_name")
    @ApiModelProperty(value = "组件名称")
    @Schema(description = "组件名称")
    @Size(max = 255, message = "组件名称最大长度要小于 255", groups = {AddGroup.class})
    private String sysSfcName;

    /**
     * 组件中文名称
     */
    @TableField(value = "sys_sfc_chinese_name")
    @ApiModelProperty(value = "组件中文名称")
    @Schema(description = "组件中文名称")
    @Size(max = 255, message = "组件中文名称最大长度要小于 255", groups = {AddGroup.class})
    private String sysSfcChineseName;


    /**
     * 缓存模式
     */
    @TableField(value = "sys_sfc_model_cache")
    @ApiModelProperty(value = "缓存模式")
    @Schema(description = "缓存模式")
    private String sysSfcModelCache;
    /**
     * 组件功能, 多个逗号分隔
     */
    @TableField(value = "sys_sfc_function")
    @ApiModelProperty(value = "组件功能, 多个逗号分隔")
    @Schema(description = "组件功能, 多个逗号分隔")
    @Size(max = 255, message = "组件功能, 多个逗号分隔最大长度要小于 255",  groups = {AddGroup.class})
    private String sysSfcFunction;

    /**
     * 组件图标
     */
    @TableField(value = "sys_sfc_icon")
    @ApiModelProperty(value = "组件图标")
    @Schema(description = "组件图标")
    @Size(max = 255, message = "组件图标最大长度要小于 255",  groups = {AddGroup.class})
    private String sysSfcIcon;

    /**
     * 分类；HOME
     */
    @TableField(value = "sys_sfc_category")
    @ApiModelProperty(value = "分类；HOME")
    @Schema(description = "分类；HOME")
    @Size(max = 255, message = "分类；HOME最大长度要小于 255",  groups = {AddGroup.class})
    private String sysSfcCategory;

    /**
     * 组件存放路径
     */
    @TableField(value = "sys_sfc_path")
    @ApiModelProperty(value = "组件存放路径")
    @Schema(description = "组件存放路径")
    @Size(max = 255, message = "组件存放路径最大长度要小于 255")
    private String sysSfcPath;
    /**
     * 组件代理地址
     */
    @TableField(value = "sys_sfc_proxy")
    @ApiModelProperty(value = "组件代理地址")
    @Schema(description = "组件代理地址")
    @Size(max = 255, message = "组件代理地址最大长度要小于 255")
    private String sysSfcProxy;


    /**
     * 组件类型; 0:文件; 1:文本
     */
    @TableField(value = "sys_sfc_type")
    @ApiModelProperty(value = "组件类型; 0:文件; 1:文本")
    @Schema(description = "组件类型; 0:文件; 1:文本")
    private Integer sysSfcType;

    /**
     * 组件版本
     */
    @TableField(value = "sys_sfc_version")
    @ApiModelProperty(value = "组件版本")
    @Schema(description = "组件版本")
    private String sysSfcVersion;

    /**
     * 组件内容
     */
    @TableField(value = "sys_sfc_content")
    @ApiModelProperty(value = "组件内容")
    @Schema(description = "组件内容")
    private String sysSfcContent;
    /**
     * 组件状态; 0:未启用
     */
    @TableField(value = "sys_sfc_status")
    @ApiModelProperty(value = "组件状态; 0:未启用")
    @Schema(description = "组件状态; 0:未启用")
    private Integer sysSfcStatus;

    /**
     * 组件描述
     */
    @TableField(value = "sys_sfc_desc")
    @ApiModelProperty(value = "组件描述")
    @Schema(description = "组件描述")
    @Size(max = 255, message = "组件描述最大长度要小于 255")
    private String sysSfcDesc;
    /**
     * 是否已安装
     */
    @TableField(exist = false)
    @ApiModelProperty(value = "是否已安装")
    @Schema(description = "是否已安装")
    private Integer sysSfcInstall;
}