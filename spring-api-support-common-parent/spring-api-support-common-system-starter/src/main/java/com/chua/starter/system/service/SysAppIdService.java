package com.chua.starter.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chua.starter.entity.system.SysAppId;

/**
* appid应用标识 服务
*
* @since 2025/01/05
* @author CH
 */
public interface SysAppIdService extends IService<SysAppId> {

    /**
    * 根据 appid值 查询appid
    *
    * @param appIdValue appid值
    * @return AppId
     */
    SysAppId getByAppIdValue(String appIdValue);

    /**
    * 更新使用次数和最后使用时间
    *
    * @param appIdValue appid值
     */
    void updateUseInfo(String appIdValue);
}

