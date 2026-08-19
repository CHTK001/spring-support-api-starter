package com.chua.starter.system.pojo.online;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 在线用户视图对象
 *
 * @since 2024/12/15
 * @author CH
 */
@Data
@Schema(description = "在线用户信息")
public class OnlineUserVO {

    @Schema(description = "用户ID")
    /** 用户ID */
    private String userId;

    @Schema(description = "用户名")
    /** Username */
    private String username;

    @Schema(description = "昵称")
    /** Nickname */
    private String nickname;

    @Schema(description = "部门名称")
    /** Dept名称 */
    private String deptName;

    @Schema(description = "登录IP")
    /** LoginIP */
    private String loginIp;

    @Schema(description = "登录地址")
    /** Login地址 */
    private String loginAddress;

    @Schema(description = "浏览器")
    /** Browser */
    private String browser;

    @Schema(description = "操作系统")
    /** OS */
    private String os;

    @Schema(description = "登录时间")
    /** Login时间 */
    private LocalDateTime loginTime;

    @Schema(description = "Token")
    /** 令牌 */
    private String token;

    @Schema(description = "登录类型")
    /** Login类型 */
    private String loginType;
}
