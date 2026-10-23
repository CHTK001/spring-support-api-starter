package com.chua.starter.system.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

/**
 * 通用树形结构构建工具
 *
 * <p>委托 {@code com.chua.common.support.utils.TreeUtils#build} 组合实现；
 * 退化输入（重复 ID / 环）回落本类遗留算法以保持原返回语义。
 *
 * @since 4.0.0.43
 * @author CH
 * @deprecated 使用 {@code com.chua.common.support.utils.TreeUtils}，本类仅为兼容保留
 */
@Deprecated(since = "4.0.0.43", forRemoval = false)
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
     * 根判定：pid 为空或父 id 不在集合中（孤儿归根），各层保持入参相对顺序。
     *
     * @param sources 源列表（平铺）
     * @param mapper  节点映射器（把源元素转为树节点）
     * @param <S>     源元素类型
     * @param <T>     扩展对象类型
     * @return 根节点扩展对象列表（已构建完整的树）
     */
    public static <S, T> List<T> toTree(List<S> sources, Function<S, TreeNode<T>> mapper) {
        if (sources == null || sources.isEmpty()) {
            return new ArrayList<>(0);
        }
        List<TreeNode<T>> nodes = new ArrayList<>(sources.size());
        for (S source : sources) {
            TreeNode<T> node = mapper.apply(source);
            if (node != null) {
                nodes.add(node);
            }
        }
        if (nodes.isEmpty()) {
            return new ArrayList<>(0);
        }
        try {
            Set<TreeNode<T>> nonLeaf = Collections.newSetFromMap(new IdentityHashMap<>(nodes.size()));
            List<TreeNode<T>> roots = com.chua.common.support.utils.TreeUtils.build(
                    nodes, TreeNode::getId, TreeNode::getPid,
                    (node, kids) -> {
                        nonLeaf.add(node);
                        List<T> exts = new ArrayList<>(kids.size());
                        for (TreeNode<T> kid : kids) {
                            exts.add(kid.getExt());
                        }
                        node.acceptChildren(exts);
                    });
            // 遗留算法对叶子节点同样回填空列表，此处补齐以保持返回语义一致
            for (TreeNode<T> node : nodes) {
                if (!nonLeaf.contains(node)) {
                    node.acceptChildren(new ArrayList<>(0));
                }
            }
            List<T> result = new ArrayList<>(roots.size());
            for (TreeNode<T> root : roots) {
                result.add(root.getExt());
            }
            return result;
        } catch (IllegalArgumentException | IllegalStateException ex) {
            // 退化输入（重复 id / 环 / 自环）保持遗留算法的返回语义，不抛错
            return buildTree(nodes);
        }
    }

    /**
     * 依据节点父子关系构建树。
     *
     * @param nodes 全部节点
     * @param <T>   扩展对象类型
     * @return 根节点扩展对象列表
     */
    private static <T> List<T> buildTree(List<TreeNode<T>> nodes) {
        List<T> roots = new ArrayList<>(nodes.size());
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
     * @return collectChildren的结果
     */
    private static <T> void collectChildren(TreeNode<T> node, List<TreeNode<T>> nodes) {
        List<T> children = new ArrayList<>(nodes.size());
        for (TreeNode<T> candidate : nodes) {
            if (isChildOf(candidate, node)) {
                collectChildren(candidate, nodes);
                children.add(candidate.getExt());
            }
        }
        node.acceptChildren(children);
    }
}
