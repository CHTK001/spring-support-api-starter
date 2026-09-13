package com.chua.starter.system.task;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.chua.starter.system.service.SysAnnouncementService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * 公告定时发布/下线任务单元测试
 *
 * <p>验证 scheduleAnnouncementStatus 在草稿到期、已发布过期时
 * 调用 service.update 做状态流转, 并在发生变更时清理活跃公告缓存;
 * 无变更时不清理缓存。</p>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("公告定时发布任务测试")
class SysAnnouncementScheduleTaskTest {

    @Mock
    private SysAnnouncementService sysAnnouncementService;

    @Mock
    private CacheManager cacheManager;

    @InjectMocks
    private SysAnnouncementScheduleTask scheduleTask;

    @Test
    @DisplayName("待发布公告到达发布时间-执行状态流转(草稿->已发布)")
    void shouldPublishWhenDraftTimeReached() {
        // 第一次update(草稿->已发布)返回3条, 第二次(已发布->下架)返回0
        doReturn(3, 0).when(sysAnnouncementService).update(any(Wrapper.class));
        Cache cache = mock(Cache.class);
        when(cacheManager.getCache("announcement:active")).thenReturn(cache);

        scheduleTask.scheduleAnnouncementStatus();

        // 两次状态流转都应执行
        verify(sysAnnouncementService, times(2)).update(any(Wrapper.class));
        // 有发布变更, 应清理活跃缓存
        verify(cache).clear();
    }

    @Test
    @DisplayName("已发布公告到达过期时间-执行状态流转(已发布->已下架)")
    void shouldExpireWhenPublishTimeReached() {
        // 第一次(草稿->发布)返回0, 第二次(已发布->下架)返回2
        doReturn(0, 2).when(sysAnnouncementService).update(any(Wrapper.class));
        Cache cache = mock(Cache.class);
        when(cacheManager.getCache("announcement:active")).thenReturn(cache);

        scheduleTask.scheduleAnnouncementStatus();

        verify(sysAnnouncementService, times(2)).update(any(Wrapper.class));
        verify(cache).clear();
    }

    @Test
    @DisplayName("无公告需要流转-状态不变且不清理缓存")
    void shouldNotEvictCacheWhenNoChange() {
        doReturn(0, 0).when(sysAnnouncementService).update(any(Wrapper.class));

        scheduleTask.scheduleAnnouncementStatus();

        verify(sysAnnouncementService, times(2)).update(any(Wrapper.class));
        verify(cacheManager, never()).getCache(anyString());
    }

    @Test
    @DisplayName("定时任务-service异常时吞掉异常不向外抛出")
    void shouldSwallowException() {
        doThrow(new RuntimeException("db error")).when(sysAnnouncementService).update(any(Wrapper.class));

        // 不应抛出
        scheduleTask.scheduleAnnouncementStatus();

        verify(sysAnnouncementService, atLeastOnce()).update(any(Wrapper.class));
    }
}
