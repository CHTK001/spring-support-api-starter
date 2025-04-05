package com.chua.starter.monitor.controller;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.pojo.request.MaintenanceScriptRequest;
import com.chua.starter.monitor.pojo.request.RunScriptRequest;
import com.chua.starter.monitor.pojo.response.MaintenanceScriptResponse;
import com.chua.starter.monitor.pojo.response.RunScriptResponse;
import com.chua.starter.monitor.service.MaintenanceScriptService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 维护脚本管理控制器
 * 提供维护脚本的增删改查、执行等接口
 *
 * @author CH
 */
@RestController
@RequestMapping("/api/maintenance/script")
@Tag(name = "维护脚本管理", description = "维护脚本的创建、修改、删除、查询、执行等操作，并支持脚本自动同步到维护组内所有主机")
@RequiredArgsConstructor
public class MaintenanceScriptController {

    /**
     * 维护脚本服务
     */
    private final MaintenanceScriptService maintenanceScriptService;

    /**
     * 创建维护脚本
     * 脚本创建后会自动上传到维护组下所有启用的主机相同位置
     *
     * @param request 请求参数
     * @return 创建结果
     */
    @PostMapping("save")
    @Operation(summary = "创建维护脚本", description = "创建一个新的维护脚本，返回创建后的脚本信息。脚本将自动上传到维护组下所有启用的主机相同位置。")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "创建成功", 
            content = @Content(schema = @Schema(implementation = MaintenanceScriptResponse.class))),
        @ApiResponse(responseCode = "400", description = "参数错误"),
        @ApiResponse(responseCode = "500", description = "服务器错误")
    })
    public ReturnResult<MaintenanceScriptResponse> createMaintenanceScript(
            @Parameter(description = "维护脚本信息", required = true) 
            @RequestBody MaintenanceScriptRequest request) {
        if (request.getMaintenanceGroupId() == null) {
            return ReturnResult.error("维护组ID不能为空");
        }
        return maintenanceScriptService.createMaintenanceScript(request);
    }

    /**
     * 更新维护脚本
     * 脚本更新后会自动覆盖到维护组下所有启用的主机相同位置的脚本
     *
     * @param request 请求参数
     * @return 更新结果
     */
    @PutMapping("update")
    @Operation(summary = "更新维护脚本", description = "根据ID更新维护脚本信息，返回更新后的脚本信息。更新后的脚本将自动覆盖到维护组下所有启用的主机相同位置的脚本。")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "更新成功", 
            content = @Content(schema = @Schema(implementation = MaintenanceScriptResponse.class))),
        @ApiResponse(responseCode = "404", description = "维护脚本不存在"),
        @ApiResponse(responseCode = "500", description = "服务器错误")
    })
    public ReturnResult<MaintenanceScriptResponse> updateMaintenanceScript(
            @Parameter(description = "维护脚本信息", required = true) 
            @RequestBody MaintenanceScriptRequest request) {
        return maintenanceScriptService.updateMaintenanceScript(request);
    }

    /**
     * 删除维护脚本
     * 脚本删除后会自动删除维护组下所有启用的主机相同位置的脚本
     *
     * @param maintenanceScriptId 脚本ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除维护脚本", description = "根据ID删除维护脚本。删除后会自动删除维护组下所有启用的主机相同位置的脚本。")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "删除成功", 
            content = @Content(schema = @Schema(implementation = Boolean.class))),
        @ApiResponse(responseCode = "500", description = "服务器错误")
    })
    public ReturnResult<Boolean> deleteMaintenanceScript(
            @Parameter(description = "维护脚本ID", required = true) 
            @PathVariable("id") Long maintenanceScriptId) {
        return maintenanceScriptService.deleteMaintenanceScript(maintenanceScriptId);
    }

    /**
     * 获取维护脚本详情
     *
     * @param maintenanceScriptId 脚本ID
     * @return 脚本详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取维护脚本详情", description = "根据ID获取维护脚本详细信息")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功", 
            content = @Content(schema = @Schema(implementation = MaintenanceScriptResponse.class))),
        @ApiResponse(responseCode = "404", description = "维护脚本不存在"),
        @ApiResponse(responseCode = "500", description = "服务器错误")
    })
    public ReturnResult<MaintenanceScriptResponse> getMaintenanceScript(
            @Parameter(description = "维护脚本ID", required = true) 
            @PathVariable("id") Long maintenanceScriptId) {
        return maintenanceScriptService.getMaintenanceScript(maintenanceScriptId);
    }

    /**
     * 获取维护脚本列表
     *
     * @param maintenanceGroupId 维护组ID
     * @return 脚本列表
     */
    @GetMapping("/list")
    @Operation(summary = "获取维护脚本列表", description = "根据维护组ID获取该组下的所有维护脚本")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功", 
            content = @Content(schema = @Schema(implementation = MaintenanceScriptResponse.class))),
        @ApiResponse(responseCode = "400", description = "参数错误"),
        @ApiResponse(responseCode = "500", description = "服务器错误")
    })
    public ReturnResult<List<MaintenanceScriptResponse>> listMaintenanceScripts(
            @Parameter(description = "维护组ID", required = true) 
            @RequestParam(value = "groupId") Long maintenanceGroupId) {
        if (maintenanceGroupId == null) {
            return ReturnResult.error("维护组ID不能为空");
        }
        return maintenanceScriptService.listMaintenanceScripts(maintenanceGroupId);
    }

    /**
     * 运行脚本
     * 
     * 注意：此接口直接执行脚本内容，不依赖于主机上已存在的脚本文件
     *
     * @param request 请求参数
     * @return 运行结果
     */
    @PostMapping("/run")
    @Operation(summary = "运行脚本", description = "执行指定的维护脚本，并返回执行结果。此操作直接执行脚本内容，不依赖于主机上已存在的脚本文件。")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "执行成功", 
            content = @Content(schema = @Schema(implementation = RunScriptResponse.class))),
        @ApiResponse(responseCode = "400", description = "参数错误"),
        @ApiResponse(responseCode = "404", description = "脚本或主机不存在"),
        @ApiResponse(responseCode = "500", description = "服务器错误")
    })
    public ReturnResult<RunScriptResponse> runScript(
            @Parameter(description = "脚本执行参数", required = true) 
            @RequestBody RunScriptRequest request) {
        return maintenanceScriptService.runScript(request);
    }

    /**
     * 更新脚本状态
     *
     * @param maintenanceScriptId 脚本ID
     * @param status              状态
     * @return 更新结果
     */
    @PutMapping("/{id}/status/{status}")
    @Operation(summary = "更新脚本状态", description = "修改维护脚本的状态（启用/禁用等）")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "更新成功", 
            content = @Content(schema = @Schema(implementation = Boolean.class))),
        @ApiResponse(responseCode = "404", description = "维护脚本不存在"),
        @ApiResponse(responseCode = "500", description = "服务器错误")
    })
    public ReturnResult<Boolean> updateScriptStatus(
            @Parameter(description = "维护脚本ID", required = true) 
            @PathVariable("id") Long maintenanceScriptId,
            @Parameter(description = "状态值: 0-禁用, 1-启用", required = true) 
            @PathVariable("status") Integer status) {
        return maintenanceScriptService.updateScriptStatus(maintenanceScriptId, status);
    }
} 