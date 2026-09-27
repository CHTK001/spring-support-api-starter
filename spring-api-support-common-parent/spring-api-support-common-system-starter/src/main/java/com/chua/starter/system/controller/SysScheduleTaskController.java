package com.chua.starter.system.controller;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.common.support.annotations.Permission;
import com.chua.starter.oauth.client.support.contants.AuthConstant;
import com.chua.starter.system.task.DynamicScheduleRegistry;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 定时任务调度接口
 *
 * <p>为 Actuator 只读的 {@code scheduledtasks} 提供运行期可写能力：
 * 修改 cron、启停任务、立即执行一次。任务由 {@link DynamicScheduleRegistry} 统一托管。</p>
 *
 * @author CH
 * @since 4.0.0.51
 */
@Tag(name = "定时任务调度接口")
@RestController
@RequestMapping("/v2/schedule")
@RequiredArgsConstructor
@Slf4j
public class SysScheduleTaskController {

    /**
     * 动态调度注册中心
     */
    private final DynamicScheduleRegistry registry;

    /**
     * 任务列表
     *
     * @return 任务列表（含 cron、启停状态、下次/上次执行时间）
     */
    @Operation(summary = "定时任务列表")
    @GetMapping("/tasks")
    public ReturnResult<List<Map<String, Object>>> tasks() {
        return ReturnResult.ok(registry.list());
    }

    /**
     * 更新任务的 cron / 启停
     *
     * @param key 任务标识
     * @param req 更新请求
     * @return 更新结果
     */
    @Operation(summary = "更新定时任务 cron 或启停")
    @PutMapping("/tasks/{key}")
    @Permission(value = "sys:schedule:update", role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnResult<Boolean> update(@PathVariable String key, @RequestBody ScheduleUpdateRequest req) {
        if (!registry.contains(key)) {
            return ReturnResult.illegal("任务不存在: " + key);
        }
        if (null != req.getCron() && !registry.updateCron(key, req.getCron())) {
            return ReturnResult.illegal("cron 表达式非法");
        }
        if (null != req.getEnabled()) {
            registry.setEnabled(key, req.getEnabled());
        }
        return ReturnResult.ok(true);
    }

    /**
     * 立即执行一次任务
     *
     * @param key 任务标识
     * @return 执行结果
     */
    @Operation(summary = "立即执行一次定时任务")
    @PostMapping("/tasks/{key}/run")
    @Permission(value = "sys:schedule:update", role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnResult<Boolean> runOnce(@PathVariable String key) {
        if (!registry.contains(key)) {
            return ReturnResult.illegal("任务不存在: " + key);
        }
        registry.runOnce(key);
        return ReturnResult.ok(true);
    }

    /**
     * 定时任务更新请求
     */
    @Data
    public static class ScheduleUpdateRequest {
        /**
         * 新 cron 表达式（可选）
         */
        private String cron;
        /**
         * 是否启用（可选）
         */
        private Boolean enabled;
    }
}
