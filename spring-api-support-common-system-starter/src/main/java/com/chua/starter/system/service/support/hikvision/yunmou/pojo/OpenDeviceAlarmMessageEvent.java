package com.chua.starter.system.service.support.hikvision.yunmou.pojo;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 设备报警消息
 *
 * @author CH
 */
@NoArgsConstructor
@Data
public class OpenDeviceAlarmMessageEvent {


    @SerializedName("alarmTime")
    private String alarmTime;
    @SerializedName("channel")
    private Integer channel;
    @SerializedName("channelType")
    private Integer channelType;
    @SerializedName("relationId")
    private String relationId;
    @SerializedName("customInfo")
    private String customInfo;
    @SerializedName("requestTime")
    private Long requestTime;
    @SerializedName("devSerial")
    private String devSerial;
    @SerializedName("alarmType")
    private String alarmType;
    @SerializedName("customType")
    private String customType;
    @SerializedName("alarmId")
    private String alarmId;
    @SerializedName("channelName")
    private String channelName;
    @SerializedName("location")
    private String location;
    @SerializedName("describe")
    private String describe;
    @SerializedName("pictureList")
    private List<PictureListDTO> pictureList;
    @SerializedName("status")
    private Integer status;

    @NoArgsConstructor
    @Data
    public static class PictureListDTO {
        @SerializedName("shortUrl")
        private String shortUrl;
        @SerializedName("id")
        private String id;
        @SerializedName("url")
        private String url;
    }
}
