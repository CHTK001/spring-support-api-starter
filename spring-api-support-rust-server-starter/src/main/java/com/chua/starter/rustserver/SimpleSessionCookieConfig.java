package com.chua.starter.rustserver;

import jakarta.servlet.SessionCookieConfig;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * {@link SessionCookieConfig} 简易实现。
 */
public class SimpleSessionCookieConfig implements SessionCookieConfig {

    private String name = "JSESSIONID";
    private String domain;
    private String path = "/";
    private String comment;
    private boolean httpOnly = true;
    private boolean secure;
    private int maxAge = -1;
    private final Map<String, String> attributes = new HashMap<>();

    @Override
    public void setName(String name) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Session cookie name cannot be null or empty");
        }
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setDomain(String domain) {
        this.domain = domain;
    }

    @Override
    public String getDomain() {
        return domain;
    }

    @Override
    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public void setComment(String comment) {
        this.comment = comment;
    }

    @Override
    public String getComment() {
        return comment;
    }

    @Override
    public void setHttpOnly(boolean httpOnly) {
        this.httpOnly = httpOnly;
    }

    @Override
    public boolean isHttpOnly() {
        return httpOnly;
    }

    @Override
    public void setSecure(boolean secure) {
        this.secure = secure;
    }

    @Override
    public boolean isSecure() {
        return secure;
    }

    @Override
    public void setMaxAge(int maxAge) {
        this.maxAge = maxAge;
    }

    @Override
    public int getMaxAge() {
        return maxAge;
    }

    @Override
    public Map<String, String> getAttributes() {
        return Collections.unmodifiableMap(attributes);
    }

    @Override
    public void setAttribute(String name, String value) {
        attributes.put(name, value);
    }

    @Override
    public String getAttribute(String name) {
        return attributes.get(name);
    }
}
