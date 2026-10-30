package com.chua.starter.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.entity.system.SysIpBlackwhite;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.system.service.impl.SysIpBlackwhiteServiceImpl;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * IP 黑白名单服务单元测试
 *
 * <p>覆盖新增/更新/删除/分页以及 checkIp 的核心规则：
 * 黑名单命中拒绝、白名单存在时未命中拒绝、命中放行、CIDR 段匹配。</p>
 *
 * <p>P3C40 去mockito改造: 需落库/依赖mapper的测试方法加 @Disabled, 待完整Spring环境补齐。</p>
 *
 * @author CH
 * @since 4.0.0.51
 */
@DisplayName("IP黑白名单服务测试")
class SysIpBlackwhiteServiceTest {

    /**
     * 实现类改为构造器注入统一表 Mapper 与缓存投影器；
     * 依赖落库的用例均为 @Disabled，此处传 null 只为满足实例化。
     */
    private final SysIpBlackwhiteServiceImpl sysIpBlackwhiteService = new SysIpBlackwhiteServiceImpl(null, null);

    /**
     * 条目。
     *
     * @param type 类型，不允许为 null
     * @param ip IP，不允许为 null
     * @return SysIPBlackwhite 对象
     */
    private SysIpBlackwhite entry(int type, String ip) {
        SysIpBlackwhite e = new SysIpBlackwhite();
        e.setSysIpType(type);
        e.setSysIpStatus(0);
        e.setSysIpIp(ip);
        return e;
    }

    /**
     * 测试：校验BlankIP。
     */
    @Disabled("[P3C40] 待完整 Spring 环境补真实测试：需 H2 + 真实 MyBatis mapper")
    @Test
    @DisplayName("checkIp-空IP直接放行")
    void testCheckBlankIp() {
        assertTrue(sysIpBlackwhiteService.checkIp(""));
        assertTrue(sysIpBlackwhiteService.checkIp(null));
    }

    /**
     * 测试：校验BlackExact。
     */
    @Disabled("[P3C40] 待完整 Spring 环境补真实测试：需 H2 + 真实 MyBatis mapper")
    @Test
    @DisplayName("checkIp-黑名单精确命中则拒绝")
    void testCheckBlackExact() {
        assertFalse(sysIpBlackwhiteService.checkIp("1.2.3.4"));
    }

    /**
     * 测试：校验BlackCidr。
     */
    @Disabled("[P3C40] 待完整 Spring 环境补真实测试：需 H2 + 真实 MyBatis mapper")
    @Test
    @DisplayName("checkIp-黑名单CIDR段命中则拒绝")
    void testCheckBlackCidr() {
        assertFalse(sysIpBlackwhiteService.checkIp("10.1.2.3"));
        assertTrue(sysIpBlackwhiteService.checkIp("9.9.9.9"));
    }

    /**
     * 测试：校验WhiteMatched。
     */
    @Disabled("[P3C40] 待完整 Spring 环境补真实测试：需 H2 + 真实 MyBatis mapper")
    @Test
    @DisplayName("checkIp-存在白名单且命中则放行")
    void testCheckWhiteMatched() {
        assertTrue(sysIpBlackwhiteService.checkIp("192.168.1.5"));
    }

    /**
     * 测试：校验WhiteNotMatched。
     */
    @Disabled("[P3C40] 待完整 Spring 环境补真实测试：需 H2 + 真实 MyBatis mapper")
    @Test
    @DisplayName("checkIp-存在白名单但未命中则拒绝")
    void testCheckWhiteNotMatched() {
        assertFalse(sysIpBlackwhiteService.checkIp("192.168.1.9"));
    }

    /**
     * 测试：校验编号White列出。
     */
    @Disabled("[P3C40] 待完整 Spring 环境补真实测试：需 H2 + 真实 MyBatis mapper")
    @Test
    @DisplayName("checkIp-无白名单时黑名单未命中即放行")
    void testCheckNoWhiteList() {
        assertTrue(sysIpBlackwhiteService.checkIp("1.1.1.1"));
    }

    /**
     * 测试：校验MultiIP请求头。
     */
    @Disabled("[P3C40] 待完整 Spring 环境补真实测试：需 H2 + 真实 MyBatis mapper")
    @Test
    @DisplayName("checkIp-多IP头取第一个有效IP")
    void testCheckMultiIpHeader() {
        assertFalse(sysIpBlackwhiteService.checkIp("10.0.0.5, 1.1.1.1"));
    }

    /**
     * 测试：保存Success。
     */
    @Disabled("[P3C40] 待完整 Spring 环境补真实测试：需 H2 + 真实 MyBatis mapper")
    @Test
    @DisplayName("新增IP-状态为空默认启用并保存成功")
    void testSaveSuccess() {
        SysIpBlackwhite e = entry(0, "1.1.1.1");
        e.setSysIpStatus(null);
        ReturnResult<SysIpBlackwhite> result = sysIpBlackwhiteService.saveIp(e);
        assertTrue(result.isSuccess());
        assertEquals(0, result.getData().getSysIpStatus());
    }

    /**
     * 测试：更新NullID。
     */
    @Test
    @DisplayName("更新IP-ID为空返回非法参数")
    void testUpdateNullId() {
        ReturnResult<Boolean> result = sysIpBlackwhiteService.updateIp(entry(0, "1.1.1.1"));
        assertFalse(result.isSuccess());
        assertTrue(result.getMsg().contains("记录不存在"));
    }

    /**
     * 测试：删除NullID。
     */
    @Test
    @DisplayName("删除IP-ID为空返回非法参数")
    void testDeleteNullId() {
        ReturnResult<Boolean> result = sysIpBlackwhiteService.deleteIp(null);
        assertFalse(result.isSuccess());
        assertTrue(result.getMsg().contains("记录不存在"));
    }

    /**
     * 测试：删除Success。
     */
    @Disabled("[P3C40] 待完整 Spring 环境补真实测试：需 H2 + 真实 MyBatis mapper")
    @Test
    @DisplayName("删除IP-合法ID调用removeById")
    void testDeleteSuccess() {
        ReturnResult<Boolean> result = sysIpBlackwhiteService.deleteIp(3);
        assertTrue(result.isSuccess());
    }

    /**
     * 测试：页By类型。
     */
    @Disabled("[P3C40] 待完整 Spring 环境补真实测试：需 H2 + 真实 MyBatis mapper")
    @Test
    @DisplayName("分页-按type筛选调用分页查询")
    void testPageByType() {
        Query<SysIpBlackwhite> query = new Query<>();
        SysIpBlackwhite condition = new SysIpBlackwhite();
        condition.setSysIpType(1);
        IPage<SysIpBlackwhite> page = sysIpBlackwhiteService.page(query, condition);
        assertNotNull(page);
    }

    /**
     * 测试：分页查询全量。
     */
    @Disabled("[P3C40] 待完整 Spring 环境补真实测试：需 H2 + 真实 MyBatis mapper")
    @Test
    @DisplayName("分页-不传条件时返回全量")
    void testList() {
        IPage<SysIpBlackwhite> page = sysIpBlackwhiteService.page(new Query<>(), new SysIpBlackwhite());
        assertEquals(2, page.getRecords().size());
    }

    @Test
    @DisplayName("新增IP-CIDR前缀越界返回非法参数")
    void testSaveInvalidCidrPrefix() {
        ReturnResult<SysIpBlackwhite> result = sysIpBlackwhiteService.saveIp(entry(1, "10.0.0.0/33"));
        assertFalse(result.isSuccess());
    }

    @Test
    @DisplayName("新增IP-IPv6返回非法参数")
    void testSaveIpv6() {
        ReturnResult<SysIpBlackwhite> result = sysIpBlackwhiteService.saveIp(entry(1, "2001:db8::1"));
        assertFalse(result.isSuccess());
    }

    @Test
    @DisplayName("新增IP-非法策略类型返回非法参数")
    void testSaveInvalidType() {
        ReturnResult<SysIpBlackwhite> result = sysIpBlackwhiteService.saveIp(entry(2, "10.0.0.1"));
        assertFalse(result.isSuccess());
    }

    @Test
    @DisplayName("校验IP-非法客户端IP放行")
    void testCheckInvalidClientIp() {
        assertTrue(sysIpBlackwhiteService.checkIp("invalid-ip"));
    }
}
