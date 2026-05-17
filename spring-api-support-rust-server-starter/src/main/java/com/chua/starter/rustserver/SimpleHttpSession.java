package com.chua.starter.rustserver;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpSessionBindingEvent;
import jakarta.servlet.http.HttpSessionBindingListener;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * {@link HttpSession} 简易实现。
 */
public class SimpleHttpSession implements HttpSession {

    private final String id;
    private final long creationTime;
    private final Map<String, Object> attributes = new ConcurrentHashMap<>();
    private volatile long lastAccessedTime;
    private volatile int maxInactiveInterval = 1800; // 默认 30 分钟
    private volatile boolean invalidated = false;
    private volatile boolean isNew = true;
    private final ServletContext servletContext;

    public SimpleHttpSession(String id, ServletContext servletContext) {
        this.id = id;
        this.creationTime = System.currentTimeMillis();
        this.lastAccessedTime = this.creationTime;
        this.servletContext = servletContext;
    }

    @Override
    public long getCreationTime() {
        checkValid();
        return creationTime;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public long getLastAccessedTime() {
        checkValid();
        return lastAccessedTime;
    }

    public void setLastAccessedTime(long time) {
        this.lastAccessedTime = time;
    }

    @Override
    public ServletContext getServletContext() {
        return servletContext;
    }

    @Override
    public void setMaxInactiveInterval(int interval) {
        this.maxInactiveInterval = interval;
    }

    @Override
    public int getMaxInactiveInterval() {
        return maxInactiveInterval;
    }

    @Override
    public Object getAttribute(String name) {
        checkValid();
        return attributes.get(name);
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        checkValid();
        return Collections.enumeration(attributes.keySet());
    }

    @Override
    public void setAttribute(String name, Object value) {
        checkValid();
        if (value == null) {
            removeAttribute(name);
            return;
        }
        Object old = attributes.put(name, value);
        fireBindingListeners(name, value, old);
    }

    @Override
    public void removeAttribute(String name) {
        checkValid();
        Object old = attributes.remove(name);
        if (old instanceof HttpSessionBindingListener) {
            ((HttpSessionBindingListener) old).valueUnbound(
                    new HttpSessionBindingEvent(this, name, old));
        }
    }

    @Override
    public void invalidate() {
        checkValid();
        this.invalidated = true;
        attributes.clear();
    }

    @Override
    public boolean isNew() {
        checkValid();
        return isNew;
    }

    public void setNew(boolean isNew) {
        this.isNew = isNew;
    }

    public boolean isValid() {
        if (invalidated) return false;
        if (maxInactiveInterval > 0) {
            long elapsed = System.currentTimeMillis() - lastAccessedTime;
            if (elapsed > maxInactiveInterval * 1000L) {
                invalidated = true;
                attributes.clear();
                return false;
            }
        }
        return true;
    }

    private void checkValid() {
        if (invalidated) {
            throw new IllegalStateException("Session '" + id + "' already invalidated");
        }
    }

    private void fireBindingListeners(String name, Object newValue, Object oldValue) {
        if (oldValue instanceof HttpSessionBindingListener) {
            ((HttpSessionBindingListener) oldValue).valueUnbound(
                    new HttpSessionBindingEvent(this, name, oldValue));
        }
        if (newValue instanceof HttpSessionBindingListener) {
            ((HttpSessionBindingListener) newValue).valueBound(
                    new HttpSessionBindingEvent(this, name, newValue));
        }
    }
}
