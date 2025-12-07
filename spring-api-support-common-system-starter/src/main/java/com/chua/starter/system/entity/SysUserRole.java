package com.chua.starter.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 鐢ㄦ埛 - 瑙掕壊鍏崇郴琛? *
 * @author CH
 * @since 2024/8/7
 */
@ApiModel(description = "鐢ㄦ埛 - 瑙掕壊鍏崇郴琛?)
@Schema(description = "鐢ㄦ埛 - 瑙掕壊鍏崇郴琛?)
@Data
@TableName(value = "sys_user_role")
public class SysUserRole implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "sys_user_role_id", type = IdType.AUTO)
    @ApiModelProperty(value = "涓婚敭ID")
    @Schema(description = "涓婚敭ID")
    @NotNull(message = "涓嶈兘涓簄ull")
    private Integer sysUserRoleId;

    /**
     * 鐢ㄦ埛ID
     */
    @TableField(value = "sys_user_id")
    @ApiModelProperty(value = "鐢ㄦ埛ID")
    @Schema(description = "鐢ㄦ埛ID")
    private Integer sysUserId;

    /**
     * 瑙掕壊ID
     */
    @TableField(value = "sys_role_id")
    @ApiModelProperty(value = "瑙掕壊ID")
    @Schema(description = "瑙掕壊ID")
    private Integer sysRoleId;
}