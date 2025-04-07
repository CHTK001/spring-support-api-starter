package com.chua.starter.monitor.service.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.protocol.ClientSetting;
import com.chua.common.support.utils.DigestUtils;
import com.chua.ssh.support.ssh.SshClient;
import com.chua.starter.common.support.utils.RequestUtils;
import com.chua.starter.monitor.client.SshClientManager;
import com.chua.starter.monitor.entity.MaintenanceHost;
import com.chua.starter.monitor.mapper.MaintenanceHostMapper;
import com.chua.starter.monitor.pojo.request.MaintenanceHostRequest;
import com.chua.starter.monitor.pojo.request.MaintenanceLogRequest;
import com.chua.starter.monitor.pojo.response.MaintenanceHostResponse;
import com.chua.starter.monitor.service.MaintenanceHostService;
import com.chua.starter.monitor.service.MaintenanceLogService;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 维护主机服务实现类
 *
 * @author CH
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MaintenanceHostServiceImpl implements MaintenanceHostService {
    private static final Map<String, CacheClient> SERVER_MAP = new ConcurrentHashMap<>();

    private final MaintenanceHostMapper maintenanceHostMapper;
    private final SshClientManager sshClientManager;
    private final MaintenanceLogService maintenanceLogService;

    @Override
    public ReturnResult<MaintenanceHostResponse> createMaintenanceHost(MaintenanceHostRequest request) {
        try {
            if (request.getMaintenanceGroupId() == null) {
                return ReturnResult.error("维护组ID不能为空");
            }
            
            MaintenanceHost maintenanceHost = new MaintenanceHost();
            BeanUtils.copyProperties(request, maintenanceHost);

            // 默认启用
            if (maintenanceHost.getMaintenanceHostEnabled() == null) {
                maintenanceHost.setMaintenanceHostEnabled(true);
            }

            maintenanceHost.setMaintenanceHostCreateTime(LocalDateTime.now());
            maintenanceHost.setMaintenanceHostUpdateTime(LocalDateTime.now());

            maintenanceHostMapper.insert(maintenanceHost);

            MaintenanceHostResponse response = new MaintenanceHostResponse();
            BeanUtils.copyProperties(maintenanceHost, response);
            
            // 记录操作日志
            logOperation(maintenanceHost, 1, "创建维护主机", true, null);
            
            return ReturnResult.success(response);
        } catch (Exception e) {
            return ReturnResult.error("创建维护主机失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<MaintenanceHostResponse> updateMaintenanceHost(MaintenanceHostRequest request) {
        try {
            MaintenanceHost maintenanceHost = maintenanceHostMapper.selectById(request.getMaintenanceHostId());
            if (maintenanceHost == null) {
                return ReturnResult.error("维护主机不存在");
            }

            BeanUtils.copyProperties(request, maintenanceHost);
            maintenanceHost.setMaintenanceHostUpdateTime(LocalDateTime.now());

            maintenanceHostMapper.updateById(maintenanceHost);

            MaintenanceHostResponse response = new MaintenanceHostResponse();
            BeanUtils.copyProperties(maintenanceHost, response);
            
            // 记录操作日志
            logOperation(maintenanceHost, 2, "更新维护主机", true, null);

            return ReturnResult.success(response);
        } catch (Exception e) {
            return ReturnResult.error("更新维护主机失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Boolean> deleteMaintenanceHost(Long maintenanceHostId) {
        try {
            // 删除主机前关闭并移除其SSH连接
            MaintenanceHost host = maintenanceHostMapper.selectById(maintenanceHostId);
            if (host != null) {
                sshClientManager.closeSession(maintenanceHostId);
                
                // 记录操作日志
                logOperation(host, 3, "删除维护主机", true, null);
            }
            
            maintenanceHostMapper.deleteById(maintenanceHostId);
            return ReturnResult.success(true);
        } catch (Exception e) {
            return ReturnResult.error("删除维护主机失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<MaintenanceHostResponse> getMaintenanceHost(Long maintenanceHostId) {
        try {
            MaintenanceHost maintenanceHost = maintenanceHostMapper.selectById(maintenanceHostId);
            if (maintenanceHost == null) {
                return ReturnResult.error("维护主机不存在");
            }

            MaintenanceHostResponse response = new MaintenanceHostResponse();
            BeanUtils.copyProperties(maintenanceHost, response);

            return ReturnResult.success(response);
        } catch (Exception e) {
            return ReturnResult.error("获取维护主机失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<List<MaintenanceHostResponse>> listMaintenanceHosts(Long maintenanceGroupId) {
        try {
            if (maintenanceGroupId == null) {
                return ReturnResult.error("维护组ID不能为空");
            }
            
            LambdaQueryWrapper<MaintenanceHost> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(MaintenanceHost::getMaintenanceGroupId, maintenanceGroupId);

            List<MaintenanceHost> maintenanceHosts = maintenanceHostMapper.selectList(queryWrapper);

            List<MaintenanceHostResponse> responses = maintenanceHosts.stream()
                    .map(host -> {
                        MaintenanceHostResponse response = new MaintenanceHostResponse();
                        BeanUtils.copyProperties(host, response);
                        return response;
                    })
                    .collect(Collectors.toList());

            return ReturnResult.success(responses);
        } catch (Exception e) {
            return ReturnResult.error("获取维护主机列表失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Boolean> enableMaintenanceHost(Long maintenanceHostId, Boolean enabled) {
        try {
            MaintenanceHost maintenanceHost = maintenanceHostMapper.selectById(maintenanceHostId);
            if (maintenanceHost == null) {
                return ReturnResult.error("维护主机不存在");
            }

            maintenanceHost.setMaintenanceHostEnabled(enabled);
            maintenanceHost.setMaintenanceHostUpdateTime(LocalDateTime.now());

            maintenanceHostMapper.updateById(maintenanceHost);
            
            // 记录操作日志
            String action = enabled ? "启用" : "禁用";
            logOperation(maintenanceHost, 4, action + "维护主机", true, null);
            
            // 如果禁用主机，关闭其SSH连接
            if (!enabled) {
                sshClientManager.closeSession(maintenanceHostId);
            }

            return ReturnResult.success(true);
        } catch (Exception e) {
            return ReturnResult.error((enabled ? "启用" : "停用") + "维护主机失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Boolean> testConnection(Integer maintenanceHostId) {
        try {
            MaintenanceHost maintenanceHost = maintenanceHostMapper.selectById(maintenanceHostId);
            if (maintenanceHost == null) {
                return ReturnResult.error("维护主机不存在");
            }

            boolean connected = testSshConnection(maintenanceHost);
            
            // 记录操作日志
            String resultMessage = connected ? "连接成功" : "连接失败";
            logOperation(maintenanceHost, 5, "测试主机连接", connected, resultMessage);

            if (connected) {
                return ReturnResult.success(true);
            } else {
                return ReturnResult.error("连接测试失败");
            }
        } catch (Exception e) {
            MaintenanceHost maintenanceHost = maintenanceHostMapper.selectById(maintenanceHostId);
            if (maintenanceHost != null) {
                // 记录操作日志
                logOperation(maintenanceHost, 5, "测试主机连接", false, "连接异常: " + e.getMessage());
            }
            return ReturnResult.error("连接测试失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Map<String, Object>> openTerminal(Long hostId) {
        try {
            // 检查主机是否存在
            MaintenanceHost maintenanceHost = maintenanceHostMapper.selectById(hostId);
            if (maintenanceHost == null) {
                return ReturnResult.error("维护主机不存在");
            }
            
            // 检查主机是否启用
            if (!maintenanceHost.getMaintenanceHostEnabled()) {
                return ReturnResult.error("维护主机已禁用，无法建立连接");
            }
            
            // 尝试建立SSH连接
            boolean connected = testSshConnection(maintenanceHost);
            if (!connected) {
                logOperation(maintenanceHost, 6, "打开终端连接", false, "无法建立SSH连接");
                return ReturnResult.error("无法建立SSH连接，请检查主机配置");
            }
            
            // 生成唯一的会话ID
            String genId = UUID.randomUUID().toString().replace("-", "");
            
            // 构建返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("genId", genId);
            result.put("hostId", hostId);
            result.put("connected", true);
            
            // 记录日志
            logOperation(maintenanceHost, 6, "打开终端连接", true, "会话ID: " + genId);
            
            return ReturnResult.success(result);
        } catch (Exception e) {
            MaintenanceHost maintenanceHost = maintenanceHostMapper.selectById(hostId);
            if (maintenanceHost != null) {
                logOperation(maintenanceHost, 6, "打开终端连接", false, "异常: " + e.getMessage());
            }
            return ReturnResult.error("打开终端失败: " + e.getMessage());
        }
    }

    @Override
    public SshClient getSshClient(String genId) {
        MaintenanceHost monitorSysGen = this.maintenanceHostMapper.selectById(genId);
        CacheClient cacheClient = SERVER_MAP.get(genId);
        if (null == cacheClient) {
            SshClient sshClient = createClient(monitorSysGen);
            SERVER_MAP.put(genId, new CacheClient(sshClient));
            return sshClient;
        }
        return cacheClient.getSshClient();
    }
    private SshClient createClient(MaintenanceHost monitorSysGen) {
        SshClient sshClient = new SshClient(ClientSetting.builder()
                .username(monitorSysGen.getMaintenanceHostUsername())
                .password(DigestUtils.aesDecrypt(monitorSysGen.getMaintenanceHostPassword(), "1234567890Oil#@1"))
                .host(monitorSysGen.getMaintenanceHostAddress())
                .port(monitorSysGen.getMaintenanceHostPort())
                .build()
        );
        sshClient.connect();
        return sshClient;
    }
    /**
     * 测试SSH连接
     *
     * @param host 维护主机
     * @return 连接是否成功
     */
    private boolean testSshConnection(MaintenanceHost host) {
        Session session = null;
        try {
            // 使用SshClientManager获取Session
            session = sshClientManager.getSession(host);
            return session != null && session.isConnected();
        } catch (JSchException e) {
            return false;
        }
    }
    
    /**
     * 记录操作日志
     *
     * @param host           主机对象
     * @param operationType  操作类型
     * @param operationDesc  操作描述
     * @param result         操作结果
     * @param resultMessage  结果消息
     */
    private void logOperation(MaintenanceHost host, Integer operationType, String operationDesc, Boolean result, String resultMessage) {
        try {
            MaintenanceLogRequest logRequest = new MaintenanceLogRequest();
            logRequest.setMaintenanceGroupId(host.getMaintenanceGroupId());
            logRequest.setMaintenanceHostId(host.getMaintenanceHostId());
            logRequest.setOperationType(operationType);
            logRequest.setTargetType(2); // 2表示维护主机
            
            // 构建操作详情JSON
            Map<String, Object> details = new HashMap<>();
            details.put("maintenanceHostId", host.getMaintenanceHostId());
            details.put("maintenanceHostName", host.getMaintenanceHostName());
            details.put("maintenanceHostAddress", host.getMaintenanceHostAddress());
            details.put("operationDesc", operationDesc);
            logRequest.setOperationDetails(JSON.toJSONString(details));
            
            // 设置操作结果
            logRequest.setOperationResult(result);
            logRequest.setResultMessage(resultMessage);
            
            // 获取客户端IP
            String ipAddress = RequestUtils.getIpAddress();
            logRequest.setIpAddress(ipAddress);
            
            // 调用日志服务记录日志
            maintenanceLogService.logOperation(logRequest);
        } catch (Exception e) {
            // 记录日志失败不应影响主业务逻辑
            System.err.println("记录操作日志失败: " + e.getMessage());
        }
    }

    @Data
    @AllArgsConstructor
    static class CacheClient {
        private SshClient sshClient;
    }
}