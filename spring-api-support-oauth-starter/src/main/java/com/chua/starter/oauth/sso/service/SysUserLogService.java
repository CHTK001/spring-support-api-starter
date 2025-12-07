package com.chua.starter.oauth.sso.service;

import com.chua.starter.service.model.SysUserLog;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 用户日志服务接口
 *
 * @author CH
 */
public interface SysUserLogService extends IService<SysUserLog> {

    /**
     * 异步保存用户日志
     * 使用 Java 21 虚拟线程实现异步处理，提高系统性能
     * 只保存登录和登出类型的用户操作日志
     *
     * @param userLog 用户日志实体对象，包含完整的日志信息
     */
    void asyncSaveUserLog(SysUserLog userLog);

}
