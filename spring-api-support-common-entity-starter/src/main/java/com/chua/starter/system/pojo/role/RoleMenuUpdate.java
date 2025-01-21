package com.chua.starter.system.pojo.role;

import com.chua.common.support.validator.group.SelectGroup;
import com.chua.common.support.validator.group.UpdateGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * @author CH
 * @since 2024/8/8
 */
@Data
@Schema(description = "角色菜单")
public class RoleMenuUpdate {

    /**
     * 角色ID
     */
    @NotNull(message = "角色ID不能为空", groups = {UpdateGroup.class, SelectGroup.class})
    @Schema(description = "角色ID")
    private Integer roleId;

    /**
     * 菜单ID
     */
    @NotNull(message = "菜单ID不能为空", groups = {UpdateGroup.class})
    @Schema(description = "菜单ID")
    private List<Integer> menuId;
}
