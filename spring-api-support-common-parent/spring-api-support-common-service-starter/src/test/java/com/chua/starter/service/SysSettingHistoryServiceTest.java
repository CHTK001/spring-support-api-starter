package com.chua.starter.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.entity.service.SysSetting;
import com.chua.starter.entity.service.SysSettingHistory;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.service.impl.SysSettingHistoryServiceImpl;
import com.chua.starter.service.mapper.SysSettingHistoryMapper;
import com.chua.starter.service.mapper.SysSettingMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 系统配置变更历史服务单元测试
 *
 * <p>覆盖记录历史(值变化才记录/携带批次号)、按设置ID分页、
 * 按批次号查询、单条回滚(旧值写回并生成新历史)、批次回滚。</p>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("设置历史与回滚测试")
class SysSettingHistoryServiceTest {

    @Mock
    private SysSettingHistoryMapper sysSettingHistoryMapper;

    @Mock
    private SysSettingMapper sysSettingMapper;

    @Mock
    private SysSettingService sysSettingService;

    @InjectMocks
    private SysSettingHistoryServiceImpl sysSettingHistoryService;

    private SysSetting setting(Integer id, String value) {
        SysSetting s = new SysSetting();
        s.setSysSettingId(id);
        s.setSysSettingName("站点标题");
        s.setSysSettingGroup("basic");
        s.setSysSettingValue(value);
        return s;
    }

    @Test
    @DisplayName("保存历史记录-值变化时入库并携带批次号")
    void shouldRecordHistoryWhenValueChanged() {
        when(sysSettingHistoryMapper.insert(any(SysSettingHistory.class))).thenReturn(1);

        SysSetting oldS = setting(1, "old");
        SysSetting newS = setting(1, "new");
        sysSettingHistoryService.recordUpdate(oldS, newS, "u1", "admin", "batch-001");

        ArgumentCaptor<SysSettingHistory> captor = ArgumentCaptor.forClass(SysSettingHistory.class);
        verify(sysSettingHistoryMapper).insert(captor.capture());
        SysSettingHistory h = captor.getValue();
        assertEquals("old", h.getSysSettingOldValue());
        assertEquals("new", h.getSysSettingNewValue());
        assertEquals("UPDATE", h.getSysSettingOperation());
        assertEquals("batch-001", h.getSysSettingBatchNo(), "应透传批次号");
    }

    @Test
    @DisplayName("保存历史记录-值未变化时不入库")
    void shouldSkipRecordWhenValueUnchanged() {
        SysSetting oldS = setting(1, "same");
        SysSetting newS = setting(1, "same");

        sysSettingHistoryService.recordUpdate(oldS, newS, "u1", "admin", "batch-001");

        verify(sysSettingHistoryMapper, never()).insert(any(SysSettingHistory.class));
    }

    @Test
    @DisplayName("按设置ID分页查询历史")
    void shouldPageHistoryBySettingId() {
        Query<SysSettingHistory> query = new Query<>();
        Page<SysSettingHistory> page = new Page<>();
        page.setRecords(Collections.singletonList(new SysSettingHistory()));
        page.setTotal(1);
        when(sysSettingHistoryMapper.selectPage(any(Page.class), any())).thenReturn(page);

        com.baomidou.mybatisplus.core.metadata.IPage<SysSettingHistory> result =
                sysSettingHistoryService.pageHistory(query, 1, "basic");

        assertEquals(1, result.getTotal());
    }

    @Test
    @DisplayName("按批次号查询历史")
    void shouldListHistoryByBatchNo() {
        SysSettingHistory h1 = new SysSettingHistory();
        h1.setSysSettingBatchNo("batch-001");
        when(sysSettingHistoryMapper.selectList(any())).thenReturn(Arrays.asList(h1, h1));

        List<SysSettingHistory> list = sysSettingHistoryService.listByBatchNo("batch-001");

        assertEquals(2, list.size());
    }

    @Test
    @DisplayName("单条回滚成功-旧值写回设置表并生成新历史")
    void shouldRollbackSingleHistorySuccessfully() {
        // 历史记录为 UPDATE 操作, 旧值=oldValue
        SysSettingHistory history = new SysSettingHistory();
        history.setSysSettingId(1);
        history.setSysSettingName("站点标题");
        history.setSysSettingGroup("basic");
        history.setSysSettingOperation("UPDATE");
        history.setSysSettingOldValue("oldValue");
        when(sysSettingHistoryMapper.selectById(10L)).thenReturn(history);

        // 当前设置值为 currentValue, 回滚应写回 oldValue
        SysSetting current = setting(1, "currentValue");
        when(sysSettingMapper.selectById(1)).thenReturn(current);
        when(sysSettingMapper.updateById(any(SysSetting.class))).thenReturn(1);
        when(sysSettingHistoryMapper.insert(any(SysSettingHistory.class))).thenReturn(1);

        ReturnResult<Boolean> result = sysSettingHistoryService.rollbackToHistory(10L, "u1", "admin");

        assertTrue(result.isSuccess());
        // 旧值已写回设置表
        ArgumentCaptor<SysSetting> settingCaptor = ArgumentCaptor.forClass(SysSetting.class);
        verify(sysSettingMapper).updateById(settingCaptor.capture());
        assertEquals("oldValue", settingCaptor.getValue().getSysSettingValue(), "应将旧值写回设置表");
        // 回滚本身产生一条新历史(ROLLBACK), 共1次insert
        ArgumentCaptor<SysSettingHistory> histCaptor = ArgumentCaptor.forClass(SysSettingHistory.class);
        verify(sysSettingHistoryMapper, times(1)).insert(any(SysSettingHistory.class));
        assertEquals("ROLLBACK", histCaptor.getValue().getSysSettingOperation());
        verify(sysSettingService).clearCache(any(SysSetting.class));
    }

    @Test
    @DisplayName("单条回滚-历史不存在返回错误")
    void shouldThrowWhenHistoryNotFound() {
        when(sysSettingHistoryMapper.selectById(99L)).thenReturn(null);

        ReturnResult<Boolean> result = sysSettingHistoryService.rollbackToHistory(99L, "u1", "admin");

        assertFalse(result.isSuccess());
        assertTrue(result.getMsg().contains("历史记录不存在"));
    }

    @Test
    @DisplayName("批次回滚成功-遍历批次内所有历史逐条回滚")
    void shouldRollbackBatchSuccessfully() {
        SysSettingHistory h1 = new SysSettingHistory();
        h1.setSysSettingId(1);
        h1.setSysSettingName("n1");
        h1.setSysSettingGroup("g");
        h1.setSysSettingOperation("UPDATE");
        h1.setSysSettingOldValue("old1");
        SysSettingHistory h2 = new SysSettingHistory();
        h2.setSysSettingId(2);
        h2.setSysSettingName("n2");
        h2.setSysSettingGroup("g");
        h2.setSysSettingOperation("UPDATE");
        h2.setSysSettingOldValue("old2");
        when(sysSettingHistoryMapper.selectList(any())).thenReturn(Arrays.asList(h1, h2));
        when(sysSettingMapper.selectById(anyInt())).thenReturn(setting(1, "cur"));
        when(sysSettingMapper.updateById(any(SysSetting.class))).thenReturn(1);
        when(sysSettingHistoryMapper.insert(any(SysSettingHistory.class))).thenReturn(1);

        ReturnResult<Boolean> result = sysSettingHistoryService.rollbackToBatch("batch-001", "u1", "admin");

        assertTrue(result.isSuccess());
        // 两条历史各回滚一次, 各产生一条 ROLLBACK 历史 -> 2 次 updateById + 2 次 insert(回滚历史)
        verify(sysSettingMapper, times(2)).updateById(any(SysSetting.class));
        verify(sysSettingHistoryMapper, times(2)).insert(any(SysSettingHistory.class));
    }

    @Test
    @DisplayName("批次回滚-空批次返回错误")
    void shouldThrowWhenBatchEmpty() {
        when(sysSettingHistoryMapper.selectList(any())).thenReturn(Collections.emptyList());

        ReturnResult<Boolean> result = sysSettingHistoryService.rollbackToBatch("empty", "u1", "admin");

        assertFalse(result.isSuccess());
        assertTrue(result.getMsg().contains("未找到该批次"));
    }
}
