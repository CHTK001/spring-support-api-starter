package com.chua.starter.system.result;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.chua.common.support.validator.group.AddGroup;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Set;

/**
 * 用户登录瑙嗗浘瀵硅薄
 *
 * @author haoxr
 * @since 2022/1/14
 */
@Schema(description = "当前登录用户视图对象")
@Data
public class UserInfoVO {

    @TableId(value = "sys_user_id", type = IdType.AUTO)
    @ApiModelProperty(value = "")
    @Schema(description = "")
    @NotNull(message = "涓嶈兘涓簄ull")
    private Integer sysUserId;

    /**
     * 账号
     */
    @TableField(value = "sys_user_username")
    @ApiModelProperty(value = "账号")
    @Schema(description = "账号")
    @Size(max = 255, message = "账号鏈€澶ч暱搴﹁灏忎簬 255")
    @NotEmpty(message = "账号不能为空", groups = {AddGroup.class})
    private String sysUserUsername;

    /**
     * 名称
     */
    @TableField(value = "sys_user_nickname")
    @ApiModelProperty(value = "名称")
    @Schema(description = "名称")
    @Size(max = 255, message = "名称鏈€澶ч暱搴﹁灏忎簬 255")
    private String sysUserNickname;

    /**
     * 手机号"
     */
    @TableField(value = "sys_user_phone")
    @ApiModelProperty(value = "手机号")
    @Schema(description = "手机号")
    @Size(max = 255, message = "手机号锋渶澶ч暱搴﹁灏忎簬 255")
    private String sysUserPhone;
    /**
     * 邮箱
     */
    @TableField(value = "sys_user_email")
    @ApiModelProperty(value = "邮箱")
    @Schema(description = "邮箱")
    @Size(max = 255, message = "邮箱鏈€澶ч暱搴﹁灏忎簬 255")
    private String sysUserEmail;
    @Schema(description = "头像地址")
    private String avatar;

    /**
     * 绉熸埛ID
     */
    @Schema(description = "绉熸埛ID")
    private String tenantId;

    @Schema(description = "用户角色编码闆嗗悎")
    private Set<String> roles;

    @Schema(description = "用户权限鏍囪瘑闆嗗悎")
    private Set<String> perms;

}
