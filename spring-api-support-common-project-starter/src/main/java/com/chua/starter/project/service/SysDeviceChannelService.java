package com.chua.starter.project.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.system.entity.SysDeviceChannel;
import com.chua.starter.system.pojo.device.SysDeviceChannelV1Request;

/**
 * 设备管道服务接口
 *
 * @author CH
 * @since 2023-04-05
 */
public interface SysDeviceChannelService extends IService<SysDeviceChannel> {

    /**
     * 同步设备管道事件消息
     * <p>
     * 该方法用于处理设备管道的事件消息同步，确保设备状态与系统记录一致。
     *
     * @param v1Request 设备管道请求对象，包含设备序列号和管道列表信息
     *                  示例：{
     *                  "sysDeviceSerialNumber": "SN123456789",
     *                  "channels": [
     *                  {
     *                  "channelId": "CH001",
     *                  "channelName": "主通道",
     *                  "status": "ACTIVE"
     *                  }
     *                  ]
     *                  }
     * @return ReturnResult<Boolean> 返回操作结果，成功返回true，失败返回false
     */
    ReturnResult<Boolean> syncEventMessage(SysDeviceChannelV1Request v1Request);
}
