package com.chua.starter.project.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.entity.project.SysDeviceMessageEvent;
import com.chua.starter.project.pojo.device.CardDevicePageV1Request;

public interface SysDeviceMessageEventService extends IService<SysDeviceMessageEvent> {


    /**
     * 同步事件
     *
     * @param request 请求
     * @return 结果
     */
    ReturnResult<Boolean> syncEventMessage(CardDevicePageV1Request request);
}
