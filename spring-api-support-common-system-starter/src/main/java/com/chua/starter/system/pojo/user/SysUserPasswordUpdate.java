package com.chua.starter.service.pojo.user;

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
     * 新密�?
     */
    @Schema(description = "新密�?)
    private String newPassword;

    /**
     * 旧密�?
     */
    @Schema(description = "旧密�?)
    private String oldPassword;
}
