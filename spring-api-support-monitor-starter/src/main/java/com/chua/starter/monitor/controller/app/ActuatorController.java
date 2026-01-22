package com.chua.starter.monitor.controller.app;

import com.chua.common.support.network.http.HttpClient;
import com.chua.common.support.network.http.HttpMethod;
import com.chua.common.support.network.http.HttpResponse;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.core.utils.StringUtils;
import com.chua.starter.common.support.actuator.ActuatorAuthenticationFilter;
import com.chua.starter.monitor.pojo.app.ActuatorRequest;
import com.chua.starter.monitor.pojo.node.LogLevelRequest;
import com.chua.sync.support.pojo.ClientInfo;
import com.chua.sync.support.server.SyncServer;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * actuator接口
 *
 * @author CH
 * @version 1.0.0
 * @since 2024/9/13
 */
@Slf4j
@RestController
@RequestMapping("v1/actuator")
@Tag(name = "actuator接口")
@RequiredArgsConstructor
public class ActuatorController {

    /**
     * 日志级别设置主题
     */
    private static final String TOPIC_LOG_LEVEL = "monitor/control/log-level";

    @Autowired(required = false)
    private SyncServer syncServer;

    /**
     * 获取actuator信息
     *
     * @param request 请求
     * @return ReturnResult
     * @author CH
     * @since 1.0.0
     */
    @Operation(summary = "获取actuator信息")
    @PostMapping("call")
    public ReturnResult<String> post(@RequestBody ActuatorRequest request) {
        HttpResponse response = HttpClient.newHttpMethod(HttpMethod.valueOf(request.getMethod()))
                .body(request.getBody())
                .header("Authorization", ActuatorAuthenticationFilter.getKey(
                        StringUtils.defaultString(request.getUsername(), "actuator"),
                        StringUtils.defaultString(request.getUsername(), "actuator")))
                .url(request.getUrl())
                .newInvoker().execute();
        return ReturnResult.of(response);
    }

    /**
     * 设置节点日志级别
     * <p>
     * 通过 SyncServer 下发日志级别设置命令到指定客户端
     * </p>
     *
     * @param request 日志级别设置请求
     * @return 设置结果
     * @author CH
     * @since 1.0.0
     */
    @Operation(summary = "设置节点日志级别")
    @PostMapping("log-level")
    public ReturnResult<Boolean> setLogLevel(@Validated @RequestBody LogLevelRequest request) {
        // 校验 SyncServer 是否可用
        if (syncServer == null) {
            log.warn("[Actuator] SyncServer 未初始化");
            return ReturnResult.error("SyncServer 未初始化，无法下发命令");
        }

        // 查找客户端ID
        String clientId = findClientId(request);
        if (clientId == null) {
            log.warn("[Actuator] 未找到目标客户端: ipAddress={}, port={}, clientId={}",
                    request.getIpAddress(), request.getPort(), request.getClientId());
            return ReturnResult.error("节点不在线或未注册");
        }

        // 构建日志级别设置命令
        Map<String, Object> command = new HashMap<>();
        command.put("action", "SET_LOG_LEVEL");
        command.put("loggerName", request.getLoggerName());
        command.put("level", request.getLogLevel());
        command.put("timestamp", System.currentTimeMillis());

        // 通过 SyncServer 下发命令
        syncServer.send(clientId, TOPIC_LOG_LEVEL, command);

        log.info("[Actuator] 日志级别设置命令已下发: clientId={}, loggerName={}, level={}",
                clientId, request.getLoggerName(), request.getLogLevel());

        return ReturnResult.success(true);
    }

    /**
     * 查找客户端ID
     * <p>
     * 优先使用请求中的 clientId，否则通过 ipAddress + port 查找
     * </p>
     *
     * @param request 请求对象
     * @return 客户端ID，未找到返回 null
     * @author CH
     * @since 1.0.0
     */
    private String findClientId(LogLevelRequest request) {
        // 优先使用请求中的 clientId
        if (StringUtils.isNotBlank(request.getClientId())) {
            // 验证 clientId 是否有效
            Map<String, ClientInfo> allClients = syncServer.getAllClients();
            if (allClients.containsKey(request.getClientId())) {
                return request.getClientId();
            }
            log.debug("[Actuator] 指定的 clientId 无效: {}", request.getClientId());
        }

        // 通过 ipAddress + port 查找
        if (StringUtils.isNotBlank(request.getIpAddress()) && request.getPort() != null) {
            return syncServer.findClientId(request.getIpAddress(), request.getPort(), null);
        }

        return null;
    }
}
