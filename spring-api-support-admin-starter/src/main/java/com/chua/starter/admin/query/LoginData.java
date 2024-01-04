package com.chua.starter.admin.query;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 登录信息
 *
 * @author CH
 */
@ApiModel("登录信息")
@Data
public class LoginData {

    @ApiModelProperty("账号")
    @NotNull(message = "账号不能为空")
    private String username;
    @ApiModelProperty("密码")
    @NotNull(message = "密码不能为空")
    private String password;
    @ApiModelProperty("校验码")
    @NotNull(message = "校验码不能为空")
    private String verifyCode;
}
