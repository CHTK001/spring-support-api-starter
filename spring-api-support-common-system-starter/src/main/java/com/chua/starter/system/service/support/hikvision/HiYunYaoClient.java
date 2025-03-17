package com.chua.starter.system.service.support.hikvision;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chua.common.support.converter.Converter;
import com.chua.common.support.function.InitializingAware;
import com.chua.common.support.lang.date.DateTime;
import com.chua.common.support.utils.ThreadUtils;
import com.chua.starter.system.entity.SysDeviceCardEvent;
import com.chua.starter.system.entity.SysProject;
import com.chua.starter.system.pojo.device.CardDevicePageV1Request;
import com.chua.starter.system.pojo.device.DevicePreviewUrlV1Request;
import com.chua.starter.system.service.support.hikvision.adaptor.CarFactory;
import com.chua.starter.system.service.support.hikvision.adaptor.FaceFactory;
import com.chua.starter.system.service.support.hikvision.adaptor.MetroCardFactory;
import com.chua.starter.system.service.support.hikvision.adaptor.ResourceFactory;
import com.chua.starter.system.service.support.hikvision.adaptor.sub.EntranceFactory;
import com.chua.starter.system.service.support.hikvision.query.SysDeviceCameraOnOffline;
import com.chua.starter.system.service.support.hikvision.result.OnOffline;
import com.chua.starter.system.service.support.hikvision.yunyao.YunYaoEntranceFactory;
import com.chua.starter.system.service.support.hikvision.yunyao.YunYaoResourceFactory;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author CH
 */
@Slf4j
public class HiYunYaoClient extends HikvisionClient implements InitializingAware {


    private static final String TOKEN_URL = "https://api2.hik-cloud.com/oauth/token";
    private final SysProject factory;
    private final ScheduledExecutorService SCHEDULED_EXECUTOR_SERVICE = ThreadUtils.newScheduledThreadPoolExecutor(1, "check-token");
    private String token;

    {
        Runtime.getRuntime().addShutdownHook(new Thread(SCHEDULED_EXECUTOR_SERVICE::shutdownNow));

        SCHEDULED_EXECUTOR_SERVICE.scheduleAtFixedRate(this::afterPropertiesSet, 0, 20, TimeUnit.MINUTES);
    }

    public HiYunYaoClient(String[] protocol, String host, String appKey, String secretAccessKey, SysProject sysProject) {
        super(protocol, host, appKey, secretAccessKey);
        this.factory = sysProject;
        afterPropertiesSet();
    }

    @Override
    public CarFactory getCarFactory() {
        return super.getCarFactory();
    }

    @Override
    public ResourceFactory getResourceFactory() {
        return new YunYaoResourceFactory(this, factory, token);
    }

    @Override
    public FaceFactory getFaceFactory() {
        return super.getFaceFactory();
    }

    @Override
    public MetroCardFactory getMetroCardFactory() {
        return super.getMetroCardFactory();
    }

    @Override
    public EntranceFactory getEntranceFactory() {
        return new YunYaoEntranceFactory(this, factory, token);
    }

    @Override
    public List<SysDeviceCameraOnOffline> getOnOfflineTimeline(String sysCameraTemplateSerialNumber, String sysCameraTemplateResourceType) {
        Map<String, Object> params = new HashMap<>(3);
        params.put("projectId", factory.getSysProjectSign());
        params.put("deviceSerialList", new String[]{sysCameraTemplateSerialNumber});

        Map<String, String> headers = new HashMap<>(1);
        headers.put("access_token", token);
        try {
            HiYunMouHttpClientResult result = HiYunYaoClientUtils.sendPostResponse("https://open.hikyun.com/artemis/api/eits/v2/global/device/onoffline/last", headers, params);
            JSONObject jsonObject = JSON.parseObject(result.getData());
            log.info("{}", jsonObject.getString("msg"));
            return jsonObject.getJSONObject("data").getJSONArray("list").stream()
                    .map(e -> {
                        JSONObject e1 = (JSONObject) e;
                        SysDeviceCameraOnOffline item = new SysDeviceCameraOnOffline();
                        item.setCollectTime(Converter.convertIfNecessary(e1.getString("occurTime"), LocalDateTime.class));
                        item.setOnline(e1.getInteger("status"));

                        return item;
                    }).toList();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<OnOffline> checkOnOffline(String[] sysCameraTemplateSerialNumbers) {
        Map<String, Object> params = new HashMap<>(3);
        params.put("projectId", factory.getSysProjectSign());
        params.put("deviceSerials", sysCameraTemplateSerialNumbers);

        Map<String, String> headers = new HashMap<>(1);
        headers.put("access_token", token);
        try {
            HiYunMouHttpClientResult result = HiYunYaoClientUtils.sendPostResponse("https://open.hikyun.com/artemis/api/eits/v2/global/devicestatus/online/search/byDeviceSerial", headers, params);
            JSONObject jsonObject = JSON.parseObject(result.getData());
            log.info("{}", jsonObject.getString("msg"));
            JSONArray data = jsonObject.getJSONArray("data");
            if (null == data) {
                return Collections.emptyList();
            }
            return data.stream()
                    .map(e -> {
                        JSONObject e1 = (JSONObject) e;
                        OnOffline item = new OnOffline();
                        item.setSysCameraTemplateSerialNumber(e1.getString("deviceSerial"));
                        item.setOnline(e1.getInteger("onlineStatus"));

                        return item;
                    }).toList();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public String recordUrl(String sysDeviceSerialNumber, String sysDeviceResourceType, DevicePreviewUrlV1Request request) {
        Map<String, Object> params = new HashMap<>(11);
        params.put("projectId", factory.getSysProjectSign());
        params.put("deviceSerial", sysDeviceSerialNumber);
        params.put("channelNo", request.getChannelNo());
        params.put("expireTime", request.getExpireTime());
        params.put("protocol", getProtocol(request.getProtocol()));
        params.put("supportH265", request.getSupportH265());
        params.put("quality", request.getQuality());
        params.put("code", request.getEzopenCode());
        params.put("type", request.getRecordType());
        params.put("startTime", null == request.getStartTime() ? null : DateTime.of(request.getStartTime()).toStandard());
        params.put("stopTime", null == request.getEndTime() ? null : DateTime.of(request.getEndTime()).toStandard());

        Map<String, String> headers = new HashMap<>(1);
        headers.put("access_token", token);
        try {
            HiYunMouHttpClientResult result = HiYunYaoClientUtils.sendPostResponse("https://open.hikyun.com/artemis/api/eits/v1/global/live/address/get/by/deviceSerial", headers, params);
            JSONObject jsonObject = JSON.parseObject(result.getData());
            log.info("{}", jsonObject.getString("msg"));
            return jsonObject.getJSONObject("data").getString("url");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public IPage<SysDeviceCardEvent> cardHistoryPage(CardDevicePageV1Request request) {
        return new Page<>();
    }

    @Override
    public boolean syncEventMessage(CardDevicePageV1Request request) {
        return false;
    }

    @Override
    public String previewUrl(String sysDeviceSerialNumber, String sysDeviceResourceType, DevicePreviewUrlV1Request request) {
        Map<String, Object> params = new HashMap<>(12);
        params.put("projectId", factory.getSysProjectSign());
        params.put("deviceSerial", sysDeviceSerialNumber);
        params.put("channelNo", request.getChannelNo());
        params.put("expireTime", request.getExpireTime());
        params.put("protocol", getProtocol(request.getProtocol()));
        params.put("supportH265", request.getSupportH265());
        params.put("quality", request.getQuality());
        params.put("code", request.getEzopenCode());
        params.put("startTime", null == request.getStartTime() ? null : DateTime.of(request.getStartTime()).toStandard());
        params.put("stopTime", null == request.getEndTime() ? null : DateTime.of(request.getEndTime()).toStandard());

        Map<String, String> headers = new HashMap<>(1);
        headers.put("access_token", token);
        try {
            HiYunMouHttpClientResult result = HiYunYaoClientUtils.sendPostResponse("https://open.hikyun.com/artemis/api/eits/v1/global/live/address/get/by/deviceSerial", headers, params);
            JSONObject jsonObject = JSON.parseObject(result.getData());
            log.info("{}", jsonObject.getString("msg"));
            return jsonObject.getJSONObject("data").getString("url");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取协议
     * 1-ezopen、2-hls、3-rtmp、4-flv，默认为1
     *
     * @param protocol 协议
     * @return 协议
     */
    private Integer getProtocol(String protocol) {
        switch (protocol) {
            case "ws":
                return 4;
            case "rtsp", "rtmp":
                return 3;
            case "hls":
                return 2;
            default:
                return 1;
        }
    }

    @Override
    public void afterPropertiesSet() {
        try {
            Map<String, Object> params = new HashMap<>(3);
            params.put("accessKey", artemisConfig.getAppKey());
            params.put("secretKey", artemisConfig.getAppSecret());
            params.put("productCode", factory.getSysProjectSignCode());
//            params.put("app", "app");
            HiYunMouHttpClientResult result = HiYunYaoClientUtils.sendPostResponse("https://open.hikyun.com/artemis/oauth/token/v2", params);
            if (result != null && result.isSuccess()) {
                JSONObject jsonObject = JSON.parseObject(result.getData());
                this.token = jsonObject.getJSONObject("data").getString("access_token");
            } else {
                log.error("getAccessToken failed,result:{}", result);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }


    @Override
    public String getToken() {
        return token;
    }

    @Override
    public String refreshToken() {
        afterPropertiesSet();
        return getToken();
    }
}
