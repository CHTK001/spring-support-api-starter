package com.chua.starter.system.pojo.tenant;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 租户同步
 *
 * @author CH
 */
@Data
@Schema(name = "租户同步")
@AllArgsConstructor
@NoArgsConstructor
public class SysTenantSyncV1Request {

    /**
     * 服务id
     */
    @Schema(description = "租户id")
    private Integer sysTenantId;
}
