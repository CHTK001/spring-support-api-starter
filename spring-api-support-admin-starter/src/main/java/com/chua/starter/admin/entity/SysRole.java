package com.chua.starter.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chua.common.support.datasource.annotation.Column;
import com.chua.common.support.datasource.annotation.Table;
import com.chua.starter.mybatis.pojo.SysBase;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;

/**
 * 角色表
 */
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_role")
@Data
@Table(comment = "角色表")
public class SysRole extends SysBase {
    /**
     *
     */
    @TableId(type = IdType.AUTO)
    @Schema(description = "主键")
    private Long roleId;

    /**
     * 角色名称
     */
    @Schema(description = "角色名称")
    @NotNull(message = "角色名称不能为空")
    private String roleName;

    /**
     * 角色编码
     */
    @Schema(description = "角色编码")
    @NotNull(message = "角色编码不能为空")
    private String roleCode;

    /**
     * 显示顺序
     */
    @Schema(description = "显示顺序")
    @Column(defaultValue = "1", comment = "显示顺序")
    private Integer roleSort;

    /**
     * 角色状态(1-正常；0-停用)
     */
    @Schema(description = "角色状态(1-正常；0-停用)")
    @Column(defaultValue = "1", comment = "角色状态(1-正常；0-停用)")
    private Integer roleStatus;

    /**
     * 是否系统角色
     */
    @Column(comment = "是否系统角色; 0:非系统角色", defaultValue = "0")
    private Integer roleSys;
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
    @Column(comment = "数据权限; 1: 全部可见<br/>" +
            "2: 本人可见<br/>" +
            "3: 所在部门可见<br/>" +
            "4: 所在部门及子级可见<br/>" +
            "5: 选择的部门可见<br/>" +
            "6: 自定义<br/>", defaultValue = "1")
    private String roleDataScope;
    /**
     * 自定义数据权限规则
     */
    @Schema(description = "自定义数据权限规则")
    @Column(length = 1000)
    private String roleDataRule;
    /**
     * 控制台样式
     */
    @Schema(description = "控制台样式")
    private String roleDashboard;
    /**
     * 组件
     */
    @Schema(description = "组件")
    private String roleGrid;
}