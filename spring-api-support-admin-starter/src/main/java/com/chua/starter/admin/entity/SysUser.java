package com.chua.starter.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.chua.common.support.datasource.annotation.Column;
import com.chua.starter.common.support.serialize.JsonArrayToLongDeserialize;
import com.chua.starter.common.support.serialize.JsonArrayToStringDeserialize;
import com.chua.starter.mybatis.pojo.SysBase;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;

/**
 * 用户表
 */
@EqualsAndHashCode(callSuper = true)
@Data
@JsonIgnoreProperties(value = {"userPassword", "userSalt"}, allowSetters = true)
public class SysUser extends SysBase {
    /**
     *
     */
    @TableId(type = IdType.AUTO)
    @Schema(description = "主键")
    private Long userId;

    /**
     * 用户编号
     */
    @Schema(description = "用户编号")
    private String userCode;

    /**
     * 用户名
     */
    @Schema(description = "用户名")
    @NotNull(message = "用户名不能为空")
    private String username;

    /**
     * 用户真实姓名
     */
    @Schema(description = "用户真实姓名")
    private String userRealName;

    /**
     * 用户备注
     */
    @Schema(description = "用户备注")
    private String userMarker;

    /**
     * 昵称
     */
    @Schema(description = "昵称")
    private String userNickname;

    /**
     * 性别((1:男;2:女))
     */
    @Schema(description = "性别(1:男;2:女)")
    @Column(comment = "性别(1:男;2:女)")
    private Integer userGender;
    /**
     * 是否系统账号(1:是)
     */
    @Schema(description = " 是否系统账号(1:是)")
    @Column(comment = " 是否系统账号(1:是)")
    private Integer userSys;

    /**
     * 密码
     */
    @Schema(description = "密码")
    @NotNull(message = "密码不能为空")
    private String userPassword;

    /**
     * 注册IP
     */
    @Schema(description = "注册IP")
    private String userRegisterIp;

    /**
     * 注册地址
     */
    @Schema(description = "注册地址")
    private String userRegisterAddress;

    /**
     * 部门ID
     */
    @Schema(description = "部门ID")
    @NotNull(message = "用户名部门不能为空")
    @JsonDeserialize(using = JsonArrayToLongDeserialize.class)
    private Long deptId;

    /**
     * 用户头像
     */
    @Schema(description = "用户头像")
    private String userAvatar;

    /**
     * 联系方式
     */
    @Schema(description = "联系方式")
    @NotNull(message = "联系方式不能为空")
    private String userMobile;
    /**
     * 联系方式
     */
    @Schema(description = "住址")
    private String userAddress;

    /**
     * 用户状态((1:正常;0:禁用))
     */
    @Schema(description = "用户状态(1:正常;0:禁用)")
    @Column(defaultValue = "1", comment = "用户状态(1:正常;0:禁用)")
    private Integer userStatus;

    /**
     * 用户邮箱
     */
    @Schema(description = "用户邮箱")
    private String userEmail;
    /**
     * 逻辑删除标识(0:未删除;1:已删除)
     */
    @TableLogic(value = "0", delval = "1")
    @Schema(description = "逻辑删除标识(0:未删除;1:已删除)")
    @Column(defaultValue = "0", comment = "逻辑删除标识(0:未删除;1:已删除)")
    private Integer userDeleted;
    /**
     * 盐
     */
    @Schema(description = "盐")
    private String userSalt;
    /**
     * 密钥
     */
    @TableField(exist = false)
    private String userSeRan;
    /**
     * 角色ids
     */
    @TableField(exist = false)
    @Schema(description = "角色ids")
    @NotNull(message = "用户角色不能为空")
    @JsonDeserialize(using = JsonArrayToStringDeserialize.class)
    private String roleId;
    /**
     * 角色名称
     */
    @TableField(exist = false)
    @Schema(description = "角色名称")
    private String roleNames;
    /**
     * 部门名称
     */
    @TableField(exist = false)
    @Schema(description = "部门名称")
    private String deptName;

}