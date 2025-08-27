package com.chua.starter.project.service.support.hikvision.result;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

/**
 * 设备通道
 *
 * @author CH
 */
@Data
public class HikDeviceChannel {

    @SerializedName("deviceSerial")
    private String deviceSerial;
    @SerializedName("channelId")
    private String channelId;
    @SerializedName("channelName")
    private String channelName;
    @SerializedName("channelNo")
    private Integer channelNo;
    @SerializedName("channelStatus")
    private Integer channelStatus;
    @SerializedName("channelType")
    private String channelType;
    @SerializedName("isUse")
    private Integer isUse;
    @SerializedName("ipcSerial")
    private String ipcSerial;
    @SerializedName("childDeviceModel")
    private String childDeviceModel;
}
