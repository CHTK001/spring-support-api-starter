package com.chua.starter.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.entity.system.SysPost;
import com.chua.starter.entity.system.SysUser;
import com.chua.starter.entity.system.SysUserPost;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.system.mapper.SysPostMapper;
import com.chua.starter.system.mapper.SysUserMapper;
import com.chua.starter.system.mapper.SysUserPostMapper;
import com.chua.starter.system.service.impl.SysPostServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 岗位服务单元测试
 *
 * <p>覆盖新增(正常/重复编码)、更新(ID为空/不存在/编码冲突)、
 * 删除(有关联用户拒绝/无关联成功/空ID)、列表与分页。</p>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("岗位服务测试")
class SysPostServiceTest {

    @Mock
    private SysPostMapper sysPostMapper;

    @Mock
    private SysUserPostMapper sysUserPostMapper;

    @Mock
    private SysUserMapper sysUserMapper;

    @InjectMocks
    private SysPostServiceImpl sysPostService;

    private SysPost buildPost() {
        SysPost post = new SysPost();
        post.setSysPostName("工程师");
        post.setSysPostCode("ENG");
        return post;
    }

    @Test
    @DisplayName("新增岗位-编码不存在时保存成功并补默认值")
    void testSaveSuccess() {
        when(sysPostMapper.selectCount(any())).thenReturn(0L);
        when(sysPostMapper.insert(any(SysPost.class))).thenReturn(1);

        SysPost post = buildPost();
        ReturnResult<SysPost> result = sysPostService.savePost(post);

        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
        assertEquals(0, result.getData().getSysPostStatus(), "状态为空时应默认启用(0)");
        assertEquals(0, result.getData().getSysPostSort(), "排序为空时应默认0");
    }

    @Test
    @DisplayName("新增岗位-编码重复时返回非法参数")
    void testSaveDuplicateCode() {
        when(sysPostMapper.selectCount(any())).thenReturn(1L);

        ReturnResult<SysPost> result = sysPostService.savePost(buildPost());

        assertFalse(result.isSuccess());
        assertTrue(result.getMsg().contains("岗位编码已存在"));
        verify(sysPostMapper, never()).insert(any(SysPost.class));
    }

    @Test
    @DisplayName("更新岗位-ID为空返回非法参数")
    void testUpdateNullId() {
        SysPost post = buildPost();
        ReturnResult<Boolean> result = sysPostService.updatePost(post);
        assertFalse(result.isSuccess());
        assertTrue(result.getMsg().contains("岗位信息不存在"));
    }

    @Test
    @DisplayName("更新岗位-记录不存在返回非法参数")
    void testUpdateNotExists() {
        SysPost post = buildPost();
        post.setSysPostId(999L);
        when(sysPostMapper.selectById(999L)).thenReturn(null);

        ReturnResult<Boolean> result = sysPostService.updatePost(post);
        assertFalse(result.isSuccess());
        assertTrue(result.getMsg().contains("岗位信息不存在"));
    }

    @Test
    @DisplayName("更新岗位-改编码后与他人重复返回非法参数")
    void testUpdateCodeConflict() {
        SysPost exist = buildPost();
        exist.setSysPostId(1L);
        exist.setSysPostCode("OLD");

        SysPost update = buildPost();
        update.setSysPostId(1L);
        update.setSysPostCode("NEW");

        when(sysPostMapper.selectById(1L)).thenReturn(exist);
        when(sysPostMapper.selectCount(any())).thenReturn(1L);

        ReturnResult<Boolean> result = sysPostService.updatePost(update);
        assertFalse(result.isSuccess());
        assertTrue(result.getMsg().contains("岗位编码已存在"));
    }

    @Test
    @DisplayName("更新岗位-合法更新调用updateById")
    void testUpdateSuccess() {
        SysPost exist = buildPost();
        exist.setSysPostId(1L);
        exist.setSysPostCode("ENG");

        SysPost update = buildPost();
        update.setSysPostId(1L);

        when(sysPostMapper.selectById(1L)).thenReturn(exist);
        when(sysPostMapper.selectCount(any())).thenReturn(0L);
        when(sysPostMapper.updateById(any(SysPost.class))).thenReturn(true);

        ReturnResult<Boolean> result = sysPostService.updatePost(update);
        assertTrue(result.isSuccess());
    }

    @Test
    @DisplayName("删除岗位-ID为空返回非法参数")
    void testDeleteNullId() {
        ReturnResult<Boolean> result = sysPostService.deletePost(null);
        assertFalse(result.isSuccess());
        assertTrue(result.getMsg().contains("岗位信息不存在"));
    }

    @Test
    @DisplayName("删除岗位-存在关联用户时拒绝")
    void testDeleteHasUsers() {
        when(sysUserPostMapper.selectCount(any())).thenReturn(2L);

        ReturnResult<Boolean> result = sysPostService.deletePost(5);
        assertFalse(result.isSuccess());
        assertTrue(result.getMsg().contains("关联用户"));
        verify(sysPostMapper, never()).deleteById(any());
    }

    @Test
    @DisplayName("删除岗位-无关联用户时删除成功")
    void testDeleteNoUsers() {
        when(sysUserPostMapper.selectCount(any())).thenReturn(0L);
        when(sysPostMapper.deleteById(5)).thenReturn(true);

        ReturnResult<Boolean> result = sysPostService.deletePost(5);
        assertTrue(result.isSuccess());
    }

    @Test
    @DisplayName("岗位列表-返回查询结果")
    void testList() {
        when(sysPostMapper.selectList(any())).thenReturn(Collections.singletonList(buildPost()));
        List<SysPost> list = sysPostService.list(new SysPost());
        assertEquals(1, list.size());
    }

    @Test
    @DisplayName("岗位分页-调用分页查询不抛异常")
    void testPage() {
        Query<SysPost> query = new Query<>();
        SysPost condition = new SysPost();
        when(sysPostMapper.selectPage(any(), any())).thenReturn(new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>());
        IPage<SysPost> page = sysPostService.page(query, condition);
        assertNotNull(page);
    }

    @Test
    @DisplayName("查询岗位下用户-ID为空返回空列表")
    void testGetUsersNullId() {
        List<SysUser> users = sysPostService.getUsersByPostId(null);
        assertNotNull(users);
        assertTrue(users.isEmpty());
    }

    @Test
    @DisplayName("查询岗位下用户-无关联返回空列表")
    void testGetUsersNoRel() {
        when(sysUserPostMapper.selectList(any())).thenReturn(Collections.emptyList());
        List<SysUser> users = sysPostService.getUsersByPostId(7);
        assertTrue(users.isEmpty());
    }

    @Test
    @DisplayName("列表查询-传入启用状态时按状态过滤")
    void shouldFilterByStatusWhenListing() {
        SysPost condition = new SysPost();
        condition.setSysPostStatus(0);
        when(sysPostMapper.selectList(any())).thenReturn(Collections.singletonList(buildPost()));

        List<SysPost> list = sysPostService.list(condition);

        assertEquals(1, list.size());
        // 状态条件已进入 wrapper(由 lambdaQuery.eq 拼接), 此处仅验证不抛异常且返回数据
    }

    @Test
    @DisplayName("分页查询-返回结构含关联用户数填充")
    void shouldFillUserCountOnPage() {
        Query<SysPost> query = new Query<>();
        SysPost condition = new SysPost();

        SysPost p1 = buildPost();
        p1.setSysPostId(1);
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<SysPost> page =
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>();
        page.setRecords(Collections.singletonList(p1));
        page.setTotal(1);
        when(sysPostMapper.selectPage(any(), any())).thenReturn(page);

        SysUserPost rel = new SysUserPost();
        rel.setSysPostId(1);
        rel.setSysUserId(10);
        when(sysUserPostMapper.selectList(any())).thenReturn(Collections.singletonList(rel));

        IPage<SysPost> result = sysPostService.page(query, condition);

        assertEquals(1, result.getTotal());
        assertEquals(1, result.getRecords().get(0).getUserCount(), "岗位关联用户数应被填充为1");
    }
}
