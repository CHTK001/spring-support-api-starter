package com.chua.starter.monitor.service.impl;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.utils.MapUtils;
import com.chua.starter.monitor.service.node.NodeMaintenanceService;
import com.chua.sync.support.SyncServer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * 节点维护服务实现
 * <p>
 * 通过 SyncServer 向目标节点发送维护指令
 * </p>
 *
 * @author CH
 * @version 1.0.0
 * @since 2025/01/17
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NodeMaintenanceServiceImpl implements NodeMaintenanceService {

    private final SyncServer syncServer;

    /**
     * 维护 Topic 常量
     */
    private static final String TOPIC_BACKUP = "monitor/maintenance/backup";
    private static final String TOPIC_UPGRADE = "monitor/maintenance/upgrade";
    private static final String TOPIC_RESTORE = "monitor/maintenance/restore";

    /**
     * 请求超时时间（秒）
     */
    private static final int REQUEST_TIMEOUT = 30;

    // ==================== 备份功能 ====================

    @Override
    public ReturnResult<Map<String, Object>> createBackupForNode(String ipAddress, Integer port, String description) {
        log.info("[NodeMaintenance] 创建备份: {}:{}, description={}", ipAddress, port, description);

        Map<String, Object> request = new LinkedHashMap<>();
        request.put("action", "create");
        request.put("description", description);

        return sendRequest(ipAddress, port, TOPIC_BACKUP, request);
    }

    @Override
    @SuppressWarnings("unchecked")
    public ReturnResult<List<Map<String, Object>>> listBackupsForNode(String ipAddress, Integer port) {
        log.debug("[NodeMaintenance] 获取备份列表: {}:{}", ipAddress, port);

        Map<String, Object> request = new LinkedHashMap<>();
        request.put("action", "list");

        ReturnResult<Map<String, Object>> result = sendRequest(ipAddress, port, TOPIC_BACKUP, request);
        if (!result.isOk()) {
            return ReturnResult.error(result.getMsg());
        }

        Object data = result.getData().get("data");
        if (data instanceof List) {
            return ReturnResult.ok((List<Map<String, Object>>) data);
        }
        return ReturnResult.ok(List.of());
    }

    @Override
    public ReturnResult<Boolean> deleteBackupForNode(String ipAddress, Integer port, String fileName) {
        log.info("[NodeMaintenance] 删除备份: {}:{}, fileName={}", ipAddress, port, fileName);

        Map<String, Object> request = new LinkedHashMap<>();
        request.put("action", "delete");
        request.put("fileName", fileName);

        ReturnResult<Map<String, Object>> result = sendRequest(ipAddress, port, TOPIC_BACKUP, request);
        return result.isOk() ? ReturnResult.ok(true) : ReturnResult.error(result.getMsg());
    }

    @Override
    @SuppressWarnings("unchecked")
    public ReturnResult<Map<String, Object>> getBackupContentForNode(String ipAddress, Integer port, String fileName) {
        log.debug("[NodeMaintenance] 获取备份内容: {}:{}, fileName={}", ipAddress, port, fileName);

        Map<String, Object> request = new LinkedHashMap<>();
        request.put("action", "download");
        request.put("fileName", fileName);

        ReturnResult<Map<String, Object>> result = sendRequest(ipAddress, port, TOPIC_BACKUP, request);
        if (!result.isOk()) {
            return result;
        }

        Object data = result.getData().get("data");
        if (data instanceof Map) {
            return ReturnResult.ok((Map<String, Object>) data);
        }
        return ReturnResult.error("备份内容格式错误");
    }

    // ==================== 升级功能 ====================

    @Override
    @SuppressWarnings("unchecked")
    public ReturnResult<List<Map<String, Object>>> listUpgradePackagesForNode(String ipAddress, Integer port) {
        log.debug("[NodeMaintenance] 获取升级包列表: {}:{}", ipAddress, port);

        Map<String, Object> request = new LinkedHashMap<>();
        request.put("action", "list");

        ReturnResult<Map<String, Object>> result = sendRequest(ipAddress, port, TOPIC_UPGRADE, request);
        if (!result.isOk()) {
            return ReturnResult.error(result.getMsg());
        }

        Object data = result.getData().get("data");
        if (data instanceof List) {
            return ReturnResult.ok((List<Map<String, Object>>) data);
        }
        return ReturnResult.ok(List.of());
    }

    @Override
    @SuppressWarnings("unchecked")
    public ReturnResult<Map<String, Object>> uploadUpgradePackageForNode(String ipAddress, Integer port,
                                                                          String fileName, String fileContent) {
        log.info("[NodeMaintenance] 上传升级包: {}:{}, fileName={}", ipAddress, port, fileName);

        Map<String, Object> request = new LinkedHashMap<>();
        request.put("action", "upload");
        request.put("fileName", fileName);
        request.put("fileContent", fileContent);

        ReturnResult<Map<String, Object>> result = sendRequest(ipAddress, port, TOPIC_UPGRADE, request);
        if (!result.isOk()) {
            return result;
        }

        Object data = result.getData().get("data");
        if (data instanceof Map) {
            return ReturnResult.ok((Map<String, Object>) data);
        }
        return ReturnResult.ok(Map.of());
    }

    @Override
    @SuppressWarnings("unchecked")
    public ReturnResult<Map<String, Object>> executeUpgradeForNode(String ipAddress, Integer port,
                                                                    String fileName, boolean autoRestart, 
                                                                    boolean autoBackup) {
        log.info("[NodeMaintenance] 执行升级: {}:{}, fileName={}, autoRestart={}, autoBackup={}", 
                ipAddress, port, fileName, autoRestart, autoBackup);

        Map<String, Object> request = new LinkedHashMap<>();
        request.put("action", "execute");
        request.put("fileName", fileName);
        request.put("autoRestart", autoRestart);
        request.put("autoBackup", autoBackup);

        ReturnResult<Map<String, Object>> result = sendRequest(ipAddress, port, TOPIC_UPGRADE, request);
        if (!result.isOk()) {
            return result;
        }

        Object data = result.getData().get("data");
        if (data instanceof Map) {
            return ReturnResult.ok((Map<String, Object>) data);
        }
        return ReturnResult.ok(Map.of());
    }

    @Override
    @SuppressWarnings("unchecked")
    public ReturnResult<Map<String, Object>> getUpgradeStatusForNode(String ipAddress, Integer port) {
        log.debug("[NodeMaintenance] 获取升级状态: {}:{}", ipAddress, port);

        Map<String, Object> request = new LinkedHashMap<>();
        request.put("action", "status");

        ReturnResult<Map<String, Object>> result = sendRequest(ipAddress, port, TOPIC_UPGRADE, request);
        if (!result.isOk()) {
            return result;
        }

        Object data = result.getData().get("data");
        if (data instanceof Map) {
            return ReturnResult.ok((Map<String, Object>) data);
        }
        return ReturnResult.ok(Map.of());
    }

    // ==================== 还原功能 ====================

    @Override
    @SuppressWarnings("unchecked")
    public ReturnResult<Map<String, Object>> previewRestoreForNode(String ipAddress, Integer port, String fileName) {
        log.debug("[NodeMaintenance] 预览还原: {}:{}, fileName={}", ipAddress, port, fileName);

        Map<String, Object> request = new LinkedHashMap<>();
        request.put("action", "preview");
        request.put("fileName", fileName);

        ReturnResult<Map<String, Object>> result = sendRequest(ipAddress, port, TOPIC_RESTORE, request);
        if (!result.isOk()) {
            return result;
        }

        Object data = result.getData().get("data");
        if (data instanceof Map) {
            return ReturnResult.ok((Map<String, Object>) data);
        }
        return ReturnResult.ok(Map.of());
    }

    @Override
    @SuppressWarnings("unchecked")
    public ReturnResult<Map<String, Object>> executeRestoreForNode(String ipAddress, Integer port, String fileName) {
        log.info("[NodeMaintenance] 执行还原: {}:{}, fileName={}", ipAddress, port, fileName);

        Map<String, Object> request = new LinkedHashMap<>();
        request.put("action", "execute");
        request.put("fileName", fileName);

        ReturnResult<Map<String, Object>> result = sendRequest(ipAddress, port, TOPIC_RESTORE, request);
        if (!result.isOk()) {
            return result;
        }

        Object data = result.getData().get("data");
        if (data instanceof Map) {
            return ReturnResult.ok((Map<String, Object>) data);
        }
        return ReturnResult.ok(Map.of());
    }

    // ==================== 辅助方法 ====================

    /**
     * 发送请求到目标节点
     *
     * @param ipAddress 节点IP
     * @param port      节点端口
     * @param topic     主题
     * @param request   请求数据
     * @return 响应结果
     */
    @SuppressWarnings("unchecked")
    private ReturnResult<Map<String, Object>> sendRequest(String ipAddress, Integer port, 
                                                           String topic, Map<String, Object> request) {
        try {
            String sessionId = buildSessionId(ipAddress, port);

            // 异步发送请求并等待响应
            CompletableFuture<Object> future = syncServer.sendRequest(sessionId, topic, request);
            Object response = future.get(REQUEST_TIMEOUT, TimeUnit.SECONDS);

            if (response instanceof Map) {
                Map<String, Object> responseMap = (Map<String, Object>) response;
                int code = MapUtils.getIntValue(responseMap, "code", 500);
                String message = MapUtils.getString(responseMap, "message", "未知错误");

                if (code == 200) {
                    return ReturnResult.ok(responseMap);
                } else {
                    return ReturnResult.error(message);
                }
            }

            return ReturnResult.error("响应格式错误");
        } catch (java.util.concurrent.TimeoutException e) {
            log.error("[NodeMaintenance] 请求超时: {}:{}, topic={}", ipAddress, port, topic);
            return ReturnResult.error("请求超时，节点可能离线或响应缓慢");
        } catch (Exception e) {
            log.error("[NodeMaintenance] 请求失败: {}:{}, topic={}", ipAddress, port, topic, e);
            return ReturnResult.error("请求失败: " + e.getMessage());
        }
    }

    /**
     * 构建会话ID
     */
    private String buildSessionId(String ipAddress, Integer port) {
        return ipAddress + ":" + port;
    }
}
