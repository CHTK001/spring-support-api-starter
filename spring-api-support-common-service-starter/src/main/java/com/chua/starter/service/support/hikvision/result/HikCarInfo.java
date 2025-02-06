package com.chua.starter.service.support.hikvision.result;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * <p>
 * 海康车辆/车库信息
 * </p>
 *
 * @author ch
 * @since 2022-06-28
 */
@NoArgsConstructor
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
public class HikCarInfo {
    @JsonProperty("total")
    private Integer total;
    @JsonProperty("pageNo")
    private Integer pageNo;
    @JsonProperty("pageSize")
    private Integer pageSize;
    @JsonProperty("list")
    private List<ListDTO> list;

    @NoArgsConstructor
    @Data
    public static class ListDTO {
        @JsonProperty("vehicleId")
        private String vehicleId;
        @JsonProperty("plateNo")
        private String plateNo;
        @JsonProperty("personId")
        private String personId;
        @JsonProperty("regionIndexCode")
        private String regionIndexCode;
        @JsonProperty("plateType")
        private Integer plateType;
        @JsonProperty("plateColor")
        private Integer plateColor;
        @JsonProperty("vehicleType")
        private Integer vehicleType;
        @JsonProperty("vehicleColor")
        private Integer vehicleColor;
        @JsonProperty("description")
        private String description;
        @JsonProperty("createTime")
        private String createTime;
        @JsonProperty("updateTime")
        private String updateTime;
        @JsonProperty("regionName")
        private String regionName;
        @JsonProperty("regionPath")
        private String regionPath;
        @JsonProperty("regionPathName")
        private String regionPathName;
        @JsonProperty("isBandPerson")
        private Integer isBandPerson;
        @JsonProperty("personName")
        private String personName;
        @JsonProperty("orgPath")
        private String orgPath;
        @JsonProperty("orgPathName")
        private String orgPathName;

    }
}
