package com.chua.starter.device.pojo.request;

import com.chua.common.support.utils.group.SelectGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 设备预览地址请求。
 */
@Data
@Schema(description = "设备预览地址请求")
public class DevicePreviewUrlV1Request {

    @Schema(description = "设备ID")
    @NotNull(message = "设备ID不能为空", groups = SelectGroup.class)
    private Integer sysDeviceId;

    @Schema(description = "预览类型;hik:HIK私有协议;rtsp:RTSP协议;rtmp:RTMP协议;hls:HLS协议;ws:Websocket协议")
    private String protocol = "hls";

    @Schema(description = "预览模式;0:UDP;1:TCP")
    private Integer transMode = 1;

    @Schema(description = "通道号")
    @NotNull(message = "通道号不能为空", groups = SelectGroup.class)
    private String channelNo = "101";

    @Schema(description = "子码流类型;0:主码流")
    private Integer subtype;

    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    @Schema(description = "结束时间")
    private LocalDateTime endTime;

    @Schema(description = "ezopen协议地址的设备的视频加密密码")
    private String ezopenCode;

    @Schema(description = "过期时长，单位秒")
    private Integer expireTime = 365;

    @Schema(description = "地址的类型，1-预览，2-本地录像回放，3-云存储录像回放")
    private Integer recordType = 2;

    @Schema(description = "是否支持H265")
    private Integer supportH265 = 1;

    @Schema(description = "视频清晰度，1-高清（主码流）、2-流畅（子码流）")
    private Integer quality = 1;
}
