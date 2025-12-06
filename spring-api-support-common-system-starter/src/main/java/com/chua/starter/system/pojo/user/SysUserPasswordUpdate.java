package com.chua.starter.system.pojo.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author CH
 * @since 2024/8/26
 */
@Data
@Schema(description = "修改密码")
public class SysUserPasswordUpdate {

    /**
     * 新密码
     */
    @Schema(description = "新密码")
    private String newPassword;

    /**
     * 旧密码
     */
    @Schema(description = "旧密码")
    private String oldPassword;
}
