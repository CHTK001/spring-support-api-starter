package com.chua.starter.tenant.server.controller;
import com.chua.starter.common.support.annotations.SysLog;

import com.chua.starter.common.support.lang.ReturnPageResult;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.base.validator.group.AddGroup;
import com.chua.common.support.base.validator.group.UpdateGroup;
import com.chua.starter.common.support.annotations.Permission;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.mybatis.utils.PageResultUtils;
import com.chua.starter.tenant.entity.SysTenant;
import com.chua.starter.tenant.entity.SysTenantStats;
import com.chua.starter.tenant.server.pojo.SysTenantBindServiceV1Request;
import com.chua.starter.tenant.server.service.SysTenantService;
import com.chua.starter.tenant.server.support.TenantDataSourceResolver;
import com.chua.starter.tenant.server.support.TenantDatasourceOption;
import com.chua.starter.tenant.server.support.TenantDatasourceView;
import com.chua.starter.tenant.server.support.TenantEndpoint;
import com.chua.starter.tenant.server.support.TenantEndpointResolver;
import com.chua.starter.tenant.server.support.TenantServiceExpiryJob;
import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 租户管理接口
 *
 * @since 2024/12/06
 * @author CH
 */
@RestController
@Slf4j
@Api(tags = "租户管理接口")
@Tag(name = "租户管理接口")
@RequestMapping("/v2/tenant")
@RequiredArgsConstructor
@SuppressWarnings("ALL")
public class SysTenantController {

    /**
     * systenant服务
    */
    private final SysTenantService sysTenantService;

    /**
     * 租户服务器地址解析器
     */
    private final TenantEndpointResolver tenantEndpointResolver;

    /**
     * 到期服务清理器
     */
    private final TenantServiceExpiryJob tenantServiceExpiryJob;

    /**
     * 租户库连接信息解析器（下拉项与只读回显）
     */
    private final TenantDataSourceResolver tenantDataSourceResolver;

    /**
     * 查询租户列表
     *
     * @param sysTenant 租户查询条件
     * @return 租户列表
     */
    @GetMapping("list")
    @Operation(summary = "查询租户列表")
    public ReturnResult<List<SysTenant>> listForSysTenant(@ParameterObject SysTenant sysTenant) {
        return sysTenantService.listForSysTenant(sysTenant);
    }

    /**
     * 分页查询租户
     *
     * @param query     分页查询对象
     * @param sysTenant 租户查询条件
     * @return 分页结果
     */
    @GetMapping("page")
    @Operation(summary = "分页查询租户")
    @Permission("sys:tenant:page")
    public ReturnPageResult<SysTenant> pageForSysTenant(@ParameterObject Query<SysTenant> query,
                                                        @ParameterObject SysTenant sysTenant) {
        return PageResultUtils.ok(sysTenantService.pageForSysTenant(query, sysTenant));
    }

    /**
     * 获取租户详情
     *
     * @param sysTenantId 租户标识
     * @return 租户详情
     * @param "租户ID" "租户ID"
     */
    @GetMapping("get")
    @Operation(summary = "获取租户详情")
    @Permission("sys:tenant:get")
    public ReturnResult<SysTenant> getForSysTenant(@Parameter(description = "租户ID") String sysTenantId) {
        return sysTenantService.getForSysTenant(sysTenantId);
    }

    /**
     * 保存租户
     *
     * @param sysTenant     租户对象
     * @param bindingResult 校验结果
     * @return 保存结果
     */
    @PostMapping("save")
    @SysLog
    @Operation(summary = "保存租户")
    @Permission("sys:tenant:save")
    public ReturnResult<SysTenant> saveForSysTenant(@RequestBody @Validated(AddGroup.class) SysTenant sysTenant,
                                                    BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ReturnResult.illegal(bindingResult.getAllErrors().getFirst().getDefaultMessage());
        }
        return sysTenantService.saveForSysTenant(sysTenant);
    }

    /**
     * 更新租户
     *
     * @param sysTenant     租户对象
     * @param bindingResult 校验结果
     * @return 更新结果
     */
    @PutMapping("update")
    @SysLog
    @Operation(summary = "更新租户")
    @Permission("sys:tenant:update")
    public ReturnResult<Boolean> updateForSysTenant(@RequestBody @Validated(UpdateGroup.class) SysTenant sysTenant,
                                                    BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ReturnResult.illegal(bindingResult.getAllErrors().getFirst().getDefaultMessage());
        }
        return sysTenantService.updateForSysTenant(sysTenant);
    }

    /**
     * 删除租户
     *
     * @param sysTenantId 租户标识
     * @return 删除结果
     * @param "租户ID" "租户ID"
     */
    @DeleteMapping("delete")
    @SysLog
    @Operation(summary = "删除租户")
    @Permission("sys:tenant:delete")
    public ReturnResult<Boolean> deleteForSysTenant(@Parameter(description = "租户ID") String sysTenantId) {
        return sysTenantService.deleteForSysTenant(sysTenantId);
    }

    /**
     * 绑定租户服务
     *
     * @param request 绑定请求
     * @return 绑定结果
     */
    @PutMapping("bind/service")
    @SysLog(module = "绑定")
    @Operation(summary = "绑定租户服务")
    @Permission("sys:tenant:bind")
    public ReturnResult<Boolean> bindServiceForSysTenant(@RequestBody SysTenantBindServiceV1Request request) {
        return sysTenantService.bindServiceForSysTenant(request);
    }

    /**
     * 更新租户状态
     *
     * @param sysTenantId 租户标识
     * @param status      状态值 (0-启用, 1-禁用)
     * @return 更新结果
     */
    @PutMapping("status")
    @SysLog(module = "状态")
    @Operation(summary = "更新租户状态")
    @Permission("sys:tenant:status")
    public ReturnResult<Boolean> updateStatusForSysTenant(
            @Parameter(description = "租户ID") String sysTenantId,
            @Parameter(description = "状态值: 0-启用, 1-禁用") Integer status) {
        return sysTenantService.updateStatusForSysTenant(sysTenantId, status);
    }

    /**
     * 获取租户统计数据
     *
     * @return 统计数据
     */
    @GetMapping("stats")
    @Operation(summary = "获取租户统计数据")
    public ReturnResult<SysTenantStats> statsForSysTenant() {
        return sysTenantService.statsForSysTenant();
    }

    /**
     * 查询租户服务器地址。
     *
     * <p>回答「这个租户的请求该打到哪台服务器」。优先返回租户端当前在线实例自报的地址，
     * 没有在线实例时退回 {@code sys_tenant_home_url}。两者都没有则返回 {@code null}。</p>
     *
     * @param sysTenantId 租户标识
     * @return 地址信息；解析不到时为 {@code null}
     */
    @GetMapping("endpoint")
    @Operation(summary = "查询租户服务器地址")
    @Permission("sys:tenant:get")
    public ReturnResult<TenantEndpoint> endpointForSysTenant(
            @Parameter(description = "租户ID") String sysTenantId) {
        return ReturnResult.ok(tenantEndpointResolver.resolve(sysTenantId));
    }

    /**
     * 数据库来源下拉项。
     *
     * <p>租户表单要选「用哪个实例」，但租户管理员角色通常没有数据源中心的权限。
     * 这里代理一层：只返回实例级信息（名称/地址/端口），<b>不含口令</b>，
     * 依赖 {@code sys:tenant:get} 而不是数据源中心的权限点。</p>
     *
     * @return 可选项：「自填」+「平台自身实例」+ 数据源中心里可用的 jdbc 数据源
     */
    @GetMapping("datasource/options")
    @Operation(summary = "租户库来源下拉项")
    @Permission("sys:tenant:get")
    public ReturnResult<List<TenantDatasourceOption>> datasourceOptionsForSysTenant() {
        return ReturnResult.ok(tenantDataSourceResolver.options());
    }

    /**
     * 租户库连接信息只读回显。
     *
     * @param sysTenantId 租户标识
     * @return 摘要；租户未登记独立库时为 {@code null}
     */
    @GetMapping("datasource/current")
    @Operation(summary = "租户库连接信息回显")
    @Permission("sys:tenant:get")
    public ReturnResult<TenantDatasourceView> datasourceCurrentForSysTenant(
            @Parameter(description = "租户ID") String sysTenantId) {
        if (sysTenantId == null || sysTenantId.isBlank()) {
            return ReturnResult.illegal("租户ID不能为空");
        }
        return ReturnResult.ok(tenantDataSourceResolver.describe(sysTenantService.getById(sysTenantId)));
    }

    /**
     * 重置租户密码（推送给租户端）
     *
     * @param sysTenantId       租户标识
     * @param sysTenantPassword 新密码
     * @return 重置结果
     */
    @PutMapping("reset-password")
    @SysLog
    @Operation(summary = "重置租户密码")
    @Permission("sys:tenant:update")
    public ReturnResult<Boolean> resetPasswordForSysTenant(
            @Parameter(description = "租户ID") String sysTenantId,
            @Parameter(description = "新密码") String sysTenantPassword) {
        return sysTenantService.resetPasswordForSysTenant(sysTenantId, sysTenantPassword);
    }

    /**
     * 重置租户菜单（按订阅重新计算并推送给租户端）
     *
     * @param sysTenantId 租户标识
     * @return 重置结果
     */
    @PutMapping("reset-menu")
    @SysLog
    @Operation(summary = "重置租户菜单")
    @Permission("sys:tenant:bind")
    public ReturnResult<Boolean> resetMenuForSysTenant(
            @Parameter(description = "租户ID") String sysTenantId) {
        if (sysTenantId == null || sysTenantId.isBlank()) {
            return ReturnResult.illegal("租户ID不能为空");
        }
        sysTenantService.resetMenuForSysTenant(sysTenantId);
        return ReturnResult.ok(true);
    }

    /**
     * 初始化（或补齐）租户独立库的业务表。
     *
     * <p>与平台主库执行同一批 {@code db/init} 迁移脚本，版本记录写在租户库自己的
     * {@code sys_database_version} 里，因此可以重复调用：只跑没执行过的脚本。
     * 用于建租户时脚本失败的补做，以及存量租户库升级后补跑新增迁移。</p>
     *
     * @param sysTenantId 租户标识
     * @return 执行摘要
     */
    @PutMapping("init-database")
    @SysLog
    @Operation(summary = "初始化租户库业务表（幂等，可重复执行）")
    @Permission("sys:tenant:update")
    public ReturnResult<String> initDatabaseForSysTenant(
            @Parameter(description = "租户ID") String sysTenantId) {
        return sysTenantService.initTenantDatabase(sysTenantId);
    }

    /**
     * 把平台租户账号同步成租户库管理员。
     *
     * <p>建租户时已自动执行；这里给三类场景补做：历史租户（建的时候还没有这一步）、
     * 建库/建表当时失败、以及人工在租户库里改坏了管理员账号。重复调用安全（有则覆盖口令，无则新建）。</p>
     *
     * @param sysTenantId 租户标识
     * @return 同步结果说明
     */
    @PutMapping("sync-admin")
    @SysLog
    @Operation(summary = "同步租户库管理员（幂等，可重复执行）")
    @Permission("sys:tenant:update")
    public ReturnResult<String> syncAdminForSysTenant(
            @Parameter(description = "租户ID") String sysTenantId) {
        return sysTenantService.syncTenantDatabaseAdmin(sysTenantId);
    }

    /**
     * 清理已到期的租户服务订阅。
     *
     * <p>定时任务每 10 分钟跑一次；本接口用于立刻执行（验收、排障），
     * 语义与定时任务完全一致：删绑定 → 重写租户库服务目录 → 重推菜单。</p>
     *
     * @return 本次删除的绑定条数
     */
    @PutMapping("services/expire")
    @SysLog
    @Operation(summary = "清理已到期的租户服务（幂等）")
    @Permission("sys:tenant:update")
    public ReturnResult<Integer> expireServicesForSysTenant() {
        return ReturnResult.ok(tenantServiceExpiryJob.purgeExpired());
    }
}
