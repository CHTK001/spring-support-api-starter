package com.chua.starter.system.service.support.hikvision.yunmou.pojo;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 门禁
 *
 * @author CH
 */
@NoArgsConstructor
@Data
public class OpenEventAccessMessageEvent {


    @SerializedName("eventId")
    private String eventId;
    @SerializedName("dateTime")
    private String dateTime;
    @SerializedName("serialNo")
    private Integer serialNo;
    @SerializedName("employeeNo")
    private String employeeNo;
    @SerializedName("deviceSerial")
    private String deviceSerial;
    @SerializedName("deviceName")
    private String deviceName;
    @SerializedName("channelNo")
    private String channelNo;
    private String channelName;
    @SerializedName("cardReaderNo")
    private Integer cardReaderNo;
    private Integer cardReaderKind;
    @SerializedName("currentEvent")
    private Boolean currentEvent;
    @SerializedName("doorNo")
    private Integer doorNo;
    @SerializedName("eventType")
    private String eventType;
    @SerializedName("eventCode")
    private String eventCode;
    private String eventSource;
    @SerializedName("eventDescription")
    private String eventDescription;
    @SerializedName("pictureURL")
    private String pictureURL;
    @SerializedName("name")
    private String name;
}
