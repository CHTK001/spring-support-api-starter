package com.chua.starter.monitor.service.script;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.chua.starter.monitor.entity.MonitorSysGenScriptExecution;
import com.chua.starter.monitor.pojo.request.ScriptExecuteRecordQueryRequest;

/**
 * 脚本执行记录服务接口
 *
 * @author CH
 * @since 2024-12-12
 */
public interface MonitorSysGenScriptExecutionService extends IService<MonitorSysGenScriptExecution> {

    /**
     * 分页查询执行记录
     *
     * @param request 查询请求
     * @return 分页结果
     */
    Page<MonitorSysGenScriptExecution> pageForRecord(ScriptExecuteRecordQueryRequest request);

    /**
     * 获取执行记录详情
     *
     * @param recordId 记录ID
     * @return 执行记录
     */
    MonitorSysGenScriptExecution getDetailForRecord(Long recordId);

    /**
     * 删除执行记录
     *
     * @param recordId 记录ID
     * @return 是否成功
     */
    boolean deleteForRecord(Long recordId);

    /**
     * 清空执行记录
     *
     * @param scriptId 脚本ID（可选）
     * @return 删除的记录数
     */
    int clearForRecord(Integer scriptId);
}
