package com.chua.starter.system.pojo.dict;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 瀛楀吀椤瑰睘鎬ц姹? *
 * @author CH
 * @since 2024/8/20
 */
@Data
@Schema(description = "瀛楀吀椤瑰睘鎬ц姹?)
public class DictItemPropertyV1Request {

    /**
     * 瀛楀吀缂栫爜
     */
    @Schema(description = "瀛楀吀缂栫爜")
    private String dictCode;

    /**
     * 瀛楀吀椤瑰€?     */
    @Schema(description = "瀛楀吀椤瑰€?)
    private String dictItemValue;
}