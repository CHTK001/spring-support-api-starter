package com.chua.starter.monitor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.entity.MaintenanceHost;
import com.chua.starter.monitor.mapper.MaintenanceHostMapper;
import com.chua.starter.monitor.pojo.request.MaintenanceHostRequest;
import com.chua.starter.monitor.pojo.response.MaintenanceHostResponse;
import com.chua.starter.monitor.service.MaintenanceHostService;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

@Service
public class MaintenanceHostServiceImpl implements MaintenanceHostService {

    @Resource
    private MaintenanceHostMapper maintenanceHostMapper;

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

            return ReturnResult.success(response);
        } catch (Exception e) {
            return ReturnResult.error("更新维护主机失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Boolean> deleteMaintenanceHost(Long maintenanceHostId) {
        try {
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

            boolean connected = testSshConnection(
                    maintenanceHost.getMaintenanceHostAddress(),
                    maintenanceHost.getMaintenanceHostPort(),
                    maintenanceHost.getMaintenanceHostUsername(),
                    maintenanceHost.getMaintenanceHostPassword()
            );

            if (connected) {
                return ReturnResult.success(true);
            } else {
                return ReturnResult.error("连接测试失败");
            }
        } catch (Exception e) {
            return ReturnResult.error("连接测试失败: " + e.getMessage());
        }
    }

    private boolean testSshConnection(String host, Integer port, String username, String password) {
        JSch jsch = new JSch();
        Session session = null;
        try {
            session = jsch.getSession(username, host, port);
            session.setPassword(password);

            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);

            session.connect(5000);

            return session.isConnected();
        } catch (Exception e) {
            return false;
        } finally {
            if (session != null && session.isConnected()) {
                session.disconnect();
            }
        }
    }
} 