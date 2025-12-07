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
 * 瑙掕壊 - 鑿滃崟鍏崇郴琛? *
 * @author CH
 * @since 2024/8/7
 */
@ApiModel(description = "瑙掕壊 - 鑿滃崟鍏崇郴琛?)
@Schema(description = "瑙掕壊 - 鑿滃崟鍏崇郴琛?)
@Data
@TableName(value = "sys_role_menu")
public class SysRoleMenu implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "sys_role_menu_id", type = IdType.AUTO)
    @ApiModelProperty(value = "涓婚敭ID")
    @Schema(description = "涓婚敭ID")
    @NotNull(message = "涓嶈兘涓簄ull")
    private Integer sysRoleMenuId;

    /**
     * 瑙掕壊ID
     */
    @TableField(value = "sys_role_id")
    @ApiModelProperty(value = "瑙掕壊ID")
    @Schema(description = "瑙掕壊ID")
    private Integer sysRoleId;

    /**
     * 鑿滃崟ID
     */
    @TableField(value = "sys_menu_id")
    @ApiModelProperty(value = "鑿滃崟ID")
    @Schema(description = "鑿滃崟ID")
    private Integer sysMenuId;
}