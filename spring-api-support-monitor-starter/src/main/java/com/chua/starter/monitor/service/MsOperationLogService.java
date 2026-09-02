package com.chua.starter.monitor.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chua.starter.monitor.entity.MsOperationLog;

/**
 * 操作日志服务接口。
 *
 * @since 2026-08-11
 * @author CH
 */
public interface MsOperationLogService extends IService<MsOperationLog> {

    /**
     * 记录一条操作日志。失败时静默处理，不影响主业务流程。
     *
     * @param operator    操作用户ID
     * @param operation   操作类型（CREATE/UPDATE/DELETE/ENABLE/DISABLE/MOVE/SET/DEL）
     * @param targetModule 目标模块（HOST/TASK/REDIS/CONFIG）
     * @param targetId    目标对象ID
     * @param description 操作描述
     */
    void log(String operator, String operation, String targetModule, String targetId, String description);
}
