package com.chua.starter.project.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.project.mapper.SysDeviceMessageEventMapper;
import com.chua.starter.project.mapper.SysProjectMapper;
import com.chua.starter.project.service.SysDeviceMessageEventService;
import com.chua.starter.project.service.support.CameraService;
import com.chua.starter.project.entity.SysDeviceMessageEvent;
import com.chua.starter.system.pojo.device.CardDevicePageV1Request;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static com.chua.starter.project.service.impl.SysDeviceServiceImpl.reload;

/**
 * 设备消息事件服务实现类
 *
 * @author CH
 * @since 2023-04-15
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SysDeviceMessageEventServiceImpl extends ServiceImpl<SysDeviceMessageEventMapper, SysDeviceMessageEvent> implements
        SysDeviceMessageEventService {

    /**
     * 项目Mapper，用于获取项目相关信息
     */
    final SysProjectMapper sysProjectMapper;

    /**
     * 同步事件消息
     *
     * @param request 卡片设备分页请求参数 {@link CardDevicePageV1Request}
     *                <p>示例:</p>
     *                <pre>
     *                                                             {
     *                                                               "sysProjectId": 1,
     *                                                               "sysDeviceName": "摄像头001",
     *                                                               "sysDeviceSerialNumber": "SN123456789",
     *                                                               "startTime": "2023-04-15T00:00:00",
     *                                                               "endTime": "2023-04-15T23:59:59"
     *                                                             }
     *                                                             </pre>
     * @return 同步结果，成功返回true，失败返回错误信息
     */
    @Override
    public ReturnResult<Boolean> syncEventMessage(CardDevicePageV1Request request) {
        CameraService cameraService = reload(sysProjectMapper, request.getSysProjectId());
        if (null == cameraService) {
            return ReturnResult.illegal("当前系统不支持该设备厂家, 请联系管理员!");
        }
        // 调用摄像头服务同步事件消息
        boolean result = cameraService.syncEventMessage(request);
        if (!result) {
            return ReturnResult.illegal("同步事件消息失败");
        }
        return ReturnResult.preconditioning(true).toResult();
    }

}
