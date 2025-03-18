package com.chua.starter.system.service.support.hikvision.yunmou.consumer;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.chua.common.support.annotations.Spi;
import com.chua.common.support.lang.date.DateUtils;
import com.chua.common.support.objects.annotation.AutoInject;
import com.chua.starter.system.entity.SysDevice;
import com.chua.starter.system.entity.SysDeviceMessageEvent;
import com.chua.starter.system.service.SysDeviceMessageEventService;
import com.chua.starter.system.service.SysDeviceService;
import com.chua.starter.system.service.support.hikvision.HikvisionClient;
import com.chua.starter.system.service.support.hikvision.yunmou.pojo.OpenDeviceOnofflineMessageEvent;
import com.chua.starter.system.service.support.hikvision.yunmou.pojo.YunMouCardEventMessage;

import java.text.ParseException;

/**
 * 设备上下线消息
 *
 * @author CH
 */
@Spi("open_device_onoffline")
public class OpenDeviceOnofflineMessageHandler implements MessageHandler {
    @AutoInject
    private SysDeviceMessageEventService sysDeviceMessageEventService;
    @AutoInject
    private SysDeviceService sysDeviceService;

    @Override
    public void handle(YunMouCardEventMessage yunMouCardEventMessage, HikvisionClient client) {
        String content = yunMouCardEventMessage.getContent();
        OpenDeviceOnofflineMessageEvent openDeviceOnofflineMessageEvent = JSON.parseObject(content, OpenDeviceOnofflineMessageEvent.class);
        SysDeviceMessageEvent sysDeviceMessageEvent = sysDeviceMessageEventService.getOne(
                Wrappers.<SysDeviceMessageEvent>lambdaQuery()
                        .eq(SysDeviceMessageEvent::getSysDeviceMessageEventEventId, openDeviceOnofflineMessageEvent.getDeviceId())
                        .eq(SysDeviceMessageEvent::getSysDeviceMessageEventType, yunMouCardEventMessage.getMsgType())
                        .eq(SysDeviceMessageEvent::getSysDeviceSerialNumber, openDeviceOnofflineMessageEvent.getDeviceSerial())
                , false);
        if (null == sysDeviceMessageEvent) {
            sysDeviceMessageEvent = new SysDeviceMessageEvent();
        }
        sysDeviceMessageEvent.setSysDeviceMessageEventEventType(yunMouCardEventMessage.getMsgType());
        sysDeviceMessageEvent.setSysDeviceName(openDeviceOnofflineMessageEvent.getDeviceName());
        sysDeviceMessageEvent.setSysDeviceSerialNumber(openDeviceOnofflineMessageEvent.getDeviceSerial());
        sysDeviceMessageEvent.setSysDeviceMessageEventType(yunMouCardEventMessage.getMsgType());
        sysDeviceMessageEvent.setSysDeviceMessageEventEventType(openDeviceOnofflineMessageEvent.getMsgType());
        try {
            sysDeviceMessageEvent.setSysDeviceMessageEventDataTime(DateUtils.toLocalDateTime(DateUtils.parseDate(openDeviceOnofflineMessageEvent.getOccurTime(), "yyyy-MM-dd HH:mm:ss")));
        } catch (ParseException ignored) {
        }

        updateDeviceInfo(openDeviceOnofflineMessageEvent);

        if (null == sysDeviceMessageEvent.getSysDeviceMessageEventId()) {
            sysDeviceMessageEventService.save(sysDeviceMessageEvent);
            return;
        }

        sysDeviceMessageEventService.updateById(sysDeviceMessageEvent);
    }

    /**
     * 更新设备信息
     *
     * @param openDeviceOnofflineMessageEvent
     */
    private void updateDeviceInfo(OpenDeviceOnofflineMessageEvent openDeviceOnofflineMessageEvent) {
        try {
            updateDeviceInfo(openDeviceOnofflineMessageEvent);
            SysDevice sysDevice = sysDeviceService.getOne(
                    Wrappers.<SysDevice>lambdaQuery()
                            .eq(SysDevice::getSysDeviceSerialNumber, openDeviceOnofflineMessageEvent.getDeviceSerial())
                    , false);
            if (null != sysDevice) {
                sysDevice.setSysDeviceNetAddress(openDeviceOnofflineMessageEvent.getNatIp());
                sysDevice.setSysDeviceOnline("online".equalsIgnoreCase(openDeviceOnofflineMessageEvent.getMsgType()) ? 1 : 0);
                sysDevice.setSysDeviceType(openDeviceOnofflineMessageEvent.getDevType());
                try {
                    sysDevice.setCreateTime(DateUtils.toLocalDateTime(DateUtils.parseDate(openDeviceOnofflineMessageEvent.getRegTime(), "yyyy-MM-dd HH:mm:ss")));
                } catch (ParseException ignored) {
                }
                try {
                    sysDevice.setUpdateTime(DateUtils.toLocalDateTime(DateUtils.parseDate(openDeviceOnofflineMessageEvent.getOccurTime(), "yyyy-MM-dd HH:mm:ss")));
                } catch (ParseException ignored) {
                }
                sysDeviceService.updateById(sysDevice);
            }
        } catch (Exception ignored) {
        }
    }
}
