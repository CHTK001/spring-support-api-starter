package com.chua.starter.monitor.service.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.socketio.support.session.SocketSessionTemplate;
import com.chua.starter.common.support.utils.RequestUtils;
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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.*;
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
                logger.error("删除脚本失败: 脚本 [{}] 不存在", maintenanceScriptId);
                return ReturnResult.error("维护脚本不存在");
            }
            
            // 记录脚本信息，用于后续删除主机上的脚本
            String scriptPath = maintenanceScript.getMaintenanceScriptPath();
            Long groupId = maintenanceScript.getMaintenanceGroupId();
            String scriptName = maintenanceScript.getMaintenanceScriptName();
            
            logger.info("开始删除脚本 [{}]({}) 及其在维护组 [{}] 下所有主机上的副本", 
                scriptName, maintenanceScriptId, groupId);
            
            // 记录操作日志
            logOperation(maintenanceScript, 3, "删除维护脚本", true, null);
            
            maintenanceScriptMapper.deleteById(maintenanceScriptId);
            logger.info("已从数据库删除脚本 [{}]({})", scriptName, maintenanceScriptId);
            
            // 使用Socket会话通知前端开始删除远程主机上的脚本
            String eventKey = "event-maintenance-" + groupId;
            logger.debug("发送脚本删除事件到前端，eventKey=[{}]", eventKey);
            socketSessionTemplate.send(eventKey, "开始从远程主机删除脚本 [" + scriptName + "]");
            
            // 异步删除维护组下所有主机上的脚本
            CompletableFuture.runAsync(() -> {
                try {
                    logger.info("开始异步删除远程主机上的脚本 [{}]", scriptPath);
                    deleteScriptFromHosts(groupId, scriptPath);
                    socketSessionTemplate.send(eventKey, "脚本 [" + scriptName + "] 已从所有远程主机删除");
                    logger.info("脚本 [{}] 已从所有远程主机删除完成", scriptName);
                } catch (Exception e) {
                    logger.error("从远程主机删除脚本 [{}] 失败: {}", scriptName, e.getMessage(), e);
                    socketSessionTemplate.send(eventKey, "从远程主机删除脚本 [" + scriptName + "] 失败: " + e.getMessage());
                }
            });
            
            logger.info("删除脚本 [{}]({}) 的请求已处理完成", scriptName, maintenanceScriptId);
            return ReturnResult.success(true);
        } catch (Exception e) {
            logger.error("删除维护脚本失败: {}", e.getMessage(), e);
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
            String eventKey = "event-maintenance-" + script.getMaintenanceGroupId();
            socketSessionTemplate.send(eventKey, "同步脚本失败，维护组下没有启用的主机");
            return;
        }
        
        String scriptContent = script.getMaintenanceScriptContent();
        String scriptPath = script.getMaintenanceScriptPath();
        String eventKey = "event-maintenance-" + script.getMaintenanceGroupId();
        
        logger.info("开始同步脚本 [{}] 到维护组 [{}] 下的 {} 台主机", 
            scriptPath, script.getMaintenanceGroupId(), hosts.size());
        
        // 为每个主机上传脚本
        List<CompletableFuture<Void>> futures = hosts.stream()
            .map(host -> CompletableFuture.runAsync(() -> {
                try {
                    logger.info("正在将脚本 [{}] {} 到主机 [{}]", 
                        scriptPath, isOverwrite ? "更新" : "上传", host.getMaintenanceHostAddress());
                    
                    socketSessionTemplate.send(eventKey, "正在将脚本 [" + scriptPath + "] 上传到主机 [" + host.getMaintenanceHostAddress() + "]");
                    uploadScriptToHost(host, scriptContent, scriptPath, isOverwrite);
                    
                    socketSessionTemplate.send(eventKey, "已成功将脚本 [" + scriptPath + "] 上传到主机 [" + host.getMaintenanceHostAddress() + "]");
                    logger.info("已成功将脚本 [{}] {} 到主机 [{}]", 
                        scriptPath, 
                        isOverwrite ? "更新" : "上传", 
                        host.getMaintenanceHostAddress());
                } catch (Exception e) {
                    socketSessionTemplate.send(eventKey, "将脚本 [" + scriptPath + "] 上传到主机 [" + host.getMaintenanceHostAddress() + "] 失败: " + e.getMessage());
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
        
        logger.info("脚本 [{}] 已同步到维护组 [{}] 下的所有主机完成", scriptPath, script.getMaintenanceGroupId());
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
            String eventKey = "event-maintenance-" + groupId;
            socketSessionTemplate.send(eventKey, "删除脚本失败，维护组下没有启用的主机");
            return;
        }
        
        String eventKey = "event-maintenance-" + groupId;
        logger.info("开始从维护组 [{}] 下的 {} 台主机删除脚本 [{}]", groupId, hosts.size(), scriptPath);
        
        // 为每个主机删除脚本
        List<CompletableFuture<Void>> futures = hosts.stream()
            .map(host -> CompletableFuture.runAsync(() -> {
                try {
                    logger.info("正在从主机 [{}] 删除脚本 [{}]", host.getMaintenanceHostAddress(), scriptPath);
                    socketSessionTemplate.send(eventKey, "正在从主机 [" + host.getMaintenanceHostAddress() + "] 删除脚本 [" + scriptPath + "]");
                    
                    deleteScriptFromHost(host, scriptPath);
                    
                    logger.info("已成功从主机 [{}] 删除脚本 [{}]", host.getMaintenanceHostAddress(), scriptPath);
                    socketSessionTemplate.send(eventKey, "已成功从主机 [" + host.getMaintenanceHostAddress() + "] 删除脚本 [" + scriptPath + "]");
                } catch (Exception e) {
                    logger.error("从主机 [{}] 删除脚本 [{}] 失败: {}", 
                        host.getMaintenanceHostAddress(), scriptPath, e.getMessage(), e);
                    socketSessionTemplate.send(eventKey, "从主机 [" + host.getMaintenanceHostAddress() + "] 删除脚本 [" + scriptPath + "] 失败: " + e.getMessage());
                }
            }))
            .collect(Collectors.toList());
        
        // 等待所有删除任务完成
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        
        logger.info("脚本 [{}] 已从维护组 [{}] 下的所有主机删除完成", scriptPath, groupId);
    }
    
    /**
     * 上传脚本到特定主机
     *
     * @param host 维护主机
     * @param scriptContent 脚本内容
     * @param scriptPath 脚本路径
     * @param isOverwrite 是否覆盖
     * @throws Exception 上传过程中可能发生的异常
     */
    private void uploadScriptToHost(MaintenanceHost host, String scriptContent, String scriptPath, boolean isOverwrite)
            throws Exception {
        ChannelSftp sftp = null;
        
        try {
            logger.debug("开始连接主机 [{}] 上传脚本 [{}]", 
                host.getMaintenanceHostAddress(), scriptPath);
                
            // 获取SFTP通道
            sftp = sshClientManager.createSftpChannel(host);
            
            // 创建目录（如果不存在）
            String directory = getParentDirectory(scriptPath);
            if (directory != null && !directory.isEmpty()) {
                createParentDirectories(sftp, directory);
                logger.debug("已在主机 [{}] 上创建目录 [{}]", 
                    host.getMaintenanceHostAddress(), directory);
            }
            
            // 检查文件是否存在
            boolean fileExists = false;
            try {
                sftp.stat(scriptPath);
                fileExists = true;
                logger.debug("脚本 [{}] 在主机 [{}] 上已存在", 
                    scriptPath, host.getMaintenanceHostAddress());
            } catch (SftpException e) {
                if (e.id != ChannelSftp.SSH_FX_NO_SUCH_FILE) {
                    throw e;
                }
                logger.debug("脚本 [{}] 在主机 [{}] 上不存在", 
                    scriptPath, host.getMaintenanceHostAddress());
            }
            
            // 如果文件存在但不允许覆盖，则抛出异常
            if (fileExists && !isOverwrite) {
                logger.warn("脚本 [{}] 在主机 [{}] 上已存在，但未设置覆盖选项", 
                    scriptPath, host.getMaintenanceHostAddress());
                throw new Exception("脚本已存在，未设置覆盖选项");
            }
            
            // 设置权限标志
            String chmodCommand = "chmod 755 " + scriptPath;
            
            // 上传文件
            try (InputStream is = new ByteArrayInputStream(scriptContent.getBytes())) {
                logger.debug("开始上传脚本内容到主机 [{}] 路径 [{}]", 
                    host.getMaintenanceHostAddress(), scriptPath);
                sftp.put(is, scriptPath);
                
                // 设置文件权限为可执行
                ChannelExec exec = (ChannelExec) sshClientManager.getSession(host).openChannel("exec");
                logger.debug("设置脚本 [{}] 在主机 [{}] 上的执行权限", 
                    scriptPath, host.getMaintenanceHostAddress());
                exec.setCommand(chmodCommand);
                exec.connect();
                Thread.sleep(100); // 等待命令执行
                exec.disconnect();
            }
            
            logger.info("脚本 [{}] 已成功上传到主机 [{}]", 
                scriptPath, host.getMaintenanceHostAddress());
        } finally {
            // 关闭SFTP通道
            if (sftp != null && sftp.isConnected()) {
                try {
                    sftp.disconnect();
                    logger.debug("已关闭与主机 [{}] 的SFTP连接", host.getMaintenanceHostAddress());
                } catch (Exception e) {
                    logger.error("关闭SFTP连接失败: {}", e.getMessage());
                }
            }
        }
    }
    
    /**
     * 从特定主机上删除脚本
     *
     * @param host 维护主机
     * @param scriptPath 脚本路径
     * @throws Exception 删除过程中可能发生的异常
     */
    private void deleteScriptFromHost(MaintenanceHost host, String scriptPath) throws Exception {
        ChannelSftp sftp = null;
        
        try {
            logger.debug("开始连接主机 [{}] 删除脚本 [{}]", 
                host.getMaintenanceHostAddress(), scriptPath);
                
            // 获取SFTP通道
            sftp = (ChannelSftp) sshClientManager.getSession(host).openChannel("sftp");
            
            // 检查文件是否存在
            boolean fileExists = false;
            try {
                sftp.stat(scriptPath);
                fileExists = true;
                logger.debug("脚本 [{}] 在主机 [{}] 上存在，准备删除", 
                    scriptPath, host.getMaintenanceHostAddress());
            } catch (SftpException e) {
                if (e.id != ChannelSftp.SSH_FX_NO_SUCH_FILE) {
                    throw e;
                }
                logger.warn("脚本 [{}] 在主机 [{}] 上不存在，无需删除", 
                    scriptPath, host.getMaintenanceHostAddress());
                return; // 文件不存在，无需删除
            }
            
            // 删除文件
            if (fileExists) {
                sftp.rm(scriptPath);
                logger.info("脚本 [{}] 已从主机 [{}] 成功删除", 
                    scriptPath, host.getMaintenanceHostAddress());
            }
        } finally {
            // 关闭SFTP通道
            if (sftp != null && sftp.isConnected()) {
                try {
                    sftp.disconnect();
                    logger.debug("已关闭与主机 [{}] 的SFTP连接", host.getMaintenanceHostAddress());
                } catch (Exception e) {
                    logger.error("关闭SFTP连接失败: {}", e.getMessage());
                }
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

            // 生成唯一的任务ID
            String taskId = UUID.randomUUID().toString().replace("-", "");
            
            // 记录操作日志
            logOperation(script, 5, "执行维护脚本", true, "任务ID: " + taskId);
            
            // 使用Socket会话通知前端开始执行脚本
            String eventKey = "event-maintenance-" + script.getMaintenanceGroupId();
            logger.debug("发送脚本执行开始事件到前端，eventKey=[{}], taskId=[{}]", eventKey, taskId);
            socketSessionTemplate.send(eventKey, "开始执行脚本 [" + script.getMaintenanceScriptName() + "]，任务ID: " + taskId);
            socketSessionTemplate.send(taskId, "开始执行脚本 [" + script.getMaintenanceScriptName() + "]，任务ID: " + taskId);

            // 创建响应对象
            RunScriptResponse response = new RunScriptResponse();
            response.setTaskId(taskId);
            response.setStatus("PROCESSING");
            response.setResults(new HashMap<>());
            
            // 异步执行脚本
            CompletableFuture.runAsync(() -> {
            Map<String, String> results = new HashMap<>();
            boolean allSuccess = true;
                int completedHosts = 0;
                int totalHosts = hosts.size();
                
                // 发送任务开始状态
                Map<String, Object> startStatus = new HashMap<>();
                startStatus.put("status", "RUNNING");
                startStatus.put("message", "开始执行脚本 [" + script.getMaintenanceScriptName() + "]");
                startStatus.put("progress", 0);
                startStatus.put("total", totalHosts);
                startStatus.put("completed", 0);
                socketSessionTemplate.send(eventKey, JSON.toJSONString(startStatus));
                socketSessionTemplate.send(taskId, JSON.toJSONString(startStatus));

            for (MaintenanceHost host : hosts) {
                try {
                        // 更新单个主机执行状态
                        Map<String, Object> hostRunning = new HashMap<>();
                        hostRunning.put("hostId", host.getMaintenanceHostId());
                        hostRunning.put("hostAddress", host.getMaintenanceHostAddress());
                        hostRunning.put("status", "RUNNING");
                        hostRunning.put("message", "正在执行脚本...");
                        socketSessionTemplate.send(eventKey, JSON.toJSONString(hostRunning));
                        socketSessionTemplate.send(taskId, JSON.toJSONString(hostRunning));

                        // 执行脚本
                        String result = executeScript(host, script.getMaintenanceScriptContent());
                    results.put(host.getMaintenanceHostAddress(), result);
                        
                        // 更新成功状态
                        Map<String, Object> hostSuccess = new HashMap<>();
                        hostSuccess.put("hostId", host.getMaintenanceHostId());
                        hostSuccess.put("hostAddress", host.getMaintenanceHostAddress());
                        hostSuccess.put("status", "SUCCESS");
                        hostSuccess.put("message", "脚本执行成功");
                        socketSessionTemplate.send(eventKey, JSON.toJSONString(hostSuccess));
                        socketSessionTemplate.send(taskId, JSON.toJSONString(hostSuccess));
                } catch (Exception e) {
                        logger.error("在主机 [{}] 上执行脚本失败: {}", host.getMaintenanceHostAddress(), e.getMessage(), e);
                    results.put(host.getMaintenanceHostAddress(), "执行失败: " + e.getMessage());
                    allSuccess = false;
                        
                        // 更新失败状态
                        Map<String, Object> hostFailed = new HashMap<>();
                        hostFailed.put("hostId", host.getMaintenanceHostId());
                        hostFailed.put("hostAddress", host.getMaintenanceHostAddress());
                        hostFailed.put("status", "FAILED");
                        hostFailed.put("message", "脚本执行失败: " + e.getMessage());
                        socketSessionTemplate.send(eventKey, JSON.toJSONString(hostFailed));
                        socketSessionTemplate.send(taskId, JSON.toJSONString(hostFailed));
                    }
                    
                    // 更新整体进度
                    completedHosts++;
                    int progress = (int) ((completedHosts / (double) totalHosts) * 100);
                    Map<String, Object> progressStatus = new HashMap<>();
                    progressStatus.put("status", "RUNNING");
                    progressStatus.put("progress", progress);
                    progressStatus.put("total", totalHosts);
                    progressStatus.put("completed", completedHosts);
                    socketSessionTemplate.send(eventKey, JSON.toJSONString(progressStatus));
                    socketSessionTemplate.send(taskId, JSON.toJSONString(progressStatus));
                }
                
                // 发送任务完成状态
                String finalStatus = allSuccess ? "SUCCESS" : "COMPLETED_WITH_ERRORS";
                Map<String, Object> completeStatus = new HashMap<>();
                completeStatus.put("status", finalStatus);
                completeStatus.put("message", "脚本 [" + script.getMaintenanceScriptName() + "] 执行" + (allSuccess ? "成功" : "完成，但有错误"));
                completeStatus.put("progress", 100);
                completeStatus.put("total", totalHosts);
                completeStatus.put("completed", completedHosts);
                completeStatus.put("results", results);
                socketSessionTemplate.send(taskId, JSON.toJSONString(completeStatus));
                socketSessionTemplate.send(eventKey, JSON.toJSONString(completeStatus));

                // 发送事件通知
                socketSessionTemplate.send(eventKey, "脚本 [" + script.getMaintenanceScriptName() + "] 执行" + (allSuccess ? "成功" : "完成，但有错误"));
                socketSessionTemplate.send(taskId, "脚本 [" + script.getMaintenanceScriptName() + "] 执行" + (allSuccess ? "成功" : "完成，但有错误"));

                // 记录最终结果
                logOperation(script, 5, "执行维护脚本", allSuccess, "任务执行" + (allSuccess ? "成功" : "完成，但有错误"));
            });

            return ReturnResult.success(response);
        } catch (Exception e) {
            logger.error("执行脚本失败: {}", e.getMessage(), e);
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
                logger.error("同步脚本失败: 脚本 [{}] 不存在", maintenanceScriptId);
                return ReturnResult.error("维护脚本不存在");
            }
            
            logger.info("开始同步脚本 [{}]({}) 到维护组 [{}] 下的所有主机", 
                maintenanceScript.getMaintenanceScriptName(), 
                maintenanceScriptId,
                maintenanceScript.getMaintenanceGroupId());
            
            // 记录操作日志
            logOperation(maintenanceScript, 4, "同步维护脚本", true, null);
            
            // 使用Socket会话通知前端开始同步
            String eventKey = "event-maintenance-" + maintenanceScript.getMaintenanceGroupId();
            logger.debug("发送同步开始事件到前端，eventKey=[{}]", eventKey);
            socketSessionTemplate.send(eventKey, "开始同步脚本 [" + maintenanceScript.getMaintenanceScriptName() + "]");
            
            // 异步将脚本同步到维护组下所有主机
            CompletableFuture.runAsync(() -> {
                try {
                    logger.info("正在执行异步同步脚本任务...");
                    syncScriptToHosts(maintenanceScript, true);
                    socketSessionTemplate.send(eventKey, "脚本 [" + maintenanceScript.getMaintenanceScriptName() + "] 同步完成");
                    logger.info("脚本 [{}] 同步完成", maintenanceScript.getMaintenanceScriptName());
                } catch (Exception e) {
                    logger.error("同步脚本到主机失败: {}", e.getMessage(), e);
                    socketSessionTemplate.send(eventKey, "脚本 [" + maintenanceScript.getMaintenanceScriptName() + "] 同步失败: " + e.getMessage());
                }
            });
            
            logger.info("同步脚本请求已提交，脚本 [{}]({})", 
                maintenanceScript.getMaintenanceScriptName(), 
                maintenanceScriptId);
            return ReturnResult.success(true);
        } catch (Exception e) {
            logger.error("同步维护脚本失败: {}", e.getMessage(), e);
            return ReturnResult.error("同步维护脚本失败: " + e.getMessage());
        }
    }

    /**
     * 记录操作日志
     *
     * @param script 脚本信息
     * @param operationType 操作类型：1-创建，2-更新，3-删除，4-同步
     * @param operationDesc 操作描述
     * @param result 操作结果
     * @param resultMessage 结果消息
     */
    private void logOperation(MaintenanceScript script, Integer operationType, String operationDesc, Boolean result, String resultMessage) {
        try {
            logger.debug("记录脚本 [{}]({}) 的操作日志: [{}] [{}] [{}]", 
                script.getMaintenanceScriptName(), 
                script.getMaintenanceScriptId(),
                operationType, 
                operationDesc,
                result ? "成功" : "失败");
                
            String ipAddress = RequestUtils.getIpAddress();
            
            MaintenanceLogRequest logRequest = new MaintenanceLogRequest();
            logRequest.setMaintenanceGroupId(script.getMaintenanceGroupId());
            logRequest.setMaintenanceScriptId(script.getMaintenanceScriptId());
            logRequest.setOperationType(operationType);
            logRequest.setOperationDetails(operationDesc);
            logRequest.setOperationResult(result);
            logRequest.setResultMessage(resultMessage);
            logRequest.setIpAddress(ipAddress);
            
            ReturnResult<Boolean> logResult = maintenanceLogService.logOperation(logRequest);
            if (!logResult.isOk()) {
                logger.warn("记录脚本操作日志失败: {}", logResult.getMsg());
            } else {
                logger.debug("脚本操作日志记录成功");
            }
        } catch (Exception e) {
            logger.error("记录脚本操作日志异常: {}", e.getMessage(), e);
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

    /**
     * 获取文件的父目录路径
     *
     * @param filePath 完整的文件路径
     * @return 父目录路径，如果没有父目录则返回null
     */
    private String getParentDirectory(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return null;
        }
        
        int lastSlashIndex = filePath.lastIndexOf('/');
        if (lastSlashIndex <= 0) {
            return null; // 没有父目录或在根目录
        }
        
        return filePath.substring(0, lastSlashIndex);
    }
    
    /**
     * 递归创建父目录
     *
     * @param sftp SFTP通道
     * @param dirPath 目录路径
     * @throws SftpException 如果创建目录过程中出错
     */
    private void createParentDirectories(ChannelSftp sftp, String dirPath) throws SftpException {
        if (dirPath == null || dirPath.isEmpty() || dirPath.equals("/")) {
            return;
        }
        
        try {
            sftp.stat(dirPath);
            // 目录已存在，无需创建
        } catch (SftpException e) {
            if (e.id != ChannelSftp.SSH_FX_NO_SUCH_FILE) {
                throw e; // 其他错误，需要抛出
            }
            
            // 目录不存在，先创建父目录
            String parentDir = getParentDirectory(dirPath);
            if (parentDir != null && !parentDir.isEmpty()) {
                createParentDirectories(sftp, parentDir);
            }
            
            // 创建当前目录
            logger.debug("创建目录: {}", dirPath);
            sftp.mkdir(dirPath);
        }
    }
} 