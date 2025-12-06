package com.chua.starter.project.pojo.device;

import com.chua.common.support.validator.group.SelectGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 设备预览地址
 *
 * @author CH
 */
@Data
@Schema(description = "设备预览地址")
public class DevicePreviewUrlV1Request {

    /**
     * 设备ID
     */
    @Schema(description = "设备ID")
    @NotNull(message = "设备ID不能为空", groups = SelectGroup.class)
    private Integer sysDeviceId;

    /**
     * 预览类型
     */
    @Schema(description = "预览类型;“hik�?HIK私有协议，使用视频SDK进行播放时，传入此类型；“rtsp�?RTSP协议；“rtmp�?RTMP协议；“hls�?HLS协议（HLS协议只支持海康SDK协议、EHOME协议、ONVIF协议接入的设备；只支持H264视频编码和AAC音频编码）。\n" +
            "“ws�?Websocket协议（一般用于H5视频播放器取流播放）")
    private String protocol = "hls";

    /**
     * 预览模式
     */
    @Schema(description = "预览模式;0:UDP\n" +
            "1:TCP")
    private Integer transMode = 1;

    /**
     * 通道�?
     */
    @Schema(description = "通道�?)
    @NotNull(message = "通道号不能为�?, groups = SelectGroup.class)
    private String channelNo = "101";


    /**
     * 子码流类�?
     */
    @Schema(description = "子码流类�?0:主码�?)
    private Integer subtype;

    /**
     * 开始时�?
     */
    @Schema(description = "开始时�?)
    private LocalDateTime startTime;
    /**
     * 结束时间
     */
    @Schema(description = "结束时间")
    private LocalDateTime endTime;

    /**
     * ezopen协议地址的设备的视频加密密码
     */
    private String ezopenCode;
    /**
     * 过期时长，单位秒；针对hls/rtmp/flv设置有效期，相对时间�?0�?720�?
     */
    @Schema(description = "过期时长，单位秒；针对hls/rtmp/flv设置有效期，相对时间�?0�?720�?)
    private Integer expireTime = 365;

    /**
     * 地址的类型，1-预览�?-本地录像回放�?-云存储录像回放，非必选，默认�?；回放仅支持rtmp、ezopen、flv协议
     */
    @Schema(description = "地址的类型，1-预览�?-本地录像回放�?-云存储录像回放，非必选，默认�?；回放仅支持rtmp、ezopen、flv协议")
    private Integer recordType = 2;
    /**
     * 是否支持H265
     */
    @Schema(description = "是否支持H265")
    private Integer supportH265 = 1;
    /**
     * 视频清晰度，1-高清（主码流）�?-流畅（子码流�?
     */
    @Schema(description = "视频清晰度，1-高清（主码流）�?-流畅（子码流�?)
    private Integer quality = 1;
}
