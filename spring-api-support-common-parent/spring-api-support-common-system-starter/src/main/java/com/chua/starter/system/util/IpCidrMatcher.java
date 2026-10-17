package com.chua.starter.system.util;

import com.chua.common.support.network.net.IpCidrUtils;

/*
 * 此前为自实现，现委托 utils IpCidrUtils（@since 4.0.0.43），行为一致。
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
