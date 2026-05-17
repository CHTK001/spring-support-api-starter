package com.chua.starter.rustserver;

import jakarta.servlet.ServletContext;
import jakarta.servlet.SessionCookieConfig;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Session 管理器。
 *
 * <p>线程安全，支持定时清理过期 session（每 60 秒）。</p>
 */
public class SessionManager {

    private static final Logger log = LoggerFactory.getLogger(SessionManager.class);

    private final ConcurrentHashMap<String, SimpleHttpSession> sessions = new ConcurrentHashMap<>();
    private final ServletContext servletContext;
    private final SessionCookieConfig cookieConfig;
    private final ScheduledExecutorService cleanupExecutor;
    private volatile int defaultMaxInactiveInterval = 1800; // 30 分钟

    public SessionManager(ServletContext servletContext) {
        this.servletContext = servletContext;
        this.cookieConfig = new SimpleSessionCookieConfig();
        // 定期清理过期 session
        this.cleanupExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "rust-session-cleanup");
            t.setDaemon(true);
            return t;
        });
        this.cleanupExecutor.scheduleAtFixedRate(this::cleanupExpired, 60, 60, TimeUnit.SECONDS);
    }

    /**
     * 创建新 session。
     */
    public SimpleHttpSession createSession() {
        String id = UUID.randomUUID().toString().replace("-", "");
        SimpleHttpSession session = new SimpleHttpSession(id, servletContext);
        session.setMaxInactiveInterval(defaultMaxInactiveInterval);
        session.setNew(true);
        sessions.put(id, session);
        log.debug("[RustSession] Created session: {}", id);
        return session;
    }

    /**
     * 根据 ID 获取 session。
     */
    public SimpleHttpSession getSession(String id) {
        if (id == null) return null;
        SimpleHttpSession session = sessions.get(id);
        if (session == null) return null;
        if (!session.isValid()) {
            sessions.remove(id);
            log.debug("[RustSession] Removed expired session: {}", id);
            return null;
        }
        session.setLastAccessedTime(System.currentTimeMillis());
        return session;
    }

    /**
     * 销毁 session。
     */
    public void removeSession(String id) {
        SimpleHttpSession removed = sessions.remove(id);
        if (removed != null) {
            log.debug("[RustSession] Removed session: {}", id);
        }
    }

    /**
     * 清理过期 session。
     */
    public void cleanupExpired() {
        sessions.values().removeIf(s -> {
            if (!s.isValid()) {
                log.debug("[RustSession] Cleanup expired session: {}", s.getId());
                return true;
            }
            return false;
        });
    }

    /**
     * 设置默认 session 超时（秒）。
     */
    public void setDefaultMaxInactiveInterval(int interval) {
        this.defaultMaxInactiveInterval = interval;
    }

    public SessionCookieConfig getCookieConfig() {
        return cookieConfig;
    }

    /**
     * 关闭清理线程池。
     */
    public void shutdown() {
        cleanupExecutor.shutdown();
    }

    public int getActiveSessionCount() {
        return sessions.size();
    }
}
