package com.chua.starter.system.pojo.service;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 服务绑定
 *
 * @author CH
 */
@Data
@Schema(name = "服务绑定")
public class SysServiceBindV1Request {

    /**
     * 服务id
     */
    @Schema(description = "服务id")
    private Integer sysServiceId;

    /**
     * 服务模块id
     */
    @Schema(description = "服务模块id")
    private List<Integer> sysServiceModuleIds;
}
