package com.chua.starter.project.service.support.hikvision.yunmou.pojo;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 设备信息
 *
 * @author CH
 */
@NoArgsConstructor
@Data
public class YunMouDeviceResourceResourcesResult {

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
        @SerializedName("deviceId")
        private String deviceId;
        @SerializedName("deviceName")
        private String deviceName;
        @SerializedName("deviceModel")
        private String deviceModel;
        @SerializedName("deviceSerial")
        private String deviceSerial;
        @SerializedName("deviceStatus")
        private Integer deviceStatus;
        @SerializedName("groupId")
        private String groupId;
        @SerializedName("isEncrypt")
        private Integer isEncrypt;
        @SerializedName("deviceVersion")
        private String deviceVersion;
    }
}
