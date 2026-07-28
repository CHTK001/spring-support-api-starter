package com.chua.starter.monitor.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.utils.StringUtils;
import com.chua.starter.common.support.lang.ReturnPageResult;
import com.chua.starter.monitor.entity.MonitorServer;
import com.chua.starter.monitor.service.MonitorServerService;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.mybatis.utils.PageResultUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 被监控服务器主机管理接口，提供服务器分页查询、列表查询、详情、保存、更新与删除能力。
 *
 * @author CH
 * @since 2026-07-28
 */
@Slf4j
@RestController
@RequestMapping("/v1/gen/server")
@Api(tags = "服务器主机管理")
@Tag(name = "服务器主机管理")
@RequiredArgsConstructor
public class MonitorServerController {

    /**
     * 服务器主机服务
     */
    private final MonitorServerService serverService;

    /**
     * 服务器分页查询。
     *
     * @param page   分页参数
     * @param entity 查询条件（名称/IP/分组/状态）
     * @return 服务器分页列表
     */
    @GetMapping("/page")
    @Operation(summary = "服务器分页查询")
    public ReturnPageResult<MonitorServer> page(Query<MonitorServer> page, MonitorServer entity) {
        Object name = entity.getServerName();
        Object ip = entity.getServerIp();
        Object group = entity.getServerGroup();
        Object status = entity.getServerStatus();
        return PageResultUtils.ok(serverService.page(page.createPage(),
                Wrappers.<MonitorServer>lambdaQuery()
                        .like(StringUtils.isNotEmpty(entity.getServerName()),
                                MonitorServer::getServerName, name)
                        .like(StringUtils.isNotEmpty(entity.getServerIp()),
                                MonitorServer::getServerIp, ip)
                        .eq(StringUtils.isNotEmpty(entity.getServerGroup()),
                                MonitorServer::getServerGroup, group)
                        .eq(status != null,
                                MonitorServer::getServerStatus, status)
                        .orderByDesc(MonitorServer::getServerId)));
    }

    /**
     * 服务器列表查询（全部）。
     *
     * @return 服务器列表
     */
    @GetMapping("/list")
    @Operation(summary = "服务器列表查询")
    public ReturnResult<List<MonitorServer>> list() {
        return ReturnResult.ok(serverService.list());
    }

    /**
     * 获取服务器下拉选项（ID 与名称）。
     *
     * @return 服务器选项列表
     */
    @GetMapping("/options")
    @Operation(summary = "获取服务器下拉选项")
    public ReturnResult<List<Map<String, Object>>> options() {
        return ReturnResult.ok(serverService.listOptions());
    }

    /**
     * 获取服务器详情。
     *
     * @param serverId 服务器ID
     * @return 服务器详情
     */
    @GetMapping("/detail/{serverId}")
    @Operation(summary = "获取服务器详情")
    public ReturnResult<MonitorServer> detail(@ApiParam("服务器ID") @PathVariable Integer serverId) {
        return ReturnResult.ok(serverService.getById(serverId));
    }

    /**
     * 保存服务器信息，新服务器默认状态为启用。
     *
     * @param server 服务器实体
     * @return 保存后的服务器
     */
    @PostMapping("/save")
    @Operation(summary = "保存服务器")
    public ReturnResult<MonitorServer> save(@RequestBody MonitorServer server) {
        if (server.getServerStatus() == null) {
            server.setServerStatus(1);
        }
        serverService.save(server);
        return ReturnResult.ok(server);
    }

    /**
     * 更新服务器信息。
     *
     * @param server 服务器实体（必须包含服务器ID）
     * @return 是否更新成功
     */
    @PutMapping("/update")
    @Operation(summary = "更新服务器")
    public ReturnResult<Boolean> update(@RequestBody MonitorServer server) {
        if (server.getServerId() == null) {
            return ReturnResult.illegal("服务器ID不能为空");
        }
        return ReturnResult.ok(serverService.updateById(server));
    }

    /**
     * 删除服务器信息。
     *
     * @param serverId 服务器ID
     * @return 是否删除成功
     */
    @DeleteMapping("/remove/{serverId}")
    @Operation(summary = "删除服务器")
    public ReturnResult<Boolean> remove(@ApiParam("服务器ID") @PathVariable Integer serverId) {
        return ReturnResult.ok(serverService.removeById(serverId));
    }
}
