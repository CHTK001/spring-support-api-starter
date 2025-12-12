package com.chua.starter.monitor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.utils.StringUtils;
import com.chua.starter.monitor.entity.MonitorSysGenScriptExecution;
import com.chua.starter.monitor.mapper.MonitorSysGenScriptExecutionMapper;
import com.chua.starter.monitor.pojo.request.ScriptExecuteRecordQueryRequest;
import com.chua.starter.monitor.service.script.MonitorSysGenScriptExecutionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * 脚本执行记录服务实现类
 *
 * @author CH
 * @since 2024-12-12
 */
@Slf4j
@Service
public class MonitorSysGenScriptExecutionServiceImpl 
        extends ServiceImpl<MonitorSysGenScriptExecutionMapper, MonitorSysGenScriptExecution>
        implements MonitorSysGenScriptExecutionService {

    /**
     * 分页查询执行记录
     *
     * @param request 查询请求
     * @return 分页结果
     */
    @Override
    public Page<MonitorSysGenScriptExecution> pageForRecord(ScriptExecuteRecordQueryRequest request) {
        log.debug("分页查询脚本执行记录: page={}, pageSize={}, scriptId={}", 
                request.getPage(), request.getPageSize(), request.getScriptId());
        
        Page<MonitorSysGenScriptExecution> page = new Page<>(request.getPage(), request.getPageSize());
        
        LambdaQueryWrapper<MonitorSysGenScriptExecution> wrapper = new LambdaQueryWrapper<>();
        
        // 脚本ID筛选
        if (request.getScriptId() != null) {
            wrapper.eq(MonitorSysGenScriptExecution::getMonitorSysGenScriptId, request.getScriptId());
        }
        
        // 脚本名称模糊搜索
        if (StringUtils.isNotBlank(request.getScriptName())) {
            wrapper.like(MonitorSysGenScriptExecution::getMonitorSysGenScriptExecutionScriptName, 
                    request.getScriptName());
        }
        
        // 执行方式筛选
        if (StringUtils.isNotBlank(request.getExecuteMethod())) {
            wrapper.eq(MonitorSysGenScriptExecution::getMonitorSysGenScriptExecutionMethod, 
                    MonitorSysGenScriptExecution.ExecuteMethod.fromCode(request.getExecuteMethod()));
        }
        
        // 执行结果筛选
        if (StringUtils.isNotBlank(request.getExecuteResult())) {
            wrapper.eq(MonitorSysGenScriptExecution::getMonitorSysGenScriptExecutionStatus, 
                    MonitorSysGenScriptExecution.ExecutionStatus.fromCode(request.getExecuteResult()));
        }
        
        // 时间范围筛选
        if (StringUtils.isNotBlank(request.getStartTime())) {
            LocalDate startDate = LocalDate.parse(request.getStartTime(), DateTimeFormatter.ISO_LOCAL_DATE);
            wrapper.ge(MonitorSysGenScriptExecution::getMonitorSysGenScriptExecutionStartTime, 
                    LocalDateTime.of(startDate, LocalTime.MIN));
        }
        
        if (StringUtils.isNotBlank(request.getEndTime())) {
            LocalDate endDate = LocalDate.parse(request.getEndTime(), DateTimeFormatter.ISO_LOCAL_DATE);
            wrapper.le(MonitorSysGenScriptExecution::getMonitorSysGenScriptExecutionStartTime, 
                    LocalDateTime.of(endDate, LocalTime.MAX));
        }
        
        // 按开始时间倒序
        wrapper.orderByDesc(MonitorSysGenScriptExecution::getMonitorSysGenScriptExecutionStartTime);
        
        return this.page(page, wrapper);
    }

    /**
     * 获取执行记录详情
     *
     * @param recordId 记录ID
     * @return 执行记录
     */
    @Override
    public MonitorSysGenScriptExecution getDetailForRecord(Long recordId) {
        log.debug("获取脚本执行记录详情: recordId={}", recordId);
        return this.getById(recordId);
    }

    /**
     * 删除执行记录
     *
     * @param recordId 记录ID
     * @return 是否成功
     */
    @Override
    public boolean deleteForRecord(Long recordId) {
        log.info("删除脚本执行记录: recordId={}", recordId);
        return this.removeById(recordId);
    }

    /**
     * 清空执行记录
     *
     * @param scriptId 脚本ID（可选）
     * @return 删除的记录数
     */
    @Override
    public int clearForRecord(Integer scriptId) {
        log.info("清空脚本执行记录: scriptId={}", scriptId);
        return this.baseMapper.clearByScriptId(scriptId);
    }
}
