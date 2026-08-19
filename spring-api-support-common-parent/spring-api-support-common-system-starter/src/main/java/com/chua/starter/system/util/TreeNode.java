package com.chua.starter.system.util;

import com.chua.common.support.function.SafeConsumer;

import java.util.List;

/**
 * 通用树节点
 *
 * <p>用于将平铺列表构建为树形结构，支持自定义扩展对象（{@link #ext}）。
 * 原 com.chua.common.support.lang.treenode.TreeNode 在当前 utils 版本中已移除，
 * 此处提供等价实现以保证部门树 / 菜单树功能可用。
 *
 * @param <T> 扩展对象类型
 * @since 2026/08/02
 * @author CH
 */
public class TreeNode<T> {

    /**
     * 节点 ID
     */
    private String id;

    /**
     * 父节点 ID
     */
    private String pid;

    /**
     * 节点标签
     */
    private String label;

    /**
     * 节点值
     */
    private String value;

    /**
     * 扩展对象（业务 VO）
     */
    private T ext;

    /**
     * 子节点列表回调（用于把子列表回填到业务 VO）
     */
    private SafeConsumer<List<T>> childrenConsumer;

    /**
     * 设置节点 ID
     *
     * @param id 节点 ID
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * 设置父节点 ID
     *
     * @param pid 父节点 ID
     */
    public void setPid(String pid) {
        this.pid = pid;
    }

    /**
     * 设置节点标签
     *
     * @param label 节点标签
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * 设置节点值
     *
     * @param value 节点值
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * 设置扩展对象
     *
     * @param ext 扩展对象
     */
    public void setExt(T ext) {
        this.ext = ext;
    }

    /**
     * 设置子节点列表回调
     *
     * @param childrenConsumer 子节点列表回调
     */
    public void setChildrenConsumer(SafeConsumer<List<T>> childrenConsumer) {
        this.childrenConsumer = childrenConsumer;
    }

    /**
     * 获取节点 ID
     *
     * @return 节点 ID
     */
    public String getId() {
        return id;
    }

    /**
     * 获取父节点 ID
     *
     * @return 父节点 ID
     */
    public String getPid() {
        return pid;
    }

    /**
     * 获取扩展对象
     *
     * @return 扩展对象
     */
    public T getExt() {
        return ext;
    }

    /**
     * 回填子节点列表到扩展对象
     *
     * @param children 子节点列表
     */
    public void acceptChildren(List<T> children) {
        if (childrenConsumer != null) {
            try {
                childrenConsumer.safeAccept(children);
            } catch (Throwable throwable) {
                throw new IllegalStateException("树节点子列表回填失败", throwable);
            }
        }
    }
}
