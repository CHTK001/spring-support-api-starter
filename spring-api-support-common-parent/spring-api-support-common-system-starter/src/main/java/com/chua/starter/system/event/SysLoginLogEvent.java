package com.chua.starter.system.event;

import com.chua.starter.oauth.client.support.provider.LoginData;
import com.chua.starter.oauth.client.support.user.LoginAuthResult;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 登录日志事件
 *
 * <p>认证侧（本地 static 认证 / oauth-client 同进程认证）在登录成功或失败时发布本事件，
 * 由 system 侧的登录日志监听器统一消费并落库，实现认证与日志存储解耦。</p>
 *
 * <p>本事件为进程内事件（Spring ApplicationEvent 默认在发布线程内联、同步执行），
 * 因此可携带当前 HttpServletRequest；它不用于跨进程（独立远程 oauth 服务）通信，
 * 跨进程需借助 webhook / 消息队列。</p>
 *
 * @since 4.0.0.42
 */
@Getter
public class SysLoginLogEvent extends ApplicationEvent {

    /** 登录入参（用户名、登录方式等） */
    private final LoginData loginData;
    /** 当前请求（同步事件内有效，用于取 IP、UA） */
    private final transient HttpServletRequest request;
    /** 浏览器指纹 */
    private final String fingerprint;
    /** 认证结果（成功时携带，null 表示未通过） */
    private final transient LoginAuthResult authResult;
    /** 失败原因（成功时为 null） */
    private final String message;
    /** 本次登录耗时（毫秒） */
    private final long cost;

    public SysLoginLogEvent(Object source, LoginData loginData, HttpServletRequest request,
                            String fingerprint, LoginAuthResult authResult, String message, long cost) {
        super(source);
        this.loginData = loginData;
        this.request = request;
        this.fingerprint = fingerprint;
        this.authResult = authResult;
        this.message = message;
        this.cost = cost;
    }
}
