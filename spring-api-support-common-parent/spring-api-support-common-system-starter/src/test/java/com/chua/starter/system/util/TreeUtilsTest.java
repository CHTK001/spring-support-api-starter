package com.chua.starter.system.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * TreeUtils.toTree 行为锁定。
 *
 * <p>toTree 委托 {@code com.chua.common.support.utils.TreeUtils#build} 组合实现，
 * 本类锁定：平铺成树、孤儿归根、空输入、入参顺序保持。</p>
 *
 * @author CH
 * @since 4.0.0.51
 */
@DisplayName("TreeUtils.toTree 行为锁定")
class TreeUtilsTest {

    /**
     * 构造测试节点。
     *
     * @param id       节点标识
     * @param pid      父节点标识
     * @param children 子列表回填槽
     * @return 树节点
     */
    private static TreeNode<Slot> node(String id, String pid, Slot slot) {
        TreeNode<Slot> treeNode = new TreeNode<>();
        treeNode.setId(id);
        treeNode.setPid(pid);
        treeNode.setExt(slot);
        treeNode.setChildrenConsumer(children -> slot.children = children);
        return treeNode;
    }

    /**
     * 平铺列表按 pid 建树，根下挂子，子下挂孙。
     */
    @Test
    @DisplayName("平铺成树-父子三层挂载正确")
    // [P3C NM-命名 豁免]
    void 平铺成树() {
        List<Slot> slots = List.of(new Slot("root"), new Slot("child"), new Slot("grand"));
        List<Slot> tree = TreeUtils.toTree(
                List.of(new String[]{"root", null}, new String[]{"child", "root"}, new String[]{"grand", "child"}),
                pair -> node(pair[0], pair[1], slots.stream()
                        .filter(slot -> slot.name.equals(pair[0])).findFirst().orElseThrow()));

        assertEquals(1, tree.size());
        assertEquals("root", tree.get(0).name);
        assertEquals(1, tree.get(0).children.size());
        assertEquals("child", tree.get(0).children.get(0).name);
        assertEquals(1, tree.get(0).children.get(0).children.size());
        assertEquals("grand", tree.get(0).children.get(0).children.get(0).name);
    }

    /**
     * 父 id 不在集合中的孤儿节点归根。
     */
    @Test
    @DisplayName("孤儿归根-父缺失时成为根")
    // [P3C NM-命名 豁免]
    void 孤儿归根() {
        Slot root = new Slot("root");
        Slot orphan = new Slot("orphan");
        List<Slot> tree = TreeUtils.toTree(
                List.of(new String[]{"root", null}, new String[]{"orphan", "missing"}),
                pair -> node(pair[0], pair[1], "root".equals(pair[0]) ? root : orphan));

        assertEquals(2, tree.size());
        assertEquals("root", tree.get(0).name);
        assertEquals("orphan", tree.get(1).name);
        assertTrue(tree.get(1).children.isEmpty());
    }

    /**
     * 空输入与全空映射返回空列表，不抛错。
     */
    @Test
    @DisplayName("空输入-返回空列表")
    // [P3C NM-命名 豁免]
    void 空输入() {
        assertTrue(TreeUtils.<String[], Slot>toTree(null, pair -> null).isEmpty());
        assertTrue(TreeUtils.<String[], Slot>toTree(List.of(), pair -> null).isEmpty());
        List<String[]> single = new ArrayList<>();
        single.add(new String[]{"a", null});
        List<Slot> mappedEmpty = TreeUtils.toTree(single, pair -> null);
        assertTrue(mappedEmpty.isEmpty());
    }

    /**
     * 根与同层子节点保持入参相对顺序。
     */
    @Test
    @DisplayName("排序-保持入参相对顺序")
    // [P3C NM-命名 豁免]
    void 保持顺序() {
        List<String[]> pairs = new ArrayList<>();
        pairs.add(new String[]{"b", null});
        pairs.add(new String[]{"a", null});
        pairs.add(new String[]{"b2", "b"});
        pairs.add(new String[]{"b1", "b"});
        List<Slot> tree = TreeUtils.toTree(pairs, pair -> node(pair[0], pair[1], new Slot(pair[0])));

        assertEquals(List.of("b", "a"), List.of(tree.get(0).name, tree.get(1).name));
        assertEquals(List.of("b2", "b1"),
                List.of(tree.get(0).children.get(0).name, tree.get(0).children.get(1).name));
    }

    /**
     * 业务扩展槽。
     */
    private static class Slot {
        private final String name;
        private List<Slot> children = new ArrayList<>();

        private Slot(String name) {
            this.name = name;
        }
    }
}
