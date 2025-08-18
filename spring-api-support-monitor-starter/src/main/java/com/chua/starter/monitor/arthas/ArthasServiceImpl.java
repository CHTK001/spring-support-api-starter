package com.chua.starter.monitor.arthas;

import com.chua.common.support.http.HttpClient;
import com.chua.common.support.http.HttpResponse;
import com.chua.common.support.json.Json;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.entity.MonitorArthasTunnelConfig;
import com.chua.starter.monitor.entity.NodeSystem;
import com.chua.starter.monitor.mapper.MonitorArthasTunnelConfigMapper;
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

    private final NodeSystemService nodeSystemService;
    private final MonitorArthasTunnelConfigMapper arthasTunnelConfigMapper;

    @Override
    public Map<String, Object> getNodeArthasConfig(String nodeId) {
        NodeSystem node = nodeSystemService.getById(nodeId);
        if (node == null) {
            throw new IllegalArgumentException("节点不存在: " + nodeId);
        }

        String baseUrl = String.format("http://%s:%s/arthas-config", node.getNodeSystemIpAddress(),
                node.getNodeSystemPort());
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
    public ArthasTunnelConfig getServerTunnelConfig(String serverId) {
        ArthasTunnelConfig cfg = new ArthasTunnelConfig();
        MonitorArthasTunnelConfig rec = arthasTunnelConfigMapper.selectByServerId(serverId);
        if (rec != null) {
            cfg.setArthasTunnelConfigAddress(rec.getMonitorArthasTunnelConfigAddress());
            cfg.setArthasTunnelConfigHttp(rec.getArthasTunnelConfigHttp());
            cfg.setArthasTunnelConfigUsername(rec.getMonitorArthasTunnelConfigUsername());
            cfg.setArthasTunnelConfigPassword(rec.getMonitorArthasTunnelConfigPassword());
        }
        return cfg;
    }

    @Override
    public boolean setServerTunnelConfig(String serverId, ArthasTunnelConfig config) {
        MonitorArthasTunnelConfig rec = arthasTunnelConfigMapper.selectByServerId(serverId);
        if (rec == null) {
            rec = new MonitorArthasTunnelConfig();
            rec.setMonitorArthasTunnelConfigServerId(serverId);
        }
        if (config != null) {
            rec.setMonitorArthasTunnelConfigAddress(config.getArthasTunnelConfigAddress());
            rec.setMonitorArthasTunnelConfigUsername(config.getArthasTunnelConfigUsername());
            rec.setArthasTunnelConfigHttp(config.getArthasTunnelConfigHttp());
            rec.setMonitorArthasTunnelConfigPassword(config.getArthasTunnelConfigPassword());
        }
        if (rec.getMonitorArthasTunnelConfigId() == null) {
            return arthasTunnelConfigMapper.insert(rec) > 0;
        } else {
            return arthasTunnelConfigMapper.updateById(rec) > 0;
        }
    }
}
