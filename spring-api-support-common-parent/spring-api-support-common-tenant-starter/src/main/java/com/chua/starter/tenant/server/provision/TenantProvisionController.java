package com.chua.starter.tenant.server.provision;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.common.support.annotations.SysLog;
import com.chua.starter.tenant.entity.SysTenant;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 租户创建进度接口。
 *
 * <p>创建租户要建独立库、建表、建管理员，耗时以分钟计，前端不能干等一个同步响应。
 * 这里只负责「起任务 + 查快照 + 取消」，真正的步骤推进与套接字推送在
 * {@link TenantProvisionService}：接口立即返回 {@code taskId}，前端据此订阅套接字，
 * 断线时用 {@link #status(String)} 轮询兜底。</p>
 *
 * <p><b>能力接口为什么单独存在</b>：前端按 {@code capabilities} 决定是否渲染「取消创建」、
 * 是否订阅套接字。实际未开启自动建库时，取消按钮不会有任何效果，不如不显示。</p>
 *
 * @since 4.0.0.51
 * @author CH
 */
@Slf4j
@RestController
@Tag(name = "租户创建进度")
@RequestMapping("/v2/tenant/provision")
@RequiredArgsConstructor
public class TenantProvisionController {

    /**
     * 租户创建进度服务
     */
    private final TenantProvisionService tenantProvisionService;

    /**
     * 创建进度能力说明。
     *
     * @return 是否可取消、套接字协议 / 模块 / 主题、步骤定义
     */
    @GetMapping("/capabilities")
    @Operation(summary = "租户创建进度能力说明")
    public ReturnResult<Map<String, Object>> capabilities() {
        return ReturnResult.ok(tenantProvisionService.capabilities());
    }

    /**
     * 启动带进度的租户创建。
     *
     * @param sysTenant 租户信息
     * @return 任务快照，含 {@code taskId}
     */
    @PostMapping("/create")
    @SysLog(module = "创建")
    @Operation(summary = "启动租户创建")
    public ReturnResult<Map<String, Object>> create(@RequestBody SysTenant sysTenant) {
        return ReturnResult.ok(tenantProvisionService.start(sysTenant));
    }

    /**
     * 查询任务快照（套接字断线时的兜底）。
     *
     * @param taskId 任务标识
     * @return 任务快照
     */
    @GetMapping("/status")
    @Operation(summary = "查询租户创建进度")
    public ReturnResult<Map<String, Object>> status(@RequestParam String taskId) {
        return ReturnResult.ok(tenantProvisionService.statusOf(taskId));
    }

    /**
     * 取消任务（只在步骤边界生效）。
     *
     * @param taskId 任务标识
     * @return 是否已受理
     */
    @PutMapping("/cancel")
    @SysLog(module = "取消")
    @Operation(summary = "取消租户创建")
    public ReturnResult<Boolean> cancel(@RequestParam String taskId) {
        return ReturnResult.ok(tenantProvisionService.cancel(taskId));
    }
}
