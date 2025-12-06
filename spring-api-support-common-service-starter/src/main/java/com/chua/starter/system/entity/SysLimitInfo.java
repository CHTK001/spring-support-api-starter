package com.chua.starter.system.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

import java.util.Date;

/**
 * 限流事件信息
 * 
 * @author CH
 * @since 2025/9/28
 */
@Getter
@Setter
public class SysLimitInfo extends ApplicationEvent {
    
    /**
     * 限流配置ID
     */
    private Long sysLimitConfigurationId;

    /**
     * 接口路径
     */
    private String sysLimitPath;

    /**
     * 限流规则名称
     */
    private String sysLimitName;

    /**
     * 限流维度（GLOBAL, IP, USER, API）
     */
    private String sysLimitDimension;

    /**
     * 限流键值（如IP地址、用户ID等）
     */
    private String sysLimitKey;

    /**
     * 限流时间
     */
    private Date sysLimitTime;

    /**
     * 用户ID
     */
    private Long sysUserId;

    /**
     * 用户名
     */
    private String sysUserName;

    /**
     * 客户端IP
     */
    private String clientIp;

    /**
     * 请求URL
     */
    private String requestUrl;

    /**
     * 请求方法
     */
    private String requestMethod;

    /**
     * 用户代理
     */
    private String userAgent;

    /**
     * 构造函数
     *
     * @param source 事件源对象
     */
    public SysLimitInfo(Object source) {
        super(source);
    }
}