package com.chua.starter.system.pojo.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 鐢ㄦ埛瀵嗙爜淇敼
 *
 * @author CH
 * @since 2024/8/20
 */
@Data
@Schema(description = "鐢ㄦ埛瀵嗙爜淇敼")
public class SysUserPasswordUpdate {

    /**
     * 鏃у瘑鐮?     */
    @Schema(description = "鏃у瘑鐮")
    private String oldPassword;

    /**
     * 鏂板瘑鐮"     */
    @Schema(description = "鏂板瘑鐮")
    private String newPassword;

    /**
     * 纭瀵嗙爜
     */
    @Schema(description = "纭瀵嗙爜")
    private String confirmPassword;
}