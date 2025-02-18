package com.chua.starter.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.chua.common.support.validator.group.AddGroup;
import com.chua.common.support.validator.group.UpdateGroup;
import com.chua.starter.mybatis.pojo.SysBase;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 系统服务
 */
@EqualsAndHashCode(callSuper = true)
@ApiModel(description = "系统服务")
@Schema(description = "系统服务")
@Data
@TableName(value = "sys_service")
public class SysService extends SysBase {
    @TableId(value = "sys_service_id", type = IdType.AUTO)
    @ApiModelProperty(value = "")
    @Schema(description = "")
    @NotNull(message = "服务ID不能为空", groups = {UpdateGroup.class})
    private Integer sysServiceId;

    /**
     * 服务名称
     */
    @TableField(value = "sys_service_name")
    @ApiModelProperty(value = "服务名称")
    @Schema(description = "服务名称")
    @NotBlank(message = "服务名称不能为空", groups = {AddGroup.class})
    private String sysServiceName;

    /**
     * 服务唯一编码
     */
    @TableField(value = "sys_service_code")
    @ApiModelProperty(value = "服务唯一编码")
    @Schema(description = "服务唯一编码")
    @NotBlank(message = "服务唯一编码不能为空", groups = {AddGroup.class})
    private String sysServiceCode;

    /**
     * 是否删除; 0:正常
     */
    @TableField(value = "sys_service_delete")
    @ApiModelProperty(value = "是否删除; 0:正常")
    @Schema(description = "是否删除; 0:正常")
    @TableLogic(value = "0", delval = "1")
    private Integer sysServiceDelete;

    /**
     * 是否禁用; 0:正常
     */
    @TableField(value = "sys_service_status")
    @ApiModelProperty(value = "是否禁用; 0:正常")
    @Schema(description = "是否禁用; 0:正常")
    private Integer sysServiceStatus;

    /**
     * 优先级
     */
    @TableField(value = "sys_service_sort")
    @ApiModelProperty(value = "优先级")
    @Schema(description = "优先级")
    private Integer sysServiceSort;

    /**
     * 到期时间
     */
    @TableField(value = "sys_service_valid_time")
    @ApiModelProperty(value = "到期时间")
    @Schema(description = "到期时间")
    @NotNull(message = "到期时间不能为空", groups = {AddGroup.class})
    private LocalDateTime sysServiceValidTime;

    /**
     * 版本号
     */
    @TableField(value = "sys_service_version")
    @ApiModelProperty(value = "版本号")
    @Schema(description = "版本号")
    private String sysServiceVersion;

    /**
     * 适用环境
     */
    @TableField(value = "sys_service_profile")
    @ApiModelProperty(value = "适用环境")
    @Schema(description = "适用环境")
    private String sysServiceProfile;

    /**
     * 服务角色权限
     */
    @TableField(value = "sys_service_role")
    @ApiModelProperty(value = "服务角色权限")
    @Schema(description = "服务角色权限")
    private String sysServiceRole;

    /**
     * 服务类型; 字段表8
     */
    @TableField(value = "sys_service_category")
    @ApiModelProperty(value = "服务类型; 字段表8")
    @Schema(description = "服务类型; 字段表8")
    @NotNull(message = "服务类型不能为空", groups = {AddGroup.class})
    private Integer sysServiceCategory;

    /**
     * 服务描述
     */
    @TableField(value = "sys_service_remark")
    @ApiModelProperty(value = "服务描述")
    @Schema(description = "服务描述")
    private String sysServiceRemark;

}