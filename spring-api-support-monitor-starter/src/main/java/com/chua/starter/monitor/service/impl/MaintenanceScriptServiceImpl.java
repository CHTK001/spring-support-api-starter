package com.chua.starter.monitor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.entity.MaintenanceHost;
import com.chua.starter.monitor.entity.MaintenanceScript;
import com.chua.starter.monitor.mapper.MaintenanceHostMapper;
import com.chua.starter.monitor.mapper.MaintenanceScriptMapper;
import com.chua.starter.monitor.pojo.request.MaintenanceScriptRequest;
import com.chua.starter.monitor.pojo.request.RunScriptRequest;
import com.chua.starter.monitor.pojo.response.MaintenanceScriptResponse;
import com.chua.starter.monitor.pojo.response.RunScriptResponse;
import com.chua.starter.monitor.service.MaintenanceScriptService;
import com.jcraft.jsch.*;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MaintenanceScriptServiceImpl implements MaintenanceScriptService {

    @Resource
    private MaintenanceScriptMapper maintenanceScriptMapper;

    @Resource
    private MaintenanceHostMapper maintenanceHostMapper;

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
            return ReturnResult.success(response);
        } catch (Exception e) {
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

            return ReturnResult.success(response);
        } catch (Exception e) {
            return ReturnResult.error("更新维护脚本失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Boolean> deleteMaintenanceScript(Long maintenanceScriptId) {
        try {
            maintenanceScriptMapper.deleteById(maintenanceScriptId);
            return ReturnResult.success(true);
        } catch (Exception e) {
            return ReturnResult.error("删除维护脚本失败: " + e.getMessage());
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
                    String result = executeScript(
                            host.getMaintenanceHostAddress(),
                            host.getMaintenanceHostPort(),
                            host.getMaintenanceHostUsername(),
                            host.getMaintenanceHostPassword(),
                            script.getMaintenanceScriptContent()
                    );
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

    private String executeScript(String host, Integer port, String username, String password, String scriptContent) throws JSchException, IOException {
        JSch jsch = new JSch();
        Session session = null;
        Channel channel = null;

        try {
            session = jsch.getSession(username, host, port);
            session.setPassword(password);

            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);

            session.connect(30000);

            channel = session.openChannel("exec");
            ((ChannelExec) channel).setCommand(scriptContent);

            channel.setInputStream(null);
            InputStream stdout = channel.getInputStream();
            InputStream stderr = ((ChannelExec) channel).getErrStream();

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
            if (session != null) {
                session.disconnect();
            }
        }
    }
} 