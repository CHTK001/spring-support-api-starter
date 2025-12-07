package com.chua.starter.project.service.support.hikvision.yunmou.consumer;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.chua.common.support.annotations.Spi;
import com.chua.common.support.lang.date.DateUtils;
import com.chua.common.support.objects.annotation.AutoInject;
import com.chua.starter.project.service.SysDeviceMessageEventService;
import com.chua.starter.project.service.support.hikvision.HikvisionClient;
import com.chua.starter.project.service.support.hikvision.yunmou.pojo.OpenEventAccessMessageEvent;
import com.chua.starter.project.service.support.hikvision.yunmou.pojo.YunMouCardEventMessage;
import com.chua.starter.entity.project.SysDeviceMessageEvent;

import java.text.ParseException;

/**
 * 门禁事件
 *
 * @author CH
 */
@Spi("open_event_access")
public class OpenEventAccessMessageHandler implements MessageHandler {

    @AutoInject
    private SysDeviceMessageEventService sysDeviceMessageEventService;

    @Override
    public void handle(YunMouCardEventMessage yunMouCardEventMessage, HikvisionClient client) {
        String content = yunMouCardEventMessage.getContent();
        OpenEventAccessMessageEvent openEventAccessMessageEvent = JSON.parseObject(content, OpenEventAccessMessageEvent.class);
        SysDeviceMessageEvent sysDeviceMessageEvent = sysDeviceMessageEventService.getOne(
                Wrappers.<SysDeviceMessageEvent>lambdaQuery()
                        .eq(SysDeviceMessageEvent::getSysDeviceMessageEventEventId, openEventAccessMessageEvent.getEventId())
                        .eq(SysDeviceMessageEvent::getSysDeviceMessageEventType, yunMouCardEventMessage.getMsgType())
                        .eq(SysDeviceMessageEvent::getSysDeviceSerialNumber, openEventAccessMessageEvent.getDeviceSerial())
                , false);
        if (null == sysDeviceMessageEvent) {
            sysDeviceMessageEvent = new SysDeviceMessageEvent();
        }
        sysDeviceMessageEvent.setSysDeviceMessageEventCardNo(String.valueOf(openEventAccessMessageEvent.getCardReaderNo()));
        sysDeviceMessageEvent.setSysDeviceMessageEventCardReaderKind(String.valueOf(openEventAccessMessageEvent.getCardReaderKind()));
        sysDeviceMessageEvent.setSysDeviceMessageEventCardReaderNo(openEventAccessMessageEvent.getCardReaderNo());
        sysDeviceMessageEvent.setSysDeviceMessageEventChannelNo(String.valueOf(openEventAccessMessageEvent.getChannelNo()));
        sysDeviceMessageEvent.setSysDeviceMessageEventChannelName(openEventAccessMessageEvent.getChannelName());
        sysDeviceMessageEvent.setSysDeviceMessageEventDoorNo(String.valueOf(openEventAccessMessageEvent.getDoorNo()));
        sysDeviceMessageEvent.setSysDeviceMessageEventEmployeeNo(openEventAccessMessageEvent.getEmployeeNo());
        sysDeviceMessageEvent.setSysDeviceMessageEventEmployeeName(openEventAccessMessageEvent.getName());
        sysDeviceMessageEvent.setSysDeviceMessageEventEventCode(openEventAccessMessageEvent.getEventCode());
        sysDeviceMessageEvent.setSysDeviceMessageEventEventDescription(openEventAccessMessageEvent.getEventDescription());
        sysDeviceMessageEvent.setSysDeviceMessageEventEventId(openEventAccessMessageEvent.getEventId());
        sysDeviceMessageEvent.setSysDeviceMessageEventEventPictureUrl(openEventAccessMessageEvent.getPictureURL());
        sysDeviceMessageEvent.setSysDeviceMessageEventEventSource(openEventAccessMessageEvent.getEventSource());
        sysDeviceMessageEvent.setSysDeviceMessageEventEventType(yunMouCardEventMessage.getMsgType());
        sysDeviceMessageEvent.setSysDeviceMessageEventType(yunMouCardEventMessage.getMsgType());
        sysDeviceMessageEvent.setSysDeviceName(openEventAccessMessageEvent.getDeviceName());
        sysDeviceMessageEvent.setSysDeviceSerialNumber(openEventAccessMessageEvent.getDeviceSerial());
        try {
            sysDeviceMessageEvent.setSysDeviceMessageEventDataTime(DateUtils.toLocalDateTime(DateUtils.parseDate(openEventAccessMessageEvent.getDateTime(), "yyyy-MM-dd'T'HH:mm:ss+08:00")));
        } catch (ParseException ignored) {
        }
        if (null == sysDeviceMessageEvent.getSysDeviceMessageEventId()) {
            sysDeviceMessageEventService.save(sysDeviceMessageEvent);
            return;
        }

        sysDeviceMessageEventService.updateById(sysDeviceMessageEvent);
    }
}
