package com.chua.starter.system.pojo.tenant;

import com.chua.starter.system.entity.SysTenantService;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 服务绑定
 *
 * @author CH
 */
@Data
@Schema(name = "租户服务绑定")
@AllArgsConstructor
@NoArgsConstructor
public class SysTenantServiceBindV1Request {

    /**
     * 服务id
     */
    @Schema(description = "租户id")
    private Integer sysTenantId;

    /**
     * 服务模块id
     */
    @Schema(description = "服务id")
    private List<SysTenantService> sysServiceIds;
}
