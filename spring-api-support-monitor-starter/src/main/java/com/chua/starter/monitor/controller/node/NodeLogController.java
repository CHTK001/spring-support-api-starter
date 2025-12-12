package com.chua.starter.monitor.controller.node;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.service.node.NodeLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 节点日志控制器
 * <p>
 * 提供节点日志订阅和查看功能的 REST API
 * </p>
 *
 * @author CH
 * @version 1.0.0
 * @since 2025/01/17
 */
@Slf4j
@RestController
@RequestMapping("/v1/node/log")
@RequiredArgsConstructor
@Tag(name = "节点日志", description = "节点日志订阅和查看功能")
public class NodeLogController {

    private final NodeLogService nodeLogService;

    /**
     * 订阅节点日志
     */
    @PostMapping("/subscribe")
    @Operation(summary = "订阅日志", description = "订阅节点的实时日志推送")
    public ReturnResult<Map<String, Object>> subscribeLogForNode(
            @Parameter(description = "节点IP地址") @RequestParam String ipAddress,
            @Parameter(description = "节点端口") @RequestParam Integer port,
            @Parameter(description = "日志级别") @RequestParam(defaultValue = "INFO") String level) {
        return nodeLogService.subscribeLogForNode(ipAddress, port, level);
    }

    /**
     * 取消订阅节点日志
     */
    @PostMapping("/unsubscribe")
    @Operation(summary = "取消订阅", description = "取消订阅节点的日志推送")
    public ReturnResult<Boolean> unsubscribeLogForNode(
            @Parameter(description = "节点IP地址") @RequestParam String ipAddress,
            @Parameter(description = "节点端口") @RequestParam Integer port) {
        return nodeLogService.unsubscribeLogForNode(ipAddress, port);
    }
}
