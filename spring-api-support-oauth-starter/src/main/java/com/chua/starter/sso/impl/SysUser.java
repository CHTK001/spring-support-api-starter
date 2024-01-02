package com.chua.starter.sso.impl;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.chua.common.support.datasource.annotation.Column;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 用户表
 *
 * @author CH
 * @since 2023/10/16
 */
@Data
public class SysUser {
    /**
     *
     */
    @TableId(type = IdType.AUTO)
    private Long userId;
    /**
     * 联系方式
     */
    private String userAddress;
    /**
     * 用户编号
     */
    private String userCode;
    /**
     * 用户编号
     */
    private String userMarker;

    /**
     * 用户名
     */
    @NotNull(message = "用户名不能为空")
    private String username;

    /**
     * 用户真实姓名
     */
    private String userRealName;

    /**
     * 昵称
     */
    private String userNickname;

    /**
     * 性别((1:男;2:女))
     */
    @Column(comment = "性别(1:男;2:女)")
    private Integer userGender;
    /**
     * 是否系统账号(1:是)
     */
    @Column(comment = " 是否系统账号(1:是)")
    private Integer userSys;

    /**
     * 密码
     */
    private String userPassword;

    /**
     * 注册IP
     */
    private String userRegisterIp;

    /**
     * 注册地址
     */
    private String userRegisterAddress;

    /**
     * 部门ID
     */
    @NotNull(message = "用户名部门不能为空")
    private Long deptId;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 联系方式
     */
    @NotNull(message = "联系方式不能为空")
    private String userMobile;

    /**
     * 用户状态((1:正常;0:禁用))
     */
    @Column(defaultValue = "1", comment = "用户状态(1:正常;0:禁用)")
    private Integer userStatus;

    /**
     * 用户邮箱
     */
    private String userEmail;
    /**
     * 逻辑删除标识(0:未删除;1:已删除)
     */
    @TableLogic(value = "0", delval = "1")
    @Column(defaultValue = "0", comment = "逻辑删除标识(0:未删除;1:已删除)")
    private Integer userDeleted;
    /**
     * 盐
     */
    private String userSalt;

}