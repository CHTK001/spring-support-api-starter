package com.chua.starter.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.entity.system.SysIpBlackwhite;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.system.mapper.SysIpBlackwhiteMapper;
import com.chua.starter.system.service.impl.SysIpBlackwhiteServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * IP 黑白名单服务单元测试
 *
 * <p>覆盖新增/更新/删除/分页以及 checkIp 的核心规则：
 * 黑名单命中拒绝、白名单存在时未命中拒绝、命中放行、CIDR 段匹配。</p>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("IP黑白名单服务测试")
class SysIpBlackwhiteServiceTest {

    @Mock
    private SysIpBlackwhiteMapper sysIpBlackwhiteMapper;

    @InjectMocks
    private SysIpBlackwhiteServiceImpl sysIpBlackwhiteService;

    private SysIpBlackwhite entry(int type, String ip) {
        SysIpBlackwhite e = new SysIpBlackwhite();
        e.setSysIpType(type);
        e.setSysIpStatus(0);
        e.setSysIpIp(ip);
        return e;
    }

    @Test
    @DisplayName("checkIp-空IP直接放行")
    void testCheckBlankIp() {
        assertTrue(sysIpBlackwhiteService.checkIp(""));
        assertTrue(sysIpBlackwhiteService.checkIp(null));
        verify(sysIpBlackwhiteMapper, never()).selectList(any());
    }

    @Test
    @DisplayName("checkIp-黑名单精确命中则拒绝")
    void testCheckBlackExact() {
        when(sysIpBlackwhiteMapper.selectList(any()))
                .thenReturn(Collections.singletonList(entry(1, "1.2.3.4")));
        assertFalse(sysIpBlackwhiteService.checkIp("1.2.3.4"));
    }

    @Test
    @DisplayName("checkIp-黑名单CIDR段命中则拒绝")
    void testCheckBlackCidr() {
        // 第1次(黑名单)命中段 -> 拒绝; 第2次黑名单不命中, 白名单为空 -> 放行
        when(sysIpBlackwhiteMapper.selectList(any()))
                .thenReturn(Collections.singletonList(entry(1, "10.0.0.0/8")))
                .thenReturn(Collections.singletonList(entry(1, "10.0.0.0/8")))
                .thenReturn(new ArrayList<>());
        // 10.1.2.3 落在 10.0.0.0/8 内 -> 黑名单命中即拒绝
        assertFalse(sysIpBlackwhiteService.checkIp("10.1.2.3"));
        // 9.9.9.9 不在黑名单段内, 且无白名单 -> 放行
        assertTrue(sysIpBlackwhiteService.checkIp("9.9.9.9"));
    }

    @Test
    @DisplayName("checkIp-存在白名单且命中则放行")
    void testCheckWhiteMatched() {
        // 第一次调用(黑名单)返回空, 第二次调用(白名单)返回命中项
        when(sysIpBlackwhiteMapper.selectList(any()))
                .thenReturn(new ArrayList<>(),
                        Collections.singletonList(entry(0, "192.168.1.5")));
        assertTrue(sysIpBlackwhiteService.checkIp("192.168.1.5"));
    }

    @Test
    @DisplayName("checkIp-存在白名单但未命中则拒绝")
    void testCheckWhiteNotMatched() {
        when(sysIpBlackwhiteMapper.selectList(any()))
                .thenReturn(new ArrayList<>(),
                        Collections.singletonList(entry(0, "192.168.1.5")));
        assertFalse(sysIpBlackwhiteService.checkIp("192.168.1.9"));
    }

    @Test
    @DisplayName("checkIp-无白名单时黑名单未命中即放行")
    void testCheckNoWhiteList() {
        // 黑名单返回不匹配项, 白名单返回空
        when(sysIpBlackwhiteMapper.selectList(any()))
                .thenReturn(Collections.singletonList(entry(1, "8.8.8.8")),
                        new ArrayList<>());
        assertTrue(sysIpBlackwhiteService.checkIp("1.1.1.1"));
    }

    @Test
    @DisplayName("checkIp-多IP头取第一个有效IP")
    void testCheckMultiIpHeader() {
        when(sysIpBlackwhiteMapper.selectList(any()))
                .thenReturn(Collections.singletonList(entry(1, "10.0.0.5")),
                        new ArrayList<>());
        // X-Forwarded-For 多段, 取第一个
        assertFalse(sysIpBlackwhiteService.checkIp("10.0.0.5, 1.1.1.1"));
    }

    @Test
    @DisplayName("新增IP-状态为空默认启用并保存成功")
    void testSaveSuccess() {
        when(sysIpBlackwhiteMapper.insert(any(SysIpBlackwhite.class))).thenReturn(1);
        SysIpBlackwhite e = entry(0, "1.1.1.1");
        e.setSysIpStatus(null);
        ReturnResult<SysIpBlackwhite> result = sysIpBlackwhiteService.saveIp(e);
        assertTrue(result.isSuccess());
        assertEquals(0, result.getData().getSysIpStatus());
    }

    @Test
    @DisplayName("更新IP-ID为空返回非法参数")
    void testUpdateNullId() {
        ReturnResult<Boolean> result = sysIpBlackwhiteService.updateIp(entry(0, "1.1.1.1"));
        assertFalse(result.isSuccess());
        assertTrue(result.getMsg().contains("记录不存在"));
    }

    @Test
    @DisplayName("删除IP-ID为空返回非法参数")
    void testDeleteNullId() {
        ReturnResult<Boolean> result = sysIpBlackwhiteService.deleteIp(null);
        assertFalse(result.isSuccess());
        assertTrue(result.getMsg().contains("记录不存在"));
    }

    @Test
    @DisplayName("删除IP-合法ID调用removeById")
    void testDeleteSuccess() {
        when(sysIpBlackwhiteMapper.deleteById(3)).thenReturn(true);
        ReturnResult<Boolean> result = sysIpBlackwhiteService.deleteIp(3);
        assertTrue(result.isSuccess());
    }

    @Test
    @DisplayName("分页-按type筛选调用分页查询")
    void testPageByType() {
        Query<SysIpBlackwhite> query = new Query<>();
        SysIpBlackwhite condition = new SysIpBlackwhite();
        condition.setSysIpType(1);
        when(sysIpBlackwhiteMapper.selectPage(any(), any()))
                .thenReturn(new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>());
        IPage<SysIpBlackwhite> page = sysIpBlackwhiteService.page(query, condition);
        assertNotNull(page);
    }

    @Test
    @DisplayName("列表-返回数据")
    void testList() {
        when(sysIpBlackwhiteMapper.selectList(any()))
                .thenReturn(Arrays.asList(entry(0, "1.1.1.1"), entry(1, "2.2.2.2")));
        assertEquals(2, sysIpBlackwhiteService.list().size());
    }
}
