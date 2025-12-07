package com.chua.starter.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.chua.common.support.datasource.annotation.ColumnLength;
import com.chua.common.support.validator.group.AddGroup;
import com.chua.starter.mybatis.pojo.SysBase;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;

/**
 * 鐢ㄦ埛琛?
 *
 * @author CH
 * @since 2024/8/7
 */
@ApiModel(description = "鐢ㄦ埛琛?)
@Schema(description = "鐢ㄦ埛琛?)
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_user")
public class SysUser extends SysBase implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "sys_user_id", type = IdType.AUTO)
    @ApiModelProperty(value = "鐢ㄦ埛ID")
    @Schema(description = "鐢ㄦ埛ID")
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
     * 灞呬綇鍦板潃
     */
    @TableField(value = "sys_user_full_address")
    @ApiModelProperty(value = "灞呬綇鍦板潃")
    @Schema(description = "灞呬綇鍦板潃")
    @ColumnLength(400)
    @Size(max = 400, message = "灞呬綇鍦板潃鏈€澶ч暱搴﹁灏忎簬 400")
    private String sysUserFullAddress;

    /**
     * 瀵嗙爜
     */
    @TableField(value = "sys_user_password")
    @ApiModelProperty(value = "瀵嗙爜")
    @Schema(description = "瀵嗙爜")
    @Size(max = 255, message = "瀵嗙爜鏈€澶ч暱搴﹁灏忎簬 255")
    @NotEmpty(message = "瀵嗙爜涓嶈兘涓虹┖", groups = {AddGroup.class})
    private String sysUserPassword;

    /**
     * 澶村儚
     */
    @TableField(value = "sys_user_avatar")
    @ApiModelProperty(value = "澶村儚")
    @Schema(description = "澶村儚")
    @ColumnLength(4000)
    @Size(max = 4000, message = "澶村儚鏈€澶ч暱搴﹁灏忎簬 4000")
    private String sysUserAvatar;

    /**
     * 鐩?
     */
    @TableField(value = "sys_user_salt")
    @ApiModelProperty(value = "鐩?)
    @Schema(description = "鐩?)
    @Size(max = 255, message = "鐩愭渶澶ч暱搴﹁灏忎簬 255")
    private String sysUserSalt;

    /**
     * TOTP
     */
    @TableField(value = "sys_user_totp_code")
    @ApiModelProperty(value = "TOTP")
    @Schema(description = "TOTP")
    private String sysUserTotpCode;

    /**
     * 鏄惁鍒犻櫎; 0: 鏈垹闄?
     */
    @TableField(value = "sys_user_delete")
    @ApiModelProperty(value = "鏄惁鍒犻櫎; 0: 鏈垹闄?)
    @Schema(description = "鏄惁鍒犻櫎; 0: 鏈垹闄?)
    @TableLogic(value = "0", delval = "1")
    private Integer sysUserDelete;

    /**
     * 閮ㄩ棬ID
     */
    @TableField(value = "sys_dept_id")
    @ApiModelProperty(value = "閮ㄩ棬ID")
    @Schema(description = "閮ㄩ棬ID")
    private Integer sysDeptId;

    /**
     * 鏄惁鍚敤; 0:绂佺敤
     */
    @TableField(value = "sys_user_status")
    @ApiModelProperty(value = "鏄惁鍚敤; 0:绂佺敤")
    @Schema(description = "鏄惁鍚敤; 0:绂佺敤")
    private Integer sysUserStatus;

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

    /**
     * 鎬у埆; 0:鐢? 1:濂? 2:鍏朵粬
     */
    @TableField(value = "sys_user_sex")
    @ApiModelProperty(value = "鎬у埆; 0:鐢? 1:濂? 2:鍏朵粬")
    @Schema(description = "鎬у埆; 0:鐢? 1:濂? 2:鍏朵粬")
    private Integer sysUserSex;

    /**
     * 鐧诲綍娆℃暟
     */
    @TableField(value = "sys_user_login_count")
    @ApiModelProperty(value = "鐧诲綍娆℃暟")
    @Schema(description = "鐧诲綍娆℃暟")
    private Integer sysUserLongCount;

    /**
     * 鏄惁绯荤粺璐﹀彿; 0: 闈炵郴缁熻处鍙?
     */
    @TableField(value = "sys_user_in_system")
    @ApiModelProperty(value = "鏄惁绯荤粺璐﹀彿; 0: 闈炵郴缁熻处鍙?)
    @Schema(description = "鏄惁绯荤粺璐﹀彿; 0: 闈炵郴缁熻处鍙?)
    private Integer sysUserInSystem;

    /**
     * 韬唤璇?
     */
    @TableField(value = "sys_user_card")
    @ApiModelProperty(value = "韬唤璇?)
    @Schema(description = "韬唤璇?)
    @Size(max = 255, message = "韬唤璇佹渶澶ч暱搴﹁灏忎簬 255")
    private String sysUserCard;

    /**
     * 澶囨敞
     */
    @TableField(value = "sys_user_remark")
    @ApiModelProperty(value = "澶囨敞")
    @Schema(description = "澶囨敞")
    @Size(max = 255, message = "澶囨敞鏈€澶ч暱搴﹁灏忎簬 255")
    private String sysUserRemark;

    /**
     * 鏈€鍚庝竴娆＄櫥褰曞湴鐐?
     */
    @TableField(value = "sys_user_last_address")
    @ApiModelProperty(value = "鏈€鍚庝竴娆＄櫥褰曞湴鐐?)
    @Schema(description = "鏈€鍚庝竴娆＄櫥褰曞湴鐐?)
    @Size(max = 255, message = "鏈€鍚庝竴娆＄櫥褰曞湴鐐规渶澶ч暱搴﹁灏忎簬 255")
    private String sysUserLastAddress;

    /**
     * 娉ㄥ唽鍦扮偣
     */
    @TableField(value = "sys_user_register_address")
    @ApiModelProperty(value = "娉ㄥ唽鍦扮偣")
    @Schema(description = "娉ㄥ唽鍦扮偣")
    private String sysUserRegisterAddress;

    /**
     * 鏈€鍚庝竴娆＄櫥褰旾P
     */
    @TableField(value = "sys_user_last_ip")
    @ApiModelProperty(value = "鏈€鍚庝竴娆＄櫥褰旾P")
    @Schema(description = "鏈€鍚庝竴娆＄櫥褰旾P")
    @Size(max = 255, message = "鏈€鍚庝竴娆＄櫥褰旾P鏈€澶ч暱搴﹁灏忎簬 255")
    private String sysUserLastIp;

    /**
     * 娉ㄥ唽IP
     */
    @TableField(value = "sys_user_register_ip")
    @ApiModelProperty(value = "娉ㄥ唽IP")
    @Schema(description = "娉ㄥ唽IP")
    @Size(max = 255, message = "娉ㄥ唽IP鏈€澶ч暱搴﹁灏忎簬 255")
    private String sysUserRegisterIp;

    /**
     * 瀹㈡埛绔疘D
     */
    @TableField(value = "sys_client_id")
    @ApiModelProperty(value = "瀹㈡埛绔疘D")
    @Schema(description = "瀹㈡埛绔疘D")
    private String sysClientId;

    /**
     * 瑙掕壊鍚嶇О
     */
    @Schema(description = "瑙掕壊(绯荤粺鐢熸垚)")
    @TableField(exist = false)
    private List<SysRole> userRoles;

    @TableField(exist = false)
    private Integer sysTenantId;
}