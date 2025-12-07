package com.chua.starter.project.service.support.hikvision;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.chua.common.support.bean.BeanUtils;
import com.chua.common.support.lang.date.DateTime;
import com.chua.starter.project.service.support.hikvision.adaptor.CarFactory;
import com.chua.starter.project.service.support.hikvision.adaptor.FaceFactory;
import com.chua.starter.project.service.support.hikvision.adaptor.MetroCardFactory;
import com.chua.starter.project.service.support.hikvision.adaptor.ResourceFactory;
import com.chua.starter.project.service.support.hikvision.adaptor.sub.EntranceFactory;
import com.chua.starter.project.service.support.hikvision.query.SysDeviceCameraOnOffline;
import com.chua.starter.project.service.support.hikvision.result.OnOffline;
import com.chua.starter.project.service.support.hikvision.result.OnOfflineResult;
import com.chua.starter.project.service.support.hikvision.result.OnlineResult;
import com.chua.starter.project.service.support.hikvision.result.PreviewUrlResult;
import com.chua.starter.project.service.support.hikvision.sdk.ArtemisHttpUtil;
import com.chua.starter.project.service.support.hikvision.sdk.config.ArtemisConfig;
import com.chua.starter.project.entity.SysDeviceCardEvent;
import com.chua.starter.project.pojo.device.CardDevicePageV1Request;
import com.chua.starter.project.pojo.device.DevicePreviewUrlV1Request;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import javax.management.openmbean.KeyAlreadyExistsException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.chua.starter.project.service.support.hikvision.HikvisionEnum.*;

/**
 * 海康客户端
 *
 * @author CH
 */
@Slf4j
public class HikvisionClient implements Client {
    private static final String CODE = "code";
    private static final String DATA = "data";
    /**
     * artemis路径
     */
    public static String ARTEMIS_PATH = "/artemis";
    private final String[] protocol;
    protected ArtemisConfig artemisConfig = new ArtemisConfig();
    @Getter
    @Setter
    private String studentOrgIndexCode;
    @Getter
    @Setter
    private String teacherOrgIndexCode;
    @Getter
    @Setter
    private Integer schoolId;

    public HikvisionClient(String[] protocol, String host, String appKey, String secretAccessKey) {
        this.protocol = protocol;
        artemisConfig.setHost(host);
        artemisConfig.setAppKey(appKey);
        artemisConfig.setAppSecret(secretAccessKey);
    }


    public HikvisionClient(String host, String appKey, String secretAccessKey) {
        this(new String[]{"https"}, host, appKey, secretAccessKey);
    }


    /**
     * 获取token
     *
     * @return token
     */
    public String getToken() {
        return null;
    }


    /**
     * 刷新token
     *
     * @return token
     */
    public String refreshToken() {
        return null;
    }
    /**
     * 车
     *
     * @return 获取设备机构接口
     */
    @Override
    public CarFactory getCarFactory() {
        return new CarFactory(this);
    }

    /**
     * 获取设备机构接口
     *
     * @return 获取设备机构接口
     */
    @Override
    public ResourceFactory getResourceFactory() {
        return new ResourceFactory(this);
    }

    /**
     * 人脸应用
     *
     * @return 人脸应用
     */
    @Override
    public FaceFactory getFaceFactory() {
        return new FaceFactory(this);
    }

    /**
     * 一卡通
     *
     * @return 获取门禁接口
     */
    @Override
    public MetroCardFactory getMetroCardFactory() {
        return new MetroCardFactory(this);
    }

    /**
     * 获取门禁接口
     *
     * @return 获取门禁接口
     */
    @Override
    public EntranceFactory getEntranceFactory() {
        return new EntranceFactory(this);
    }

    @Override
    public List<SysDeviceCameraOnOffline> getOnOfflineTimeline(String sysCameraTemplateSerialNumber, String sysCameraTemplateResourceType) {
        JSONObject json = new JSONObject();
        json.put("pageNo", 1);
        json.put("pageSize", 100);
        json.put("indexCode", sysCameraTemplateSerialNumber);
        json.put("resourceType", sysCameraTemplateResourceType);
        try {
            OnOfflineResult onOfflineResult = this.executePost(DOOR_SEARCH, json, OnOfflineResult.class);
            OnOfflineResult.DataDTO data = onOfflineResult.getData();
            if (null == data) {
                return Collections.emptyList();
            }
            List<OnOfflineResult.DataDTO.ListDTO> list = data.getList();
            return BeanUtils.copyPropertiesList(list, SysDeviceCameraOnOffline.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    @Override
    public List<OnOffline> checkOnOffline(String[] sysCameraTemplateSerialNumbers) {
        JSONObject json = new JSONObject();
        json.put("indexCodes", sysCameraTemplateSerialNumbers);
        json.put("pageNo", 1);
        json.put("pageSize", 100);
        try {
            OnlineResult onlineResult = this.executePost(ONLINE, json, OnlineResult.class);
            OnlineResult.DataDTO data = onlineResult.getData();
            if (null == data) {
                return Collections.emptyList();
            }
            List<OnlineResult.DataDTO.ListDTO> list = data.getList();
            return list.stream().map(it -> {
                OnOffline onOffline = new OnOffline();
                onOffline.setSysCameraTemplateSerialNumber(it.getIndexCode());
                onOffline.setOnline(it.getOnline());
                return onOffline;
            }).toList();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    @Override
    public String previewUrl(String sysDeviceSerialNumber, String sysDeviceResourceType, DevicePreviewUrlV1Request request) {
        JSONObject json = new JSONObject();
        json.put("cameraIndexCode", sysDeviceSerialNumber);
        json.put("protocol", protocol);
        json.put("streamType", request.getSubtype());
        json.put("transmode", request.getTransMode());
        PreviewUrlResult previewUrlResult = this.executePost(CAMERAS_PREVIEW_URLS, json, PreviewUrlResult.class);
        if (null == previewUrlResult) {
            throw new RuntimeException("获取预览地址失败!");
        }
        if (!"0".equals(previewUrlResult.getCode())) {
            throw new RuntimeException("获取预览地址失败!");
        }
        return previewUrlResult.getData().getUrl();
    }


    @Override
    public String recordUrl(String sysDeviceSerialNumber, String sysDeviceResourceType, DevicePreviewUrlV1Request request) {
        return "";
    }

    @Override
    public IPage<SysDeviceCardEvent> cardHistoryPage(CardDevicePageV1Request request) {
        String url = "/api/v1/open/accessControl/accessEvents/actions/list" + "?pageNo=" + request.getPage() +
                "&pageSize=" + request.getPageSize() +
                "&deviceSerial=" + request.getSysDeviceSerialNumber() +
                "&major=5" +
                "&major=75" +
                "&startTime=" + URLEncoder.encode(DateTime.of(ObjectUtils.defaultIfNull(
                request.getStartTime(),
                LocalDateTime.now().minusDays(7)
        )).toStandard(), StandardCharsets.UTF_8) +
                "&endTime=" + URLEncoder.encode(DateTime.of(ObjectUtils.defaultIfNull(
                request.getStartTime(),
                LocalDateTime.now()
        )).toStandard(), StandardCharsets.UTF_8);

        String s = this.executeGet(url, Collections.emptyMap(), String.class);
        return null;
    }

    @Override
    public boolean syncEventMessage(CardDevicePageV1Request request) {
        return false;
    }

    /**
     * 调用海康接口
     *
     * @param url  调用地址
     * @param type 返回类型
     * @return 结果
     */
    public <T> T executeGet(final String url, Map<String, String> headers, Class<T> type) {
        String artemis = ArtemisHttpUtil.doGetArtemis(analysisPath(url),
                null,
                null,
                null,
                headers,
                artemisConfig);
        return executeResponse(artemis, type);
    }

    /**
     * 调用海康接口
     *
     * @param url  调用地址
     * @param type 返回类型
     * @return 结果
     */
    public <T> T executeGet(final String url, JSONObject query, Map<String, String> headers, Class<T> type) {
        String artemis = ArtemisHttpUtil.doGetArtemis(analysisPath(url),
                query.entrySet().stream()
                        .collect(Collectors.toMap(Map.Entry::getKey, it -> it.getValue().toString()))
                ,
                null,
                null,
                headers,
                artemisConfig);
        return executeResponse(artemis, type);
    }
    /**
     * 调用海康接口
     *
     * @param url  调用地址
     * @param json 参数
     * @param type 返回类型
     * @return 结果
     */
    public <T> T executePost(final String url, JSONArray json, Class<T> type) {
        String artemis = ArtemisHttpUtil.doPostStringArtemis(analysisPath(url),
                json.toJSONString(),
                null,
                null,
                "application/json",
                artemisConfig);
        return executeResponse(artemis, type);
    }

    /**
     * 调用海康接口
     *
     * @param url  调用地址
     * @param json 参数
     * @param type 返回类型
     * @return 结果
     */
    public <T> T executePostForm(final String url, JSONObject json, Map<String, String> headers, Class<T> type) {
        String artemis = ArtemisHttpUtil.doPostStringArtemis(analysisPath(url),
                json.toJSONString(),
                null,
                null,
                "application/x-www-form-urlencoded",
                headers,
                artemisConfig);
        return executeResponse(artemis, type);
    }

    /**
     * 调用海康接口
     *
     * @param url  调用地址
     * @param json 参数
     * @param type 返回类型
     * @return 结果
     */
    public <T> T executePost(final String url, JSONObject json, Map<String, String> headers, Class<T> type) {
        String artemis = ArtemisHttpUtil.doPostStringArtemis(analysisPath(url),
                json.toJSONString(),
                null,
                null,
                "application/json",
                headers,
                artemisConfig);
        return executeResponse(artemis, type);
    }
    /**
     * 调用海康接口
     *
     * @param url  调用地址
     * @param json 参数
     * @param type 返回类型
     * @return 结果
     */
    public <T> T executePost(final String url, JSONObject json, Class<T> type) {
        String artemis = ArtemisHttpUtil.doPostStringArtemis(analysisPath(url), json.toJSONString(), null, null, "application/json", artemisConfig);
        return executeResponse(artemis, type);
    }

    /**
     * 调用海康接口
     *
     * @param hikvisionEnum 调用类型
     * @param json          参数
     * @param type          返回类型
     * @return 结果
     */
    public <T> T executePost(final HikvisionEnum hikvisionEnum, JSONArray json, Class<T> type) {
        return executePost(ARTEMIS_PATH.concat(hikvisionEnum.getUrl()), json, type);
    }

    /**
     * 调用海康接口
     *
     * @param hikvisionEnum 调用类型
     * @param json          参数
     * @param type          返回类型
     * @return 结果
     */
    public <T> T executePost(final HikvisionEnum hikvisionEnum, JSONObject json, Class<T> type) {
        return executePost(ARTEMIS_PATH.concat(hikvisionEnum.getUrl()), json, type);
    }

    /**
     * 分析地址
     *
     * @param url 链接
     * @return path
     */
    private Map<String, String> analysisPath(String url) {
        Map<String, String> path = new HashMap<String, String>(protocol.length);
        for (String s : protocol) {
            path.put(s.concat("://"), url);
        }
        return path;
    }

    /**
     * 结果
     *
     * @param artemis 结果
     * @param type    类型
     * @param <T>     类型
     * @return T
     */
    private <T> T executeResponse(String artemis, Class<T> type) {
        if (StringUtils.isBlank(artemis)) {
            return null;
        }
        JSONObject artemisObject = JSONObject.parseObject(artemis);
        log.info("返回状态码: {}", artemisObject.get(CODE));
        if (artemisObject.containsKey("msg")) {
            log.warn("返回信息: {}", artemisObject.getString("msg"));
        }
        if (isFailure(artemisObject)) {
            if ("0x00052301".equalsIgnoreCase(artemisObject.getString(CODE))) {
                throw new KeyAlreadyExistsException();
            }
            if ("0x00072002".equalsIgnoreCase(artemisObject.getString(CODE))) {
                throw new RuntimeException(artemisObject.getString("msg"));
            }
            try {
                return type.newInstance();
            } catch (Exception ignored) {
            }
        }
        if (String.class.isAssignableFrom(type)) {
            Object o = artemisObject.get(DATA);
            if (o instanceof JSONArray) {
                return (T) ((JSONArray) o).toJSONString();
            }

            if (o instanceof JSONObject) {
                return (T) ((JSONObject) o).toJSONString();
            }
            return (T) JSON.toJSONString(o);
        }
        String string = artemisObject.getString(DATA);
        if (string.startsWith("[")) {
            return (T) JSON.parseArray(string, type);
        }
        return JSON.parseObject(string, type);
    }

    /**
     * 是否查询失败
     *
     * @param artemisObject 请求条件
     * @return 结果
     */
    private boolean isFailure(JSONObject artemisObject) {
        return !"0".equals(artemisObject.getString(CODE)) && !"200".equals(artemisObject.getString(CODE));
    }

}
