package com.chua.starter.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.system.entity.SysDeviceChannel;
import com.chua.starter.system.pojo.device.SysDeviceChannelV1Request;

public interface SysDeviceChannelService extends IService<SysDeviceChannel> {


    /**
     * 更新管道消息
     *
     * @param v1Request 管道
     * @return boolean
     */
    ReturnResult<Boolean> syncEventMessage(SysDeviceChannelV1Request v1Request);
}
