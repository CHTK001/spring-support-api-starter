package com.chua.starter.system.util;

import com.chua.common.support.network.net.IpCidrUtils;

/**
 * IPv4 CIDR 匹配工具（兼容壳）。
 *
 * <p>此前为自实现，现委托 utils 的 {@link IpCidrUtils}（@since 4.0.0.43），行为一致；
 * 保留本类只为不破坏既有调用方。</p>
 *
 * @author CH
 * @since 4.0.0.51
 */
public final class IpCidrMatcher {

    private IpCidrMatcher() {
    }

    public static String canonicalize(String value) {
        return IpCidrUtils.canonicalize(value);
    }

    public static String canonicalizeIpv4(String value) {
        return IpCidrUtils.canonicalizeIpv4(value);
    }

    public static String canonicalizeCidr(String value) {
        return IpCidrUtils.canonicalizeCidr(value);
    }

    public static boolean matches(String candidateIp, String cidr) {
        return IpCidrUtils.matches(candidateIp, cidr);
    }

    public static boolean matchesEntry(String candidateIp, String entry) {
        return IpCidrUtils.matchesEntry(candidateIp, entry);
    }
}
