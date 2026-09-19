package com.chua.starter.entity.system;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chua.common.support.base.validator.group.AddGroup;
import com.chua.common.support.base.validator.group.UpdateGroup;
import com.chua.starter.mybatis.pojo.SysBase;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * IP黑白名单表
 *
 * @since 4.0.0.43
 * @author CH
 */
@Schema(description = "IP黑白名单表")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_ip_blackwhite")
public class SysIpBlackwhite extends SysBase implements Serializable {
    /** 串行版本UID */
    private static final long serialVersionUID = 1L;

    /**
     * 主键标识
     */
    @TableId(value = "sys_ip_id", type = IdType.AUTO)
    @Schema(description = "主键ID")
    @NotNull(message = "主键ID不能为空", groups = {UpdateGroup.class})
    /** sysipid */
    private Integer sysIpId;

    /**
     * 类型: 0白名单 1黑名单
     */
    @TableField(value = "sys_ip_type")
    @Schema(description = "类型: 0白名单 1黑名单")
    @NotNull(message = "类型不能为空", groups = {AddGroup.class})
    /** sysip类型 */
    private Integer sysIpType;

    /**
     * IP地址或CIDR段
     */
    @TableField(value = "sys_ip_ip")
    @Schema(description = "IP地址或CIDR段")
    @Size(max = 255, message = "IP地址最大长度需小于255")
    @NotBlank(message = "IP地址不能为空", groups = {AddGroup.class})
    /** sysip */
    private String sysIpIp;

    /**
     * 状态: 0启用 1禁用
     */
    @TableField(value = "sys_ip_status")
    @Schema(description = "状态: 0启用 1禁用")
    /** sysip状态 */
    private Integer sysIpStatus;

    /**
     * 备注
     */
    @TableField(value = "sys_ip_remark")
    @Schema(description = "备注")
    @Size(max = 255, message = "备注最大长度需小于255")
    /** sysipremark */
    private String sysIpRemark;
}
