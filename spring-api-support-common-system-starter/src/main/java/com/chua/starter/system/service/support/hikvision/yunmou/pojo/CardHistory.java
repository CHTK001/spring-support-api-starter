package com.chua.starter.system.service.support.hikvision.yunmou.pojo;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Data
public class CardHistory {

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
        @SerializedName("major")
        private Integer major;
        @SerializedName("minor")
        private Integer minor;
        @SerializedName("time")
        private String time;
        @SerializedName("employeeNo")
        private String employeeNo;
        @SerializedName("serialNo")
        private Integer serialNo;
    }
}
