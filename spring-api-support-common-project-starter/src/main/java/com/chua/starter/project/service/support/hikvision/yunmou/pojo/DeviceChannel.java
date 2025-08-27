package com.chua.starter.project.service.support.hikvision.yunmou.pojo;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Data
public class DeviceChannel {


    @SerializedName("pageNo")
    private Integer pageNo;
    @SerializedName("pageSize")
    private Integer pageSize;
    @SerializedName("total")
    private Integer total;
    @SerializedName("rows")
    private List<RowsDTO> rows;

    @NoArgsConstructor
    @Data
    public static class RowsDTO {
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
}
