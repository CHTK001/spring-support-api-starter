package com.chua.starter.oauth.sso.support.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.chua.starter.oauth.sso.support.entity.SysOAuthTraceLog;
import com.chua.starter.oauth.sso.support.mapper.SysOAuthTraceLogMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;

/**
 * OAuth追踪日志服务
 * 提供异步保存和敏感字段脱敏功能
 *
 * @author CH
 * @since 2025/01/01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OAuthTraceLogService {

    private final SysOAuthTraceLogMapper traceLogMapper;

    /**
     * 需要完全隐藏的字段名
     */
    private static final Set<String> FULL_MASK_FIELDS = Set.of(
            "password", "passwd", "pwd", "secret", "secretKey", "x-oauth-secret-key"
    );

    /**
     * 需要部分隐藏的字段名（保留首尾4字符）
     */
    private static final Set<String> PARTIAL_MASK_FIELDS = Set.of(
            "token", "accessKey", "x-oauth-access-key", "x-oauth-token", "x-oauth-cookie",
            "refreshToken", "x-oauth-refresh-token"
    );

    /**
     * 需要部分隐藏的字段名（保留首8字符）
     */
    private static final Set<String> SHORT_MASK_FIELDS = Set.of(
            "fingerprint", "x-oauth-fingerprint"
    );

    /**
     * 异步保存追踪日志
     *
     * @param traceLog 追踪日志
     */
    @Async
    public void saveAsync(SysOAuthTraceLog traceLog) {
        try {
            // 对请求参数进行脱敏处理
            if (traceLog.getSysRequestParams() != null) {
                traceLog.setSysRequestParams(maskSensitiveData(traceLog.getSysRequestParams()));
            }
            // 对AccessKey进行脱敏
            if (traceLog.getSysAccessKey() != null) {
                traceLog.setSysAccessKey(maskPartial(traceLog.getSysAccessKey()));
            }
            
            traceLogMapper.insert(traceLog);
            log.debug("[OAuth追踪]日志保存成功 - traceId: {}", traceLog.getSysTraceId());
        } catch (Exception e) {
            log.error("[OAuth追踪]日志保存失败 - traceId: {}, error: {}", 
                    traceLog.getSysTraceId(), e.getMessage());
        }
    }

    /**
     * 构建追踪日志对象
     *
     * @param traceId       追踪ID
     * @param operationType 操作类型
     * @param clientAppName 客户端应用名
     * @param clientIp      客户端IP
     * @param requestUri    请求URI
     * @return 追踪日志对象
     */
    public SysOAuthTraceLog buildTraceLog(String traceId, String operationType,
                                          String clientAppName, String clientIp,
                                          String requestUri) {
        SysOAuthTraceLog traceLog = new SysOAuthTraceLog();
        traceLog.setSysTraceId(traceId);
        traceLog.setSysOperationType(operationType);
        traceLog.setSysClientAppName(clientAppName);
        traceLog.setSysClientIp(clientIp);
        traceLog.setSysRequestUri(requestUri);
        traceLog.setCreateTime(new Date());
        return traceLog;
    }

    /**
     * 对JSON字符串中的敏感数据进行脱敏
     *
     * @param jsonStr JSON字符串
     * @return 脱敏后的JSON字符串
     */
    public String maskSensitiveData(String jsonStr) {
        if (jsonStr == null || jsonStr.isBlank()) {
            return jsonStr;
        }
        try {
            JSONObject json = JSON.parseObject(jsonStr);
            maskJsonObject(json);
            return json.toJSONString();
        } catch (Exception e) {
            // 如果不是JSON格式，返回原字符串
            return jsonStr;
        }
    }

    /**
     * 递归处理JSON对象中的敏感字段
     */
    private void maskJsonObject(JSONObject json) {
        if (json == null) {
            return;
        }
        for (String key : new HashSet<>(json.keySet())) {
            Object value = json.get(key);
            String lowerKey = key.toLowerCase();
            
            if (value instanceof String strValue) {
                // 完全隐藏的字段
                if (FULL_MASK_FIELDS.stream().anyMatch(f -> lowerKey.contains(f.toLowerCase()))) {
                    json.put(key, "******");
                }
                // 部分隐藏的字段（首尾4字符）
                else if (PARTIAL_MASK_FIELDS.stream().anyMatch(f -> lowerKey.contains(f.toLowerCase()))) {
                    json.put(key, maskPartial(strValue));
                }
                // 部分隐藏的字段（首8字符）
                else if (SHORT_MASK_FIELDS.stream().anyMatch(f -> lowerKey.contains(f.toLowerCase()))) {
                    json.put(key, maskShort(strValue));
                }
            } else if (value instanceof JSONObject nestedJson) {
                maskJsonObject(nestedJson);
            }
        }
    }

    /**
     * 部分脱敏 - 保留首尾4字符
     */
    private String maskPartial(String value) {
        if (value == null || value.length() <= 8) {
            return "****";
        }
        return value.substring(0, 4) + "****" + value.substring(value.length() - 4);
    }

    /**
     * 短脱敏 - 保留首8字符
     */
    private String maskShort(String value) {
        if (value == null || value.length() <= 8) {
            return value;
        }
        return value.substring(0, 8) + "****";
    }

    /**
     * 将Map转换为脱敏后的JSON字符串
     */
    public String mapToMaskedJson(Map<String, Object> params) {
        if (params == null || params.isEmpty()) {
            return null;
        }
        JSONObject json = new JSONObject(params);
        maskJsonObject(json);
        return json.toJSONString();
    }
}
