package com.chua.starter.system.service;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.system.service.impl.SysMenuServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 菜单缓存刷新单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("菜单缓存刷新测试")
class MenuCacheTest {

    @InjectMocks
    private SysMenuServiceImpl sysMenuService;

    @Test
    @DisplayName("刷新菜单缓存应返回成功")
    void testRefreshCache() {
        ReturnResult<Boolean> result = sysMenuService.refreshCache();
        assertNotNull(result);
        // @CacheEvict 注解的实际清除效果需要集成测试验证
        // 这里验证方法可正常调用且返回结果不为 null
    }
}
