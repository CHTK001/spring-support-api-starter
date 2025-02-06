package com.chua.starter.service.support.hikvision;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.TypeReference;
import com.chua.common.support.bean.BeanMap;
import com.chua.common.support.function.InitializingAware;
import com.chua.common.support.utils.ThreadUtils;
import com.chua.starter.service.support.hikvision.adaptor.CarFactory;
import com.chua.starter.service.support.hikvision.adaptor.FaceFactory;
import com.chua.starter.service.support.hikvision.adaptor.MetroCardFactory;
import com.chua.starter.service.support.hikvision.adaptor.ResourceFactory;
import com.chua.starter.service.support.hikvision.adaptor.sub.EntranceFactory;
import com.chua.starter.service.support.hikvision.adaptor.sub.YyxxFactory;
import com.chua.starter.service.support.hikvision.adaptor.sub.level3.RyjzpFactory;
import com.chua.starter.service.support.hikvision.query.DoorQuery;
import com.chua.starter.service.support.hikvision.query.PeopleInfo;
import com.chua.starter.service.support.hikvision.query.SysDeviceCameraOnOffline;
import com.chua.starter.service.support.hikvision.result.DoorEventResult;
import com.chua.starter.service.support.hikvision.result.OnOffline;
import com.chua.starter.service.support.hikvision.result.PeopleSearchResult;
import com.chua.starter.service.support.hikvision.result.ResourceResult;
import com.chua.starter.system.entity.SysProject;
import com.chua.starter.system.pojo.device.DevicePreviewUrlV1Request;
import lombok.extern.slf4j.Slf4j;

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
public class HiYunMouClient extends HikvisionClient implements InitializingAware {


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
        return new ResourceFactory(this) {
            @Override
            public YyxxFactory getPeopleFactory() {
                return new YyxxFactory(client) {
                    @Override
                    public RyjzpFactory getPeoplePictureFactory() {
                        return new RyjzpFactory(client) {
                            @Override
                            public ResourceResult add(PeopleInfo peopleInfo) {
                                Map<String, Object> params = new HashMap<>(8);
                                params.put("projectId", sysProject.getSysProjectSign());
                                params.put("productCode", sysProject.getSysProjectSignCode());
                                params.putAll(BeanMap.of(params));
                                params.put("personId", peopleInfo.getPersonId());
                                params.put("personLabelId", peopleInfo.getPersonId());
                                params.put("personNum", peopleInfo.getPersonId());

                                Map<String, String> headers = new HashMap<>(1);
                                headers.put("access_token", token);
                                try {
                                    HiYunMouHttpClientResult result = HiYunYaoClientUtils.sendPostResponse("https://open.hikyun.com/artemis/api/eits/fdcs/v1/person/face/add", headers, params);
                                    JSONObject jsonObject = JSON.parseObject(result.getData());
                                    peopleInfo.setPersonId(jsonObject.getString("personId"));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    return null;
                                }
                                return new ResourceResult();
                            }

                            @Override
                            public List<ResourceResult> remove(String[] personIds) {
                                Map<String, Object> params = new HashMap<>(3);
                                params.put("projectId", sysProject.getSysProjectSign());
                                params.put("productCode", sysProject.getSysProjectSignCode());
                                params.put("personIds", personIds);

                                Map<String, String> headers = new HashMap<>(1);
                                headers.put("access_token", token);
                                try {
                                    HiYunMouHttpClientResult result = HiYunYaoClientUtils.sendPostResponse("https://open.hikyun.com/artemis/api/eits/fdcs/v1/person/face/delete", headers, params);
                                    System.out.print(result.toString());
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    return null;
                                }
                                return Collections.emptyList();
                            }

                            @Override
                            public PeopleSearchResult search(int page, int pageSize) {
                                return super.search(page, pageSize);
                            }

                            @Override
                            public String addFace(String personId, String faceData) {
                                Map<String, Object> params = new HashMap<>(4);
                                params.put("projectId", sysProject.getSysProjectSign());
                                params.put("productCode", sysProject.getSysProjectSignCode());
                                params.put("faceData", faceData);
                                params.put("personId", personId);

                                Map<String, String> headers = new HashMap<>(1);
                                headers.put("access_token", token);
                                HiYunMouHttpClientResult result = null;
                                try {
                                    result = HiYunYaoClientUtils.sendPostResponse("https://open.hikyun.com/artemis/api/eits/fdcs/v1/person/face/upload", headers, params);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    return null;
                                }
                                return result.getData();
                            }

                            @Override
                            public String deleteFace(String faceId) {
                                Map<String, Object> params = new HashMap<>(3);
                                params.put("projectId", sysProject.getSysProjectSign());
                                params.put("productCode", sysProject.getSysProjectSignCode());
                                params.put("faceIds", new String[]{faceId});

                                Map<String, String> headers = new HashMap<>(1);
                                headers.put("access_token", token);
                                try {
                                    HiYunMouHttpClientResult result = HiYunYaoClientUtils.sendPostResponse("https://open.hikyun.com/artemis/api/eits/fdcs/v1/person/face/delete", headers, params);
                                    System.out.print(result.toString());
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    return null;
                                }
                                return null;
                            }
                        };
                    }
                };
            }
        };
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
        return new EntranceFactory(this) {
            @Override
            public DoorEventResult doorEvent(DoorQuery query) {

                Map<String, Object> params = new HashMap<>(6);
                params.put("projectId", sysProject.getSysProjectSign());
                params.put("productCode", sysProject.getSysProjectSignCode());
                params.put("pageNo", query.getPageNo());
                params.put("pageSize", query.getPageSize());
                params.put("eventStartTime", query.getStartTime());
                params.put("eventEndTime", query.getEndTime());

                Map<String, String> headers = new HashMap<>(1);
                headers.put("access_token", token);
                HiYunMouHttpClientResult result = HiYunYaoClientUtils.sendPostResponse("https://open.hikyun.com/artemis/api/eits/aceventcs/v1/event/acs/person/page", headers, params);
                if (result != null && result.getCode() == 401) {
                    log.error("pullMessages failed,result:{}", result);
                } else if (result != null && result.isSuccess()) {
                    List<HiYunMouMessage> apiResult = JSON.parseObject(result.getData(), new TypeReference<List<HiYunMouMessage>>() {
                    });
                    log.info("apiResult:{}", apiResult);
                } else {
                    log.error("pullMessages failed,result:{}", result);
                }

                return null;
            }
        };
    }

    @Override
    public List<SysDeviceCameraOnOffline> getOnOfflineTimeline(String sysCameraTemplateSerialNumber, String sysCameraTemplateResourceType) {
        return List.of();
    }

    @Override
    public List<OnOffline> checkOnOffline(String[] sysCameraTemplateSerialNumbers) {
        return List.of();
    }

    @Override
    public String recordUrl(String sysDeviceSerialNumber, String sysDeviceResourceType, DevicePreviewUrlV1Request request) {
        return "";
    }


    @Override
    public void afterPropertiesSet() {
        try {
            Map<String, Object> params = new HashMap<>(3);
            params.put("accessKey", artemisConfig.getAppKey());
            params.put("secretKey", artemisConfig.getAppSecret());
            params.put("productCode", sysProject.getSysProjectSignCode());
//            params.put("app", "app");
            HiYunMouHttpClientResult result = HiYunYaoClientUtils.sendPostResponse("https://open.hikyun.com/artemis/oauth/token/v2", params);
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
}
