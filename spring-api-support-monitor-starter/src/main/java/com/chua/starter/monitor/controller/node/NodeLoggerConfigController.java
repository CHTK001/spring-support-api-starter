package com.chua.starter.monitor.controller.node;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.pojo.BatchOperationResultDTO;
import com.chua.starter.monitor.pojo.NodeLoggerConfigDTO;
import com.chua.starter.monitor.pojo.NodeLoggerStatsDTO;
import com.chua.starter.monitor.service.NodeLoggerConfigService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 节点日志器配置管理控制器
 * 
 * 提供节点日志器配置的完整管理功能，包括： - 获取节点日志器配置列表 - 设置和重置日志器等级 - 批量操作多个节点 - 刷新和同步配置 -
 * 统计和监控功能
 * 
 * 主要API路径： - GET /node/plugin/node-logger/nodes/{nodeUrl}/loggers - 获取节点日志器配置 -
 * POST /node/plugin/node-logger/nodes/{nodeUrl}/loggers/{loggerName}/level -
 * 设置日志器等级 - POST /node/plugin/node-logger/nodes/{nodeUrl}/refresh - 刷新节点配置 -
 * GET /node/plugin/node-logger/applications - 获取所有应用名称 - POST
 * /node/plugin/node-logger/applications/{applicationName}/loggers/{loggerName}/level
 * - 批量设置等级
 *
 * @author CH
 * @since 2025/01/17
 */
@RestController
@RequestMapping("/node/plugin/node-logger")
@Api(tags = "节点日志器配置管理")
@Tag(name = "节点日志器配置管理", description = "节点日志器配置的完整生命周期管理API")
@RequiredArgsConstructor
@Slf4j
@Validated
public class NodeLoggerConfigController {

    private final NodeLoggerConfigService loggerConfigService;

    /**
     * 获取节点的所有日志器配置
     *
     * @param nodeUrl 节点URL（Base64编码）
     * @return 日志器配置列表
     */
    @GetMapping("/nodes/{nodeUrl}/loggers")
    @Operation(summary = "获取节点日志器配置", description = "获取指定节点的所有日志器配置信息")
    @ApiOperation(value = "获取节点日志器配置", notes = "获取指定节点的所有日志器配置信息")
    public ReturnResult<List<NodeLoggerConfigDTO>> getNodeLoggers(
            @PathVariable("nodeUrl") @Parameter(description = "节点URL（Base64编码）", required = true) @ApiParam(value = "节点URL（Base64编码）", required = true) @NotBlank(message = "节点URL不能为空") String nodeUrl) {

        try {
            log.info("获取节点日志器配置请求: nodeUrl={}", nodeUrl);

            ReturnResult<List<NodeLoggerConfigDTO>> result = loggerConfigService.getNodeLoggers(nodeUrl);

            if (result.isSuccess()) {
                log.info("获取节点日志器配置成功: nodeUrl={}, 配置数量={}", nodeUrl,
                        result.getData() != null ? result.getData().size() : 0);
            } else {
                log.warn("获取节点日志器配置失败: nodeUrl={}, 错误信息={}", nodeUrl, result.getMsg());
            }

            return result;

        } catch (Exception e) {
            log.error("获取节点日志器配置异常: nodeUrl={}", nodeUrl, e);
            return ReturnResult.error("获取节点日志器配置失败: " + e.getMessage());
        }
    }

    /**
     * 获取指定日志器的详细配置
     *
     * @param nodeUrl    节点URL（Base64编码）
     * @param loggerName 日志器名称
     * @return 日志器配置详情
     */
    @GetMapping("/nodes/{nodeUrl}/loggers/{loggerName}")
    @Operation(summary = "获取日志器详细配置", description = "获取指定节点的特定日志器的详细配置信息")
    @ApiOperation(value = "获取日志器详细配置", notes = "获取指定节点的特定日志器的详细配置信息")
    public ReturnResult<NodeLoggerConfigDTO> getLoggerConfig(
            @PathVariable("nodeUrl") @Parameter(description = "节点URL（Base64编码）", required = true) @ApiParam(value = "节点URL（Base64编码）", required = true) @NotBlank(message = "节点URL不能为空") String nodeUrl,

            @PathVariable("loggerName") @Parameter(description = "日志器名称", required = true) @ApiParam(value = "日志器名称", required = true) @NotBlank(message = "日志器名称不能为空") String loggerName) {

        try {
            log.info("获取日志器详细配置请求: nodeUrl={}, loggerName={}", nodeUrl, loggerName);

            ReturnResult<NodeLoggerConfigDTO> result = loggerConfigService.getLoggerConfig(nodeUrl, loggerName);

            if (result.isSuccess()) {
                log.info("获取日志器详细配置成功: nodeUrl={}, loggerName={}", nodeUrl, loggerName);
            } else {
                log.warn("获取日志器详细配置失败: nodeUrl={}, loggerName={}, 错误信息={}", nodeUrl, loggerName, result.getMsg());
            }

            return result;

        } catch (Exception e) {
            log.error("获取日志器详细配置异常: nodeUrl={}, loggerName={}", nodeUrl, loggerName, e);
            return ReturnResult.error("获取日志器详细配置失败: " + e.getMessage());
        }
    }

    /**
     * 设置日志器等级
     *
     * @param nodeUrl    节点URL（Base64编码）
     * @param loggerName 日志器名称
     * @param level      日志等级
     * @return 设置结果
     */
    @PostMapping("/nodes/{nodeUrl}/loggers/{loggerName}/level")
    @Operation(summary = "设置日志器等级", description = "设置指定节点的特定日志器的日志等级")
    @ApiOperation(value = "设置日志器等级", notes = "设置指定节点的特定日志器的日志等级")
    public ReturnResult<Boolean> setLoggerLevel(
            @PathVariable("nodeUrl") @Parameter(description = "节点URL（Base64编码）", required = true) @ApiParam(value = "节点URL（Base64编码）", required = true) @NotBlank(message = "节点URL不能为空") String nodeUrl,

            @PathVariable("loggerName") @Parameter(description = "日志器名称", required = true) @ApiParam(value = "日志器名称", required = true) @NotBlank(message = "日志器名称不能为空") String loggerName,

            @RequestParam("level") @Parameter(description = "日志等级（ERROR/WARN/INFO/DEBUG/TRACE，空字符串表示继承）", required = true) @ApiParam(value = "日志等级（ERROR/WARN/INFO/DEBUG/TRACE，空字符串表示继承）", required = true) @NotNull(message = "日志等级不能为null") String level) {

        try {
            log.info("设置日志器等级请求: nodeUrl={}, loggerName={}, level={}", nodeUrl, loggerName, level);

            ReturnResult<Boolean> result = loggerConfigService.setLoggerLevel(nodeUrl, loggerName, level);

            if (result.isSuccess()) {
                log.info("设置日志器等级成功: nodeUrl={}, loggerName={}, level={}", nodeUrl, loggerName, level);
            } else {
                log.warn("设置日志器等级失败: nodeUrl={}, loggerName={}, level={}, 错误信息={}", nodeUrl, loggerName, level,
                        result.getMsg());
            }

            return result;

        } catch (Exception e) {
            log.error("设置日志器等级异常: nodeUrl={}, loggerName={}, level={}", nodeUrl, loggerName, level, e);
            return ReturnResult.error("设置日志器等级失败: " + e.getMessage());
        }
    }

    /**
     * 刷新节点的日志器配置
     *
     * @param nodeUrl 节点URL（Base64编码）
     * @return 刷新后的配置列表
     */
    @PostMapping("/nodes/{nodeUrl}/refresh")
    @Operation(summary = "刷新节点日志器配置", description = "从远程节点重新获取并同步日志器配置")
    @ApiOperation(value = "刷新节点日志器配置", notes = "从远程节点重新获取并同步日志器配置")
    public ReturnResult<List<NodeLoggerConfigDTO>> refreshNodeLoggers(
            @PathVariable("nodeUrl") @Parameter(description = "节点URL（Base64编码）", required = true) @ApiParam(value = "节点URL（Base64编码）", required = true) @NotBlank(message = "节点URL不能为空") String nodeUrl) {

        try {
            log.info("刷新节点日志器配置请求: nodeUrl={}", nodeUrl);

            // refresh 操作实际上就是重新获取最新配置，与 getNodeLoggers 功能相同
            ReturnResult<List<NodeLoggerConfigDTO>> result = loggerConfigService.getNodeLoggers(nodeUrl);

            if (result.isSuccess()) {
                log.info("刷新节点日志器配置成功: nodeUrl={}, 配置数量={}", nodeUrl,
                        result.getData() != null ? result.getData().size() : 0);
            } else {
                log.warn("刷新节点日志器配置失败: nodeUrl={}, 错误信息={}", nodeUrl, result.getMsg());
            }

            return result;

        } catch (Exception e) {
            log.error("刷新节点日志器配置异常: nodeUrl={}", nodeUrl, e);
            return ReturnResult.error("刷新节点日志器配置失败: " + e.getMessage());
        }
    }

    /**
     * 获取所有应用名称列表
     *
     * @return 应用名称列表
     */
    @GetMapping("/applications")
    @Operation(summary = "获取所有应用名称", description = "获取系统中所有应用的名称列表")
    @ApiOperation(value = "获取所有应用名称", notes = "获取系统中所有应用的名称列表")
    public ReturnResult<List<String>> getAllApplicationNames() {

        try {
            log.info("获取所有应用名称请求");

            ReturnResult<List<String>> result = loggerConfigService.getAllApplicationNames();

            if (result.isSuccess()) {
                log.info("获取所有应用名称成功: 应用数量={}", result.getData() != null ? result.getData().size() : 0);
            } else {
                log.warn("获取所有应用名称失败: 错误信息={}", result.getMsg());
            }

            return result;

        } catch (Exception e) {
            log.error("获取所有应用名称异常", e);
            return ReturnResult.error("获取所有应用名称失败: " + e.getMessage());
        }
    }

    /**
     * 获取节点日志器配置统计信息
     *
     * @param nodeUrl 节点URL（Base64编码）
     * @return 统计信息
     */
    @GetMapping("/nodes/{nodeUrl}/stats")
    @Operation(summary = "获取日志器配置统计", description = "获取指定节点的日志器配置统计信息")
    @ApiOperation(value = "获取日志器配置统计", notes = "获取指定节点的日志器配置统计信息")
    public ReturnResult<NodeLoggerStatsDTO> getLoggerConfigStats(
            @PathVariable("nodeUrl") @Parameter(description = "节点URL（Base64编码）", required = true) @ApiParam(value = "节点URL（Base64编码）", required = true) @NotBlank(message = "节点URL不能为空") String nodeUrl) {

        try {
            log.info("获取日志器配置统计请求: nodeUrl={}", nodeUrl);

            ReturnResult<NodeLoggerStatsDTO> result = loggerConfigService.getLoggerConfigStats(nodeUrl);

            if (result.isSuccess()) {
                log.info("获取日志器配置统计成功: nodeUrl={}", nodeUrl);
            } else {
                log.warn("获取日志器配置统计失败: nodeUrl={}, 错误信息={}", nodeUrl, result.getMsg());
            }

            return result;

        } catch (Exception e) {
            log.error("获取日志器配置统计异常: nodeUrl={}", nodeUrl, e);
            return ReturnResult.error("获取日志器配置统计失败: " + e.getMessage());
        }
    }

    /**
     * 重置日志器配置（恢复为继承状态）
     *
     * @param nodeUrl    节点URL（Base64编码）
     * @param loggerName 日志器名称
     * @return 重置结果
     */
    @DeleteMapping("/nodes/{nodeUrl}/loggers/{loggerName}/level")
    @Operation(summary = "重置日志器等级", description = "重置指定节点的特定日志器等级为继承状态")
    @ApiOperation(value = "重置日志器等级", notes = "重置指定节点的特定日志器等级为继承状态")
    public ReturnResult<Boolean> resetLoggerLevel(
            @PathVariable("nodeUrl") @Parameter(description = "节点URL（Base64编码）", required = true) @ApiParam(value = "节点URL（Base64编码）", required = true) @NotBlank(message = "节点URL不能为空") String nodeUrl,

            @PathVariable("loggerName") @Parameter(description = "日志器名称", required = true) @ApiParam(value = "日志器名称", required = true) @NotBlank(message = "日志器名称不能为空") String loggerName) {

        try {
            log.info("重置日志器等级请求: nodeUrl={}, loggerName={}", nodeUrl, loggerName);

            ReturnResult<Boolean> result = loggerConfigService.resetLoggerLevel(nodeUrl, loggerName);

            if (result.isSuccess()) {
                log.info("重置日志器等级成功: nodeUrl={}, loggerName={}", nodeUrl, loggerName);
            } else {
                log.warn("重置日志器等级失败: nodeUrl={}, loggerName={}, 错误信息={}", nodeUrl, loggerName, result.getMsg());
            }

            return result;

        } catch (Exception e) {
            log.error("重置日志器等级异常: nodeUrl={}, loggerName={}", nodeUrl, loggerName, e);
            return ReturnResult.error("重置日志器等级失败: " + e.getMessage());
        }
    }

    /**
     * 检查节点是否在线并可访问
     *
     * @param nodeUrl 节点URL（Base64编码）
     * @return 节点状态
     */
    @GetMapping("/nodes/{nodeUrl}/status")
    @Operation(summary = "检查节点状态", description = "检查指定节点是否在线并可访问")
    @ApiOperation(value = "检查节点状态", notes = "检查指定节点是否在线并可访问")
    public ReturnResult<Boolean> checkNodeStatus(
            @PathVariable("nodeUrl") @Parameter(description = "节点URL（Base64编码）", required = true) @ApiParam(value = "节点URL（Base64编码）", required = true) @NotBlank(message = "节点URL不能为空") String nodeUrl) {

        try {
            log.info("检查节点状态请求: nodeUrl={}", nodeUrl);

            ReturnResult<Boolean> result = loggerConfigService.checkNodeStatus(nodeUrl);

            if (result.isSuccess()) {
                log.info("检查节点状态成功: nodeUrl={}, 在线状态={}", nodeUrl, result.getData());
            } else {
                log.warn("检查节点状态失败: nodeUrl={}, 错误信息={}", nodeUrl, result.getMsg());
            }

            return result;

        } catch (Exception e) {
            log.error("检查节点状态异常: nodeUrl={}", nodeUrl, e);
            return ReturnResult.error("检查节点状态失败: " + e.getMessage());
        }
    }

    /**
     * 批量设置应用的所有节点日志等级
     *
     * @param applicationName 应用名称
     * @param loggerName      日志器名称
     * @param level           日志等级
     * @return 批量设置结果
     */
    @PostMapping("/applications/{applicationName}/loggers/{loggerName}/level")
    @Operation(summary = "批量设置应用日志等级", description = "批量设置指定应用的所有节点的特定日志器等级")
    @ApiOperation(value = "批量设置应用日志等级", notes = "批量设置指定应用的所有节点的特定日志器等级")
    public ReturnResult<BatchOperationResultDTO> setLoggerLevelForAllNodes(
            @PathVariable("applicationName") @Parameter(description = "应用名称", required = true) @ApiParam(value = "应用名称", required = true) @NotBlank(message = "应用名称不能为空") String applicationName,

            @PathVariable("loggerName") @Parameter(description = "日志器名称", required = true) @ApiParam(value = "日志器名称", required = true) @NotBlank(message = "日志器名称不能为空") String loggerName,

            @RequestParam("level") @Parameter(description = "日志等级", required = true) @ApiParam(value = "日志等级", required = true) @NotNull(message = "日志等级不能为null") String level) {

        try {
            log.info("批量设置应用日志等级请求: applicationName={}, loggerName={}, level={}", applicationName, loggerName, level);

            ReturnResult<BatchOperationResultDTO> result = loggerConfigService
                    .setLoggerLevelForAllNodes(applicationName, loggerName, level);

            if (result.isSuccess()) {
                log.info("批量设置应用日志等级成功: applicationName={}, loggerName={}, level={}", applicationName, loggerName,
                        level);
            } else {
                log.warn("批量设置应用日志等级失败: applicationName={}, loggerName={}, level={}, 错误信息={}", applicationName,
                        loggerName, level, result.getMsg());
            }

            return result;

        } catch (Exception e) {
            log.error("批量设置应用日志等级异常: applicationName={}, loggerName={}, level={}", applicationName, loggerName, level,
                    e);
            return ReturnResult.error("批量设置应用日志等级失败: " + e.getMessage());
        }
    }

    /**
     * 批量重置应用的所有节点日志器配置
     *
     * @param applicationName 应用名称
     * @param loggerName      日志器名称
     * @return 批量重置结果
     */
    @DeleteMapping("/applications/{applicationName}/loggers/{loggerName}/level")
    @Operation(summary = "批量重置应用日志等级", description = "批量重置指定应用的所有节点的特定日志器等级为继承状态")
    @ApiOperation(value = "批量重置应用日志等级", notes = "批量重置指定应用的所有节点的特定日志器等级为继承状态")
    public ReturnResult<BatchOperationResultDTO> resetLoggerLevelForAllNodes(
            @PathVariable("applicationName") @Parameter(description = "应用名称", required = true) @ApiParam(value = "应用名称", required = true) @NotBlank(message = "应用名称不能为空") String applicationName,

            @PathVariable("loggerName") @Parameter(description = "日志器名称", required = true) @ApiParam(value = "日志器名称", required = true) @NotBlank(message = "日志器名称不能为空") String loggerName) {

        try {
            log.info("批量重置应用日志等级请求: applicationName={}, loggerName={}", applicationName, loggerName);

            ReturnResult<BatchOperationResultDTO> result = loggerConfigService
                    .resetLoggerLevelForAllNodes(applicationName, loggerName);

            if (result.isSuccess()) {
                log.info("批量重置应用日志等级成功: applicationName={}, loggerName={}", applicationName, loggerName);
            } else {
                log.warn("批量重置应用日志等级失败: applicationName={}, loggerName={}, 错误信息={}", applicationName, loggerName,
                        result.getMsg());
            }

            return result;

        } catch (Exception e) {
            log.error("批量重置应用日志等级异常: applicationName={}, loggerName={}", applicationName, loggerName, e);
            return ReturnResult.error("批量重置应用日志等级失败: " + e.getMessage());
        }
    }

    /**
     * 获取指定应用的所有节点URL
     *
     * @param applicationName 应用名称
     * @return 节点URL列表
     */
    @GetMapping("/applications/{applicationName}/nodes")
    @Operation(summary = "获取应用节点列表", description = "获取指定应用的所有节点URL列表")
    @ApiOperation(value = "获取应用节点列表", notes = "获取指定应用的所有节点URL列表")
    public ReturnResult<List<String>> getNodesByApplicationName(
            @PathVariable("applicationName") @Parameter(description = "应用名称", required = true) @ApiParam(value = "应用名称", required = true) @NotBlank(message = "应用名称不能为空") String applicationName) {

        try {
            log.info("获取应用节点列表请求: applicationName={}", applicationName);

            ReturnResult<List<String>> result = loggerConfigService.getNodesByApplicationName(applicationName);

            if (result.isSuccess()) {
                log.info("获取应用节点列表成功: applicationName={}, 节点数量={}", applicationName,
                        result.getData() != null ? result.getData().size() : 0);
            } else {
                log.warn("获取应用节点列表失败: applicationName={}, 错误信息={}", applicationName, result.getMsg());
            }

            return result;

        } catch (Exception e) {
            log.error("获取应用节点列表异常: applicationName={}", applicationName, e);
            return ReturnResult.error("获取应用节点列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取远程节点的原始日志器配置（用于调试）
     *
     * @param nodeUrl 节点URL（Base64编码）
     * @return 远程日志器配置原始数据
     */
    @GetMapping("/nodes/{nodeUrl}/remote-loggers")
    @Operation(summary = "获取远程日志器配置", description = "直接从远程节点获取原始的日志器配置数据（用于调试）")
    @ApiOperation(value = "获取远程日志器配置", notes = "直接从远程节点获取原始的日志器配置数据（用于调试）")
    public ReturnResult<List<NodeLoggerConfigDTO>> fetchRemoteLoggers(
            @PathVariable("nodeUrl") @Parameter(description = "节点URL（Base64编码）", required = true) @ApiParam(value = "节点URL（Base64编码）", required = true) @NotBlank(message = "节点URL不能为空") String nodeUrl) {

        try {
            log.info("获取远程日志器配置请求: nodeUrl={}", nodeUrl);

            ReturnResult<List<NodeLoggerConfigDTO>> result = loggerConfigService.fetchRemoteLoggers(nodeUrl);

            if (result.isSuccess()) {
                log.info("获取远程日志器配置成功: nodeUrl={}, 配置数量={}", nodeUrl,
                        result.getData() != null ? result.getData().size() : 0);
            } else {
                log.warn("获取远程日志器配置失败: nodeUrl={}, 错误信息={}", nodeUrl, result.getMsg());
            }

            return result;

        } catch (Exception e) {
            log.error("获取远程日志器配置异常: nodeUrl={}", nodeUrl, e);
            return ReturnResult.error("获取远程日志器配置失败: " + e.getMessage());
        }
    }

    /**
     * 解码节点URL（用于调试）
     *
     * @param encodedNodeUrl Base64编码的节点URL
     * @return 解码后的节点URL
     */
    @GetMapping("/utils/decode-url/{encodedNodeUrl}")
    @Operation(summary = "解码节点URL", description = "将Base64编码的节点URL解码为可读格式（用于调试）")
    @ApiOperation(value = "解码节点URL", notes = "将Base64编码的节点URL解码为可读格式（用于调试）")
    public ReturnResult<String> decodeNodeUrl(
            @PathVariable("encodedNodeUrl") @Parameter(description = "Base64编码的节点URL", required = true) @ApiParam(value = "Base64编码的节点URL", required = true) @NotBlank(message = "编码的节点URL不能为空") String encodedNodeUrl) {

        try {
            log.info("解码节点URL请求: encodedNodeUrl={}", encodedNodeUrl);

            String decodedUrl = loggerConfigService.decodeNodeUrl(encodedNodeUrl);

            log.info("解码节点URL成功: {} -> {}", encodedNodeUrl, decodedUrl);
            return ReturnResult.success(decodedUrl);

        } catch (Exception e) {
            log.error("解码节点URL异常: encodedNodeUrl={}", encodedNodeUrl, e);
            return ReturnResult.error("解码节点URL失败: " + e.getMessage());
        }
    }
}
