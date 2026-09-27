package com.chua.starter.system.listener;

import com.chua.starter.system.event.SysLoginLogEvent;
import com.chua.starter.system.service.impl.SysLoginServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 登录日志事件监听器
 *
 * <p>消费 {@link SysLoginLogEvent}，统一调用登录服务完成登录日志落库，
 * 使认证处理器无需直接依赖日志写入细节。</p>
 *
 * <p>采用同步监听（默认）：在发布线程、当前请求仍有效时执行，
 * 因此可安全读取事件携带的 HttpServletRequest。</p>
 *
 * @since 4.0.0.42
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SysLoginLogListener {

    private final SysLoginServiceImpl sysLoginService;

    /**
     * 处理登录日志事件
     *
     * @param event 登录日志事件
     */
    @EventListener
    public void onLoginLog(SysLoginLogEvent event) {
        try {
            sysLoginService.recordLoginLog(event.getLoginData(), event.getRequest(),
                    event.getFingerprint(), event.getAuthResult(), event.getMessage(), event.getCost());
        } catch (Exception e) {
            String username = null != event.getLoginData() ? event.getLoginData().getUsername() : null;
            log.warn("登录日志事件处理失败: user={}, error={}", username, e.getMessage());
        }
    }
}
