package com.chua.starter.monitor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.lang.code.ReturnPageResult;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.entity.MaintenanceLog;
import com.chua.starter.monitor.mapper.MaintenanceLogMapper;
import com.chua.starter.monitor.pojo.request.MaintenanceLogRequest;
import com.chua.starter.monitor.service.MaintenanceLogService;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.mybatis.utils.ReturnPageResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 维护日志服务实现类
 *
 * @author CH
 */
@Slf4j
@Service
public class MaintenanceLogServiceImpl extends ServiceImpl<MaintenanceLogMapper, MaintenanceLog> implements MaintenanceLogService {

    @Override
    public ReturnResult<Boolean> logOperation(MaintenanceLogRequest request) {
        try {
            if (request.getMaintenanceGroupId() == null) {
                return ReturnResult.error("维护组ID不能为空");
            }
            
            if (request.getOperationType() == null) {
                return ReturnResult.error("操作类型不能为空");
            }
            
            if (request.getTargetType() == null) {
                return ReturnResult.error("目标类型不能为空");
            }
            
            MaintenanceLog maintenanceLog = new MaintenanceLog();
            maintenanceLog.setMaintenanceGroupId(request.getMaintenanceGroupId());
            maintenanceLog.setMaintenanceHostId(request.getMaintenanceHostId());
            maintenanceLog.setMaintenanceScriptId(request.getMaintenanceScriptId());
            maintenanceLog.setMaintenanceFileId(request.getMaintenanceFileId());
            maintenanceLog.setOperationType(request.getOperationType());
            maintenanceLog.setTargetType(request.getTargetType());
            maintenanceLog.setOperationUser(request.getOperationUser());
            maintenanceLog.setOperationDetails(request.getOperationDetails());
            maintenanceLog.setOperationResult(request.getOperationResult());
            maintenanceLog.setResultMessage(request.getResultMessage());
            maintenanceLog.setIpAddress(request.getIpAddress());
            maintenanceLog.setOperationTime(LocalDateTime.now());
            
            save(maintenanceLog);
            
            return ReturnResult.success(true);
        } catch (Exception e) {
            log.error("记录操作日志失败", e);
            return ReturnResult.error("记录操作日志失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnPageResult<MaintenanceLog> getLogsByGroupId(Long groupId, Query<MaintenanceLog> query) {
        try {
            if (groupId == null) {
                return ReturnPageResult.ok();
            }
            
            LambdaQueryWrapper<MaintenanceLog> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(MaintenanceLog::getMaintenanceGroupId, groupId);
            queryWrapper.orderByDesc(MaintenanceLog::getOperationTime);
            return ReturnPageResultUtils.ok(this.page(query.createFullPage(), queryWrapper));
        } catch (Exception e) {
            log.error("获取维护组日志失败", e);
            return ReturnPageResult.ok();
        }
    }

    @Override
    public ReturnResult<List<MaintenanceLog>> getLogsByHostId(Long hostId) {
        try {
            if (hostId == null) {
                return ReturnResult.error("主机ID不能为空");
            }
            
            LambdaQueryWrapper<MaintenanceLog> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(MaintenanceLog::getMaintenanceHostId, hostId);
            queryWrapper.orderByDesc(MaintenanceLog::getOperationTime);
            
            List<MaintenanceLog> logs = list(queryWrapper);
            
            return ReturnResult.success(logs);
        } catch (Exception e) {
            log.error("获取主机日志失败", e);
            return ReturnResult.error("获取主机日志失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<List<MaintenanceLog>> getLogsByScriptId(Long scriptId) {
        try {
            if (scriptId == null) {
                return ReturnResult.error("脚本ID不能为空");
            }
            
            LambdaQueryWrapper<MaintenanceLog> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(MaintenanceLog::getMaintenanceScriptId, scriptId);
            queryWrapper.orderByDesc(MaintenanceLog::getOperationTime);
            
            List<MaintenanceLog> logs = list(queryWrapper);
            
            return ReturnResult.success(logs);
        } catch (Exception e) {
            log.error("获取脚本日志失败", e);
            return ReturnResult.error("获取脚本日志失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<List<MaintenanceLog>> getLogsByFileId(Long fileId) {
        try {
            if (fileId == null) {
                return ReturnResult.error("文件ID不能为空");
            }
            
            LambdaQueryWrapper<MaintenanceLog> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(MaintenanceLog::getMaintenanceFileId, fileId);
            queryWrapper.orderByDesc(MaintenanceLog::getOperationTime);
            
            List<MaintenanceLog> logs = list(queryWrapper);
            
            return ReturnResult.success(logs);
        } catch (Exception e) {
            log.error("获取文件日志失败", e);
            return ReturnResult.error("获取文件日志失败: " + e.getMessage());
        }
    }
} 