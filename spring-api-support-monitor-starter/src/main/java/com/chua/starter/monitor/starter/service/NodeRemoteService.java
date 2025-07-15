package com.chua.starter.monitor.starter.service;

import com.chua.starter.monitor.starter.pojo.RemoteOperationRequest;
import com.chua.starter.monitor.starter.pojo.RemoteOperationResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 节点远程操作服务接口
 *
 * @author CH
 * @since 2024/12/30
 */
public interface NodeRemoteService {

    /**
     * 执行远程操作
     * 
     * @param request 远程操作请求
     * @return 操作响应
     */
    RemoteOperationResponse executeRemoteOperation(RemoteOperationRequest request);

    /**
     * 异步执行远程操作
     * 
     * @param request 远程操作请求
     * @return 异步操作结果
     */
    CompletableFuture<RemoteOperationResponse> executeRemoteOperationAsync(RemoteOperationRequest request);

    /**
     * 上传文件到远程节点
     * 
     * @param nodeId         节点ID
     * @param file           要上传的文件
     * @param remoteFilePath 远程文件路径
     * @param overwrite      是否覆盖已存在文件
     * @return 上传结果
     */
    RemoteOperationResponse uploadFile(String nodeId, MultipartFile file, String remoteFilePath, boolean overwrite);

    /**
     * 上传文件到远程节点（流式）
     * 
     * @param nodeId         节点ID
     * @param inputStream    文件输入流
     * @param fileName       文件名
     * @param remoteFilePath 远程文件路径
     * @param overwrite      是否覆盖已存在文件
     * @return 上传结果
     */
    RemoteOperationResponse uploadFile(String nodeId, InputStream inputStream, String fileName, String remoteFilePath,
            boolean overwrite);

    /**
     * 从远程节点下载文件
     * 
     * @param nodeId         节点ID
     * @param remoteFilePath 远程文件路径
     * @param localSavePath  本地保存路径
     * @return 下载结果
     */
    RemoteOperationResponse downloadFile(String nodeId, String remoteFilePath, String localSavePath);

    /**
     * 获取远程文件内容（小文件）
     * 
     * @param nodeId         节点ID
     * @param remoteFilePath 远程文件路径
     * @return 文件内容
     */
    String getFileContent(String nodeId, String remoteFilePath);

    /**
     * 在远程节点执行脚本
     * 
     * @param nodeId           节点ID
     * @param script           脚本内容
     * @param scriptType       脚本类型
     * @param workingDirectory 工作目录
     * @param timeout          超时时间（秒）
     * @return 执行结果
     */
    RemoteOperationResponse executeScript(String nodeId, String script, String scriptType, String workingDirectory,
            int timeout);

    /**
     * 在远程节点执行命令
     * 
     * @param nodeId           节点ID
     * @param command          命令
     * @param workingDirectory 工作目录
     * @param timeout          超时时间（秒）
     * @return 执行结果
     */
    RemoteOperationResponse executeCommand(String nodeId, String command, String workingDirectory, int timeout);

    /**
     * 获取远程节点系统信息
     * 
     * @param nodeId 节点ID
     * @return 系统信息
     */
    Map<String, Object> getSystemInfo(String nodeId);

    /**
     * 获取远程节点性能监控数据
     * 
     * @param nodeId      节点ID
     * @param monitorType 监控类型
     * @param interval    监控间隔（秒）
     * @param duration    监控持续时间（秒）
     * @return 监控数据
     */
    RemoteOperationResponse getPerformanceData(String nodeId, String monitorType, int interval, int duration);

    /**
     * 获取远程节点进程列表
     * 
     * @param nodeId 节点ID
     * @return 进程列表
     */
    List<Map<String, Object>> getProcessList(String nodeId);

    /**
     * 获取远程节点服务列表
     * 
     * @param nodeId 节点ID
     * @return 服务列表
     */
    List<Map<String, Object>> getServiceList(String nodeId);

    /**
     * 启动远程节点服务
     * 
     * @param nodeId      节点ID
     * @param serviceName 服务名称
     * @return 操作结果
     */
    RemoteOperationResponse startService(String nodeId, String serviceName);

    /**
     * 停止远程节点服务
     * 
     * @param nodeId      节点ID
     * @param serviceName 服务名称
     * @return 操作结果
     */
    RemoteOperationResponse stopService(String nodeId, String serviceName);

    /**
     * 重启远程节点服务
     * 
     * @param nodeId      节点ID
     * @param serviceName 服务名称
     * @return 操作结果
     */
    RemoteOperationResponse restartService(String nodeId, String serviceName);

    /**
     * 创建终端连接
     * 
     * @param nodeId       节点ID
     * @param terminalType 终端类型
     * @param username     用户名
     * @param password     密码
     * @param rows         终端行数
     * @param cols         终端列数
     * @return 连接结果
     */
    RemoteOperationResponse createTerminalConnection(String nodeId, String terminalType, String username,
            String password, int rows, int cols);

    /**
     * 关闭终端连接
     * 
     * @param nodeId    节点ID
     * @param sessionId 会话ID
     * @return 操作结果
     */
    RemoteOperationResponse closeTerminalConnection(String nodeId, String sessionId);

    /**
     * 向终端发送命令
     * 
     * @param nodeId    节点ID
     * @param sessionId 会话ID
     * @param command   命令
     * @return 操作结果
     */
    RemoteOperationResponse sendTerminalCommand(String nodeId, String sessionId, String command);

    /**
     * 获取远程节点文件列表
     * 
     * @param nodeId        节点ID
     * @param directoryPath 目录路径
     * @param includeHidden 是否包含隐藏文件
     * @return 文件列表
     */
    List<Map<String, Object>> getFileList(String nodeId, String directoryPath, boolean includeHidden);

    /**
     * 创建远程目录
     * 
     * @param nodeId        节点ID
     * @param directoryPath 目录路径
     * @param recursive     是否递归创建
     * @return 操作结果
     */
    RemoteOperationResponse createDirectory(String nodeId, String directoryPath, boolean recursive);

    /**
     * 删除远程文件或目录
     * 
     * @param nodeId    节点ID
     * @param path      文件或目录路径
     * @param recursive 是否递归删除
     * @return 操作结果
     */
    RemoteOperationResponse deleteFile(String nodeId, String path, boolean recursive);

    /**
     * 移动/重命名远程文件
     * 
     * @param nodeId     节点ID
     * @param sourcePath 源路径
     * @param targetPath 目标路径
     * @return 操作结果
     */
    RemoteOperationResponse moveFile(String nodeId, String sourcePath, String targetPath);

    /**
     * 复制远程文件
     * 
     * @param nodeId     节点ID
     * @param sourcePath 源路径
     * @param targetPath 目标路径
     * @param overwrite  是否覆盖已存在文件
     * @return 操作结果
     */
    RemoteOperationResponse copyFile(String nodeId, String sourcePath, String targetPath, boolean overwrite);

    /**
     * 获取文件属性
     * 
     * @param nodeId   节点ID
     * @param filePath 文件路径
     * @return 文件属性
     */
    Map<String, Object> getFileAttributes(String nodeId, String filePath);

    /**
     * 设置文件权限
     * 
     * @param nodeId      节点ID
     * @param filePath    文件路径
     * @param permissions 权限
     * @return 操作结果
     */
    RemoteOperationResponse setFilePermissions(String nodeId, String filePath, String permissions);

    /**
     * 检查节点连接状态
     * 
     * @param nodeId 节点ID
     * @return 连接状态
     */
    boolean checkNodeConnection(String nodeId);

    /**
     * 获取操作状态
     * 
     * @param operationId 操作ID
     * @return 操作状态
     */
    RemoteOperationResponse getOperationStatus(String operationId);

    /**
     * 取消操作
     * 
     * @param operationId 操作ID
     * @return 取消结果
     */
    boolean cancelOperation(String operationId);

    /**
     * 获取节点支持的功能列表
     *
     * @param nodeId 节点ID
     * @return 功能列表
     */
    List<String> getSupportedFeatures(String nodeId);
}
