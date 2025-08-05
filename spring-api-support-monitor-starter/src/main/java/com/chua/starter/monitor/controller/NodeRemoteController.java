package com.chua.starter.monitor.controller;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.pojo.RemoteOperationRequest;
import com.chua.starter.monitor.pojo.RemoteOperationResponse;
import com.chua.starter.monitor.service.NodeRemoteService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 节点远程操作控制器
 *
 * @author CH
 * @since 2024/12/30
 */
@Slf4j
@RestController
@RequestMapping("/api/node-remote")
@RequiredArgsConstructor
@Api(tags = "节点远程操作管理")
@Tag(name = "节点远程操作管理", description = "提供节点远程操作相关的API接口")
public class NodeRemoteController {

    private final NodeRemoteService nodeRemoteService;

    /**
     * 执行远程操作
     */
    @PostMapping("/execute")
    @ApiOperation(value = "执行远程操作", notes = "在指定节点上执行远程操作")
    @Operation(summary = "执行远程操作", description = "在指定节点上执行远程操作")
    public ReturnResult<RemoteOperationResponse> executeRemoteOperation(
            @Valid @RequestBody RemoteOperationRequest request) {
        try {
            log.info("执行远程操作: nodeId={}, operationType={}", request.getNodeId(), request.getOperationType());
            
            RemoteOperationResponse response = nodeRemoteService.executeRemoteOperation(request);
            return ReturnResult.ok(response);
            
        } catch (Exception e) {
            log.error("执行远程操作失败", e);
            return ReturnResult.error("执行远程操作失败: " + e.getMessage());
        }
    }

    /**
     * 异步执行远程操作
     */
    @PostMapping("/execute-async")
    @ApiOperation(value = "异步执行远程操作", notes = "在指定节点上异步执行远程操作")
    @Operation(summary = "异步执行远程操作", description = "在指定节点上异步执行远程操作")
    public ReturnResult<String> executeRemoteOperationAsync(
            @Valid @RequestBody RemoteOperationRequest request) {
        try {
            log.info("异步执行远程操作: nodeId={}, operationType={}", request.getNodeId(), request.getOperationType());
            
            CompletableFuture<RemoteOperationResponse> future = nodeRemoteService.executeRemoteOperationAsync(request);
            
            // 返回操作ID，客户端可以通过操作ID查询状态
            return ReturnResult.ok("操作已提交，请通过操作ID查询状态");
            
        } catch (Exception e) {
            log.error("异步执行远程操作失败", e);
            return ReturnResult.error("异步执行远程操作失败: " + e.getMessage());
        }
    }

    /**
     * 上传文件到远程节点
     */
    @PostMapping("/upload-file")
    @ApiOperation(value = "上传文件", notes = "上传文件到指定节点")
    @Operation(summary = "上传文件", description = "上传文件到指定节点")
    public ReturnResult<RemoteOperationResponse> uploadFile(
            @ApiParam(value = "节点ID", required = true) @RequestParam String nodeId,
            @ApiParam(value = "远程文件路径", required = true) @RequestParam String remoteFilePath,
            @ApiParam(value = "是否覆盖已存在文件") @RequestParam(defaultValue = "false") boolean overwrite,
            @ApiParam(value = "上传的文件", required = true) @RequestParam("file") MultipartFile file) {
        try {
            log.info("上传文件到节点: nodeId={}, remoteFilePath={}, fileName={}", 
                    nodeId, remoteFilePath, file.getOriginalFilename());
            
            RemoteOperationResponse response = nodeRemoteService.uploadFile(nodeId, file, remoteFilePath, overwrite);
            return ReturnResult.ok(response);
            
        } catch (Exception e) {
            log.error("上传文件失败", e);
            return ReturnResult.error("上传文件失败: " + e.getMessage());
        }
    }

    /**
     * 从远程节点下载文件
     */
    @PostMapping("/download-file")
    @ApiOperation(value = "下载文件", notes = "从指定节点下载文件")
    @Operation(summary = "下载文件", description = "从指定节点下载文件")
    public ReturnResult<RemoteOperationResponse> downloadFile(
            @ApiParam(value = "节点ID", required = true) @RequestParam String nodeId,
            @ApiParam(value = "远程文件路径", required = true) @RequestParam String remoteFilePath,
            @ApiParam(value = "本地保存路径") @RequestParam(required = false) String localSavePath) {
        try {
            log.info("从节点下载文件: nodeId={}, remoteFilePath={}", nodeId, remoteFilePath);
            
            RemoteOperationResponse response = nodeRemoteService.downloadFile(nodeId, remoteFilePath, localSavePath);
            return ReturnResult.ok(response);
            
        } catch (Exception e) {
            log.error("下载文件失败", e);
            return ReturnResult.error("下载文件失败: " + e.getMessage());
        }
    }

    /**
     * 执行脚本
     */
    @PostMapping("/execute-script")
    @ApiOperation(value = "执行脚本", notes = "在指定节点上执行脚本")
    @Operation(summary = "执行脚本", description = "在指定节点上执行脚本")
    public ReturnResult<RemoteOperationResponse> executeScript(
            @ApiParam(value = "节点ID", required = true) @RequestParam String nodeId,
            @ApiParam(value = "脚本内容", required = true) @RequestParam String script,
            @ApiParam(value = "脚本类型") @RequestParam(defaultValue = "SHELL") String scriptType,
            @ApiParam(value = "工作目录") @RequestParam(required = false) String workingDirectory,
            @ApiParam(value = "超时时间（秒）") @RequestParam(defaultValue = "30") int timeout) {
        try {
            log.info("在节点执行脚本: nodeId={}, scriptType={}", nodeId, scriptType);
            
            RemoteOperationResponse response = nodeRemoteService.executeScript(
                    nodeId, script, scriptType, workingDirectory, timeout);
            return ReturnResult.ok(response);
            
        } catch (Exception e) {
            log.error("执行脚本失败", e);
            return ReturnResult.error("执行脚本失败: " + e.getMessage());
        }
    }

    /**
     * 执行命令
     */
    @PostMapping("/execute-command")
    @ApiOperation(value = "执行命令", notes = "在指定节点上执行命令")
    @Operation(summary = "执行命令", description = "在指定节点上执行命令")
    public ReturnResult<RemoteOperationResponse> executeCommand(
            @ApiParam(value = "节点ID", required = true) @RequestParam String nodeId,
            @ApiParam(value = "命令", required = true) @RequestParam String command,
            @ApiParam(value = "工作目录") @RequestParam(required = false) String workingDirectory,
            @ApiParam(value = "超时时间（秒）") @RequestParam(defaultValue = "30") int timeout) {
        try {
            log.info("在节点执行命令: nodeId={}, command={}", nodeId, command);
            
            RemoteOperationResponse response = nodeRemoteService.executeCommand(
                    nodeId, command, workingDirectory, timeout);
            return ReturnResult.ok(response);
            
        } catch (Exception e) {
            log.error("执行命令失败", e);
            return ReturnResult.error("执行命令失败: " + e.getMessage());
        }
    }

    /**
     * 获取系统信息
     */
    @GetMapping("/system-info")
    @ApiOperation(value = "获取系统信息", notes = "获取指定节点的系统信息")
    @Operation(summary = "获取系统信息", description = "获取指定节点的系统信息")
    public ReturnResult<Map<String, Object>> getSystemInfo(
            @ApiParam(value = "节点ID", required = true) @RequestParam String nodeId) {
        try {
            log.info("获取节点系统信息: nodeId={}", nodeId);
            
            Map<String, Object> systemInfo = nodeRemoteService.getSystemInfo(nodeId);
            return ReturnResult.ok(systemInfo);
            
        } catch (Exception e) {
            log.error("获取系统信息失败", e);
            return ReturnResult.error("获取系统信息失败: " + e.getMessage());
        }
    }

    /**
     * 获取文件列表
     */
    @GetMapping("/file-list")
    @ApiOperation(value = "获取文件列表", notes = "获取指定节点目录下的文件列表")
    @Operation(summary = "获取文件列表", description = "获取指定节点目录下的文件列表")
    public ReturnResult<List<Map<String, Object>>> getFileList(
            @ApiParam(value = "节点ID", required = true) @RequestParam String nodeId,
            @ApiParam(value = "目录路径", required = true) @RequestParam String directoryPath,
            @ApiParam(value = "是否包含隐藏文件") @RequestParam(defaultValue = "false") boolean includeHidden) {
        try {
            log.info("获取节点文件列表: nodeId={}, directoryPath={}", nodeId, directoryPath);
            
            List<Map<String, Object>> fileList = nodeRemoteService.getFileList(nodeId, directoryPath, includeHidden);
            return ReturnResult.ok(fileList);
            
        } catch (Exception e) {
            log.error("获取文件列表失败", e);
            return ReturnResult.error("获取文件列表失败: " + e.getMessage());
        }
    }

    /**
     * 创建目录
     */
    @PostMapping("/create-directory")
    @ApiOperation(value = "创建目录", notes = "在指定节点创建目录")
    @Operation(summary = "创建目录", description = "在指定节点创建目录")
    public ReturnResult<RemoteOperationResponse> createDirectory(
            @ApiParam(value = "节点ID", required = true) @RequestParam String nodeId,
            @ApiParam(value = "目录路径", required = true) @RequestParam String directoryPath,
            @ApiParam(value = "是否递归创建") @RequestParam(defaultValue = "false") boolean recursive) {
        try {
            log.info("在节点创建目录: nodeId={}, directoryPath={}", nodeId, directoryPath);
            
            RemoteOperationResponse response = nodeRemoteService.createDirectory(nodeId, directoryPath, recursive);
            return ReturnResult.ok(response);
            
        } catch (Exception e) {
            log.error("创建目录失败", e);
            return ReturnResult.error("创建目录失败: " + e.getMessage());
        }
    }

    /**
     * 删除文件
     */
    @DeleteMapping("/delete-file")
    @ApiOperation(value = "删除文件", notes = "删除指定节点的文件或目录")
    @Operation(summary = "删除文件", description = "删除指定节点的文件或目录")
    public ReturnResult<RemoteOperationResponse> deleteFile(
            @ApiParam(value = "节点ID", required = true) @RequestParam String nodeId,
            @ApiParam(value = "文件路径", required = true) @RequestParam String path,
            @ApiParam(value = "是否递归删除") @RequestParam(defaultValue = "false") boolean recursive) {
        try {
            log.info("删除节点文件: nodeId={}, path={}", nodeId, path);
            
            RemoteOperationResponse response = nodeRemoteService.deleteFile(nodeId, path, recursive);
            return ReturnResult.ok(response);
            
        } catch (Exception e) {
            log.error("删除文件失败", e);
            return ReturnResult.error("删除文件失败: " + e.getMessage());
        }
    }

    /**
     * 检查节点连接状态
     */
    @GetMapping("/check-connection")
    @ApiOperation(value = "检查连接状态", notes = "检查指定节点的连接状态")
    @Operation(summary = "检查连接状态", description = "检查指定节点的连接状态")
    public ReturnResult<Boolean> checkNodeConnection(
            @ApiParam(value = "节点ID", required = true) @RequestParam String nodeId) {
        try {
            log.info("检查节点连接状态: nodeId={}", nodeId);
            
            boolean connected = nodeRemoteService.checkNodeConnection(nodeId);
            return ReturnResult.ok(connected);
            
        } catch (Exception e) {
            log.error("检查节点连接状态失败", e);
            return ReturnResult.error("检查节点连接状态失败: " + e.getMessage());
        }
    }

    /**
     * 获取操作状态
     */
    @GetMapping("/operation-status")
    @ApiOperation(value = "获取操作状态", notes = "获取指定操作的执行状态")
    @Operation(summary = "获取操作状态", description = "获取指定操作的执行状态")
    public ReturnResult<RemoteOperationResponse> getOperationStatus(
            @ApiParam(value = "操作ID", required = true) @RequestParam String operationId) {
        try {
            log.info("获取操作状态: operationId={}", operationId);
            
            RemoteOperationResponse response = nodeRemoteService.getOperationStatus(operationId);
            return ReturnResult.ok(response);
            
        } catch (Exception e) {
            log.error("获取操作状态失败", e);
            return ReturnResult.error("获取操作状态失败: " + e.getMessage());
        }
    }

    /**
     * 获取节点支持的功能列表
     */
    @GetMapping("/supported-features")
    @ApiOperation(value = "获取支持的功能", notes = "获取指定节点支持的功能列表")
    @Operation(summary = "获取支持的功能", description = "获取指定节点支持的功能列表")
    public ReturnResult<List<String>> getSupportedFeatures(
            @ApiParam(value = "节点ID", required = true) @RequestParam String nodeId) {
        try {
            log.info("获取节点支持的功能: nodeId={}", nodeId);
            
            List<String> features = nodeRemoteService.getSupportedFeatures(nodeId);
            return ReturnResult.ok(features);
            
        } catch (Exception e) {
            log.error("获取节点支持的功能失败", e);
            return ReturnResult.error("获取节点支持的功能失败: " + e.getMessage());
        }
    }
}
