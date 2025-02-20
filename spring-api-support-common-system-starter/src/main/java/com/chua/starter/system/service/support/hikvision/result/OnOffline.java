package com.chua.starter.system.service.support.hikvision.result;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 在离线
 *
 * @author CH
 */
@Data
@Schema(description = "在离线")
public class OnOffline {

    /**
     * 设备序列号
     */
    @Schema(description = "设备序列号")
    private String sysCameraTemplateSerialNumber;

    /**
     * 在线状态
     */
    @Schema(description = "在线状态;0: 离线; 1:在线")
    private Integer online;

}
