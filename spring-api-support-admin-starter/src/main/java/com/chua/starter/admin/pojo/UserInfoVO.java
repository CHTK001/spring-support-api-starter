package com.chua.starter.admin.pojo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Set;

/**
 * 用户登录视图对象
 *
 * @author haoxr
 * @since 2022/1/14
 */
@Schema(description = "当前登录用户视图对象")
@Data
public class UserInfoVO {

    @Schema(description = "用户ID")
    private String userId;

    @Schema(description = "用户性别")
    private Integer userSex;

    @Schema(description = "备注")
    private String userMarker;

    @Schema(description = "用户名称")
    private String userName;
    @Schema(description = "用户真实姓名")
    private String userRealName;

    @Schema(description = "用户昵称")
    private String nickname;
    @Schema(description = "性别")
    private String userGender;
    @Schema(description = "联系方式")
    private String userMobile;
    @Schema(description = "联系方式")
    private String userEmail;
    @Schema(description = "住址")
    private String userAddress;

    @Schema(description = "头像地址")
    private String avatar;

    @Schema(description = "用户角色编码集合")
    private Set<String> roles;

    @Schema(description = "用户权限标识集合")
    private Set<String> perms;

}
