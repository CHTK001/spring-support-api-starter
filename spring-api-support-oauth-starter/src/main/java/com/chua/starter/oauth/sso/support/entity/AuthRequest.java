package com.chua.starter.oauth.sso.support.entity;

import lombok.Data;

/**
* 身份验证请求
*
* @since 2023/09/09
* @author CH
 */
@Data
public class AuthRequest {

    /**
    * 加密数据
     */
    private String data;

    /**
    * 用户名
     */
    private String username;

    /**
    * 认证类型
     */
    private String type;

    /**
    * 密码
     */
    private String passwd;

    /**
    * 验证码
     */
    private String code;

    /**
    * 回调地址
     */
    private String redirect_url;

    /**
    * 登录模式
     */
    private String mode;

    /**
    * 是否加密传输
     */
    private boolean encipher = true;

    /**
    * 是否记住登录
     */
    private String ifRemember;
}
