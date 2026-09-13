package com.chua.starter.system.service;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.entity.system.SysErrorLog;
import com.chua.starter.system.mapper.SysErrorLogMapper;
import com.chua.starter.system.service.impl.SysErrorLogServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 前端异常日志服务单元测试
 *
 * <p>覆盖详情查询、按来源分组统计、清理 N 天前日志、批量删除与状态校验。</p>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("异常日志服务测试")
class SysErrorLogServiceTest {

    @Mock
    private SysErrorLogMapper sysErrorLogMapper;

    @InjectMocks
    private SysErrorLogServiceImpl sysErrorLogService;

    private SysErrorLog log(String source) {
        SysErrorLog e = new SysErrorLog();
        e.setSysErrorLogId(1L);
        e.setSysErrorSource(source);
        return e;
    }

    @Test
    @DisplayName("详情查询-ID为空返回null")
    void testGetDetailNull() {
        assertNull(sysErrorLogService.getDetail(null));
        verify(sysErrorLogMapper, never()).selectById(any());
    }

    @Test
    @DisplayName("详情查询-合法ID返回记录")
    void testGetDetail() {
        SysErrorLog e = log("js");
        when(sysErrorLogMapper.selectById(1L)).thenReturn(e);
        SysErrorLog detail = sysErrorLogService.getDetail(1L);
        assertNotNull(detail);
        assertEquals("js", detail.getSysErrorSource());
    }

    @Test
    @DisplayName("按来源统计-分组聚合正确")
    void testStatisticBySource() {
        when(sysErrorLogMapper.selectList(any())).thenReturn(Arrays.asList(
                log("js"), log("js"), log("rejection"), log("resource"), log(null)));
        List<Map<String, Object>> result = sysErrorLogService.statisticBySource(null, null);
        assertEquals(3, result.size(), "js/rejection/unknown 三组");
        long jsCount = result.stream().filter(r -> "js".equals(r.get("type")))
                .map(r -> ((Number) r.get("count")).longValue()).findFirst().orElse(-1L);
        assertEquals(2L, jsCount);
        // null source 归为 unknown
        boolean hasUnknown = result.stream().anyMatch(r -> "unknown".equals(r.get("type")));
        assertTrue(hasUnknown);
    }

    @Test
    @DisplayName("按来源统计-空列表返回空")
    void testStatisticBySourceEmpty() {
        when(sysErrorLogMapper.selectList(any())).thenReturn(Collections.emptyList());
        List<Map<String, Object>> result = sysErrorLogService.statisticBySource(0L, 1000L);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("清理-保留最近N天返回删除数量")
    void testCleanBefore() {
        when(sysErrorLogMapper.delete(any())).thenReturn(15);
        int deleted = sysErrorLogService.cleanBefore(30);
        assertEquals(15, deleted);
    }

    @Test
    @DisplayName("批量删除-ID集合为空返回非法参数")
    void testDeleteByIdsEmpty() {
        ReturnResult<Boolean> result = sysErrorLogService.deleteByIds(Collections.emptyList());
        assertFalse(result.isSuccess());
    }

    @Test
    @DisplayName("批量删除-合法ID调用批量删除")
    void testDeleteByIdsValid() {
        when(sysErrorLogMapper.deleteBatchIds(anyCollection())).thenReturn(3);
        ReturnResult<Boolean> result = sysErrorLogService.deleteByIds(Arrays.asList(1L, 2L, 3L));
        assertTrue(result.isSuccess());
    }

    @Test
    @DisplayName("更新状态-ID为空返回非法参数")
    void testUpdateStatusNullId() {
        ReturnResult<Boolean> result = sysErrorLogService.updateStatus(null, 1);
        assertFalse(result.isSuccess());
    }

    @Test
    @DisplayName("更新状态-非法状态值返回非法参数")
    void testUpdateStatusInvalid() {
        ReturnResult<Boolean> result = sysErrorLogService.updateStatus(1L, 9);
        assertFalse(result.isSuccess());
    }

    @Test
    @DisplayName("更新状态-合法参数调用updateById")
    void testUpdateStatusValid() {
        when(sysErrorLogMapper.updateById(any(SysErrorLog.class))).thenReturn(1);
        ReturnResult<Boolean> result = sysErrorLogService.updateStatus(1L, 1);
        assertTrue(result.isSuccess());
    }

    @Test
    @DisplayName("分页查询异常日志-委托mapper分页并按来源/消息过滤")
    void shouldPageErrorLogs() {
        com.chua.starter.mybatis.entity.Query<SysErrorLog> query = new com.chua.starter.mybatis.entity.Query<>();
        SysErrorLog condition = new SysErrorLog();
        condition.setSysErrorSource("js");
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<SysErrorLog> page =
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>();
        page.setRecords(Collections.singletonList(log("js")));
        page.setTotal(1);
        when(sysErrorLogMapper.selectPage(any(), any())).thenReturn(page);

        com.baomidou.mybatisplus.core.metadata.IPage<SysErrorLog> result =
                sysErrorLogService.pageForLog(query, condition);

        assertEquals(1, result.getTotal());
        assertEquals("js", result.getRecords().get(0).getSysErrorSource());
    }

    @Test
    @DisplayName("统计接口-返回total/todayCount/pendingCount/handledCount/handleRate")
    void shouldReturnStatisticKeys() {
        // count() 走 baseMapper.selectCount
        when(sysErrorLogMapper.selectCount(any())).thenReturn(100L, 20L, 30L, 10L, 15L);
        when(sysErrorLogMapper.selectMaps(any())).thenReturn(Collections.emptyList());

        Map<String, Object> stat = sysErrorLogService.statistic();

        assertEquals(100L, stat.get("total"));
        assertEquals(20L, stat.get("todayCount"));
        assertEquals(30L, stat.get("pendingCount"));
        assertEquals(10L, stat.get("handledCount"));
        assertNotNull(stat.get("handleRate"), "应包含处理率");
        assertNotNull(stat.get("sourceStats"));
        assertNotNull(stat.get("topErrors"));
    }

    @Test
    @DisplayName("统计接口-总数为0时处理率兜底为0")
    void shouldHandleRateZeroWhenNoTotal() {
        when(sysErrorLogMapper.selectCount(any())).thenReturn(0L, 0L, 0L, 0L, 0L);
        when(sysErrorLogMapper.selectMaps(any())).thenReturn(Collections.emptyList());

        Map<String, Object> stat = sysErrorLogService.statistic();

        assertEquals(0.0, stat.get("handleRate"));
    }
}
