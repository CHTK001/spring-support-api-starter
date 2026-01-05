package com.chua.starter.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chua.starter.entity.system.SysAppId;

/**
 * AppId应用标识 Service
 *
 * @author CH
 * @since 2025/01/05
 */
public interface SysAppIdService extends IService<SysAppId> {

    /**
     * 根据 appId值 查询AppId
     *
     * @param appIdValue AppId值
     * @return AppId
     */
    SysAppId getByAppIdValue(String appIdValue);

    /**
     * 更新使用次数和最后使用时间
     *
     * @param appIdValue AppId值
     */
    void updateUseInfo(String appIdValue);
}

