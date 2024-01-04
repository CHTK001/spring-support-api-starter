package com.chua.starter.admin.result;

import com.chua.starter.admin.pojo.RouteVO;
import lombok.Data;

import java.util.List;
import java.util.Set;

/**
 * 用户菜单结果
 * @author CH
 */
@Data
public class UserMenuResult {
    /**
     * 用户菜单
     */
    private List<RouteVO> menu;
    /**
     * 权限
     */
    private Set<String> permissions;
    /**
     * 用户所拥有的组件
     */
    private Set<String> dashboardGrid;
    /**
     * 首页样式
     */
    private String dashboard;
    /**
     * 布局
     */
    private SysUserGridResult grid;
}
