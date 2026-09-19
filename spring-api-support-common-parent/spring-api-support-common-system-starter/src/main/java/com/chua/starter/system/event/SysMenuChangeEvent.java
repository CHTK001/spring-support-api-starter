package com.chua.starter.system.event;

import org.springframework.context.ApplicationEvent;

/**
 * 菜单变更事件
 * <p>
 * 平台端菜单新增/修改/删除后发布，供租户端菜单同步等场景监听处理
 * </p>
 *
 * @since 4.0.0.42
 * @author CH
 */
public class SysMenuChangeEvent extends ApplicationEvent {

    /**
     * 菜单标识
     */
    private final Integer sysMenuId;

    /**
     * 操作类型: 创建 / 更新 / 删除
     */
    private final String operation;

    /**
     * 创建 sysmenu改变事件 实例
     * @param source 源
     * @param sysMenuId sysmenuid
     * @param operation operation
     */
    public SysMenuChangeEvent(Object source, Integer sysMenuId, String operation) {
        super(source);
        this.sysMenuId = sysMenuId;
        this.operation = operation;
    }

    /**
     * 获取sysmenuid
     *
     * @return 获取sysmenuid的结果
     */
    public Integer getSysMenuId() {
        return sysMenuId;
    }

    /**
     * 获取Operation
     *
     * @return 获取operation的结果
     */
    public String getOperation() {
        return operation;
    }
}
