package com.chua.starter.system.pojo.ornament;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 佩戴/取下饰品的请求体
 *
 * <p>{@code ornamentId} 为空表示取下该槽位的饰品。槽位与饰品的 slot 必须一致，
 * 服务端会校验，避免把昵称牌塞进头像槽。</p>
 *
 * @author CH
 * @since 4.0.0
 */
@Data
@Schema(description = "饰品佩戴请求")
public class OrnamentWearRequest {

    @Schema(description = "槽位: avatar / nameplate")
    private String slot;

    @Schema(description = "饰品ID；为空表示取下")
    private Integer ornamentId;
}
