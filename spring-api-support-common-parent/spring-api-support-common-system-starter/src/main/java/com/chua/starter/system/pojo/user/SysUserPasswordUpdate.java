package com.chua.starter.system.pojo.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用户密码修改
 *
 * @author CH
 * @since 2024/8/20
 */
@Data
@Schema(description = "用户密码修改")
public class SysUserPasswordUpdate {

    /**
     * 旧密码
     */
    @Schema(description = "旧密码")
    private String oldPassword;

    /**
     * 新密码
     */
    @Schema(description = "新密码")
    private String newPassword;

    /**
     * 确认密码
     */
    @Schema(description = "确认密码")
    private String confirmPassword;
}
