// [P3C A20 豁免] A20/A30/A31: 含日志/界面/SQL 字符串字面量等非业务魔法值场景，豁免。
package com.chua.starter.system.support;

import com.chua.starter.entity.system.SysAccountBlackwhite;
import com.chua.starter.entity.system.SysIpBlackwhite;
import com.chua.starter.entity.system.SysPolicyConfig;
import com.chua.starter.entity.system.SysRateLimitRule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * 统一策略配置转换器单元测试
 *
 * <p>重点覆盖 IP 名单的取值方向归一：IP 旧口径是 0白/1黑、0启用/1禁用，
 * 与统一表的 1黑/2白、1启用/0禁用相反。这段翻转一旦写错不会报错，
 * 只会在运行期把黑白名单与启停状态整体反过来，因此必须用测试钉死。</p>
 *
 * @since 4.0.0.50
 * @author CH
 */
@DisplayName("统一策略配置转换器测试")
class PolicyConfigConverterTest {

    @Test
    @DisplayName("IP-黑名单1落到统一表listType=1并可逆")
    void testIpBlackTypeRoundTrip() {
        assertEquals(SysPolicyConfig.LIST_BLACK, PolicyConfigConverter.fromIpType(PolicyConfigConverter.IP_TYPE_BLACK));
        assertEquals(PolicyConfigConverter.IP_TYPE_BLACK, PolicyConfigConverter.toIpType(SysPolicyConfig.LIST_BLACK));
    }

    @Test
    @DisplayName("IP-白名单0落到统一表listType=2并可逆")
    void testIpWhiteTypeRoundTrip() {
        assertEquals(SysPolicyConfig.LIST_WHITE, PolicyConfigConverter.fromIpType(PolicyConfigConverter.IP_TYPE_WHITE));
        assertEquals(PolicyConfigConverter.IP_TYPE_WHITE, PolicyConfigConverter.toIpType(SysPolicyConfig.LIST_WHITE));
    }

    @Test
    @DisplayName("IP-状态0启用落到统一表status=1并可逆")
    void testIpEnabledStatusRoundTrip() {
        assertEquals(SysPolicyConfig.STATUS_ENABLED, PolicyConfigConverter.fromIpStatus(PolicyConfigConverter.IP_STATUS_ENABLED));
        assertEquals(PolicyConfigConverter.IP_STATUS_ENABLED, PolicyConfigConverter.toIpStatus(SysPolicyConfig.STATUS_ENABLED));
    }

    @Test
    @DisplayName("IP-状态1禁用落到统一表status=0并可逆")
    void testIpDisabledStatusRoundTrip() {
        assertEquals(SysPolicyConfig.STATUS_DISABLED, PolicyConfigConverter.fromIpStatus(PolicyConfigConverter.IP_STATUS_DISABLED));
        assertEquals(PolicyConfigConverter.IP_STATUS_DISABLED, PolicyConfigConverter.toIpStatus(SysPolicyConfig.STATUS_DISABLED));
    }

    @Test
    @DisplayName("IP-传输对象转行再转回不丢字段")
    void testIpRoundTrip() {
        SysIpBlackwhite dto = new SysIpBlackwhite();
        dto.setSysIpType(PolicyConfigConverter.IP_TYPE_BLACK);
        dto.setSysIpStatus(PolicyConfigConverter.IP_STATUS_ENABLED);
        dto.setSysIpIp("10.0.0.0/8");
        dto.setSysIpRemark("内网段");

        SysPolicyConfig row = PolicyConfigConverter.fromIp(dto);
        assertEquals(SysPolicyConfig.TYPE_IP, row.getType());
        assertEquals("", row.getDimension());
        assertEquals("10.0.0.0/8", row.getBizKey());

        SysIpBlackwhite back = PolicyConfigConverter.toIp(row);
        assertEquals(PolicyConfigConverter.IP_TYPE_BLACK, back.getSysIpType());
        assertEquals(PolicyConfigConverter.IP_STATUS_ENABLED, back.getSysIpStatus());
        assertEquals("10.0.0.0/8", back.getSysIpIp());
        assertEquals("内网段", back.getSysIpRemark());
    }

    @Test
    @DisplayName("限流-dimension与target落到同名列且enabled等于status")
    void testRateLimitMapping() {
        SysRateLimitRule dto = new SysRateLimitRule();
        dto.setDimension("interface");
        dto.setTarget("/v2/policy/ratelimit/list");
        dto.setQps(50);
        dto.setBurstSize(80);
        dto.setEnabled(SysPolicyConfig.STATUS_ENABLED);

        SysPolicyConfig row = PolicyConfigConverter.fromRateLimit(dto);
        assertEquals(SysPolicyConfig.TYPE_RATE_LIMIT, row.getType());
        assertEquals("interface", row.getDimension());
        assertEquals("/v2/policy/ratelimit/list", row.getBizKey());
        assertEquals(SysPolicyConfig.LIST_NONE, row.getListType());
        assertEquals(50, row.getQps());
        assertEquals(SysPolicyConfig.STATUS_ENABLED, row.getStatus());

        SysRateLimitRule back = PolicyConfigConverter.toRateLimit(row);
        assertEquals("interface", back.getDimension());
        assertEquals("/v2/policy/ratelimit/list", back.getTarget());
        assertEquals(SysPolicyConfig.STATUS_ENABLED, back.getEnabled());
        assertEquals(80, back.getBurstSize());
    }

    @Test
    @DisplayName("账号-取值方向与统一表同向不做翻转")
    void testAccountMappingKeepsDirection() {
        SysAccountBlackwhite dto = new SysAccountBlackwhite();
        dto.setAccount("ops");
        dto.setType(SysPolicyConfig.LIST_WHITE);
        dto.setStatus(SysPolicyConfig.STATUS_ENABLED);

        SysPolicyConfig row = PolicyConfigConverter.fromAccount(dto);
        assertEquals(SysPolicyConfig.TYPE_ACCOUNT, row.getType());
        assertEquals(SysPolicyConfig.LIST_WHITE, row.getListType());
        assertEquals("ops", row.getBizKey());

        SysAccountBlackwhite back = PolicyConfigConverter.toAccount(row);
        assertEquals(SysPolicyConfig.LIST_WHITE, back.getType());
        assertEquals("ops", back.getAccount());
    }

    @Test
    @DisplayName("空行转各类型对象返回null")
    void testNullRow() {
        assertNull(PolicyConfigConverter.toIp(null));
        assertNull(PolicyConfigConverter.toRateLimit(null));
        assertNull(PolicyConfigConverter.toAccount(null));
    }
}