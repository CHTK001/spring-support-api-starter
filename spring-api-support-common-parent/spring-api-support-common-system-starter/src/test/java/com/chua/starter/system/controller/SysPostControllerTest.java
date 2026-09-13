package com.chua.starter.system.controller;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.common.support.lang.ReturnPageResult;
import com.chua.starter.entity.system.SysPost;
import com.chua.starter.entity.system.SysUser;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.system.service.SysPostService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 岗位接口单元测试
 *
 * <p>纯 Mockito 验证 Controller 对 Service 的委托、参数校验与空值兜底,
 * 不启动 Web 上下文(无 MockMvc 依赖)。</p>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("岗位接口测试")
class SysPostControllerTest {

    @Mock
    private SysPostService sysPostService;

    @InjectMocks
    private SysPostController sysPostController;

    @Test
    @DisplayName("岗位列表-委托service并返回成功")
    void testList() {
        when(sysPostService.list(any(SysPost.class))).thenReturn(Collections.singletonList(new SysPost()));
        ReturnResult<List<SysPost>> result = sysPostController.list(new SysPost());
        assertTrue(result.isSuccess());
        assertEquals(1, result.getData().size());
    }

    @Test
    @DisplayName("岗位分页-委托service")
    void testPage() {
        when(sysPostService.page(any(Query.class), any(SysPost.class)))
                .thenReturn(new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>());
        ReturnPageResult<SysPost> result = sysPostController.page(new Query<>(), new SysPost());
        assertNotNull(result);
    }

    @Test
    @DisplayName("新增-参数校验失败返回错误信息")
    void testSaveValidationError() {
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(true);
        when(bindingResult.getAllErrors())
                .thenReturn(Collections.singletonList(new ObjectError("post", "岗位名称不能为空")));

        ReturnResult<SysPost> result = sysPostController.save(new SysPost(), bindingResult);
        assertFalse(result.isSuccess());
        assertTrue(result.getMsg().contains("岗位名称不能为空"));
        verify(sysPostService, never()).savePost(any());
    }

    @Test
    @DisplayName("新增-校验通过委托savePost")
    void testSaveOk() {
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(false);
        SysPost saved = new SysPost();
        when(sysPostService.savePost(any(SysPost.class))).thenReturn(ReturnResult.ok(saved));

        ReturnResult<SysPost> result = sysPostController.save(new SysPost(), bindingResult);
        assertTrue(result.isSuccess());
        verify(sysPostService).savePost(any(SysPost.class));
    }

    @Test
    @DisplayName("更新-校验失败返回错误信息")
    void testUpdateValidationError() {
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(true);
        when(bindingResult.getAllErrors())
                .thenReturn(Collections.singletonList(new ObjectError("post", "岗位ID不能为空")));

        ReturnResult<Boolean> result = sysPostController.update(new SysPost(), bindingResult);
        assertFalse(result.isSuccess());
        verify(sysPostService, never()).updatePost(any());
    }

    @Test
    @DisplayName("删除-空postId返回非法参数")
    void testDeleteNullId() {
        ReturnResult<Boolean> result = sysPostController.delete(null);
        assertFalse(result.isSuccess());
        verify(sysPostService, never()).deletePost(any());
    }

    @Test
    @DisplayName("删除-合法postId委托service")
    void testDeleteOk() {
        when(sysPostService.deletePost(5)).thenReturn(ReturnResult.ok(true));
        ReturnResult<Boolean> result = sysPostController.delete(5);
        assertTrue(result.isSuccess());
    }

    @Test
    @DisplayName("查询岗位下用户-空postId返回非法参数")
    void testUsersNullId() {
        ReturnResult<List<SysUser>> result = sysPostController.users(null);
        assertFalse(result.isSuccess());
    }

    @Test
    @DisplayName("查询岗位下用户-合法postId委托service")
    void testUsersOk() {
        when(sysPostService.getUsersByPostId(5)).thenReturn(Collections.emptyList());
        ReturnResult<List<SysUser>> result = sysPostController.users(5);
        assertTrue(result.isSuccess());
    }
}
