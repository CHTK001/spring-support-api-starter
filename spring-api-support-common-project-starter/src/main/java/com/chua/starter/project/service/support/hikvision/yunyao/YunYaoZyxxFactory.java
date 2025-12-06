package com.chua.starter.project.service.support.hikvision.yunyao;

import com.alibaba.fastjson2.JSON;
import com.chua.starter.project.service.support.hikvision.HiYunMouHttpClientResult;
import com.chua.starter.project.service.support.hikvision.HiYunYaoClientUtils;
import com.chua.starter.project.service.support.hikvision.HikvisionClient;
import com.chua.starter.project.service.support.hikvision.adaptor.sub.ZyxxFactory;
import com.chua.starter.project.service.support.hikvision.result.DeviceResourceResourcesResult;
import com.chua.starter.project.entity.SysProject;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 海康云曜资源信息
 *
 * @author CH
 */
@Slf4j
public class YunYaoZyxxFactory extends ZyxxFactory {
    private final SysProject factory;
    private final String token;

    public YunYaoZyxxFactory(HikvisionClient hikvisionClient, SysProject factory, String token) {
        super(hikvisionClient);
        this.factory = factory;
        this.token = token;
    }


    @Override
    public DeviceResourceResourcesResult deviceResourceResources(int page, int pageSize, String resourceType) {

        Map<String, String> headers = new HashMap<>(1);
        headers.put("access_token", token);
        DeviceResourceResourcesResult deviceResourceResourcesResult = new DeviceResourceResourcesResult();
        try {
            HiYunMouHttpClientResult result = HiYunYaoClientUtils.sendPostResponse(
                    "https://open.hikyun.com/artemis/api/eits/v2/global/device/page", headers, """
                            [
                                {
                                    "key": "pageSize",
                                    "option": "eq",
                                    "value": %s
                                },
                                {
                                    "key": "pageNo",
                                    "option": "eq",
                                    "value": %s
                                },
                                {
                                    "key": "projectId",
                                    "option": "eq",
                                    "value": %s
                                }
                            ]
                            """.formatted(pageSize, page, factory.getSysProjectSign()));
            EmbeddedDeviceInfo embeddedDeviceInfo = JSON.parseObject(result.getData(), EmbeddedDeviceInfo.class);
            deviceResourceResourcesResult.setPageNo(1);
            deviceResourceResourcesResult.setPageSize(1000);
            deviceResourceResourcesResult.setTotal(embeddedDeviceInfo.getData().getTotal());
            deviceResourceResourcesResult.setList(embeddedDeviceInfo.getData().getList().stream()
                    .map(item -> {
                        DeviceResourceResourcesResult.ListDTO listDTO = new DeviceResourceResourcesResult.ListDTO();
                        listDTO.setIndexCode(item.getDeviceSerial());
                        listDTO.setName(item.getDeviceName());
                        listDTO.setRegionIndexCode(item.getDeviceOrgId());
                        listDTO.setResourceType(item.getModelType());
                        listDTO.setOnline(item.getOnlineStatus());
                        listDTO.setNetAddress(item.getNetAddress());
                        listDTO.setLatitude(String.valueOf(item.getLatitude()));
                        listDTO.setLongitude(String.valueOf(item.getLongitude()));
                        listDTO.setDeviceType(item.getDeviceType());
                        listDTO.setStatus(item.getOpenStatus());
                        listDTO.setVersion(item.getDeviceVersion());
                        listDTO.setChannelCount(item.getChannumCount());
                        listDTO.setValidateCode(item.getValidateCode());
                        listDTO.setCreateTime(item.getCreateTime());
                        listDTO.setUpdateTime(item.getUpdateTime());
                        return listDTO;
                    }).toList());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return deviceResourceResourcesResult;
    }


    @NoArgsConstructor
    @Data
    class EmbeddedDeviceInfo {
        @JsonProperty("code")
        private String code;
        @JsonProperty("msg")
        private String msg;
        @JsonProperty("data")
        private DataDTO data;

        @NoArgsConstructor
        @Data
        public static class DataDTO {
            @JsonProperty("pageNo")
            private Integer pageNo;
            @JsonProperty("pageSize")
            private Integer pageSize;
            @JsonProperty("total")
            private Integer total;
            @JsonProperty("totalPage")
            private Integer totalPage;
            @JsonProperty("list")
            private List<ListDTO> list;

            @NoArgsConstructor
            @Data
            public static class ListDTO {
                @JsonProperty("id")
                private String id;
                @JsonProperty("deviceSerial")
                private String deviceSerial;
                @JsonProperty("projectId")
                private String projectId;
                @JsonProperty("productCode")
                private String productCode;
                @JsonProperty("userName")
                private Object userName;
                @JsonProperty("deviceOrgId")
                private String deviceOrgId;
                @JsonProperty("userId")
                private Object userId;
                @JsonProperty("deviceName")
                private String deviceName;
                @JsonProperty("validateCode")
                private String validateCode;
                @JsonProperty("model")
                private String model;
                @JsonProperty("modelType")
                private String modelType;
                @JsonProperty("deviceVersion")
                private String deviceVersion;
                @JsonProperty("channumCount")
                private Integer channumCount;
                @JsonProperty("deviceKind")
                private Object deviceKind;
                @JsonProperty("operatorType")
                private Object operatorType;
                @JsonProperty("deviceId")
                private Object deviceId;
                @JsonProperty("manufacturer")
                private Object manufacturer;
                @JsonProperty("detailAddress")
                private Object detailAddress;
                @JsonProperty("province")
                private Object province;
                @JsonProperty("city")
                private Object city;
                @JsonProperty("district")
                private Object district;
                @JsonProperty("serverId")
                private Object serverId;
                @JsonProperty("treatyType")
                private String treatyType;
                @JsonProperty("deviceCategory")
                private String deviceCategory;
                @JsonProperty("deviceTypeCode")
                private String deviceTypeCode;
                @JsonProperty("connectionInfo")
                private Object connectionInfo;
                @JsonProperty("gbCode")
                private Object gbCode;
                @JsonProperty("subDomainId")
                private Object subDomainId;
                @JsonProperty("licenseId")
                private Object licenseId;
                @JsonProperty("gatewayId")
                private Object gatewayId;
                @JsonProperty("customType")
                private Object customType;
                @JsonProperty("deployMode")
                private Object deployMode;
                @JsonProperty("deviceType")
                private String deviceType;
                @JsonProperty("apiKey")
                private Object apiKey;
                @JsonProperty("extend")
                private Object extend;
                @JsonProperty("modelId")
                private Object modelId;
                @JsonProperty("modelVersion")
                private Object modelVersion;
                @JsonProperty("tag")
                private List<?> tag;
                @JsonProperty("domain")
                private Object domain;
                @JsonProperty("accessMode")
                private Object accessMode;
                @JsonProperty("ysKey")
                private Object ysKey;
                @JsonProperty("publicDoor")
                private Object publicDoor;
                @JsonProperty("depositDevice")
                private Object depositDevice;
                @JsonProperty("netAddress")
                private String netAddress;
                @JsonProperty("latitude")
                private Object latitude;
                @JsonProperty("longitude")
                private Object longitude;
                @JsonProperty("parentDeviceSerial")
                private Object parentDeviceSerial;
                @JsonProperty("unbindFlag")
                private Integer unbindFlag;
                @JsonProperty("firmwareDeviceType")
                private Object firmwareDeviceType;
                @JsonProperty("firmwareSubDeviceType")
                private Object firmwareSubDeviceType;
                @JsonProperty("createTime")
                private String createTime;
                @JsonProperty("updateTime")
                private String updateTime;
                @JsonProperty("createrId")
                private String createrId;
                @JsonProperty("operatorId")
                private Object operatorId;
                @JsonProperty("deviceOrgName")
                private String deviceOrgName;
                @JsonProperty("modelTypeStr")
                private String modelTypeStr;
                @JsonProperty("selectable")
                private Boolean selectable;
                @JsonProperty("onlineStatus")
                private Integer onlineStatus;
                @JsonProperty("onlineStatusStr")
                private String onlineStatusStr;
                @JsonProperty("openStatus")
                private Integer openStatus;
                @JsonProperty("openStatusStr")
                private String openStatusStr;
                @JsonProperty("intelliVal")
                private Integer intelliVal;
                @JsonProperty("intelliValStr")
                private String intelliValStr;
                @JsonProperty("defence")
                private Integer defence;
                @JsonProperty("defenceStr")
                private String defenceStr;
                @JsonProperty("deviceStoreType")
                private String deviceStoreType;
                @JsonProperty("isencrypt")
                private Integer isencrypt;
                @JsonProperty("updateStatus")
                private Integer updateStatus;
                @JsonProperty("hardDiskCount")
                private Integer hardDiskCount;
                @JsonProperty("hardDiskStatus")
                private String hardDiskStatus;
                @JsonProperty("diskStatus")
                private String diskStatus;
                @JsonProperty("cloudStoreStatus")
                private Object cloudStoreStatus;
                @JsonProperty("planType")
                private Object planType;
                @JsonProperty("buyNum")
                private Object buyNum;
                @JsonProperty("cloudStoreTime")
                private Object cloudStoreTime;
                @JsonProperty("alarmSoundMode")
                private Integer alarmSoundMode;
                @JsonProperty("monitorList")
                private Object monitorList;
                @JsonProperty("devicePatternMap")
                private Object devicePatternMap;
                @JsonProperty("operatorTypeStr")
                private Object operatorTypeStr;
                @JsonProperty("manufacturerStr")
                private Object manufacturerStr;
                @JsonProperty("provinceStr")
                private Object provinceStr;
                @JsonProperty("cityStr")
                private Object cityStr;
                @JsonProperty("districtStr")
                private Object districtStr;
                @JsonProperty("supportHeop")
                private Object supportHeop;
                @JsonProperty("deviceNameOrSerial")
                private Object deviceNameOrSerial;
                @JsonProperty("deviceStatus")
                private Object deviceStatus;
                @JsonProperty("containAllChildren")
                private Boolean containAllChildren;
                @JsonProperty("deviceOrgIdList")
                private Object deviceOrgIdList;
                @JsonProperty("supportEncrypt")
                private Integer supportEncrypt;
                @JsonProperty("isencryptStr")
                private String isencryptStr;
                @JsonProperty("supportEncryptStr")
                private String supportEncryptStr;
                @JsonProperty("supportBatteryManage")
                private Object supportBatteryManage;
                @JsonProperty("treatyTypeName")
                private String treatyTypeName;
                @JsonProperty("accessSign")
                private Object accessSign;
                @JsonProperty("deviceFrom")
                private DeviceFromDTO deviceFrom;
                @JsonProperty("labelTree")
                private Object labelTree;
                @JsonProperty("supportProtectionMode")
                private Object supportProtectionMode;
                @JsonProperty("supportDefence")
                private Object supportDefence;
                @JsonProperty("deviceCategoryName")
                private String deviceCategoryName;
                @JsonProperty("remoteConfigSupport")
                private Boolean remoteConfigSupport;

                @NoArgsConstructor
                @Data
                public static class DeviceFromDTO {
                }
            }
        }
    }
}
