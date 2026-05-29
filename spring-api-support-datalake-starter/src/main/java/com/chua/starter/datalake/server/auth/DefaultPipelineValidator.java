package com.chua.starter.datalake.server.auth;

import com.chua.datalake.support.spi.auth.PipelineValidator;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * 默认管线校验器 — pipelineId 合法性校验 + IP 白名单
 * <p>
 * 白名单为空时表示所有 IP 均可访问；白名单非空时只允许白名单 IP 提交数据。
 * requirePipelineId=true 时，采集请求必须携带 pipelineId，否则拒绝。
 * </p>
 *
 * @author CH
 */
public class DefaultPipelineValidator implements PipelineValidator {

    private volatile boolean enabled = true;
    private volatile boolean requirePipelineId = true;
    private final Set<String> whitelist = ConcurrentHashMap.newKeySet();

    @Override
    public boolean validate(String pipelineId, String clientIp, String payload) {
        if (!enabled) return true;

        // pipelineId 必填检查
        if (requirePipelineId && (pipelineId == null || pipelineId.isBlank())) {
            return false;
        }

        // 白名单检查 (为空时不限制)
        if (!whitelist.isEmpty() && clientIp != null) {
            return whitelist.contains(clientIp) || whitelist.contains("*");
        }

        return true;
    }

    @Override
    public boolean isEnabled() { return enabled; }

    @Override
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    @Override
    public Set<String> getWhitelist() { return whitelist; }

    @Override
    public void addToWhitelist(String item) { whitelist.add(item); }

    @Override
    public void removeFromWhitelist(String item) { whitelist.remove(item); }

    @Override
    public boolean isRequirePipelineId() { return requirePipelineId; }

    @Override
    public void setRequirePipelineId(boolean require) { this.requirePipelineId = require; }
}