package com.chua.starter.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 三方登录
 */
@Data
@TableName(value = "sys_user_third")
public class SysUserThird {
    /**
     * 主键
     */
    @TableId(value = "user_id", type = IdType.AUTO)
    private Long userId;

    /**
     * 登录码; gitee等
     */
    @TableField(value = "login_code_type")
    private String loginCodeType;

    /**
     * 登录账号
     */
    @TableField(value = "login_username")
    private String loginUsername;

    /**
     * 登录账号唯一编码
     */
    @TableField(value = "login_uuid")
    private String loginUuid;

    /**
     * 三方登录账号头像
     */
    @TableField(value = "login_avatar")
    private String loginAvatar;

    /**
     * 三方登录账号名称
     */
    @TableField(value = "login_name")
    private String loginName;

    /**
     * 创建人
     */
    @TableField(value = "create_name")
    private String createName;

    /**
     * 创建人
     */
    @TableField(value = "create_by")
    private String createBy;

    /**
     * 创建时间
     */
    @TableField(value = "create_time")
    private Date createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time")
    private Date updateTime;

    /**
     * 更新人
     */
    @TableField(value = "update_name")
    private String updateName;

    /**
     * 更新人
     */
    @TableField(value = "update_by")
    private String updateBy;
}