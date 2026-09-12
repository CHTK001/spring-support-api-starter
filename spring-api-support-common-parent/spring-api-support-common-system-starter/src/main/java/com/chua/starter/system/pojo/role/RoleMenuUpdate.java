package com.chua.starter.system.pojo.role;

import com.chua.starter.system.validator.group.SelectGroup;
import com.chua.starter.system.validator.group.UpdateGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * @since 2024/8/8
 * @author CH
 */
@Data
@Schema(description = "角色菜单")
public class RoleMenuUpdate {

    /**
      * 角色标识
     */
    @NotNull(message = "角色ID不能为空", groups = {UpdateGroup.class, SelectGroup.class})
    @Schema(description = "角色ID")
    /** 角色id */
    private Integer roleId;

    /**
    * 菜单标识
     */
    @NotNull(message = "菜单ID不能为空", groups = {UpdateGroup.class})
    @Schema(description = "菜单ID")
    /** menuid */
    private List<Integer> menuId;
}
