package com.chua.starter.system.service.support.hikvision.yunmou.pojo;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class YunMouCardEventMessage {


    @SerializedName("msgId")
    private String msgId;
    @SerializedName("msgType")
    private String msgType;
    @SerializedName("content")
    private String content;
    @SerializedName("timestamp")
    private Long timestamp;
}
