package com.chua.starter.monitor.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chua.common.support.lang.code.ReturnPageResult;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.entity.MaintenanceLog;
import com.chua.starter.monitor.pojo.request.MaintenanceLogRequest;
import com.chua.starter.mybatis.entity.Query;

import java.util.List;

/**
 * 维护日志服务接口
 *
 * @author CH
 */
public interface MaintenanceLogService extends IService<MaintenanceLog> {
    
    /**
     * 记录操作日志
     *
     * @param request 日志请求
     * @return 操作结果
     */
    ReturnResult<Boolean> logOperation(MaintenanceLogRequest request);
    
    /**
     * 根据维护组ID获取日志列表
     *
     * @param groupId 维护组ID
     * @param query
     * @return 日志列表
     */
    ReturnPageResult<MaintenanceLog> getLogsByGroupId(Long groupId, Query<MaintenanceLog> query);
    
    /**
     * 根据维护主机ID获取日志列表
     *
     * @param hostId 维护主机ID
     * @return 日志列表
     */
    ReturnResult<List<MaintenanceLog>> getLogsByHostId(Long hostId);
    
    /**
     * 根据维护脚本ID获取日志列表
     *
     * @param scriptId 维护脚本ID
     * @return 日志列表
     */
    ReturnResult<List<MaintenanceLog>> getLogsByScriptId(Long scriptId);
    
    /**
     * 根据维护文件ID获取日志列表
     *
     * @param fileId 维护文件ID
     * @return 日志列表
     */
    ReturnResult<List<MaintenanceLog>> getLogsByFileId(Long fileId);
} 