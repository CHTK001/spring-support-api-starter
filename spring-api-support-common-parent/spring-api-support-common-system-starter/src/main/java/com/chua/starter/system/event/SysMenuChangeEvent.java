package com.chua.starter.system.event;

import org.springframework.context.ApplicationEvent;

/**
 * 菜单变更事件
 * <p>
 * 平台端菜单新增/修改/删除后发布，供租户端菜单同步等场景监听处理
 * </p>
 *
 * @version 1.0.0
 * @since 4.0.0.42
 * @author CH
 */
public class SysMenuChangeEvent extends ApplicationEvent {

    /**
     * 菜单ID
     */
    private final Integer sysMenuId;

    /**
     * 操作类型: create / update / delete
     */
    private final String operation;

    public SysMenuChangeEvent(Object source, Integer sysMenuId, String operation) {
        super(source);
        this.sysMenuId = sysMenuId;
        this.operation = operation;
    }

    /** 获取SysMenuId */
    public Integer getSysMenuId() {
        return sysMenuId;
    }

    /** 获取Operation */
    public String getOperation() {
        return operation;
    }
}
