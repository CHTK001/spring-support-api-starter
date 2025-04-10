package com.chua.starter.school.dto;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Data
public class GaokaoRank {

    @SerializedName("code")
    private String code;
    @SerializedName("message")
    private String message;
    @SerializedName("data")
    private List<DataDTO> data;
    @SerializedName("md5")
    private String md5;

    @NoArgsConstructor
    @Data
    public static class DataDTO {
        @SerializedName("rank")
        private String rank;
        @SerializedName("rank_type")
        private String rankType;
        @SerializedName("rank_name")
        private String rankName;
        @SerializedName("master")
        private String master;
        @SerializedName("is_jump")
        private Integer isJump;
        @SerializedName("show")
        private List<Integer> show;
    }
}
