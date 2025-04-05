package com.chua.starter.monitor.service.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.socketio.support.session.SocketSessionTemplate;
import com.chua.starter.monitor.client.SshClientManager;
import com.chua.starter.monitor.entity.MaintenanceHost;
import com.chua.starter.monitor.entity.MaintenanceScript;
import com.chua.starter.monitor.mapper.MaintenanceHostMapper;
import com.chua.starter.monitor.mapper.MaintenanceScriptMapper;
import com.chua.starter.monitor.pojo.request.MaintenanceLogRequest;
import com.chua.starter.monitor.pojo.request.MaintenanceScriptRequest;
import com.chua.starter.monitor.pojo.request.RunScriptRequest;
import com.chua.starter.monitor.pojo.response.MaintenanceScriptResponse;
import com.chua.starter.monitor.pojo.response.RunScriptResponse;
import com.chua.starter.monitor.service.MaintenanceLogService;
import com.chua.starter.monitor.service.MaintenanceScriptService;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MaintenanceScriptServiceImpl implements MaintenanceScriptService {

    private static final Logger logger = LoggerFactory.getLogger(MaintenanceScriptServiceImpl.class);

    private final MaintenanceScriptMapper maintenanceScriptMapper;
    private final MaintenanceHostMapper maintenanceHostMapper;
    private final SshClientManager sshClientManager;
    private final MaintenanceLogService maintenanceLogService;
    private final SocketSessionTemplate socketSessionTemplate;
    @Override
    public ReturnResult<MaintenanceScriptResponse> createMaintenanceScript(MaintenanceScriptRequest request) {
        try {
            if (request.getMaintenanceGroupId() == null) {
                return ReturnResult.error("维护组ID不能为空");
            }
            
            MaintenanceScript maintenanceScript = new MaintenanceScript();
            BeanUtils.copyProperties(request, maintenanceScript);

            maintenanceScript.setMaintenanceScriptCreateTime(LocalDateTime.now());
            maintenanceScript.setMaintenanceScriptUpdateTime(LocalDateTime.now());

            maintenanceScriptMapper.insert(maintenanceScript);

            MaintenanceScriptResponse response = new MaintenanceScriptResponse();
            BeanUtils.copyProperties(maintenanceScript, response);
            
            // 记录操作日志
            logOperation(maintenanceScript, 1, "创建维护脚本", true, null);
            
            // 不再自动上传脚本到远程主机
            
            return ReturnResult.success(response);
        } catch (Exception e) {
            logger.error("创建维护脚本失败", e);
            return ReturnResult.error("创建维护脚本失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<MaintenanceScriptResponse> updateMaintenanceScript(MaintenanceScriptRequest request) {
        try {
            MaintenanceScript maintenanceScript = maintenanceScriptMapper.selectById(request.getMaintenanceScriptId());
            if (maintenanceScript == null) {
                return ReturnResult.error("维护脚本不存在");
            }

            BeanUtils.copyProperties(request, maintenanceScript);
            maintenanceScript.setMaintenanceScriptUpdateTime(LocalDateTime.now());

            maintenanceScriptMapper.updateById(maintenanceScript);

            MaintenanceScriptResponse response = new MaintenanceScriptResponse();
            BeanUtils.copyProperties(maintenanceScript, response);
            
            // 记录操作日志
            logOperation(maintenanceScript, 2, "更新维护脚本", true, null);
            
            // 不再自动更新远程主机上的脚本
            
            return ReturnResult.success(response);
        } catch (Exception e) {
            logger.error("更新维护脚本失败", e);
            return ReturnResult.error("更新维护脚本失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Boolean> deleteMaintenanceScript(Long maintenanceScriptId) {
        try {
            MaintenanceScript maintenanceScript = maintenanceScriptMapper.selectById(maintenanceScriptId);
            if (maintenanceScript == null) {
                return ReturnResult.error("维护脚本不存在");
            }
            
            // 记录脚本信息，用于后续删除主机上的脚本
            String scriptPath = maintenanceScript.getMaintenanceScriptPath();
            Long groupId = maintenanceScript.getMaintenanceGroupId();
            String scriptName = maintenanceScript.getMaintenanceScriptName();
            
            // 记录操作日志
            logOperation(maintenanceScript, 3, "删除维护脚本", true, null);
            
            maintenanceScriptMapper.deleteById(maintenanceScriptId);
            
            // 使用Socket会话通知前端开始删除远程主机上的脚本
            String eventKey = "event-maintenance-script-" + groupId;
            socketSessionTemplate.send(eventKey, "开始从远程主机删除脚本 [" + scriptName + "]");
            
            // 异步删除维护组下所有主机上的脚本
            CompletableFuture.runAsync(() -> {
                try {
                    deleteScriptFromHosts(groupId, scriptPath);
                    socketSessionTemplate.send(eventKey, "脚本 [" + scriptName + "] 已从所有远程主机删除");
                } catch (Exception e) {
                    logger.error("自动删除主机上的脚本失败: {}", e.getMessage(), e);
                    socketSessionTemplate.send(eventKey, "从远程主机删除脚本 [" + scriptName + "] 失败: " + e.getMessage());
                }
            });
            
            return ReturnResult.success(true);
        } catch (Exception e) {
            logger.error("删除维护脚本失败", e);
            return ReturnResult.error("删除维护脚本失败: " + e.getMessage());
        }
    }

    /**
     * 同步脚本到维护组下所有启用的主机
     *
     * @param script 脚本信息
     * @param isOverwrite 是否覆盖
     */
    private void syncScriptToHosts(MaintenanceScript script, boolean isOverwrite) {
        // 获取维护组下所有启用的主机
        LambdaQueryWrapper<MaintenanceHost> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MaintenanceHost::getMaintenanceGroupId, script.getMaintenanceGroupId());
        queryWrapper.eq(MaintenanceHost::getMaintenanceHostEnabled, true);
        List<MaintenanceHost> hosts = maintenanceHostMapper.selectList(queryWrapper);
        
        if (hosts.isEmpty()) {
            logger.warn("维护组 [{}] 下没有启用的主机，脚本同步操作已跳过", script.getMaintenanceGroupId());
            socketSessionTemplate.send("event-maintenance-script-" + script.getMaintenanceGroupId(), "同步脚本失败，维护组下没有启用的主机");
            return;
        }
        
        String scriptContent = script.getMaintenanceScriptContent();
        String scriptPath = script.getMaintenanceScriptPath();
        
        // 为每个主机上传脚本
        List<CompletableFuture<Void>> futures = hosts.stream()
            .map(host -> CompletableFuture.runAsync(() -> {
                try {
                    socketSessionTemplate.send("event-maintenance-script-" + script.getMaintenanceGroupId(), "正在将脚本 [" + scriptPath + "] 上传到主机 [" + host.getMaintenanceHostAddress() + "]");
                    uploadScriptToHost(host, scriptContent, scriptPath, isOverwrite);
                    socketSessionTemplate.send("event-maintenance-script-" + script.getMaintenanceGroupId(), "已成功将脚本 [" + scriptPath + "] 上传到主机 [" + host.getMaintenanceHostAddress() + "]");
                    logger.info("已成功将脚本 [{}] {} 到主机 [{}]", 
                        scriptPath, 
                        isOverwrite ? "更新" : "上传", 
                        host.getMaintenanceHostAddress());
                } catch (Exception e) {
                    socketSessionTemplate.send("event-maintenance-script-" + script.getMaintenanceGroupId(), "将脚本 [" + scriptPath + "] 上传到主机 [" + host.getMaintenanceHostAddress() + "] 失败: " + e.getMessage());
                    logger.error("将脚本 [{}] {} 到主机 [{}] 失败: {}", 
                        scriptPath, 
                        isOverwrite ? "更新" : "上传", 
                        host.getMaintenanceHostAddress(), 
                        e.getMessage(), e);
                }
            }))
                .toList();
        
        // 等待所有上传任务完成
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        
        logger.info("脚本 [{}] 已同步到维护组 [{}] 下的所有主机", scriptPath, script.getMaintenanceGroupId());
    }
    
    /**
     * 从维护组下所有主机删除脚本
     *
     * @param groupId 维护组ID
     * @param scriptPath 脚本路径
     */
    private void deleteScriptFromHosts(Long groupId, String scriptPath) {
        // 获取维护组下所有启用的主机
        LambdaQueryWrapper<MaintenanceHost> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MaintenanceHost::getMaintenanceGroupId, groupId);
        queryWrapper.eq(MaintenanceHost::getMaintenanceHostEnabled, true);
        List<MaintenanceHost> hosts = maintenanceHostMapper.selectList(queryWrapper);
        
        if (hosts.isEmpty()) {
            logger.warn("维护组 [{}] 下没有启用的主机，脚本删除操作已跳过", groupId);
            return;
        }
        
        // 为每个主机删除脚本
        List<CompletableFuture<Void>> futures = hosts.stream()
            .map(host -> CompletableFuture.runAsync(() -> {
                try {
                    deleteScriptFromHost(host, scriptPath);
                    logger.info("已成功从主机 [{}] 删除脚本 [{}]", host.getMaintenanceHostAddress(), scriptPath);
                } catch (Exception e) {
                    logger.error("从主机 [{}] 删除脚本 [{}] 失败: {}", 
                        host.getMaintenanceHostAddress(), scriptPath, e.getMessage(), e);
                }
            }))
            .collect(Collectors.toList());
        
        // 等待所有删除任务完成
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        
        logger.info("脚本 [{}] 已从维护组 [{}] 下的所有主机删除", scriptPath, groupId);
    }
    
    /**
     * 上传脚本到特定主机
     *
     * @param host 维护主机
     * @param scriptContent 脚本内容
     * @param scriptPath 脚本路径
     * @param isOverwrite 是否覆盖
     */
    private void uploadScriptToHost(MaintenanceHost host, String scriptContent, String scriptPath, boolean isOverwrite) 
        throws JSchException, IOException, SftpException {
        ChannelSftp sftp = null;
        
        try {
            socketSessionTemplate.send("event-maintenance-script-" + host.getMaintenanceGroupId(), "正在获取主机：" + host.getMaintenanceHostName());
            // 获取SFTP通道
            sftp = sshClientManager.createSftpChannel(host);
            
            // 创建目录（如果需要）
            try {
                socketSessionTemplate.send("event-maintenance-script-" + host.getMaintenanceGroupId(), "正在创建目录：" + scriptPath);
                String directory = scriptPath.substring(0, scriptPath.lastIndexOf("/"));
                socketSessionTemplate.send("event-maintenance-script-" + host.getMaintenanceGroupId(), "创建目录：" + directory);
                createDirectoryIfNotExists(sftp, directory);
            } catch (Exception e) {
                logger.warn("创建脚本目录失败，可能目录已存在: {}", e.getMessage());
                socketSessionTemplate.send("event-maintenance-script-" + host.getMaintenanceGroupId(), "创建目录失败：" + e.getMessage());
            }
            
            // 检查文件是否存在
            boolean fileExists = false;
            try {
                socketSessionTemplate.send("event-maintenance-script-" + host.getMaintenanceGroupId(), "正在检查文件是否存在：" + scriptPath);
                sftp.stat(scriptPath);
                socketSessionTemplate.send("event-maintenance-script-" + host.getMaintenanceGroupId(), "文件已存在：" + scriptPath);
                fileExists = true;
                socketSessionTemplate.send("event-maintenance-script-" + host.getMaintenanceGroupId(), "文件已存在：" + scriptPath);
            } catch (SftpException e) {
                if (e.id != ChannelSftp.SSH_FX_NO_SUCH_FILE) {
                    logger.warn("无法检查文件是否存在: {}", e.getMessage());
                    throw e;
                }
            }
            
            // 如果文件存在且不允许覆盖，则跳过
            if (fileExists && !isOverwrite) {
                socketSessionTemplate.send("event-maintenance-script-" + host.getMaintenanceGroupId(), "文件已存在且不允许覆盖，已跳过上传");
                logger.info("主机 [{}] 上的脚本 [{}] 已存在且不允许覆盖，已跳过上传", host.getMaintenanceHostAddress(), scriptPath);
                return;
            }
            
            // 上传脚本
            socketSessionTemplate.send("event-maintenance-script-" + host.getMaintenanceGroupId(), "正在上传脚本：" + scriptPath);
            try (ByteArrayInputStream inputStream = new ByteArrayInputStream(scriptContent.getBytes())) {
                sftp.put(inputStream, scriptPath);
            }

            socketSessionTemplate.send("event-maintenance-script-" + host.getMaintenanceGroupId(), "脚本上传成功：" + scriptPath);
            // 设置执行权限
            try {
                socketSessionTemplate.send("event-maintenance-script-" + host.getMaintenanceGroupId(), "正在设置脚本执行权限：" + scriptPath);
                sftp.chmod(0755, scriptPath);
            } catch (SftpException e) {
                logger.warn("无法设置脚本执行权限: {}", e.getMessage());
                socketSessionTemplate.send("event-maintenance-script-" + host.getMaintenanceGroupId(), "设置脚本执行权限失败：" + e.getMessage());
            }
            
        } finally {
            if (sftp != null) {
                sftp.disconnect();
                socketSessionTemplate.send("event-maintenance-script-" + host.getMaintenanceGroupId(), "SFTP通道已断开");
            }
        }
    }
    
    /**
     * 从特定主机删除脚本
     *
     * @param host 维护主机
     * @param scriptPath 脚本路径
     */
    private void deleteScriptFromHost(MaintenanceHost host, String scriptPath) 
        throws JSchException, IOException, SftpException {
        ChannelSftp sftp = null;
        
        try {
            // 获取SFTP通道
            sftp = sshClientManager.createSftpChannel(host);
            
            // 检查文件是否存在
            try {
                sftp.stat(scriptPath);
                // 删除文件
                sftp.rm(scriptPath);
            } catch (SftpException e) {
                if (e.id == ChannelSftp.SSH_FX_NO_SUCH_FILE) {
                    logger.info("主机 [{}] 上的脚本 [{}] 不存在，无需删除", host.getMaintenanceHostAddress(), scriptPath);
                } else {
                    throw e;
                }
            }
            
        } finally {
            if (sftp != null) {
                sftp.disconnect();
            }
        }
    }
    
    /**
     * 如果目录不存在则创建目录
     *
     * @param sftp SFTP通道
     * @param directory 目录路径
     */
    private void createDirectoryIfNotExists(ChannelSftp sftp, String directory) throws SftpException {
        if (directory == null || directory.isEmpty() || directory.equals("/")) {
            return;
        }
        
        String[] folders = directory.split("/");
        String path = "";
        
        for (String folder : folders) {
            if (folder.isEmpty()) {
                path += "/";
                continue;
            }
            
            path += folder + "/";
            
            try {
                sftp.stat(path);
            } catch (SftpException e) {
                if (e.id == ChannelSftp.SSH_FX_NO_SUCH_FILE) {
                    sftp.mkdir(path);
                } else {
                    throw e;
                }
            }
        }
    }

    @Override
    public ReturnResult<MaintenanceScriptResponse> getMaintenanceScript(Long maintenanceScriptId) {
        try {
            MaintenanceScript maintenanceScript = maintenanceScriptMapper.selectById(maintenanceScriptId);
            if (maintenanceScript == null) {
                return ReturnResult.error("维护脚本不存在");
            }

            MaintenanceScriptResponse response = new MaintenanceScriptResponse();
            BeanUtils.copyProperties(maintenanceScript, response);

            return ReturnResult.success(response);
        } catch (Exception e) {
            return ReturnResult.error("获取维护脚本失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<List<MaintenanceScriptResponse>> listMaintenanceScripts(Long maintenanceGroupId) {
        try {
            if (maintenanceGroupId == null) {
                return ReturnResult.error("维护组ID不能为空");
            }
            
            LambdaQueryWrapper<MaintenanceScript> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(MaintenanceScript::getMaintenanceGroupId, maintenanceGroupId);

            List<MaintenanceScript> maintenanceScripts = maintenanceScriptMapper.selectList(queryWrapper);

            List<MaintenanceScriptResponse> responses = maintenanceScripts.stream()
                    .map(script -> {
                        MaintenanceScriptResponse response = new MaintenanceScriptResponse();
                        BeanUtils.copyProperties(script, response);
                        return response;
                    })
                    .collect(Collectors.toList());

            return ReturnResult.success(responses);
        } catch (Exception e) {
            return ReturnResult.error("获取维护脚本列表失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Boolean> updateScriptStatus(Long maintenanceScriptId, Integer status) {
        try {
            MaintenanceScript maintenanceScript = maintenanceScriptMapper.selectById(maintenanceScriptId);
            if (maintenanceScript == null) {
                return ReturnResult.error("维护脚本不存在");
            }

            maintenanceScript.setMaintenanceScriptStatus(status);
            maintenanceScript.setMaintenanceScriptUpdateTime(LocalDateTime.now());

            maintenanceScriptMapper.updateById(maintenanceScript);

            return ReturnResult.success(true);
        } catch (Exception e) {
            return ReturnResult.error("更新脚本状态失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<RunScriptResponse> runScript(RunScriptRequest request) {
        try {
            MaintenanceScript script = maintenanceScriptMapper.selectById(request.getMaintenanceScriptId());
            if (script == null) {
                return ReturnResult.error("脚本不存在");
            }

            List<MaintenanceHost> hosts;
            if (request.getMaintenanceHostId() != null) {
                MaintenanceHost host = maintenanceHostMapper.selectById(request.getMaintenanceHostId());
                if (host == null) {
                    return ReturnResult.error("主机不存在");
                }
                hosts = Collections.singletonList(host);
            } else {
                LambdaQueryWrapper<MaintenanceHost> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(MaintenanceHost::getMaintenanceGroupId, request.getMaintenanceGroupId());
                queryWrapper.eq(MaintenanceHost::getMaintenanceHostEnabled, true);
                hosts = maintenanceHostMapper.selectList(queryWrapper);
                if (hosts.isEmpty()) {
                    return ReturnResult.error("维护组下没有可用的主机");
                }
            }

            RunScriptResponse response = new RunScriptResponse();
            Map<String, String> results = new HashMap<>();
            boolean allSuccess = true;

            for (MaintenanceHost host : hosts) {
                try {
                    String result = executeScript(host, script.getMaintenanceScriptContent());
                    results.put(host.getMaintenanceHostAddress(), result);
                } catch (Exception e) {
                    results.put(host.getMaintenanceHostAddress(), "执行失败: " + e.getMessage());
                    allSuccess = false;
                }
            }

            response.setResults(results);
            response.setAllSuccess(allSuccess);

            return ReturnResult.success(response);
        } catch (Exception e) {
            return ReturnResult.error("执行脚本失败: " + e.getMessage());
        }
    }

    /**
     * 在主机上执行脚本
     *
     * @param host 维护主机
     * @param scriptContent 脚本内容
     * @return 执行结果
     * @throws JSchException SSH连接异常
     * @throws IOException IO异常
     */
    private String executeScript(MaintenanceHost host, String scriptContent) throws JSchException, IOException {
        ChannelExec channel = null;

        try {
            // 获取执行通道
            channel = sshClientManager.createExecChannel(host);
            channel.setCommand(scriptContent);

            channel.setInputStream(null);
            InputStream stdout = channel.getInputStream();
            InputStream stderr = channel.getErrStream();

            channel.connect(30000);

            StringBuilder outputBuffer = new StringBuilder();
            byte[] buffer = new byte[1024];
            while (true) {
                while (stdout.available() > 0) {
                    int i = stdout.read(buffer, 0, 1024);
                    if (i < 0) break;
                    outputBuffer.append(new String(buffer, 0, i));
                }
                while (stderr.available() > 0) {
                    int i = stderr.read(buffer, 0, 1024);
                    if (i < 0) break;
                    outputBuffer.append(new String(buffer, 0, i));
                }
                if (channel.isClosed()) {
                    if (stdout.available() > 0 || stderr.available() > 0) continue;
                    break;
                }
                try {
                    Thread.sleep(100);
                } catch (Exception e) {
                    // 忽略
                }
            }

            return outputBuffer.toString();
        } finally {
            if (channel != null) {
                channel.disconnect();
            }
        }
    }

    @Override
    public ReturnResult<Boolean> syncScript(Long maintenanceScriptId) {
        try {
            MaintenanceScript maintenanceScript = maintenanceScriptMapper.selectById(maintenanceScriptId);
            if (maintenanceScript == null) {
                return ReturnResult.error("维护脚本不存在");
            }
            
            // 记录操作日志
            logOperation(maintenanceScript, 4, "同步维护脚本", true, null);
            
            // 使用Socket会话通知前端开始同步
            String eventKey = "event-maintenance-script-" + maintenanceScript.getMaintenanceGroupId();
            socketSessionTemplate.send(eventKey, "开始同步脚本 [" + maintenanceScript.getMaintenanceScriptName() + "]");
            
            // 异步将脚本同步到维护组下所有主机
            CompletableFuture.runAsync(() -> {
                try {
                    syncScriptToHosts(maintenanceScript, true);
                    socketSessionTemplate.send(eventKey, "脚本 [" + maintenanceScript.getMaintenanceScriptName() + "] 同步完成");
                } catch (Exception e) {
                    logger.error("同步脚本到主机失败: {}", e.getMessage(), e);
                    socketSessionTemplate.send(eventKey, "脚本 [" + maintenanceScript.getMaintenanceScriptName() + "] 同步失败: " + e.getMessage());
                }
            });
            
            return ReturnResult.success(true);
        } catch (Exception e) {
            logger.error("同步维护脚本失败", e);
            return ReturnResult.error("同步维护脚本失败: " + e.getMessage());
        }
    }

    /**
     * 记录操作日志
     *
     * @param script         脚本对象
     * @param operationType  操作类型
     * @param operationDesc  操作描述
     * @param result         操作结果
     * @param resultMessage  结果消息
     */
    private void logOperation(MaintenanceScript script, Integer operationType, String operationDesc, Boolean result, String resultMessage) {
        try {
            MaintenanceLogRequest logRequest = new MaintenanceLogRequest();
            logRequest.setMaintenanceGroupId(script.getMaintenanceGroupId());
            logRequest.setMaintenanceScriptId(script.getMaintenanceScriptId());
            logRequest.setOperationType(operationType);
            logRequest.setTargetType(3); // 3表示维护脚本
            
            // 构建操作详情JSON
            Map<String, Object> details = new HashMap<>();
            details.put("maintenanceScriptId", script.getMaintenanceScriptId());
            details.put("maintenanceScriptName", script.getMaintenanceScriptName());
            details.put("maintenanceScriptPath", script.getMaintenanceScriptPath());
            details.put("operationDesc", operationDesc);
            logRequest.setOperationDetails(JSON.toJSONString(details));
            
            // 设置操作结果
            logRequest.setOperationResult(result);
            logRequest.setResultMessage(resultMessage);
            
            // 获取客户端IP
            String ipAddress = "127.0.0.1";
            try {
                ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                if (attributes != null) {
                    HttpServletRequest request = attributes.getRequest();
                    ipAddress = getClientIp(request);
                }
            } catch (Exception e) {
                // 忽略获取IP过程中的异常
            }
            logRequest.setIpAddress(ipAddress);
            
            // 调用日志服务记录日志
            maintenanceLogService.logOperation(logRequest);
        } catch (Exception e) {
            // 记录日志失败不应影响主业务逻辑
            logger.error("记录操作日志失败: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 获取客户端IP地址
     *
     * @param request HTTP请求
     * @return IP地址
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
} 