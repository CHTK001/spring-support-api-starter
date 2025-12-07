package com.chua.starter.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chua.common.support.validator.group.AddGroup;
import com.chua.common.support.validator.group.UpdateGroup;
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

/**
 * 瑙掕壊琛?
 *
 * @author CH
 * @since 2024/8/7
 */
@ApiModel(description = "瑙掕壊琛")
@Schema(description = "瑙掕壊琛")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_role")
public class SysRole extends SysBase implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "sys_role_id", type = IdType.AUTO)
    @ApiModelProperty(value = "瑙掕壊ID")
    @Schema(description = "瑙掕壊ID")
    @NotNull(message = "瑙掕壊ID涓嶈兘涓虹┖", groups = {UpdateGroup.class})
    private Integer sysRoleId;

    /**
     * 瑙掕壊鍚嶇О
     */
    @TableField(value = "sys_role_name")
    @ApiModelProperty(value = "瑙掕壊鍚嶇О")
    @Schema(description = "瑙掕壊鍚嶇О")
    @Size(max = 255, message = "瑙掕壊鍚嶇О鏈€澶ч暱搴﹁灏忎簬 255")
    private String sysRoleName;

    /**
     * 瑙掕壊缂栫爜; 鍞竴
     */
    @TableField(value = "sys_role_code")
    @ApiModelProperty(value = "瑙掕壊缂栫爜; 鍞竴")
    @Schema(description = "瑙掕壊缂栫爜; 鍞竴")
    @Size(max = 255, message = "瑙掕壊缂栫爜; 鍞竴鏈€澶ч暱搴﹁灏忎簬 255")
    @NotEmpty(message = "瑙掕壊缂栫爜; 鍞竴涓嶈兘涓虹┖", groups = {AddGroup.class})
    private String sysRoleCode;

    /**
     * 鏄惁鍚敤
     */
    @TableField(value = "sys_role_status")
    @ApiModelProperty(value = "鏄惁鍚敤; 0:涓嶅惎鐢")
    @Schema(description = "鏄惁鍚敤; 0:涓嶅惎鐢")
    private Integer sysRoleStatus;

    /**
     * 浼樺厛绾?
     */
    @TableField(value = "sys_role_sort")
    @ApiModelProperty(value = "浼樺厛绾")
    @Schema(description = "浼樺厛绾")
    private Integer sysRoleSort;

    /**
     * 鏄惁绯荤粺瑙掕壊; 0:闈炵郴缁熻鑹
     */
    @TableField(value = "sys_role_in_system")
    @ApiModelProperty(value = "鏄惁绯荤粺瑙掕壊; 0:闈炵郴缁熻鑹")
    @Schema(description = "鏄惁绯荤粺瑙掕壊; 0:闈炵郴缁熻鑹")
    private Integer sysRoleInSystem;

    /**
     * 澶囨敞
     */
    @TableField(value = "sys_role_remark")
    @ApiModelProperty(value = "澶囨敞")
    @Schema(description = "澶囨敞")
    @Size(max = 255, message = "澶囨敞鏈€澶ч暱搴﹁灏忎簬 255")
    private String sysRoleRemark;

    /**
     * 瑙掕壊鏄惁鍙
     */
    @TableField(value = "sys_role_readable")
    @ApiModelProperty(value = "瑙掕壊鏄惁鍙")
    @Schema(description = "瑙掕壊鏄惁鍙")
    private Integer sysRoleReadable = 0x0000_0001;

    /**
     * 瑙掕壊鏄惁鍙啓
     */
    @TableField(value = "sys_role_writeable")
    @ApiModelProperty(value = "瑙掕壊鏄惁鍙啓")
    @Schema(description = "瑙掕壊鏄惁鍙啓")
    private Integer sysRoleWriteable;

    /**
     * 瑙掕壊鏄惁鍙墽琛?
     */
    @TableField(value = "sys_role_executable")
    @ApiModelProperty(value = "瑙掕壊鏄惁鍙墽琛")
    @Schema(description = "瑙掕壊鏄惁鍙墽琛")
    private Integer sysRoleExecutable;

    /**
     * 瑙掕壊鍙湅鏉跨被鍨"
     */
    @TableField(value = "sys_role_board_card")
    @ApiModelProperty(value = "瑙掕壊鍙湅鏉跨被鍨")
    @Schema(description = "瑙掕壊鍙湅鏉跨被鍨")
    private Integer sysRoleBoardCard;

    /**
     * 鐢ㄦ埛ID
     */
    @Schema(description = "鐢ㄦ埛ID")
    @TableField(exist = false)
    private Integer sysUserId;
}