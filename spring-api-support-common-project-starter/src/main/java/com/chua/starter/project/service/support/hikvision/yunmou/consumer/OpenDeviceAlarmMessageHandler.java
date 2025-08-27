package com.chua.starter.project.service.support.hikvision.yunmou.consumer;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.chua.common.support.annotations.Spi;
import com.chua.common.support.lang.date.DateUtils;
import com.chua.common.support.objects.annotation.AutoInject;
import com.chua.starter.project.service.SysDeviceMessageEventService;
import com.chua.starter.project.service.support.hikvision.HikvisionClient;
import com.chua.starter.project.service.support.hikvision.yunmou.pojo.OpenDeviceAlarmMessageEvent;
import com.chua.starter.project.service.support.hikvision.yunmou.pojo.YunMouCardEventMessage;
import com.chua.starter.system.entity.SysDeviceMessageEvent;

import java.text.ParseException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 设备报警消息
 *
 * @author CH
 */
@Spi("open_device_alarm")
public class OpenDeviceAlarmMessageHandler implements MessageHandler {
    final static Map<String, String> ALARM_MAP = new HashMap<>(290);

    static {
        ALARM_MAP.put("pir", "人体感应事件");
        ALARM_MAP.put("callhelp", "紧急遥控按钮事件");
        ALARM_MAP.put("motiondetect", "移动侦测");
        ALARM_MAP.put("babycry", "婴儿啼哭");
        ALARM_MAP.put("magnetometer", "门磁告警");
        ALARM_MAP.put("fire", "烟感告警");
        ALARM_MAP.put("gas", "可燃气体告警");
        ALARM_MAP.put("waterlogging", "水浸告警");
        ALARM_MAP.put("emergency", "紧急按钮告警");
        ALARM_MAP.put("infrared", "人体感应告警");
        ALARM_MAP.put("shelteralarm", "遮挡告警");
        ALARM_MAP.put("videoloss", "视频丢失 Video Loss");
        ALARM_MAP.put("linedetection", "越界侦测");
        ALARM_MAP.put("fielddetection", "区域入侵");
        ALARM_MAP.put("facedetection", "人脸检测事件");
        ALARM_MAP.put("doorbell", "智能门铃告警");
        ALARM_MAP.put("curtain", "幕帘告警");
        ALARM_MAP.put("move_magnetometer", "单体门磁告警");
        ALARM_MAP.put("scenechangedetection", "场景变更侦测");
        ALARM_MAP.put("defocus", "虚焦侦测");
        ALARM_MAP.put("audioexception", "音频异常侦测");
        ALARM_MAP.put("leftdetection", "物品遗留侦测、电瓶车报警");
        ALARM_MAP.put("takedetection", "物品拿取侦测");
        ALARM_MAP.put("parkingdetection", "非法停车侦测");
        ALARM_MAP.put("highdensitydetection", "人员聚集侦测");
        ALARM_MAP.put("loiterdetection", "徘徊检测侦测");
        ALARM_MAP.put("rundetection", "快速移动侦测");
        ALARM_MAP.put("enterareadetection", "进入区域侦测");
        ALARM_MAP.put("exitareadetection", "离开区域侦测");
        ALARM_MAP.put("mag_gim", "磁干扰告警");
        ALARM_MAP.put("UnderVoltage", "电池电量低告警");
        ALARM_MAP.put("intrusion", "闯入告警");
        ALARM_MAP.put("baby_motion", "婴儿大动作告警");
        ALARM_MAP.put("PowerChange", "电源切换告警");
        ALARM_MAP.put("hightemperature", "温度过高告警");
        ALARM_MAP.put("lowtemperature", "温度过低告警");
        ALARM_MAP.put("highhumidity", "湿度过高告警");
        ALARM_MAP.put("lowhumidity", "湿度过低告警");
        ALARM_MAP.put("thermalimagingfire", "热成像火点告警");
        ALARM_MAP.put("whitelistface", "陌生人告警");
        ALARM_MAP.put("DoorLock-FingerprintOpenDoor", "指纹开门");
        ALARM_MAP.put("DoorLock-PasswordOpenDoor", "密码开门");
        ALARM_MAP.put("DoorLock-SwipeOpenDoor", "刷卡开门");
        ALARM_MAP.put("DoorLock-CenterRemoteOpenDoor", "中心远程开门");
        ALARM_MAP.put("DoorLock-AppRemoteOpenDoor", "APP远程开门");
        ALARM_MAP.put("DoorLock-KeyOpenDoor", "钥匙开门");
        ALARM_MAP.put("DoorLock-RemoteDeviceOpenDoor", "遥控器开门");
        ALARM_MAP.put("DoorLock-FingerprintInfoChange", "指纹信息改动");
        ALARM_MAP.put("DoorLock-CardInfoChange", "卡信息改动");
        ALARM_MAP.put("DoorLock-PasswordInfoChange", "密码信息改动");
        ALARM_MAP.put("DoorLock-UserInfoChange", "用户信息改动");
        ALARM_MAP.put("DoorLock-SystemInfoChange", "系统信息改动");
        ALARM_MAP.put("DoorLock-AntiHijackingFingerprint", "防劫持指纹告警");
        ALARM_MAP.put("DoorLock-AntiHijackingPassword", "防劫持密码告警");
        ALARM_MAP.put("DoorLock-PryDoor", "撬门告警");
        ALARM_MAP.put("DoorLock-Locked", "门锁锁定告警");
        ALARM_MAP.put("DoorLock-BatteryLow", "电量不足告警");
        ALARM_MAP.put("DoorLock-Blacklist", "黑名单告警");
        ALARM_MAP.put("DoorLock-Offline", "掉线告警");
        ALARM_MAP.put("callmsg", "呼叫消息");
        ALARM_MAP.put("hangupmsg", "挂断消息");
        ALARM_MAP.put("DoorLock-DoorOpenAlarm", "智能锁开门提醒");
        ALARM_MAP.put("DoorLock-ConfigChangeAlarm", "智能锁配置改动提醒");
        ALARM_MAP.put("DoorLock-DoorbellAlarm", "门铃提醒");
        ALARM_MAP.put("DoorLock-Reset", "智能锁复位");
        ALARM_MAP.put("DoorLock-DataClear", "智能锁数据清空");
        ALARM_MAP.put("opendoormsg", "开门消息");
        ALARM_MAP.put("nothomemsg", "未回家消息");
        ALARM_MAP.put("lowbattery", "电量不足告警");
        ALARM_MAP.put("abnormalopendoor", "异常开门告警");
        ALARM_MAP.put("unclosed", "门未关告警");
        ALARM_MAP.put("hijack", "挟持告警");
        ALARM_MAP.put("pry", "撬开告警");
        ALARM_MAP.put("abnormalharddisk", "硬盘错误");
        ALARM_MAP.put("face_stranger", "陌生人告警");
        ALARM_MAP.put("face_compare", "人脸比对告警");
        ALARM_MAP.put("intelligentDetection", "智能检测告警");
        ALARM_MAP.put("temperatureAlarm", "温度告警");
        ALARM_MAP.put("pm25LightPollute", "PM2.5轻度污染");
        ALARM_MAP.put("pm25SeverePollute", "PM2.5重度污染");
        ALARM_MAP.put("hchoLightPollute", "甲醛轻度污染");
        ALARM_MAP.put("hchoSeverePollute", "甲醛重度污染");
        ALARM_MAP.put("tvocLightPollute", "有机气体TVOC轻度污染");
        ALARM_MAP.put("tvocSeverePollute", "有机气体TVOC重度污染");
        ALARM_MAP.put("ThirdPartyVendorCaption", "第三方抓图");
        ALARM_MAP.put("peopleCountingDetection", "区域人数检测");
        ALARM_MAP.put("face_swapper", "人脸抠图消息");
        ALARM_MAP.put("lowtemperaturepre", "温度过低预警");
        ALARM_MAP.put("hightemperaturepre", "温度过高预警");
        ALARM_MAP.put("cameraHighTemperature", "高温告警 High Temperature Alarm");
        ALARM_MAP.put("petFeederState_1", "宠物喂食器状态1 Pet Feeder Status One");
        ALARM_MAP.put("petFeederState_2", "宠物喂食器状态2 Pet Feeder Status Two");
        ALARM_MAP.put("petFeederState_3", "宠物喂食器状态3 Pet Feeder Status Three");
        ALARM_MAP.put("petFeederState_4", "宠物喂食器状态4 Pet Feeder Status Four");
        ALARM_MAP.put("petFeederState_5", "宠物喂食器状态5 Pet Feeder Status Five");
        ALARM_MAP.put("devoffline", "设备掉线");
        ALARM_MAP.put("io", "IO告警");
        ALARM_MAP.put("io-1", "IO-1告警");
        ALARM_MAP.put("io-2", "IO-2告警");
        ALARM_MAP.put("io-3", "IO33告警");
        ALARM_MAP.put("io-4", "IO-4告警");
        ALARM_MAP.put("io-5", "IO-5告警");
        ALARM_MAP.put("io-6", "IO-6告警");
        ALARM_MAP.put("io-7", "IO-7告警");
        ALARM_MAP.put("io-8", "IO-8告警");
        ALARM_MAP.put("io-9", "IO-9告警");
        ALARM_MAP.put("io-10", "IO-10告警");
        ALARM_MAP.put("io-11", "IO-11告警");
        ALARM_MAP.put("io-12", "IO-12告警");
        ALARM_MAP.put("io-13", "IO-13告警");
        ALARM_MAP.put("io-14", "IO-14告警");
        ALARM_MAP.put("io-15", "IO-15告警");
        ALARM_MAP.put("io-16", "IO-16告警");
        ALARM_MAP.put("TemperatureMeasurementPreviewAlarmRecovery", "温度预警恢复");
        ALARM_MAP.put("TemperatureMeasurementAlarmRecovery", "温度报警恢复");
        ALARM_MAP.put("VMD", "移动侦测告警开始");
        ALARM_MAP.put("tamperdetection", "遮挡告警开始");
        ALARM_MAP.put("alarmTrig", "即时防区告警");
        ALARM_MAP.put("alarmRest", "即时防区恢复");
        ALARM_MAP.put("audio24Trig", "24小时有声防区告警");
        ALARM_MAP.put("audio24Rest", "24小时有声防区恢复");
        ALARM_MAP.put("enterexitTrig", "延时防区告警");
        ALARM_MAP.put("enterexitRest", "延时防区恢复");
        ALARM_MAP.put("internalTrig", "内部延时防区告警");
        ALARM_MAP.put("internalRest", "内部延时防区恢复");
        ALARM_MAP.put("fireTrig", "火警告警");
        ALARM_MAP.put("fireRest/FirePointAlarmRecovery", "火警告警恢复");
        ALARM_MAP.put("perimeterTrig", "周界防区告警");
        ALARM_MAP.put("perimeterRest", "周界防区恢复");
        ALARM_MAP.put("silent24Trig", "24小时无声防区告警");
        ALARM_MAP.put("silent24Rest", "24小时无声防区恢复");
        ALARM_MAP.put("aux24Trig", "24小时辅助防区告警");
        ALARM_MAP.put("aux24Rest", "24小时辅助防区恢复");
        ALARM_MAP.put("vib24Trig", "24小时震动防区告警");
        ALARM_MAP.put("vib24Rest", "24小时震动防区恢复");
        ALARM_MAP.put("sensorDemol", "防区感应器被拆");
        ALARM_MAP.put("sensorRecov", "防区感应器被拆恢复");
        ALARM_MAP.put("softEmergTrig", "软防区紧急告警");
        ALARM_MAP.put("softFireTrig", "软防区火警");
        ALARM_MAP.put("softBanditTrig", "软防区匪警");
        ALARM_MAP.put("hostageReport", "挟持报告");
        ALARM_MAP.put("devTampering", "设备防拆");
        ALARM_MAP.put("devTamperRecov", "设备防拆恢复");
        ALARM_MAP.put("ACDown", "交流电掉电");
        ALARM_MAP.put("ACRecov", "交流电恢复");
        ALARM_MAP.put("lowBatVol", "蓄电池电压低");
        ALARM_MAP.put("norBatVol", "蓄电池电压正常");
        ALARM_MAP.put("telDiscon", "电话线断开");
        ALARM_MAP.put("telRecov", "电话线连接");
        ALARM_MAP.put("XBUSDiscon", "扩展总线模块掉线");
        ALARM_MAP.put("XBUSRecov", "扩展总线模块掉线恢复");
        ALARM_MAP.put("keyboardDisconn", "键盘掉线");
        ALARM_MAP.put("keyboardRecov", "键盘恢复");
        ALARM_MAP.put("KBUSTrigDiscon", "键盘总线上触发器掉线");
        ALARM_MAP.put("KBUSTrigRecov", "键盘总线上触发器恢复");
        ALARM_MAP.put("autoArmFail", "自动布防失败");
        ALARM_MAP.put("autoDisarmFail", "自动撤防失败");
        ALARM_MAP.put("wirelessExcep", "无线网络异常");
        ALARM_MAP.put("wirelessRecov", "无线网络恢复正常");
        ALARM_MAP.put("SIMExcep", "SIM卡异常");
        ALARM_MAP.put("SIMRecov", "SIM卡恢复正常");
        ALARM_MAP.put("hostReset", "主机复位");
        ALARM_MAP.put("disarm", "撤防");
        ALARM_MAP.put("arm", "布防");
        ALARM_MAP.put("autoDisarm", "自动撤防");
        ALARM_MAP.put("autoArm", "自动布防");
        ALARM_MAP.put("clear", "消警");
        ALARM_MAP.put("instantArm", "即时布防");
        ALARM_MAP.put("keyDisarm", "钥匙防区撤防");
        ALARM_MAP.put("keyArm", "钥匙防区布防");
        ALARM_MAP.put("stayArm", "留守布防");
        ALARM_MAP.put("forcedArm", "强制布防");
        ALARM_MAP.put("bypass", "旁路");
        ALARM_MAP.put("bypassRecov", "旁路恢复");
        ALARM_MAP.put("grpBypass", "子系统组旁路");
        ALARM_MAP.put("grpBypassRecov", "子系统组旁路恢复");
        ALARM_MAP.put("manualRep", "手动测试报告");
        ALARM_MAP.put("timingRep", "定时测试报告");
        ALARM_MAP.put("singleDisarm", "单防区撤防");
        ALARM_MAP.put("singleArm", "单防区布防");
        ALARM_MAP.put("keypadLock", "键盘锁定");
        ALARM_MAP.put("keypadUnlock", "即时布防");
        ALARM_MAP.put("instantArm", "键盘解锁");
        ALARM_MAP.put("printerOff", "打印机掉线");
        ALARM_MAP.put("printerOn", "打印机恢复");
        ALARM_MAP.put("instantDisarm", "即时撤防");
        ALARM_MAP.put("stayDisarm", "留守撤防");
        ALARM_MAP.put("triggerScheOn", "定时开启触发器");
        ALARM_MAP.put("triggerScheOff", "定时关闭触发器");
        ALARM_MAP.put("acTrigFail", "定时开启触发器失败");
        ALARM_MAP.put("deacTrigFail", "定时关闭触发器失败");
        ALARM_MAP.put("enterProgram", "进入编程");
        ALARM_MAP.put("exitProgram", "退出编程");
        ALARM_MAP.put("GPKDiscon", "键盘总线上GP/K掉线");
        ALARM_MAP.put("GPKRecov", "键盘总线上GP/K恢复");
        ALARM_MAP.put("MNKDiscon", "键盘总线上MN/K掉线");
        ALARM_MAP.put("MNKRecov", "键盘总线上MN/K恢复");
        ALARM_MAP.put("IPConflict", "IP冲突");
        ALARM_MAP.put("IPRecov", "IP正常");
        ALARM_MAP.put("netDiscon", "网线断开");
        ALARM_MAP.put("netRecov", "网线正常");
        ALARM_MAP.put("VMDRecov", "移动侦测告警结束");
        ALARM_MAP.put("tamperRecov", "遮挡告警结束");
        ALARM_MAP.put("videoRecov", "视频信号恢复");
        ALARM_MAP.put("viIOUnmatch", "输入/输出视频制式不匹配");
        ALARM_MAP.put("viIORecov", "输入/输出视频制式恢复正常");
        ALARM_MAP.put("viInputExc", "视频输入异常");
        ALARM_MAP.put("viInputRecov", "视频输入恢复正常");
        ALARM_MAP.put("HDDfull", "硬盘满");
        ALARM_MAP.put("HDDfree", "硬盘空闲");
        ALARM_MAP.put("HDDexception", "硬盘出错");
        ALARM_MAP.put("HDDRecov", "硬盘恢复正常");
        ALARM_MAP.put("uploadPicFail", "图片上传失败");
        ALARM_MAP.put("DetOffline", "探测器离线");
        ALARM_MAP.put("DetOnline", "探测器恢复在线");
        ALARM_MAP.put("DetLowBattery", "探测器电量欠压");
        ALARM_MAP.put("DetBatteryRecov", "探测器电量恢复正常");
        ALARM_MAP.put("zoneAddDet", "防区添加探测器");
        ALARM_MAP.put("zoneDeleteDet", "防区删除探测器");
        ALARM_MAP.put("wifiException", "WIFI通信异常");
        ALARM_MAP.put("wifiRecov", "WIFI通信恢复正常");
        ALARM_MAP.put("rfException", "RF信号异常");
        ALARM_MAP.put("rfRecov", "RF信号恢复正常");
        ALARM_MAP.put("MINOR_HOST_DESMANTLE_ALARM", "主机防拆告警");
        ALARM_MAP.put("MINOR_HOST_DESMANTLE_RESUME", "主机防拆恢复");
        ALARM_MAP.put("MINOR_CARD_READER_DESMANTLE_ALARM", "读卡器防拆告警");
        ALARM_MAP.put("MINOR_CARD_READER_DESMANTLE_RESUME", "读卡器防拆恢复");
        ALARM_MAP.put("MINOR_CASE_SENSOR_ALARM", "事件输入告警");
        ALARM_MAP.put("MINOR_CASE_SENSOR_RESUME", "事件输入恢复");
        ALARM_MAP.put("MINOR_SECURITY_MODULE_DESMANTLE_ALARM", "门控安全模块防拆告警");
        ALARM_MAP.put("MINOR_SECURITY_MODULE_DESMANTLE_RESUME", "门控安全模块防拆恢复");
        ALARM_MAP.put("MINOR_NET_BROKEN", "网络断开");
        ALARM_MAP.put("MINOR_NET_RESUME", "网络恢复");
        ALARM_MAP.put("MINOR_DEV_POWER_ON", "设备上电启动");
        ALARM_MAP.put("MINOR_DEV_POWER_OFF", "设备掉电关闭");
        ALARM_MAP.put("MINOR_DOOR_OPEN_ABNORMAL", "门异常打开（门磁）");
        ALARM_MAP.put("emergHelp", "紧急呼叫求助告警");
        ALARM_MAP.put("emergHelpRecov", "紧急呼叫求助恢复");
        ALARM_MAP.put("consulting", "业务咨询开始");
        ALARM_MAP.put("consulted", "业务咨询结束");
        ALARM_MAP.put("overAlarm", "超时告警");
        ALARM_MAP.put("overAlarmRecov", "超时告警恢复");
        ALARM_MAP.put("singleCancelArm", "单防区消警");
        ALARM_MAP.put("BUSQuery", "总线搜索");
        ALARM_MAP.put("BUSOpen", "总线开路告警");
        ALARM_MAP.put("BUSOpenRecov", "总线开路告警恢复");
        ALARM_MAP.put("BUSShort", "总线短路告警");
        ALARM_MAP.put("BUSShortRecov", "总线短路告警恢复");
        ALARM_MAP.put("exModuleExcep", "扩展模块异常");
        ALARM_MAP.put("exModuleRecov", "扩展模块恢复");
        ALARM_MAP.put("exModuleLowVol", "扩展模块电压低");
        ALARM_MAP.put("exModuleVolRecov", "扩展模块电压正常");
        ALARM_MAP.put("exModuleTamp", "扩展模块防拆触发");
        ALARM_MAP.put("exModuleTampRecov", "扩展模块防拆恢复");
        ALARM_MAP.put("exModuleAcDown", "扩展模块交流电掉电");
        ALARM_MAP.put("exModuleAcOn", "扩展模块交流电恢复");
        ALARM_MAP.put("repeaterTamp", "无线中继器防拆触发");
        ALARM_MAP.put("repeaterTampRecov", "无线中继器防拆恢复");
        ALARM_MAP.put("zoneOpenAlarm", "防区开路提醒");
        ALARM_MAP.put("zoneShortAlarm", "防区短路提醒");
        ALARM_MAP.put("telConTest", "电话链路测试");
        ALARM_MAP.put("exModuleDiscon", "扩展模块掉线");
        ALARM_MAP.put("exModuleCon", "扩展模块掉线恢复");
        ALARM_MAP.put("wlModuleDiscon", "无线扩展模块掉线");
        ALARM_MAP.put("wlModuleCon", "无线扩展模块掉线恢复");
        ALARM_MAP.put("repeaterDiscon", "无线中继器掉线");
        ALARM_MAP.put("repeaterCon", "无线中继器掉线恢复");
        ALARM_MAP.put("netExceed", "流量超额");
        ALARM_MAP.put("delExModule", "删除扩展模块");
        ALARM_MAP.put("addExModule", "添加扩展模块");
        ALARM_MAP.put("delRepeater", "删除无线中继器");
        ALARM_MAP.put("addRepeater", "添加无线中继器");
        ALARM_MAP.put("BUSRegistration", "总线注册");
        ALARM_MAP.put("TimingWakingSnapshot", "定时抓拍");
        ALARM_MAP.put("channelOccupy", "消防通道占用");
        ALARM_MAP.put("OffDuty", "人员离岗");
        ALARM_MAP.put("channelOccupyOff", "消防通道占用恢复");
        ALARM_MAP.put("OnDuty", "人员离岗恢复");
        ALARM_MAP.put("thermalimagingfireOff", "热成像火点告警恢复");
        ALARM_MAP.put("TestTemperature", "测温告警");
        ALARM_MAP.put("TestTemperatureExpect", "测温预警");
    }

    @AutoInject
    private SysDeviceMessageEventService sysDeviceMessageEventService;

    @Override
    public void handle(YunMouCardEventMessage yunMouCardEventMessage, HikvisionClient client) {
        String content = yunMouCardEventMessage.getContent();
        OpenDeviceAlarmMessageEvent openDeviceAlarmMessageEvent = JSON.parseObject(content, OpenDeviceAlarmMessageEvent.class);
        SysDeviceMessageEvent sysDeviceMessageEvent = sysDeviceMessageEventService.getOne(
                Wrappers.<SysDeviceMessageEvent>lambdaQuery()
                        .eq(SysDeviceMessageEvent::getSysDeviceMessageEventEventId, openDeviceAlarmMessageEvent.getAlarmId())
                        .eq(SysDeviceMessageEvent::getSysDeviceMessageEventType, yunMouCardEventMessage.getMsgType())
                        .eq(SysDeviceMessageEvent::getSysDeviceSerialNumber, openDeviceAlarmMessageEvent.getDevSerial())
                , false);
        if (null == sysDeviceMessageEvent) {
            sysDeviceMessageEvent = new SysDeviceMessageEvent();
        }
        sysDeviceMessageEvent.setSysDeviceMessageEventEventType(yunMouCardEventMessage.getMsgType());
        sysDeviceMessageEvent.setSysDeviceName(openDeviceAlarmMessageEvent.getDevSerial());
        sysDeviceMessageEvent.setSysDeviceMessageEventChannelName(openDeviceAlarmMessageEvent.getChannelName());
        sysDeviceMessageEvent.setSysDeviceMessageEventChannelNo(String.valueOf(openDeviceAlarmMessageEvent.getChannel()));
        sysDeviceMessageEvent.setSysDeviceMessageEventLocation(openDeviceAlarmMessageEvent.getLocation());
        sysDeviceMessageEvent.setSysDeviceSerialNumber(openDeviceAlarmMessageEvent.getDevSerial());
        sysDeviceMessageEvent.setSysDeviceMessageEventType(yunMouCardEventMessage.getMsgType());
        sysDeviceMessageEvent.setSysDeviceMessageEventEventType(openDeviceAlarmMessageEvent.getAlarmType());
        sysDeviceMessageEvent.setSysDeviceMessageEventEventTypeName(ALARM_MAP.getOrDefault(openDeviceAlarmMessageEvent.getAlarmType(), "未知告警"));
        sysDeviceMessageEvent.setSysDeviceMessageEventEventPictureUrl(
                Optional.ofNullable(openDeviceAlarmMessageEvent.getPictureList())
                        .orElse(Collections.emptyList())
                        .stream()
                        .map(OpenDeviceAlarmMessageEvent.PictureListDTO::getUrl)
                        .collect(Collectors.joining(",")));
        try {
            sysDeviceMessageEvent.setSysDeviceMessageEventDataTime(DateUtils.toLocalDateTime(DateUtils.parseDate(
                    openDeviceAlarmMessageEvent.getAlarmTime(), "yyyy-MM-dd HH:mm:ss")));
        } catch (ParseException ignored) {
        }

        if (null == sysDeviceMessageEvent.getSysDeviceMessageEventId()) {
            sysDeviceMessageEventService.save(sysDeviceMessageEvent);
            return;
        }

        sysDeviceMessageEventService.updateById(sysDeviceMessageEvent);
    }

}
