package com.chua.starter.project.service.support.hikvision.yunmou.pojo;


import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 设备上下线消息
 *
 * @author CH
 */
@NoArgsConstructor
@Data
public class OpenDeviceOnofflineMessageEvent {

    @SerializedName("groupId")
    private String groupId;
    @SerializedName("deviceId")
    private String deviceId;
    @SerializedName("deviceSerial")
    private String deviceSerial;
    @SerializedName("devType")
    private String devType;
    @SerializedName("regTime")
    private String regTime;
    @SerializedName("natIp")
    private String natIp;
    @SerializedName("msgType")
    private String msgType;
    @SerializedName("subSerial")
    private String subSerial;
    @SerializedName("occurTime")
    private String occurTime;
    @SerializedName("deviceName")
    private String deviceName;
}
