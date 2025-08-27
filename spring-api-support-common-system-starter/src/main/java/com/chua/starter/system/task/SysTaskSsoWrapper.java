package com.chua.starter.system.task;

import com.chua.starter.system.entity.SysTaskSso;
import com.chua.starter.system.service.SysTaskSsoService;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 系统任务SSO包装类
 * @author CH
 * @since 2024/12/4
 */
@Data
@AllArgsConstructor
public class SysTaskSsoWrapper {

    /**
     * 注释：声明一个SysTaskSso类型的变量，用于后续的单点登录任务处理
     */
    private SysTaskSso sysTaskSso;

    /**
     * 注释：声明一个SysTaskSsoService类型的变量，用于调用单点登录任务相关服务
     */
    private SysTaskSsoService service;
}
