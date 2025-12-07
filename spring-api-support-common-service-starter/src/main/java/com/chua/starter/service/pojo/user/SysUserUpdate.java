package com.chua.starter.service.pojo.user;

import com.chua.starter.service.model.SysUser;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @author CH
 * @since 2024/8/8
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "用户更新")
public class SysUserUpdate extends SysUser {


    /**
     * 角色ID
     */
    @Schema(description = "角色ID")
    private List<String> roleIds;

    /**
     * 是否更新角色
     */
    @Schema(description = "是否更新角色")
    private boolean updateRole;
}
