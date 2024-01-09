package com.chua.starter.sso.impl;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chua.common.support.datasource.annotation.Column;
import com.chua.common.support.datasource.annotation.Table;
import com.chua.starter.common.support.constant.DataFilterTypeEnum;
import lombok.Data;

/**
 * 角色表
 */
@TableName(value = "sys_role")
@Data
@Table(comment = "角色表")
public class SysRole {
    /**
     *
     */
    @TableId(type = IdType.AUTO)
    private Long roleId;

    /**
     * 角色名称
     */
    private String roleName;

    /**
     * 角色编码
     */
    private String roleCode;

    /**
     * 显示顺序
     */
    private Integer roleSort;

    /**
     * 角色状态(1-正常；0-停用)
     */
    @Column(defaultValue = "1")
    private Integer roleStatus;

    /**
     * 是否系统角色
     */
    @Column(comment = "是否系统角色; 0:非系统角色", defaultValue = "0")
    private Integer roleSys;

    /**
     * 数据权限
     */
    @Column(comment = "数据权限; 1: 全部可见<br/>" +
            "2: 本人可见<br/>" +
            "3: 所在部门可见<br/>" +
            "4: 所在部门及子级可见<br/>" +
            "5: 选择的部门可见<br/>" +
            "6: 自定义<br/>", defaultValue = "1")
    private DataFilterTypeEnum roleDataScope;
    /**
     * 自定义数据权限规则
     */
    private String roleDataRule;

    /**
     * 控制台样式
     */
    private String roleDashboard;
    /**
     * 控制台样式
     */
    private String roleGrid;
}