package com.chua.starter.monitor.arthas;

import com.chua.common.support.http.HttpClient;
import com.chua.common.support.http.HttpResponse;
import com.chua.common.support.json.Json;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.entity.MonitorSysGenServerSetting;
import com.chua.starter.monitor.entity.NodeSystem;
import com.chua.starter.monitor.service.MonitorSysGenServerSettingService;
import com.chua.starter.monitor.service.NodeSystemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Arthas 管理服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ArthasServiceImpl implements ArthasService {

    private static final String CUSTOM_TAGS_KEY = "arthasTunnelAddress";

    private final NodeSystemService nodeSystemService;
    private final MonitorSysGenServerSettingService serverSettingService;

    @Override
    public Map<String, Object> getNodeArthasConfig(String nodeId) {
        NodeSystem node = nodeSystemService.getById(nodeId);
        if (node == null) {
            throw new IllegalArgumentException("节点不存在: " + nodeId);
        }

        String baseUrl = String.format("http://%s:%s/arthas-config",
                node.getNodeSystemIpAddress(), node.getNodeSystemPort());
        HttpResponse response = HttpClient.get().url(baseUrl).newInvoker().execute();
        ReturnResult<String> rr = ReturnResult.of(response);
        if (!rr.isOk()) {
            throw new RuntimeException("请求节点 Arthas 配置失败: " + rr.getMsg());
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> data = Json.fromJson(rr.getData(), Map.class);
        if (data == null) {
            data = new HashMap<>();
        }
        return data;
    }

    @Override
    public String getServerTunnelAddress(Integer serverId) {
        ReturnResult<MonitorSysGenServerSetting> settingResult = serverSettingService.getOrCreateSetting(serverId);
        if (!settingResult.isOk() || settingResult.getData() == null) {
            throw new IllegalArgumentException("服务器配置不存在: " + serverId);
        }
        MonitorSysGenServerSetting setting = settingResult.getData();
        String tags = setting.getMonitorSysGenServerSettingCustomTags();
        if (tags == null || tags.isEmpty()) {
            return null;
        }
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = Json.fromJson(tags, Map.class);
            Object val = map.get(CUSTOM_TAGS_KEY);
            return val == null ? null : String.valueOf(val);
        } catch (Exception e) {
            log.warn("解析服务器自定义标签失败: serverId={}", serverId, e);
            return null;
        }
    }

    @Override
    public boolean setServerTunnelAddress(Integer serverId, String tunnelAddress) {
        ReturnResult<MonitorSysGenServerSetting> settingResult = serverSettingService.getOrCreateSetting(serverId);
        if (!settingResult.isOk() || settingResult.getData() == null) {
            throw new IllegalArgumentException("服务器配置不存在: " + serverId);
        }
        MonitorSysGenServerSetting setting = settingResult.getData();

        Map<String, Object> map = new HashMap<>();
        try {
            if (setting.getMonitorSysGenServerSettingCustomTags() != null && !setting.getMonitorSysGenServerSettingCustomTags().isEmpty()) {
                @SuppressWarnings("unchecked")
                Map<String, Object> exist = Json.fromJson(setting.getMonitorSysGenServerSettingCustomTags(), Map.class);
                if (exist != null) {
                    map.putAll(exist);
                }
            }
        } catch (Exception e) {
            log.warn("解析自定义标签失败，将覆盖写入: serverId={}", serverId, e);
        }
        map.put(CUSTOM_TAGS_KEY, tunnelAddress);
        setting.setMonitorSysGenServerSettingCustomTags(Json.toJson(map));

        return serverSettingService.updateById(setting);
    }
}


