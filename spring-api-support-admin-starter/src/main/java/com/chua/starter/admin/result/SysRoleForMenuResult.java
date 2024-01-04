package com.chua.starter.admin.result;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 角色更新
 * @author CH
 */
@Data
@Schema(description = "角色信息信息")
public class SysRoleForMenuResult {
    /**
     * 角色ID
     */
    @Schema(description = "角色I")
    @NotNull(message = "角色ID不能为空")
    private Long roleId;
    /**
     * 数据权限
     */
    @Schema(description = "数据权限; 1: 全部可见<br/>" +
            "2: 本人可见<br/>" +
            "3: 所在部门可见<br/>" +
            "4: 所在部门及子级可见<br/>" +
            "5: 选择的部门可见<br/>" +
            "6: 自定义<br/>"
    )
    private String roleDataScope;
    /**
     * 自定义数据权限规则
     */
    @Schema(description = "自定义数据权限规则")
    private String roleDataRule;
    /**
     * 菜单ID
     */
    @Schema(description = "菜单ID")
    private String[] menuIds;
    /**
     * dashboard
     */
    @Schema(description = "控制台样式")
    private String roleDashboard;
    /**
     * roleGrid
     */
    @Schema(description = "roleGrid")
    private String[] roleGrid;
    /**
     * 部门ID
     */
    @Schema(description = "部门ID")
    private String[] depts;
}
