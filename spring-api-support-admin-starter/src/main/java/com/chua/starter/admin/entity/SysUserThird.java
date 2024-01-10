package com.chua.starter.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chua.starter.mybatis.pojo.SysTenantBase;
import lombok.Data;

/**
 * 三方登录
 */
@Data
@TableName(value = "sys_user_third")
public class SysUserThird extends SysTenantBase {
    /**
     * 主键
     */
    @TableId(value = "login_third_id", type = IdType.AUTO)
    private Long loginThirdId;

    /**
     * user_id
     */
    @TableField(value = "user_id")
    private Long userId;

    /**
     * 登录码; gitee等
     */
    @TableField(value = "login_third_code_type")
    private String loginThirdCodeType;

    /**
     * 登录账号
     */
    @TableField(value = "login_third_username")
    private String loginThirdUsername;

    /**
     * 登录账号唯一编码
     */
    @TableField(value = "login_third_uuid")
    private String loginThirdUuid;

    /**
     * 三方登录账号头像
     */
    @TableField(value = "login_third_avatar")
    private String loginThirdAvatar;

    /**
     * 三方登录账号名称
     */
    @TableField(value = "login_third_name")
    private String loginThirdName;

    /**
     * 三方登录账号方式
     */
    @TableField(value = "login_third_type")
    private String loginThirdType;

}