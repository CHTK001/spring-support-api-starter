package com.chua.starter.sso.impl;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.chua.starter.mybatis.pojo.SysBase;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户三方信息表
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SysUserThird extends SysBase {
    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long userId;
    /**
     * 登录码
     */
    private String loginCodeType;
    /**
     * 登录账号
     */
    private String loginUsername;

    /**
     * 登录账号唯一编码
     */
    private String loginUuid;
    /**
     * 三方登录账号头像
     */
    private String loginAvatar;
    /**
     * 三方登录账号名称
     */
    private String loginName;

}