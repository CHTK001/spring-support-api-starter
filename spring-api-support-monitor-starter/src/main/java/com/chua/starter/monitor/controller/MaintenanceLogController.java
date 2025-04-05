package com.chua.starter.monitor.controller;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.entity.MaintenanceLog;
import com.chua.starter.monitor.service.MaintenanceLogService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 维护日志控制器
 *
 * @author CH
 */
@RestController
@RequestMapping("/v1/maintenance/log")
@RequiredArgsConstructor
@Api(tags = "维护日志管理接口", description = "提供维护操作日志的查询服务")
public class MaintenanceLogController {
    
    private final MaintenanceLogService maintenanceLogService;
    
    /**
     * 根据维护组ID获取日志列表
     *
     * @param groupId 维护组ID
     * @return 日志列表
     */
    @GetMapping("/group/{groupId}")
    @ApiOperation(value = "获取维护组日志", notes = "根据维护组ID获取该组所有操作日志")
    @ApiImplicitParam(name = "groupId", value = "维护组ID", required = true, dataTypeClass = Long.class, paramType = "path", example = "1")
    public ReturnResult<List<MaintenanceLog>> getLogsByGroupId(
            @ApiParam(value = "维护组ID", required = true, example = "1") 
            @PathVariable Long groupId) {
        return maintenanceLogService.getLogsByGroupId(groupId);
    }
    
    /**
     * 根据维护主机ID获取日志列表
     *
     * @param hostId 维护主机ID
     * @return 日志列表
     */
    @GetMapping("/host/{hostId}")
    @ApiOperation(value = "获取维护主机日志", notes = "根据维护主机ID获取该主机所有操作日志")
    @ApiImplicitParam(name = "hostId", value = "维护主机ID", required = true, dataTypeClass = Long.class, paramType = "path", example = "1")
    public ReturnResult<List<MaintenanceLog>> getLogsByHostId(
            @ApiParam(value = "维护主机ID", required = true, example = "1") 
            @PathVariable Long hostId) {
        return maintenanceLogService.getLogsByHostId(hostId);
    }
    
    /**
     * 根据维护脚本ID获取日志列表
     *
     * @param scriptId 维护脚本ID
     * @return 日志列表
     */
    @GetMapping("/script/{scriptId}")
    @ApiOperation(value = "获取维护脚本日志", notes = "根据维护脚本ID获取该脚本所有操作日志")
    @ApiImplicitParam(name = "scriptId", value = "维护脚本ID", required = true, dataTypeClass = Long.class, paramType = "path", example = "1")
    public ReturnResult<List<MaintenanceLog>> getLogsByScriptId(
            @ApiParam(value = "维护脚本ID", required = true, example = "1") 
            @PathVariable Long scriptId) {
        return maintenanceLogService.getLogsByScriptId(scriptId);
    }
    
    /**
     * 根据维护文件ID获取日志列表
     *
     * @param fileId 维护文件ID
     * @return 日志列表
     */
    @GetMapping("/file/{fileId}")
    @ApiOperation(value = "获取维护文件日志", notes = "根据维护文件ID获取该文件所有操作日志")
    @ApiImplicitParam(name = "fileId", value = "维护文件ID", required = true, dataTypeClass = Long.class, paramType = "path", example = "1")
    public ReturnResult<List<MaintenanceLog>> getLogsByFileId(
            @ApiParam(value = "维护文件ID", required = true, example = "1") 
            @PathVariable Long fileId) {
        return maintenanceLogService.getLogsByFileId(fileId);
    }
} 