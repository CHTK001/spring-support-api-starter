package com.chua.starter.system.service.support.hikvision;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chua.common.support.function.InitializingAware;
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
import com.chua.starter.system.service.support.hikvision.yunmou.adaptor.YunMouEntranceFactory;
import com.chua.starter.system.service.support.hikvision.yunmou.adaptor.YunMouResourceFactory;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author CH
 */
@Slf4j
public class HiYunMouClient extends HikvisionClient implements InitializingAware {

    public static final Map<Integer, String> MINOR_MAP = new HashMap<>(10) {
        {
            put(1, "刷卡开锁");
            put(6, "未分配权限");
            put(7, "无效时段");
            put(8, "卡号过期");
            put(9, "无此卡号");
            put(21, "门锁打开");
            put(22, "门锁关闭");
            put(75, "人脸开锁");
            put(76, "人脸认证失败");
            put(1024, "中心平台开锁");
        }
    };

    private static final String TOKEN_URL = "https://api2.hik-cloud.com/oauth/token";
    private final SysProject sysProject;
    private final ScheduledExecutorService SCHEDULED_EXECUTOR_SERVICE = ThreadUtils.newScheduledThreadPoolExecutor(1, "check-token");
    private String token;

    {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                SCHEDULED_EXECUTOR_SERVICE.shutdownNow();
            }
        });

        SCHEDULED_EXECUTOR_SERVICE.scheduleAtFixedRate(this::afterPropertiesSet, 0, 20, TimeUnit.MINUTES);
    }

    public HiYunMouClient(String[] protocol, String host, String appKey, String secretAccessKey, SysProject sysProject) {
        super(protocol, host, appKey, secretAccessKey);
        this.sysProject = sysProject;
        afterPropertiesSet();
    }

    @Override
    public CarFactory getCarFactory() {
        return super.getCarFactory();
    }

    @Override
    public ResourceFactory getResourceFactory() {
        HikvisionClient client = this;
        return new YunMouResourceFactory(client, sysProject);
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
        return new YunMouEntranceFactory(this, sysProject);
    }

    @Override
    public List<SysDeviceCameraOnOffline> getOnOfflineTimeline(String sysCameraTemplateSerialNumber, String sysCameraTemplateResourceType) {
        return List.of();
    }

    @Override
    public List<OnOffline> checkOnOffline(String[] sysCameraTemplateSerialNumbers) {
        List<OnOffline> res = new LinkedList<>();
        Map<String, String> headers = new HashMap<>(1);
        headers.put("Authorization", "Bearer " + getToken());
        try {
            for (String sysCameraTemplateSerialNumber : sysCameraTemplateSerialNumbers) {
                OnOffline onOffline = new OnOffline();
                onOffline.setSysCameraTemplateSerialNumber(sysCameraTemplateSerialNumber);
                try {
                    HiYunMouHttpClientResult result = HiYunYaoClientUtils.sendGetResponse(
                            "https://api2.hik-cloud.com/api/v1/open/basic/devices/get", headers, "deviceSerial=" + sysCameraTemplateSerialNumber);
                    /**
                     * "data": {
                     *     "deviceId": "设备id",
                     *     "deviceModel": "DS-K(D05215100)",
                     *     "deviceName": "设备名称",
                     *     "deviceSerial": "D05215100",
                     *     "deviceStatus": 1,
                     *     "groupId": "66935ed7d8e24ed0a089f4795b1706dc",
                     *     "isEncrypt": 0,
                     *     "deviceVersion": "V1.0.0 build 220614"
                     *   },
                     */
                    if (result.isSuccess()) {
                        JSONObject jsonObject = JSON.parseObject(result.getData());
                        jsonObject = jsonObject.getJSONObject("data");
                        onOffline.setOnline(jsonObject.getInteger("deviceStatus"));
                    } else {
                        onOffline.setOnline(0);
                    }
                } catch (Exception ignored) {
                    onOffline.setOnline(0);
                }
                res.add(onOffline);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return res;
    }

    @Override
    public String recordUrl(String sysDeviceSerialNumber, String sysDeviceResourceType, DevicePreviewUrlV1Request request) {
        return "";
    }


    @Override
    public void afterPropertiesSet() {
        try {
            Map<String, Object> params = new HashMap<>(3);
            params.put("client_id", artemisConfig.getAppKey());
            params.put("client_secret", artemisConfig.getAppSecret());
            params.put("grant_type", "client_credentials");
//            params.put("app", "app");
            HiYunMouHttpClientResult result = HiYunYaoClientUtils.sendPostResponse("https://api2.hik-cloud.com/oauth/token",
                    null,
                    params, true, null, null);
            if (result != null && result.isSuccess()) {
                JSONObject jsonObject = JSON.parseObject(result.getData());
                this.token = jsonObject.getString("access_token");
            } else {
                log.error("getAccessToken failed,result:{}", result);
            }
        } catch (Exception e) {
            e.printStackTrace();
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
    public String getToken() {
        return token;
    }

    @Override
    public String refreshToken() {
        afterPropertiesSet();
        return getToken();
    }
}
