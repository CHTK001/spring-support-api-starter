package com.chua.starter.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.system.entity.SysDeviceMessageEvent;
import com.chua.starter.system.mapper.SysDeviceMessageEventMapper;
import com.chua.starter.system.mapper.SysProjectMapper;
import com.chua.starter.system.pojo.device.CardDevicePageV1Request;
import com.chua.starter.system.service.SysDeviceMessageEventService;
import com.chua.starter.system.service.support.CameraService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static com.chua.starter.system.service.impl.SysDeviceServiceImpl.reload;

@Service
@Slf4j
@RequiredArgsConstructor
public class SysDeviceMessageEventServiceImpl extends ServiceImpl<SysDeviceMessageEventMapper, SysDeviceMessageEvent> implements
        SysDeviceMessageEventService {
    final SysProjectMapper sysProjectMapper;

    @Override
    public ReturnResult<Boolean> syncEventMessage(CardDevicePageV1Request request) {
        CameraService cameraService = reload(sysProjectMapper, request.getSysProjectId());
        if (null == cameraService) {
            return ReturnResult.illegal("当前系统不支持该设备厂家, 请联系管理员!");
        }
        return ReturnResult.preconditioning(true).toResult();
    }

}
