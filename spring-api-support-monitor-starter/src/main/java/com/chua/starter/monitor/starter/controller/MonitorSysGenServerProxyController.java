package com.chua.starter.monitor.starter.controller;

import com.chua.common.support.lang.code.ReturnPageResult;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.validator.group.AddGroup;
import com.chua.common.support.validator.group.UpdateGroup;
import com.chua.starter.monitor.starter.entity.MonitorSysGenServerProxy;
import com.chua.starter.monitor.starter.service.MonitorSysGenServerProxyService;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.mybatis.utils.PageResultUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static com.chua.common.support.lang.code.ReturnCode.REQUEST_PARAM_ERROR;

/**
 * 服务器代理关联控制器
 *
 * @author CH
 * @since 2024/12/25
 */
@RestController
@RequestMapping("v1/gen/server/proxy")
@Tag(name = "服务器代理关联管理")
@RequiredArgsConstructor
@Slf4j
public class MonitorSysGenServerProxyController {

    private final MonitorSysGenServerProxyService monitorSysGenServerProxyService;

    /**
     * 分页查询服务器代理关联
     *
     * @param page   分页参数
     * @param entity 查询条件
     * @return 分页结果
     */
    @ResponseBody
    @Operation(summary = "分页查询服务器代理关联")
    @GetMapping("page")
    public ReturnPageResult<MonitorSysGenServerProxy> page(Query<MonitorSysGenServerProxy> page, MonitorSysGenServerProxy entity) {
        return PageResultUtils.ok(monitorSysGenServerProxyService.pageFor(page.createPage(), entity));
    }

    /**
     * 根据服务器ID查询代理关联列表
     *
     * @param serverId 服务器ID
     * @return 代理关联列表
     */
    @Operation(summary = "根据服务器ID查询代理关联列表")
    @GetMapping("server/{serverId}")
    public ReturnResult<List<MonitorSysGenServerProxy>> getByServerId(@PathVariable Integer serverId) {
        return monitorSysGenServerProxyService.getByServerId(serverId);
    }

    /**
     * 根据代理ID查询服务器关联列表
     *
     * @param proxyId 代理ID
     * @return 服务器关联列表
     */
    @Operation(summary = "根据代理ID查询服务器关联列表")
    @GetMapping("proxy/{proxyId}")
    public ReturnResult<List<MonitorSysGenServerProxy>> getByProxyId(@PathVariable Integer proxyId) {
        return monitorSysGenServerProxyService.getByProxyId(proxyId);
    }

    /**
     * 根据服务器ID和代理类型查询代理关联
     *
     * @param serverId  服务器ID
     * @param proxyType 代理类型
     * @return 代理关联
     */
    @Operation(summary = "根据服务器ID和代理类型查询代理关联")
    @GetMapping("server/{serverId}/type/{proxyType}")
    public ReturnResult<MonitorSysGenServerProxy> getByServerIdAndProxyType(
            @PathVariable Integer serverId,
            @PathVariable String proxyType) {
        return monitorSysGenServerProxyService.getByServerIdAndProxyType(serverId, proxyType);
    }

    /**
     * 创建服务器代理关联
     *
     * @param serverProxy   服务器代理关联
     * @param bindingResult 验证结果
     * @return 创建结果
     */
    @ResponseBody
    @Operation(summary = "创建服务器代理关联")
    @PostMapping("save")
    public ReturnResult<MonitorSysGenServerProxy> save(
            @Validated(AddGroup.class) @RequestBody MonitorSysGenServerProxy serverProxy,
            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ReturnResult.illegal(REQUEST_PARAM_ERROR, bindingResult.getAllErrors().getFirst().getDefaultMessage());
        }
        return monitorSysGenServerProxyService.createServerProxy(serverProxy);
    }

    /**
     * 更新服务器代理关联
     *
     * @param serverProxy   服务器代理关联
     * @param bindingResult 验证结果
     * @return 更新结果
     */
    @ResponseBody
    @Operation(summary = "更新服务器代理关联")
    @PutMapping("update")
    public ReturnResult<Boolean> update(
            @Validated(UpdateGroup.class) @RequestBody MonitorSysGenServerProxy serverProxy,
            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ReturnResult.illegal(REQUEST_PARAM_ERROR, bindingResult.getAllErrors().getFirst().getDefaultMessage());
        }
        return monitorSysGenServerProxyService.updateServerProxy(serverProxy);
    }

    /**
     * 删除服务器代理关联
     *
     * @param id 关联ID
     * @return 删除结果
     */
    @Operation(summary = "删除服务器代理关联")
    @DeleteMapping("delete/{id}")
    public ReturnResult<Boolean> delete(@PathVariable Integer id) {
        return monitorSysGenServerProxyService.deleteServerProxy(id);
    }

    /**
     * 批量删除服务器代理关联
     *
     * @param ids 关联ID列表
     * @return 删除结果
     */
    @Operation(summary = "批量删除服务器代理关联")
    @DeleteMapping("batch-delete")
    public ReturnResult<Boolean> batchDelete(@RequestBody List<Integer> ids) {
        return monitorSysGenServerProxyService.batchDeleteServerProxy(ids);
    }

    /**
     * 测试代理连接
     *
     * @param id 关联ID
     * @return 测试结果
     */
    @Operation(summary = "测试代理连接")
    @PostMapping("test/{id}")
    public ReturnResult<Boolean> testConnection(@PathVariable Integer id) {
        return monitorSysGenServerProxyService.testProxyConnection(id);
    }

    /**
     * 启用代理
     *
     * @param id 关联ID
     * @return 操作结果
     */
    @Operation(summary = "启用代理")
    @PostMapping("enable/{id}")
    public ReturnResult<Boolean> enable(@PathVariable Integer id) {
        return monitorSysGenServerProxyService.enableProxy(id);
    }

    /**
     * 禁用代理
     *
     * @param id 关联ID
     * @return 操作结果
     */
    @Operation(summary = "禁用代理")
    @PostMapping("disable/{id}")
    public ReturnResult<Boolean> disable(@PathVariable Integer id) {
        return monitorSysGenServerProxyService.disableProxy(id);
    }

    /**
     * 获取代理连接URL
     *
     * @param id 关联ID
     * @return 代理URL
     */
    @Operation(summary = "获取代理连接URL")
    @GetMapping("url/{id}")
    public ReturnResult<String> getProxyUrl(@PathVariable Integer id) {
        return monitorSysGenServerProxyService.getProxyUrl(id);
    }

    /**
     * 批量更新代理状态
     *
     * @param request 批量更新请求
     * @return 更新结果
     */
    @Operation(summary = "批量更新代理状态")
    @PostMapping("batch-update-status")
    public ReturnResult<Boolean> batchUpdateStatus(@RequestBody BatchUpdateStatusRequest request) {
        return monitorSysGenServerProxyService.batchUpdateStatus(request.getIds(), request.getStatus());
    }

    /**
     * 获取代理统计信息
     *
     * @return 统计信息
     */
    @Operation(summary = "获取代理统计信息")
    @GetMapping("statistics")
    public ReturnResult<Map<String, Object>> getStatistics() {
        return monitorSysGenServerProxyService.getProxyStatistics();
    }

    /**
     * 获取服务器的所有代理类型
     *
     * @param serverId 服务器ID
     * @return 代理类型列表
     */
    @Operation(summary = "获取服务器的所有代理类型")
    @GetMapping("server/{serverId}/types")
    public ReturnResult<List<String>> getServerProxyTypes(@PathVariable Integer serverId) {
        return monitorSysGenServerProxyService.getServerProxyTypes(serverId);
    }

    /**
     * 克隆服务器代理配置
     *
     * @param request 克隆请求
     * @return 克隆结果
     */
    @Operation(summary = "克隆服务器代理配置")
    @PostMapping("clone")
    public ReturnResult<List<MonitorSysGenServerProxy>> cloneConfig(@RequestBody CloneConfigRequest request) {
        return monitorSysGenServerProxyService.cloneServerProxyConfig(request.getSourceServerId(), request.getTargetServerId());
    }

    /**
     * 批量更新状态请求
     */
    public static class BatchUpdateStatusRequest {
        private List<Integer> ids;
        private Integer status;

        public List<Integer> getIds() {
            return ids;
        }

        public void setIds(List<Integer> ids) {
            this.ids = ids;
        }

        public Integer getStatus() {
            return status;
        }

        public void setStatus(Integer status) {
            this.status = status;
        }
    }

    /**
     * 克隆配置请求
     */
    public static class CloneConfigRequest {
        private Integer sourceServerId;
        private Integer targetServerId;

        public Integer getSourceServerId() {
            return sourceServerId;
        }

        public void setSourceServerId(Integer sourceServerId) {
            this.sourceServerId = sourceServerId;
        }

        public Integer getTargetServerId() {
            return targetServerId;
        }

        public void setTargetServerId(Integer targetServerId) {
            this.targetServerId = targetServerId;
        }
    }
}
