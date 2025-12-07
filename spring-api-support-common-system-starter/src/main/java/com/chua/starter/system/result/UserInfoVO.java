package com.chua.starter.service.result;

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
 * 鐢ㄦ埛鐧诲綍瑙嗗浘瀵硅薄
 *
 * @author haoxr
 * @since 2022/1/14
 */
@Schema(description = "褰撳墠鐧诲綍鐢ㄦ埛瑙嗗浘瀵硅薄")
@Data
public class UserInfoVO {

    @TableId(value = "sys_user_id", type = IdType.AUTO)
    @ApiModelProperty(value = "")
    @Schema(description = "")
    @NotNull(message = "涓嶈兘涓簄ull")
    private Integer sysUserId;

    /**
     * 璐﹀彿
     */
    @TableField(value = "sys_user_username")
    @ApiModelProperty(value = "璐﹀彿")
    @Schema(description = "璐﹀彿")
    @Size(max = 255, message = "璐﹀彿鏈€澶ч暱搴﹁灏忎簬 255")
    @NotEmpty(message = "璐﹀彿涓嶈兘涓虹┖", groups = {AddGroup.class})
    private String sysUserUsername;

    /**
     * 鍚嶇О
     */
    @TableField(value = "sys_user_nickname")
    @ApiModelProperty(value = "鍚嶇О")
    @Schema(description = "鍚嶇О")
    @Size(max = 255, message = "鍚嶇О鏈€澶ч暱搴﹁灏忎簬 255")
    private String sysUserNickname;

    /**
     * 鎵嬫満鍙?
     */
    @TableField(value = "sys_user_phone")
    @ApiModelProperty(value = "鎵嬫満鍙?)
    @Schema(description = "鎵嬫満鍙?)
    @Size(max = 255, message = "鎵嬫満鍙锋渶澶ч暱搴﹁灏忎簬 255")
    private String sysUserPhone;
    /**
     * 閭
     */
    @TableField(value = "sys_user_email")
    @ApiModelProperty(value = "閭")
    @Schema(description = "閭")
    @Size(max = 255, message = "閭鏈€澶ч暱搴﹁灏忎簬 255")
    private String sysUserEmail;
    @Schema(description = "澶村儚鍦板潃")
    private String avatar;

    /**
     * 绉熸埛ID
     */
    @Schema(description = "绉熸埛ID")
    private String tenantId;

    @Schema(description = "鐢ㄦ埛瑙掕壊缂栫爜闆嗗悎")
    private Set<String> roles;

    @Schema(description = "鐢ㄦ埛鏉冮檺鏍囪瘑闆嗗悎")
    private Set<String> perms;

}
