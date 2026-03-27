package com.chua.starter.service.trace.storage;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.chua.common.support.core.annotation.Spi;
import com.chua.common.support.core.annotation.SpiDefault;
import com.chua.starter.common.support.configuration.SpringBeanUtils;
import com.chua.starter.entity.service.SysSetting;
import com.chua.starter.service.configuration.trace.RequestTraceConfigStorage;
import com.chua.starter.service.configuration.trace.RequestTraceStrategyConfig;
import com.chua.starter.service.configuration.trace.RequestTraceStrategyConfigCodec;
import com.chua.starter.service.configuration.trace.RequestTraceSupport;
import com.chua.starter.service.mapper.SysSettingMapper;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 数据库版请求追踪配置存储。
 *
 * @author CH
 * @since 2026/03/25
 */
@Spi("database")
@SpiDefault
public class DatabaseRequestTraceConfigStorage implements RequestTraceConfigStorage {

    @Override
    public String type() {
        return "database";
    }

    @Override
    public RequestTraceStrategyConfig load(String groupCode) {
        SysSettingMapper mapper = mapper();
        if (mapper == null) {
            return null;
        }
        List<SysSetting> settings = mapper.selectList(Wrappers.<SysSetting>lambdaQuery()
                .eq(SysSetting::getSysSettingGroup, groupCode)
                .orderByAsc(SysSetting::getSysSettingSort, SysSetting::getSysSettingId));
        if (settings == null || settings.isEmpty()) {
            return null;
        }
        Map<String, String> values = new LinkedHashMap<>();
        for (SysSetting setting : settings) {
            String name = RequestTraceSupport.normalize(setting.getSysSettingName());
            if (name == null) {
                continue;
            }
            values.put(name, setting.getSysSettingValue());
        }
        return RequestTraceStrategyConfigCodec.fromMap(values);
    }

    @Override
    public RequestTraceStrategyConfig save(String groupCode, RequestTraceStrategyConfig config) {
        RequestTraceStrategyConfig normalized = RequestTraceStrategyConfigCodec.normalize(config);
        SysSettingMapper mapper = mapper();
        if (mapper == null) {
            return normalized.copy();
        }

        Map<String, String> values = RequestTraceStrategyConfigCodec.toMap(normalized);
        List<SysSetting> existing = mapper.selectList(Wrappers.<SysSetting>lambdaQuery()
                .eq(SysSetting::getSysSettingGroup, groupCode));
        Map<String, SysSetting> existingByName = existing.stream()
                .filter(Objects::nonNull)
                .filter(item -> RequestTraceSupport.normalize(item.getSysSettingName()) != null)
                .collect(Collectors.toMap(SysSetting::getSysSettingName, Function.identity(), (left, right) -> right, LinkedHashMap::new));

        int sort = 1;
        for (Map.Entry<String, String> entry : values.entrySet()) {
            String name = entry.getKey();
            SysSetting setting = existingByName.remove(name);
            if (setting == null) {
                setting = new SysSetting();
                setting.setSysSettingGroup(groupCode);
                setting.setSysSettingName(name);
                setting.setSysSettingInSystem(1);
                setting.setSysSettingAdmin(1);
                setting.setSysSettingCollect(0);
                setting.setSysSettingAppInner(0);
            }
            setting.setSysSettingValue(entry.getValue());
            setting.setSysSettingSort(sort++);
            setting.setSysSettingStatus(1);
            setting.setSysSettingValueType(inferValueType(entry.getValue()));
            setting.setSysSettingConfig("request-trace-strategy");
            setting.setSysSettingRemark(describe(name));
            if (setting.getSysSettingId() == null) {
                mapper.insert(setting);
            } else {
                mapper.updateById(setting);
            }
        }

        for (SysSetting stale : existingByName.values()) {
            if (stale.getSysSettingId() != null) {
                mapper.deleteById(stale.getSysSettingId());
            }
        }
        return normalized.copy();
    }

    private SysSettingMapper mapper() {
        try {
            return SpringBeanUtils.getBean(SysSettingMapper.class);
        } catch (Exception ignored) {
            return null;
        }
    }

    private String inferValueType(String value) {
        String normalized = RequestTraceSupport.normalize(value);
        if (normalized == null) {
            return "TEXT";
        }
        if ("true".equalsIgnoreCase(normalized) || "false".equalsIgnoreCase(normalized)) {
            return "BOOL";
        }
        boolean numeric = normalized.chars().allMatch(ch -> ch == '-' || Character.isDigit(ch));
        return numeric ? "NUMBER" : "TEXT";
    }

    private String describe(String key) {
        return switch (key) {
            case "enabled" -> "请求追踪策略总开关";
            case "rawStorageType" -> "请求追踪原始记录存储类型";
            case "behaviorStorageType" -> "请求追踪行为结果存储类型";
            case "fileStoragePath" -> "文件存储根路径";
            case "aiSummaryEnabled" -> "是否启用AI摘要";
            case "aiModel" -> "AI模型标识";
            case "aiRateLimitEnabled" -> "是否启用AI分析限流";
            case "aiRateLimitWindowSeconds" -> "AI分析限流窗口(秒)";
            case "aiRateLimitMaxRequests" -> "AI分析窗口最大请求数";
            case "aiTimeoutMs" -> "AI分析超时时间(毫秒)";
            case "retentionMinutes" -> "内存窗口保留时长(分钟)";
            case "maxEvents" -> "内存最大事件数";
            default -> "请求追踪策略配置项: " + key;
        };
    }
}
