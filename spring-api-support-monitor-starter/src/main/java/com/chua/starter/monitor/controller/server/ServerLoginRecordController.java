package com.chua.starter.monitor.controller.server;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.entity.MonitorSysGenServerLoginRecord;
import com.chua.starter.monitor.service.server.ServerLoginRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

/**
 * 服务器登录记录控制器
 *
 * @author CH
 */
@RestController
@RequestMapping("v1/gen/server/login")
@Tag(name = "服务器登录记录", description = "服务器登录记录管理API")
@Slf4j
@RequiredArgsConstructor
public class ServerLoginRecordController {

    private static final int DEFAULT_TOP_N = 10;

    private final ServerLoginRecordService serverLoginRecordService;

    /**
     * 获取最近登录记录 TopN
     *
     * @param serverId 服务器ID（可选，null表示所有服务器）
     * @param topN    获取记录数（默认10）
     * @return 登录记录列表
     */
    @GetMapping("topN")
    @Operation(summary = "获取TopN登录记录", description = "获取最近N条服务器登录记录")
    public ReturnResult<List<MonitorSysGenServerLoginRecord>> getTopNLoginRecords(
            @Parameter(description = "服务器ID，不传表示所有服务器")
            @RequestParam(required = false) Integer serverId,
            @Parameter(description = "获取记录数，默认10")
            @RequestParam(defaultValue = "10") int topN) {
        try {
            List<MonitorSysGenServerLoginRecord> records =
                    serverLoginRecordService.getTopNLoginRecords(serverId, topN > 0 ? topN : DEFAULT_TOP_N);
            return ReturnResult.ok(records);
        } catch (Exception e) {
            log.error("获取登录记录失败", e);
            return ReturnResult.ok(Collections.emptyList());
        }
    }
}
