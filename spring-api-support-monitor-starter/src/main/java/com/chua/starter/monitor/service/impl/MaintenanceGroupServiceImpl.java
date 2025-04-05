package com.chua.starter.monitor.service.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.entity.MaintenanceGroup;
import com.chua.starter.monitor.mapper.MaintenanceGroupMapper;
import com.chua.starter.monitor.mapper.MaintenanceHostMapper;
import com.chua.starter.monitor.mapper.MaintenanceScriptMapper;
import com.chua.starter.monitor.pojo.request.MaintenanceGroupRequest;
import com.chua.starter.monitor.pojo.request.MaintenanceLogRequest;
import com.chua.starter.monitor.pojo.response.MaintenanceGroupResponse;
import com.chua.starter.monitor.service.MaintenanceGroupService;
import com.chua.starter.monitor.service.MaintenanceLogService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 维护组服务实现类
 * 提供维护组的增删改查等功能
 *
 * @author CH
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MaintenanceGroupServiceImpl extends ServiceImpl<MaintenanceGroupMapper, MaintenanceGroup> implements MaintenanceGroupService {

    /**
     * 维护组数据访问对象
     */
    private final MaintenanceGroupMapper maintenanceGroupMapper;

    /**
     * 维护主机数据访问对象
     */
    private final MaintenanceHostMapper maintenanceHostMapper;

    /**
     * 维护脚本数据访问对象
     */
    private final MaintenanceScriptMapper maintenanceScriptMapper;

    private final MaintenanceLogService maintenanceLogService;

    /**
     * 创建维护组
     *
     * @param request 请求参数
     * @return 创建结果
     */
    @Override
    public ReturnResult<MaintenanceGroupResponse> createMaintenanceGroup(MaintenanceGroupRequest request) {
        try {
            MaintenanceGroup maintenanceGroup = new MaintenanceGroup();
            BeanUtils.copyProperties(request, maintenanceGroup);
            
            // 默认启用状态
            if (maintenanceGroup.getMaintenanceGroupEnabled() == null) {
                maintenanceGroup.setMaintenanceGroupEnabled(true);
            }
            
            maintenanceGroup.setMaintenanceGroupCreateTime(LocalDateTime.now());
            maintenanceGroup.setMaintenanceGroupUpdateTime(LocalDateTime.now());
            
            maintenanceGroupMapper.insert(maintenanceGroup);
            
            MaintenanceGroupResponse response = new MaintenanceGroupResponse();
            BeanUtils.copyProperties(maintenanceGroup, response);
            
            // 记录操作日志
            logOperation(maintenanceGroup, 1, "创建维护组", true, null);
            
            return ReturnResult.success(response);
        } catch (Exception e) {
            log.error("创建维护组失败", e);
            return ReturnResult.error("创建维护组失败: " + e.getMessage());
        }
    }

    /**
     * 更新维护组
     *
     * @param request 请求参数
     * @return 更新结果
     */
    @Override
    public ReturnResult<MaintenanceGroupResponse> updateMaintenanceGroup(MaintenanceGroupRequest request) {
        try {
            if (request.getMaintenanceGroupId() == null) {
                return ReturnResult.error("维护组ID不能为空");
            }
            
            MaintenanceGroup maintenanceGroup = maintenanceGroupMapper.selectById(request.getMaintenanceGroupId());
            if (maintenanceGroup == null) {
                return ReturnResult.error("维护组不存在");
            }
            
            BeanUtils.copyProperties(request, maintenanceGroup);
            maintenanceGroup.setMaintenanceGroupUpdateTime(LocalDateTime.now());
            
            maintenanceGroupMapper.updateById(maintenanceGroup);
            
            MaintenanceGroupResponse response = new MaintenanceGroupResponse();
            BeanUtils.copyProperties(maintenanceGroup, response);
            
            // 记录操作日志
            logOperation(maintenanceGroup, 2, "更新维护组", true, null);
            
            return ReturnResult.success(response);
        } catch (Exception e) {
            log.error("更新维护组失败", e);
            return ReturnResult.error("更新维护组失败: " + e.getMessage());
        }
    }

    /**
     * 删除维护组
     *
     * @param maintenanceGroupId 维护组ID
     * @return 删除结果
     */
    @Override
    public ReturnResult<Boolean> deleteMaintenanceGroup(Long maintenanceGroupId) {
        try {
            if (maintenanceGroupId == null) {
                return ReturnResult.error("维护组ID不能为空");
            }
            
            MaintenanceGroup maintenanceGroup = maintenanceGroupMapper.selectById(maintenanceGroupId);
            if (maintenanceGroup == null) {
                return ReturnResult.error("维护组不存在");
            }
            
            // 记录操作日志
            logOperation(maintenanceGroup, 3, "删除维护组", true, null);
            
            maintenanceGroupMapper.deleteById(maintenanceGroupId);
            
            return ReturnResult.success(true);
        } catch (Exception e) {
            log.error("删除维护组失败", e);
            return ReturnResult.error("删除维护组失败: " + e.getMessage());
        }
    }

    /**
     * 获取维护组详情
     *
     * @param maintenanceGroupId 维护组ID
     * @return 维护组详情
     */
    @Override
    public ReturnResult<MaintenanceGroupResponse> getMaintenanceGroup(Long maintenanceGroupId) {
        try {
            MaintenanceGroup maintenanceGroup = maintenanceGroupMapper.selectById(maintenanceGroupId);
            if (maintenanceGroup == null) {
                return ReturnResult.error("维护组不存在");
            }

            MaintenanceGroupResponse response = new MaintenanceGroupResponse();
            BeanUtils.copyProperties(maintenanceGroup, response);
            
            // 获取主机数量
            Integer hostCount = maintenanceHostMapper.countHostsByGroupId(maintenanceGroupId);
            response.setHostCount(hostCount);
            
            // 获取脚本数量
            Integer scriptCount = maintenanceScriptMapper.countScriptsByGroupId(maintenanceGroupId);
            response.setScriptCount(scriptCount);

            return ReturnResult.success(response);
        } catch (Exception e) {
            log.error("获取维护组失败", e);
            return ReturnResult.error("获取维护组失败: " + e.getMessage());
        }
    }

    /**
     * 获取维护组列表
     *
     * @return 维护组列表
     */
    @Override
    public ReturnResult<List<MaintenanceGroupResponse>> listMaintenanceGroups() {
        try {
            LambdaQueryWrapper<MaintenanceGroup> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.orderByDesc(MaintenanceGroup::getMaintenanceGroupUpdateTime);
            
            List<MaintenanceGroup> maintenanceGroups = maintenanceGroupMapper.selectList(queryWrapper);
            
            List<MaintenanceGroupResponse> responses = maintenanceGroups.stream()
                    .map(group -> {
                        MaintenanceGroupResponse response = new MaintenanceGroupResponse();
                        BeanUtils.copyProperties(group, response);
                        
                        // 获取主机数量
                        Integer hostCount = maintenanceHostMapper.countHostsByGroupId(group.getMaintenanceGroupId());
                        response.setHostCount(hostCount);
                        
                        // 获取脚本数量
                        Integer scriptCount = maintenanceScriptMapper.countScriptsByGroupId(group.getMaintenanceGroupId());
                        response.setScriptCount(scriptCount);
                        
                        return response;
                    })
                    .collect(Collectors.toList());
            
            return ReturnResult.success(responses);
        } catch (Exception e) {
            log.error("获取维护组列表失败", e);
            return ReturnResult.error("获取维护组列表失败: " + e.getMessage());
        }
    }

    /**
     * 启用或停用维护组
     *
     * @param maintenanceGroupId 维护组ID
     * @param enabled            是否启用
     * @return 操作结果
     */
    @Override
    public ReturnResult<Boolean> enableMaintenanceGroup(Long maintenanceGroupId, Boolean enabled) {
        try {
            if (maintenanceGroupId == null) {
                return ReturnResult.error("维护组ID不能为空");
            }
            
            MaintenanceGroup maintenanceGroup = maintenanceGroupMapper.selectById(maintenanceGroupId);
            if (maintenanceGroup == null) {
                return ReturnResult.error("维护组不存在");
            }
            
            maintenanceGroup.setMaintenanceGroupEnabled(enabled);
            maintenanceGroup.setMaintenanceGroupUpdateTime(LocalDateTime.now());
            
            maintenanceGroupMapper.updateById(maintenanceGroup);
            
            // 记录操作日志
            String action = enabled ? "启用" : "禁用";
            logOperation(maintenanceGroup, 4, action + "维护组", true, null);
            
            return ReturnResult.success(true);
        } catch (Exception e) {
            log.error("修改维护组状态失败", e);
            return ReturnResult.error("修改维护组状态失败: " + e.getMessage());
        }
    }

    /**
     * 记录操作日志
     *
     * @param group          维护组对象
     * @param operationType  操作类型
     * @param operationDesc  操作描述
     * @param result         操作结果
     * @param resultMessage  结果消息
     */
    private void logOperation(MaintenanceGroup group, Integer operationType, String operationDesc, Boolean result, String resultMessage) {
        try {
            MaintenanceLogRequest logRequest = new MaintenanceLogRequest();
            logRequest.setMaintenanceGroupId(group.getMaintenanceGroupId());
            logRequest.setOperationType(operationType);
            logRequest.setTargetType(1); // 1表示维护组
            
            // 构建操作详情JSON
            Map<String, Object> details = new HashMap<>();
            details.put("maintenanceGroupId", group.getMaintenanceGroupId());
            details.put("maintenanceGroupName", group.getMaintenanceGroupName());
            details.put("maintenanceGroupDesc", group.getMaintenanceGroupDescription());
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
            log.error("记录操作日志失败: {}", e.getMessage(), e);
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