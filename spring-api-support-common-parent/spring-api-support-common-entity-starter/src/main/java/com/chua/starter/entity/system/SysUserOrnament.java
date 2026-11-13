package com.chua.starter.entity.system;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chua.starter.mybatis.pojo.SysBase;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户饰品佩戴表
 *
 * <p>每个槽位一行：{@code (sys_user_id, sys_ornament_slot)} 唯一。
 * 取下饰品直接删行，因此「有没有记录」就等于「这个槽位是否佩戴」。</p>
 *
 * @author CH
 * @since 4.0.0
 */
@Schema(description = "用户饰品佩戴表")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_user_ornament")
public class SysUserOrnament extends SysBase {

    @TableId(value = "sys_user_ornament_id", type = IdType.AUTO)
    @Schema(description = "佩戴记录ID")
    private Integer sysUserOrnamentId;

    @TableField(value = "sys_user_id")
    @Schema(description = "用户ID")
    private Integer sysUserId;

    @TableField(value = "sys_ornament_slot")
    @Schema(description = "槽位: avatar / nameplate")
    private String sysOrnamentSlot;

    @TableField(value = "sys_ornament_id")
    @Schema(description = "饰品ID")
    private Integer sysOrnamentId;
}
