package com.chua.starter.system.pojo.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用户密码修改
 *
 * @since 2024/8/20
 * @author CH
 */
@Data
@Schema(description = "用户密码修改")
public class SysUserPasswordUpdate {

    /**
     * 旧密码
     */
    @Schema(description = "旧密码")
    /** 旧密码 */
    private String oldPassword;

    /**
     * 新密码
     */
    @Schema(description = "新密码")
    /** 新密码 */
    private String newPassword;

    /**
     * 确认密码
     */
    @Schema(description = "确认密码")
    /** Confirm密码 */
    private String confirmPassword;
}
