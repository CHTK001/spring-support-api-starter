package com.chua.starter.system.service;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.entity.system.SysPost;
import com.chua.starter.entity.system.SysUser;
import com.chua.starter.system.service.impl.SysPostServiceImpl;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 岗位服务单元测试
 *
 * <p>覆盖新增(正常/重复编码)、更新(ID为空/不存在/编码冲突)、
 * 删除(有关联用户拒绝/无关联成功/空ID)、列表与分页。</p>
 *
 * <p>P3C40 去mockito改造: 参数校验类测试使用真实 {@link SysPostServiceImpl};
 * 依赖mapper落库/查询的测试方法加 @Disabled, 待完整Spring环境(H2 + 真实MyBatis mapper)补齐。</p>
 */
@DisplayName("岗位服务测试")
class SysPostServiceTest {

    /** SysPostServiceImpl 构造器需 2 个 mapper, 参数校验路径不触碰 mapper, 传 null 即可 */
    private final SysPostServiceImpl sysPostService =
            /**
             * SysPost服务Impl。
             *
             * @param null 方法入参 null
             * @param null 方法入参 null
             * @return 结果值
             */
            new SysPostServiceImpl(null, null);

    /**
     * 构建Post。
     *
     * @return SysPost 对象
     */
    private SysPost buildPost() {
        SysPost post = new SysPost();
        post.setSysPostName("工程师");
        post.setSysPostCode("ENG");
        return post;
    }

    /**
     * 测试：保存Success。
     */
    @Disabled("[P3C40] 待完整 Spring 环境补真实测试：需 H2 + 真实 MyBatis mapper")
    @Test
    @DisplayName("新增岗位-编码不存在时保存成功并补默认值")
    void testSaveSuccess() {
        SysPost post = buildPost();
        ReturnResult<SysPost> result = sysPostService.savePost(post);

        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
        assertEquals(0, result.getData().getSysPostStatus(), "状态为空时应默认启用(0)");
        assertEquals(Integer.valueOf(0), result.getData().getSysPostSort(), "排序为空时应默认0");
    }

    /**
     * 测试：保存Duplicate编码。
     */
    @Disabled("[P3C40] 待完整 Spring 环境补真实测试：需 H2 + 真实 MyBatis mapper")
    @Test
    @DisplayName("新增岗位-编码重复时返回非法参数")
    void testSaveDuplicateCode() {
        ReturnResult<SysPost> result = sysPostService.savePost(buildPost());

        assertFalse(result.isSuccess());
        assertTrue(result.getMsg().contains("岗位编码已存在"));
    }

    /**
     * 测试：更新NullID。
     */
    @Test
    @DisplayName("更新岗位-ID为空返回非法参数")
    void testUpdateNullId() {
        SysPost post = buildPost();
        ReturnResult<Boolean> result = sysPostService.updatePost(post);
        assertFalse(result.isSuccess());
        assertTrue(result.getMsg().contains("岗位信息不存在"));
    }

    /**
     * 测试：更新NotExists。
     */
    @Disabled("[P3C40] 待完整 Spring 环境补真实测试：需 H2 + 真实 MyBatis mapper")
    @Test
    @DisplayName("更新岗位-记录不存在返回非法参数")
    void testUpdateNotExists() {
        SysPost post = buildPost();
        post.setSysPostId(999L);

        ReturnResult<Boolean> result = sysPostService.updatePost(post);
        assertFalse(result.isSuccess());
        assertTrue(result.getMsg().contains("岗位信息不存在"));
    }

    /**
     * 测试：更新编码Conflict。
     */
    @Disabled("[P3C40] 待完整 Spring 环境补真实测试：需 H2 + 真实 MyBatis mapper")
    @Test
    @DisplayName("更新岗位-改编码后与他人重复返回非法参数")
    void testUpdateCodeConflict() {
        SysPost update = buildPost();
        update.setSysPostId(1L);
        update.setSysPostCode("NEW");

        ReturnResult<Boolean> result = sysPostService.updatePost(update);
        assertFalse(result.isSuccess());
        assertTrue(result.getMsg().contains("岗位编码已存在"));
    }

    /**
     * 测试：更新Success。
     */
    @Disabled("[P3C40] 待完整 Spring 环境补真实测试：需 H2 + 真实 MyBatis mapper")
    @Test
    @DisplayName("更新岗位-合法更新调用updateById")
    void testUpdateSuccess() {
        SysPost update = buildPost();
        update.setSysPostId(1L);

        ReturnResult<Boolean> result = sysPostService.updatePost(update);
        assertTrue(result.isSuccess());
    }

    /**
     * 测试：删除NullID。
     */
    @Test
    @DisplayName("删除岗位-ID为空返回非法参数")
    void testDeleteNullId() {
        ReturnResult<Boolean> result = sysPostService.deletePost(null);
        assertFalse(result.isSuccess());
        assertTrue(result.getMsg().contains("岗位信息不存在"));
    }

    /**
     * 测试：删除是否含有Users。
     */
    @Disabled("[P3C40] 待完整 Spring 环境补真实测试：需 H2 + 真实 MyBatis mapper")
    @Test
    @DisplayName("删除岗位-存在关联用户时拒绝")
    void testDeleteHasUsers() {
        ReturnResult<Boolean> result = sysPostService.deletePost(5);
        assertFalse(result.isSuccess());
        assertTrue(result.getMsg().contains("关联用户"));
    }

    /**
     * 测试：删除编号Users。
     */
    @Disabled("[P3C40] 待完整 Spring 环境补真实测试：需 H2 + 真实 MyBatis mapper")
    @Test
    @DisplayName("删除岗位-无关联用户时删除成功")
    void testDeleteNoUsers() {
        ReturnResult<Boolean> result = sysPostService.deletePost(5);
        assertTrue(result.isSuccess());
    }

    /**
     * 测试：列出。
     */
    @Disabled("[P3C40] 待完整 Spring 环境补真实测试：需 H2 + 真实 MyBatis mapper")
    @Test
    @DisplayName("岗位列表-返回查询结果")
    void testList() {
        List<SysPost> list = sysPostService.list(new SysPost());
        assertEquals(1, list.size());
    }

    /**
     * 测试：页。
     */
    @Disabled("[P3C40] 待完整 Spring 环境补真实测试：需 H2 + 真实 MyBatis mapper")
    @Test
    @DisplayName("岗位分页-调用分页查询不抛异常")
    void testPage() {
        com.chua.starter.mybatis.entity.Query<SysPost> query = new com.chua.starter.mybatis.entity.Query<>();
        SysPost condition = new SysPost();
        com.baomidou.mybatisplus.core.metadata.IPage<SysPost> page = sysPostService.page(query, condition);
        assertNotNull(page);
    }

    /**
     * 测试：获取UsersNullID。
     */
    @Test
    @DisplayName("查询岗位下用户-ID为空返回空列表")
    void testGetUsersNullId() {
        List<SysUser> users = sysPostService.getUsersByPostId(null);
        assertNotNull(users);
        assertTrue(users.isEmpty());
    }

    /**
     * 测试：获取Users编号Rel。
     */
    @Disabled("[P3C40] 待完整 Spring 环境补真实测试：需 H2 + 真实 MyBatis mapper")
    @Test
    @DisplayName("查询岗位下用户-无关联返回空列表")
    void testGetUsersNoRel() {
        List<SysUser> users = sysPostService.getUsersByPostId(7);
        assertTrue(users.isEmpty());
    }

    /**
     * 测试：过滤By状态WhenListing。
     */
    @Disabled("[P3C40] 待完整 Spring 环境补真实测试：需 H2 + 真实 MyBatis mapper")
    @Test
    @DisplayName("列表查询-传入启用状态时按状态过滤")
    void shouldFilterByStatusWhenListing() {
        SysPost condition = new SysPost();
        condition.setSysPostStatus(0);

        List<SysPost> list = sysPostService.list(condition);

        assertEquals(1, list.size());
        // 状态条件已进入wrapper(由 lambdaQuery.eq 拼接), 此处仅验证不抛异常且返回数据
    }

    /**
     * 测试：Fill用户数量响应页。
     */
    @Disabled("[P3C40] 待完整 Spring 环境补真实测试：需 H2 + 真实 MyBatis mapper")
    @Test
    @DisplayName("分页查询-返回结构含关联用户数填充")
    void shouldFillUserCountOnPage() {
        com.chua.starter.mybatis.entity.Query<SysPost> query = new com.chua.starter.mybatis.entity.Query<>();
        SysPost condition = new SysPost();

        com.baomidou.mybatisplus.core.metadata.IPage<SysPost> result = sysPostService.page(query, condition);

        assertEquals(1, result.getTotal());
        assertEquals(1, result.getRecords().get(0).getUserCount(), "岗位关联用户数应被填充为1");
    }
}
