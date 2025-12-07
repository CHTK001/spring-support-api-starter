package com.chua.starter.service.pojo.user;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.chua.common.support.validator.group.AddGroup;
import com.chua.starter.mybatis.pojo.SysBase;
import com.chua.starter.service.model.SysRole;
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
 * @author CH
 * @since 2024/8/20
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "鐢ㄦ埛琛")
public class SysUserVO  extends SysBase implements Serializable {
    private static final long serialVersionUID = 1L;
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
     * 鏄惁鍚敤锛?锛氱鐢?
     */
    @TableField(value = "sys_user_status")
    @ApiModelProperty(value = "鏄惁鍚敤锛?锛氱鐢")
    @Schema(description = "鏄惁鍚敤锛?锛氱鐢")
    private Integer sysUserStatus;

    /**
     * 鎵嬫満鍙?
     */
    @TableField(value = "sys_user_phone")
    @ApiModelProperty(value = "鎵嬫満鍙")
    @Schema(description = "鎵嬫満鍙")
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
     * 鎬у埆; 0:濂1:鐢 2:鍏朵粬
     */
    @TableField(value = "sys_user_sex")
    @ApiModelProperty(value = "鎬у埆; 0:濂1:鐢 2:鍏朵粬")
    @Schema(description = "鎬у埆; 0:濂1:鐢 2:鍏朵粬")
    private Integer sysUserSex;

    /**
     * 鏄惁绯荤粺璐﹀彿; 0: 闈炵郴缁熻处鍙
     */
    @TableField(value = "sys_user_in_system")
    @ApiModelProperty(value = "鏄惁绯荤粺璐﹀彿; 0: 闈炵郴缁熻处鍙")
    @Schema(description = "鏄惁绯荤粺璐﹀彿; 0: 闈炵郴缁熻处鍙")
    private Integer sysUserInSystem;

    /**
     * 韬唤璇?
     */
    @TableField(value = "sys_user_card")
    @ApiModelProperty(value = "韬唤璇")
    @Schema(description = "韬唤璇")
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
     * 鏈€鍚庝竴娆＄櫥褰曞湴鐐"
     */
    @TableField(value = "sys_user_last_address")
    @ApiModelProperty(value = "鏈€鍚庝竴娆＄櫥褰曞湴鐐")
    @Schema(description = "鏈€鍚庝竴娆＄櫥褰曞湴鐐")
    @Size(max = 255, message = "鏈€鍚庝竴娆＄櫥褰曞湴鐐规渶澶ч暱搴﹁灏忎簬 255")
    private String sysUserLastAddress;

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
     * 瑙掕壊鍚嶇О
     */
    @Schema(description = "瑙掕壊(绯荤粺鐢熸垚)")
    @TableField(exist = false)
    private List<SysRole> userRoles;
}