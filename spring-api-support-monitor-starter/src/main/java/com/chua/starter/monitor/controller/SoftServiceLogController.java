package com.chua.starter.monitor.controller;

import com.chua.common.support.lang.code.ReturnPageResult;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.entity.SoftServiceLog;
import com.chua.starter.monitor.service.SoftServiceLogService;
import com.chua.starter.mybatis.entity.Query;
import io.swagger.annotations.Api;
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
import java.io.OutputStream;
import java.util.List;

/**
 * 软件服务日志控制器
 *
 * @author CH
 * @since 2025/6/5
 */
@RestController
@RequestMapping("v1/soft/service/log")
@Tag(name = "软件服务日志管理", description = "软件服务的日志管理接口")
@Api(tags = "软件服务日志管理")
@RequiredArgsConstructor
@Slf4j
public class SoftServiceLogController {

    private final SoftServiceLogService softServiceLogService;

    /**
     * 分页查询日志列表
     *
     * @param query          查询参数
     * @param softServiceLog 查询条件
     * @return 分页日志列表
     */
    @GetMapping("page")
    @Operation(summary = "分页查询日志列表", description = "分页查询软件服务的操作日志列表")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功",
                    content = @Content(schema = @Schema(implementation = ReturnPageResult.class)))
    })
    public ReturnPageResult<SoftServiceLog> pageForLogs(
            @ParameterObject @Parameter(description = "分页查询参数") Query<SoftServiceLog> query,
            @ParameterObject @Parameter(description = "日志查询条件") SoftServiceLog softServiceLog) {
        return softServiceLogService.pageForLogs(query, softServiceLog);
    }

    /**
     * 根据安装ID查询日志列表
     *
     * @param installId 安装ID
     * @return 日志列表
     */
    @GetMapping("list/{installId}")
    @Operation(summary = "根据安装ID查询日志列表", description = "获取指定安装ID的所有操作日志")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功",
                    content = @Content(schema = @Schema(implementation = ReturnResult.class)))
    })
    public ReturnResult<List<SoftServiceLog>> getLogsByInstallId(
            @PathVariable("installId") @Parameter(description = "安装ID", required = true) Integer installId) {
        return softServiceLogService.getLogsByInstallId(installId);
    }

    /**
     * 查看日志内容
     *
     * @param installId     安装ID
     * @param operationType 操作类型
     * @return 日志内容
     */
    @GetMapping("content")
    @Operation(summary = "查看日志内容", description = "获取指定安装ID和操作类型的日志内容")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功",
                    content = @Content(schema = @Schema(implementation = ReturnResult.class)))
    })
    public ReturnResult<String> getLogContent(
            @RequestParam @Parameter(description = "安装ID", required = true) Integer installId,
            @RequestParam @Parameter(description = "操作类型，如install/start/stop/restart/uninstall", required = true) String operationType) {
        return softServiceLogService.getLogContent(installId, operationType);
    }

    /**
     * 获取日志文件列表
     *
     * @param installId 安装ID
     * @return 日志文件列表
     */
    @GetMapping("files/{installId}")
    @Operation(summary = "获取日志文件列表", description = "获取指定安装ID的所有日志文件列表")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功",
                    content = @Content(schema = @Schema(implementation = ReturnResult.class)))
    })
    public ReturnResult<List<String>> getLogFilesByInstallId(
            @PathVariable("installId") @Parameter(description = "安装ID", required = true) Integer installId) {
        return softServiceLogService.getLogFilesByInstallId(installId);
    }

    /**
     * 下载日志文件
     *
     * @param installId     安装ID
     * @param operationType 操作类型
     * @param response      HTTP响应
     */
    @GetMapping("download")
    @Operation(summary = "下载日志文件", description = "下载指定安装ID和操作类型的日志文件")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "下载成功",
                    content = @Content(mediaType = "application/octet-stream"))
    })
    public void downloadLogFile(
            @RequestParam @Parameter(description = "安装ID", required = true) Integer installId,
            @RequestParam @Parameter(description = "操作类型，如install/start/stop/restart/uninstall", required = true) String operationType,
            HttpServletResponse response) {
        try {
            ReturnResult<byte[]> result = softServiceLogService.downloadLogFile(installId, operationType);
            if (result.isOk()) {
                byte[] logContent = result.getData();
                if (logContent != null && logContent.length > 0) {
                    // 设置响应头
                    String fileName = operationType + "_" + installId + ".log";
                    response.setContentType("application/octet-stream");
                    response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
                    response.setContentLength(logContent.length);

                    // 写入响应
                    try (OutputStream outputStream = response.getOutputStream()) {
                        outputStream.write(logContent);
                        outputStream.flush();
                    }
                } else {
                    // 返回错误信息
                    response.setContentType("text/plain;charset=UTF-8");
                    response.getWriter().write("日志内容为空");
                }
            } else {
                // 返回错误信息
                response.setContentType("text/plain;charset=UTF-8");
                response.getWriter().write(result.getMsg());
            }
        } catch (IOException e) {
            log.error("下载日志文件失败: {}", e.getMessage(), e);
            try {
                response.setContentType("text/plain;charset=UTF-8");
                response.getWriter().write("下载日志文件失败: " + e.getMessage());
            } catch (IOException ex) {
                log.error("写入错误响应失败: {}", ex.getMessage(), ex);
            }
        }
    }

    /**
     * 清理指定安装ID的日志
     *
     * @param installId 安装ID
     * @return 操作结果
     */
    @DeleteMapping("clean/{installId}")
    @Operation(summary = "清理指定安装ID的日志", description = "删除指定安装ID的所有日志文件")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "清理成功",
                    content = @Content(schema = @Schema(implementation = ReturnResult.class)))
    })
    public ReturnResult<Boolean> cleanLogsByInstallId(
            @PathVariable("installId") @Parameter(description = "安装ID", required = true) Integer installId) {
        return softServiceLogService.cleanLogsByInstallId(installId);
    }

    /**
     * 追加日志内容
     *
     * @param installId     安装ID
     * @param operationType 操作类型
     * @param content       日志内容
     * @return 操作结果
     */
    @PostMapping("append")
    @Operation(summary = "追加日志内容", description = "向指定安装ID和操作类型的日志文件追加内容")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "追加成功",
                    content = @Content(schema = @Schema(implementation = ReturnResult.class)))
    })
    public ReturnResult<Boolean> appendLogContent(
            @RequestParam @Parameter(description = "安装ID", required = true) Integer installId,
            @RequestParam @Parameter(description = "操作类型，如install/start/stop/restart/uninstall", required = true) String operationType,
            @RequestBody @Parameter(description = "日志内容", required = true) String content) {
        return softServiceLogService.appendLogContent(installId, operationType, content);
    }

    /**
     * 保存日志
     *
     * @param softServiceLog 日志信息
     * @return 操作结果
     */
    @PostMapping("save")
    @Operation(summary = "保存日志", description = "创建新的软件服务操作日志")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "保存成功",
                    content = @Content(schema = @Schema(implementation = ReturnResult.class)))
    })
    public ReturnResult<Boolean> saveLog(
            @RequestBody @Parameter(description = "日志信息", required = true) SoftServiceLog softServiceLog) {
        return softServiceLogService.saveLog(softServiceLog);
    }
} 