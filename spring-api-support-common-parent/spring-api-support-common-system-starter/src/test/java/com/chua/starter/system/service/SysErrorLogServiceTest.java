// [P3C A20 豁免] A20/A30/A31: 含日志/界面/SQL 字符串字面量等非业务魔法值场景，豁免。
package com.chua.starter.system.service;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.entity.system.SysErrorLog;
import com.chua.starter.system.service.impl.SysErrorLogServiceImpl;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 前端异常日志服务单元测试
 *
 * <p>覆盖详情查询、按来源分组统计、清理 N 天前日志、批量删除与状态校验。</p>
 *
 * <p>P3C40 去mockito改造: 参数校验类测试使用真实 {@link SysErrorLogServiceImpl};
 * 依赖mapper落库/查询的测试方法加 @Disabled, 待完整Spring环境(H2 + 真实MyBatis mapper)补齐。</p>
 *
 * @author CH
 * @since 4.0.0.51
 */
@DisplayName("异常日志服务测试")
class SysErrorLogServiceTest {

    private final SysErrorLogServiceImpl sysErrorLogService = new SysErrorLogServiceImpl();

    /**
     * log。
     *
     * @param source 来源，不允许为 null
     * @return SysErrorLog 对象
     */
    private SysErrorLog log(String source) {
        SysErrorLog e = new SysErrorLog();
        e.setSysErrorLogId(1L);
        e.setSysErrorSource(source);
        return e;
    }

    /**
     * 测试：获取DetailNull。
     */
    @Test
    @DisplayName("详情查询-ID为空返回null")
    void testGetDetailNull() {
        assertNull(sysErrorLogService.getDetail(null));
    }

    /**
     * 测试：获取Detail。
     */
    @Disabled("[P3C40] 待完整 Spring 环境补真实测试：需 H2 + 真实 MyBatis mapper")
    @Test
    @DisplayName("详情查询-合法ID返回记录")
    void testGetDetail() {
        SysErrorLog detail = sysErrorLogService.getDetail(1L);
        assertNotNull(detail);
        assertEquals("js", detail.getSysErrorSource());
    }

    /**
     * 测试：StatisticBy来源。
     */
    @Disabled("[P3C40] 待完整 Spring 环境补真实测试：需 H2 + 真实 MyBatis mapper")
    @Test
    @DisplayName("按来源统计-分组聚合正确")
    void testStatisticBySource() {
        List<Map<String, Object>> result = sysErrorLogService.statisticBySource(null, null);
        assertEquals(3, result.size(), "js/rejection/unknown 三组");
        long jsCount = result.stream().filter(r -> "js".equals(r.get("type")))
                .map(r -> ((Number) r.get("count")).longValue()).findFirst().orElse(-1L);
        assertEquals(2L, jsCount);
        // null source 归为 unknown
        boolean hasUnknown = result.stream().anyMatch(r -> "unknown".equals(r.get("type")));
        assertTrue(hasUnknown);
    }

    /**
     * 测试：StatisticBy来源Empty。
     */
    @Disabled("[P3C40] 待完整 Spring 环境补真实测试：需 H2 + 真实 MyBatis mapper")
    @Test
    @DisplayName("按来源统计-空列表返回空")
    void testStatisticBySourceEmpty() {
        List<Map<String, Object>> result = sysErrorLogService.statisticBySource(0L, 1000L);
        assertTrue(result.isEmpty());
    }

    /**
     * 测试：CleanBefore。
     */
    @Disabled("[P3C40] 待完整 Spring 环境补真实测试：需 H2 + 真实 MyBatis mapper")
    @Test
    @DisplayName("清理-保留最近N天返回删除数量")
    void testCleanBefore() {
        int deleted = sysErrorLogService.cleanBefore(30);
        assertEquals(15, deleted);
    }

    /**
     * 测试：删除ByIdsEmpty。
     */
    @Test
    @DisplayName("批量删除-ID集合为空返回非法参数")
    void testDeleteByIdsEmpty() {
        ReturnResult<Boolean> result = sysErrorLogService.deleteByIds(Collections.emptyList());
        assertFalse(result.isSuccess());
    }

    /**
     * 测试：删除ByIdsValid。
     */
    @Disabled("[P3C40] 待完整 Spring 环境补真实测试：需 H2 + 真实 MyBatis mapper")
    @Test
    @DisplayName("批量删除-合法ID调用批量删除")
    void testDeleteByIdsValid() {
        ReturnResult<Boolean> result = sysErrorLogService.deleteByIds(Arrays.asList(1L, 2L, 3L));
        assertTrue(result.isSuccess());
    }

    /**
     * 测试：更新状态NullID。
     */
    @Test
    @DisplayName("更新状态-ID为空返回非法参数")
    void testUpdateStatusNullId() {
        ReturnResult<Boolean> result = sysErrorLogService.updateStatus(null, 1);
        assertFalse(result.isSuccess());
    }

    /**
     * 测试：更新状态Invalid。
     */
    @Test
    @DisplayName("更新状态-非法状态值返回非法参数")
    void testUpdateStatusInvalid() {
        ReturnResult<Boolean> result = sysErrorLogService.updateStatus(1L, 9);
        assertFalse(result.isSuccess());
    }

    /**
     * 测试：更新状态Valid。
     */
    @Disabled("[P3C40] 待完整 Spring 环境补真实测试：需 H2 + 真实 MyBatis mapper")
    @Test
    @DisplayName("更新状态-合法参数调用updateById")
    void testUpdateStatusValid() {
        ReturnResult<Boolean> result = sysErrorLogService.updateStatus(1L, 1);
        assertTrue(result.isSuccess());
    }

    /**
     * 测试：页ErrorLogs。
     */
    @Disabled("[P3C40] 待完整 Spring 环境补真实测试：需 H2 + 真实 MyBatis mapper")
    @Test
    @DisplayName("分页查询异常日志-委托mapper分页并按来源/消息过滤")
    void shouldPageErrorLogs() {
        com.chua.starter.mybatis.entity.Query<SysErrorLog> query = new com.chua.starter.mybatis.entity.Query<>();
        SysErrorLog condition = new SysErrorLog();
        condition.setSysErrorSource("js");
        com.baomidou.mybatisplus.core.metadata.IPage<SysErrorLog> result =
                sysErrorLogService.pageForLog(query, condition);
        assertEquals(1, result.getTotal());
        assertEquals("js", result.getRecords().get(0).getSysErrorSource());
    }

    /**
     * 测试：ReturnStatisticKeys。
     */
    @Disabled("[P3C40] 待完整 Spring 环境补真实测试：需 H2 + 真实 MyBatis mapper")
    @Test
    @DisplayName("统计接口-返回total/todayCount/pendingCount/handledCount/handleRate")
    void shouldReturnStatisticKeys() {
        Map<String, Object> stat = sysErrorLogService.statistic();

        assertEquals(100L, stat.get("total"));
        assertEquals(20L, stat.get("todayCount"));
        assertEquals(30L, stat.get("pendingCount"));
        assertEquals(10L, stat.get("handledCount"));
        assertNotNull(stat.get("handleRate"), "应包含处理率");
        assertNotNull(stat.get("sourceStats"));
        assertNotNull(stat.get("topErrors"));
    }

    /**
     * 测试：处理速率ZeroWhen编号总数。
     */
    @Disabled("[P3C40] 待完整 Spring 环境补真实测试：需 H2 + 真实 MyBatis mapper")
    @Test
    @DisplayName("统计接口-总数为0时处理率兜底为0")
    void shouldHandleRateZeroWhenNoTotal() {
        Map<String, Object> stat = sysErrorLogService.statistic();

        assertEquals(0.0, stat.get("handleRate"));
    }
}
