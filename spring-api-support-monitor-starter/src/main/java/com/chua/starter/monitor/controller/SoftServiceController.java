package com.chua.starter.monitor.controller;

import com.chua.common.support.lang.code.ReturnPageResult;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.entity.SoftService;
import com.chua.starter.monitor.service.SoftServiceInstallService;
import com.chua.starter.monitor.service.SoftServiceService;
import com.chua.starter.mybatis.entity.Query;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 软件服务安装记录控制器
 *
 * @author CH
 * @since 2024/7/18
 */
@RestController
@RequestMapping("v1/soft/service")
@Tag(name = "软件服务管理", description = "软件服务的管理和操作接口")
@Api(tags = "软件服务管理")
@RequiredArgsConstructor
@Slf4j
public class SoftServiceController {

    private final SoftServiceService softServiceService;
    private final SoftServiceInstallService softServiceInstallService;

    /**
     * 列表软件服务
     *
     * @param query       查询参数
     * @param softService 软件服务筛选条件
     * @return {@link ReturnPageResult}<{@link SoftService}> 分页软件服务列表
     */
    @GetMapping("page")
    @Operation(summary = "列表软件服务", description = "分页查询软件服务列表")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功",
                    content = @Content(schema = @Schema(implementation = ReturnPageResult.class)))
    })
    public ReturnPageResult<SoftService> listSoftService(
            @ParameterObject @Parameter(description = "分页查询参数") Query<SoftService> query,
            @ParameterObject @Parameter(description = "软件服务筛选条件") SoftService softService) {
        return softServiceService.pageForWithoutCommands(query, softService);
    }

    /**
     * 保存软件服务
     *
     * @param softService 软件服务信息
     * @return {@link ReturnResult}<{@link Boolean}> 保存结果
     */
    @Operation(summary = "保存软件服务", description = "创建新的软件服务")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "保存成功",
                    content = @Content(schema = @Schema(implementation = ReturnResult.class)))
    })
    @PostMapping("save")
    public ReturnResult<Boolean> saveSoftService(
            @RequestBody @Parameter(description = "软件服务信息", required = true) SoftService softService) {
        return ReturnResult.ok(softServiceService.save(softService));
    }

    /**
     * 更新软件服务
     *
     * @param softService 软件服务更新信息
     * @return {@link ReturnResult}<{@link Boolean}> 更新结果
     */
    @Operation(summary = "更新软件服务", description = "更新已有的软件服务信息")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "更新成功",
                    content = @Content(schema = @Schema(implementation = ReturnResult.class)))
    })
    @PutMapping("update")
    public ReturnResult<Boolean> updateSoftService(
            @RequestBody @Parameter(description = "软件服务更新信息", required = true) SoftService softService) {
        return ReturnResult.ok(softServiceService.updateById(softService));
    }

    /**
     * 删除软件服务
     *
     * @param id 软件服务ID
     * @return {@link ReturnResult}<{@link Boolean}> 删除结果
     */
    @Operation(summary = "删除软件服务", description = "根据ID删除软件服务")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "删除成功",
                    content = @Content(schema = @Schema(implementation = ReturnResult.class)))
    })
    @DeleteMapping("delete")
    public ReturnResult<Boolean> deleteSoftService(
            @RequestParam @Parameter(description = "软件服务ID", required = true) Integer id) {
        return ReturnResult.ok(softServiceService.removeById(id));
    }

    /**
     * 收藏服务
     *
     * @param softServiceId 软件服务ID
     * @return {@link ReturnResult}<{@link Boolean}> 收藏结果
     */
    @ApiOperation(value = "收藏服务", notes = "将指定软件服务添加到收藏夹")
    @Operation(summary = "收藏服务", description = "将指定软件服务添加到收藏夹")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "收藏成功",
                    content = @Content(schema = @Schema(implementation = ReturnResult.class)))
    })
    @PostMapping("/favorite/{softServiceId}")
    public ReturnResult<Boolean> favoriteService(
            @PathVariable("softServiceId") @Parameter(description = "软件服务ID", required = true) Integer softServiceId) {
        return softServiceService.favoriteService(softServiceId);
    }

    /**
     * 按操作系统查询软件服务
     *
     * @param query 查询参数
     * @param os    操作系统
     * @return {@link ReturnPageResult}<{@link SoftService}> 分页软件服务列表
     */
    @GetMapping("os/{os}")
    @Operation(summary = "按操作系统查询软件服务", description = "根据操作系统类型筛选软件服务")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功",
                    content = @Content(schema = @Schema(implementation = ReturnPageResult.class)))
    })
    public ReturnPageResult<SoftService> listSoftServiceByOs(
            @ParameterObject @Parameter(description = "分页查询参数") Query<SoftService> query,
            @PathVariable("os") @Parameter(description = "操作系统类型", required = true) String os) {
        SoftService softService = new SoftService();
        softService.setSoftServiceOs(os);
        return softServiceService.pageForWithoutCommands(query, softService);
    }

    /**
     * 获取所有支持的操作系统
     *
     * @return {@link ReturnResult}<{@link List}<{@link String}>> 操作系统列表
     */
    @GetMapping("os/list")
    @Operation(summary = "获取所有支持的操作系统", description = "返回系统中所有已配置的操作系统类型")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功",
                    content = @Content(schema = @Schema(implementation = ReturnResult.class)))
    })
    public ReturnResult<List<String>> listAllOs() {
        return ReturnResult.ok(softServiceService.listAllOs());
    }

    /**
     * 检测命令类型
     *
     * @param command 命令内容
     * @return {@link ReturnResult}<{@link Map}<{@link String}, {@link Object}>> 命令类型信息
     */
    @Operation(summary = "检测命令类型", description = "分析命令内容，判断其类型（单行命令或脚本）")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "检测成功",
                    content = @Content(schema = @Schema(implementation = ReturnResult.class)))
    })
    @PostMapping("detect-command-type")
    public ReturnResult<Map<String, Object>> detectCommandType(
            @RequestBody @Parameter(description = "命令内容", required = true) String command) {
        return softServiceService.detectCommandType(command);
    }

    /**
     * 更新服务状态检查命令
     *
     * @param installId   安装记录ID
     * @param command     状态检查命令
     * @param commandType 命令类型（0:单行命令，1:脚本）
     * @param successFlag 成功标识
     * @return {@link ReturnResult}<{@link Boolean}> 更新结果
     */
    @PutMapping("/updateStatusCheckCommand")
    @ApiOperation(value = "更新服务状态检查命令", notes = "设置或更新服务状态检查的命令和成功标识")
    @Operation(summary = "更新服务状态检查命令", description = "设置或更新服务状态检查的命令和成功标识")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "更新成功",
                    content = @Content(schema = @Schema(implementation = ReturnResult.class)))
    })
    public ReturnResult<Boolean> updateStatusCheckCommand(
            @RequestParam("installId") @Parameter(description = "安装记录ID", required = true) Integer installId,
            @RequestParam("command") @Parameter(description = "状态检查命令", required = true) String command,
            @RequestParam(value = "commandType", required = false, defaultValue = "0")
            @Parameter(description = "命令类型（0:单行命令，1:脚本）", schema = @Schema(defaultValue = "0")) Integer commandType,
            @RequestParam(value = "successFlag", required = false)
            @Parameter(description = "成功标识，用于判断服务是否运行") String successFlag) {
        return softServiceInstallService.updateStatusCheckCommand(installId, command, commandType, successFlag);
    }

    /**
     * 根据ID获取软件服务详情
     *
     * @param softServiceId 软件服务ID
     * @return {@link ReturnResult}<{@link SoftService}> 软件服务详情
     */
    @GetMapping("get")
    @Operation(summary = "根据ID获取软件服务详情", description = "获取指定软件服务的完整信息")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功",
                    content = @Content(schema = @Schema(implementation = ReturnResult.class)))
    })
    public ReturnResult<SoftService> getSoftServiceDetail(
            @RequestParam("softServiceId") @Parameter(description = "软件服务ID", required = true) Integer softServiceId) {
        return ReturnResult.ok(softServiceService.getById(softServiceId));
    }

    /**
     * 获取服务操作日志
     *
     * @param installId     安装ID
     * @param operationType 操作类型
     * @return {@link ReturnResult}<{@link String}> 日志内容
     */
    @GetMapping("/log")
    @Operation(summary = "获取服务操作日志", description = "获取指定安装记录的操作日志内容")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "获取成功",
                    content = @Content(schema = @Schema(implementation = ReturnResult.class)))
    })
    public ReturnResult<String> getServiceOperationLog(
            @RequestParam @Parameter(description = "安装记录ID", required = true) Integer installId,
            @RequestParam @Parameter(description = "操作类型，如install/start/stop/restart/uninstall", required = true) String operationType) {
        return softServiceInstallService.getServiceOperationLog(installId, operationType);
    }

    /**
     * 获取服务日志文件列表
     *
     * @param installId 安装ID
     * @return {@link ReturnResult}<{@link List}<{@link String}>> 日志文件列表
     */
    @GetMapping("/logs")
    @Operation(summary = "获取服务日志文件列表", description = "获取指定安装记录的所有日志文件列表")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "获取成功",
                    content = @Content(schema = @Schema(implementation = ReturnResult.class)))
    })
    public ReturnResult<List<String>> listServiceLogFiles(
            @RequestParam @Parameter(description = "安装记录ID", required = true) Integer installId) {
        return softServiceInstallService.listServiceLogFiles(installId);
    }

    /**
     * 下载服务操作日志文件
     *
     * @param installId     安装记录ID
     * @param operationType 操作类型
     * @param response      HTTP响应
     */
    @GetMapping("/log/download")
    @Operation(summary = "下载服务操作日志文件", description = "下载指定安装记录的操作日志文件")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "下载成功",
                    content = @Content(mediaType = "application/octet-stream"))
    })
    public void downloadServiceOperationLog(
            @RequestParam @Parameter(description = "安装记录ID", required = true) Integer installId,
            @RequestParam @Parameter(description = "操作类型，如install/start/stop/restart/uninstall", required = true) String operationType,
            HttpServletResponse response) {
        ReturnResult<byte[]> result = softServiceInstallService.downloadServiceOperationLog(installId, operationType);
        if (result.isOk()) {
            try {
                // 设置响应头
                response.setContentType("application/octet-stream");
                response.setHeader("Content-Disposition", "attachment; filename=" + operationType + ".log");

                // 写入日志文件内容
                byte[] logContent = result.getData();
                response.setContentLength(logContent.length);
                response.getOutputStream().write(logContent);
                response.getOutputStream().flush();
            } catch (IOException e) {
                log.error("下载日志文件失败: {}", e.getMessage(), e);
            }
        } else {
            try {
                response.setContentType("application/json");
                response.getWriter().write("{\"code\":500,\"msg\":\"" + result.getMsg() + "\"}");
            } catch (IOException e) {
                log.error("返回错误信息失败: {}", e.getMessage(), e);
            }
        }
    }
}
