package com.chua.starter.system.util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * 通用树形结构构建工具
 *
 * <p>原 com.chua.common.support.utils.TreeUtils 在当前 utils 版本中已移除，
 * 此处提供等价实现以保证部门树 / 菜单树功能可用。
 *
 * @since 2026/08/02
 * @author CH
 */
public class TreeUtils {

    /**
     * 工具类不可实例化
     */
    private TreeUtils() {
    }

    /**
     * 将平铺列表构建为树形结构。
     *
     * <p>按节点 id / pid 建立父子关系，父节点构建完成后通过
     * {@link TreeNode#acceptChildren(List)} 把子节点回填到扩展对象。
     *
     * @param sources 源列表（平铺）
     * @param mapper  节点映射器（把源元素转为树节点）
     * @param <S>     源元素类型
     * @param <T>     扩展对象类型
     * @return 根节点扩展对象列表（已构建完整的树）
     */
    public static <S, T> List<T> toTree(List<S> sources, Function<S, TreeNode<T>> mapper) {
        if (sources == null || sources.isEmpty()) {
            return new ArrayList<>();
        }
        List<TreeNode<T>> nodes = new ArrayList<>(sources.size());
        for (S source : sources) {
            TreeNode<T> node = mapper.apply(source);
            if (node != null) {
                nodes.add(node);
            }
        }
        return buildTree(nodes);
    }

    /**
     * 依据节点父子关系构建树。
     *
     * @param nodes 全部节点
     * @param <T>   扩展对象类型
     * @return 根节点扩展对象列表
     */
    private static <T> List<T> buildTree(List<TreeNode<T>> nodes) {
        List<T> roots = new ArrayList<>();
        for (TreeNode<T> node : nodes) {
            boolean hasParent = false;
            for (TreeNode<T> candidate : nodes) {
                if (candidate == node) {
                    continue;
                }
                if (isChildOf(node, candidate)) {
                    hasParent = true;
                    break;
                }
            }
            if (!hasParent) {
                collectChildren(node, nodes);
                roots.add(node.getExt());
            }
        }
        return roots;
    }

    /**
     * 判断节点是否为指定候选节点的子节点。
     *
     * @param node      待判断节点
     * @param candidate 候选父节点
     * @param <T>       扩展对象类型
     * @return true 表示是子节点
     */
    private static <T> boolean isChildOf(TreeNode<T> node, TreeNode<T> candidate) {
        return node.getPid() != null && node.getPid().equals(candidate.getId());
    }

    /**
     * 递归收集节点的所有直接子节点并回填。
     *
     * @param node  父节点
     * @param nodes 全部节点
     * @param <T>   扩展对象类型
     */
    private static <T> void collectChildren(TreeNode<T> node, List<TreeNode<T>> nodes) {
        List<T> children = new ArrayList<>();
        for (TreeNode<T> candidate : nodes) {
            if (isChildOf(candidate, node)) {
                collectChildren(candidate, nodes);
                children.add(candidate.getExt());
            }
        }
        node.acceptChildren(children);
    }
}
