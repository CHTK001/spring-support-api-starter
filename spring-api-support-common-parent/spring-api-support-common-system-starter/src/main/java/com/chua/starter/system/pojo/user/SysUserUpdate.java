package com.chua.starter.system.pojo.user;

import com.chua.starter.entity.system.SysUser;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 用户更新
 *
 * @author CH
 * @since 2024/8/8
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "用户更新")
public class SysUserUpdate extends SysUser {

    /**
     * 角色ID列表
     */
    @Schema(description = "角色ID列表")
    private List<String> roleIds;

    /**
     * 是否更新角色
     */
    @Schema(description = "是否更新角色")
    private boolean updateRole;
}
