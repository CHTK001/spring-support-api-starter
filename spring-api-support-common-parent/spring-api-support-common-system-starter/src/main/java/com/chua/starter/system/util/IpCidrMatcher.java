package com.chua.starter.system.util;

public final class IpCidrMatcher {

    private static final long IPV4_MASK = 0xFFFFFFFFL;

    private static final int IPV4_BITS = 32;

    private IpCidrMatcher() {
    }

    public static String canonicalize(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("IP不能为空");
        }
        String normalized = value.trim();
        if (normalized.indexOf('/') >= 0) {
            return canonicalizeCidr(normalized);
        }
        return formatIpv4(parseIpv4(normalized));
    }

    public static String canonicalizeIpv4(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("IP不能为空");
        }
        return formatIpv4(parseIpv4(value.trim()));
    }

    public static String canonicalizeCidr(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("CIDR不能为空");
        }
        String normalized = value.trim();
        int separator = normalized.indexOf('/');
        if (separator <= 0 || separator != normalized.lastIndexOf('/') || separator == normalized.length() - 1) {
            throw new IllegalArgumentException("CIDR格式错误");
        }
        long address = parseIpv4(normalized.substring(0, separator));
        int prefix = parsePrefix(normalized.substring(separator + 1));
        long mask = prefix == 0 ? 0L : (IPV4_MASK << (IPV4_BITS - prefix)) & IPV4_MASK;
        long network = address & mask;
        return formatIpv4(network) + "/" + prefix;
    }

    public static boolean matches(String candidateIp, String cidr) {
        try {
            String canonicalCidr = canonicalizeCidr(cidr);
            String canonicalIp = canonicalizeIpv4(candidateIp);
            int separator = canonicalCidr.indexOf('/');
            long address = parseIpv4(canonicalIp);
            long network = parseIpv4(canonicalCidr.substring(0, separator));
            int prefix = parsePrefix(canonicalCidr.substring(separator + 1));
            long mask = prefix == 0 ? 0L : (IPV4_MASK << (IPV4_BITS - prefix)) & IPV4_MASK;
            return (address & mask) == (network & mask);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public static boolean matchesEntry(String candidateIp, String entry) {
        if (entry == null || entry.isBlank()) {
            return false;
        }
        String normalized = entry.trim();
        if (normalized.indexOf('/') >= 0) {
            return matches(candidateIp, normalized);
        }
        try {
            return canonicalizeIpv4(candidateIp).equals(canonicalizeIpv4(normalized));
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static long parseIpv4(String value) {
        String[] segments = value.split("\\.", -1);
        if (segments.length != 4) {
            throw new IllegalArgumentException("IPv4格式错误");
        }
        long result = 0L;
        for (String segment : segments) {
            int octet = parseOctet(segment);
            result = (result << 8) | octet;
        }
        return result;
    }

    private static int parseOctet(String value) {
        if (value == null || value.isEmpty() || value.length() > 3) {
            throw new IllegalArgumentException("IPv4格式错误");
        }
        int result = 0;
        for (int i = 0; i < value.length(); i++) {
            char current = value.charAt(i);
            if (current < '0' || current > '9') {
                throw new IllegalArgumentException("IPv4格式错误");
            }
            result = result * 10 + current - '0';
        }
        if (result > 255) {
            throw new IllegalArgumentException("IPv4格式错误");
        }
        return result;
    }

    private static int parsePrefix(String value) {
        if (value == null || value.isEmpty() || value.length() > 2) {
            throw new IllegalArgumentException("CIDR前缀长度错误");
        }
        int prefix = 0;
        for (int i = 0; i < value.length(); i++) {
            char current = value.charAt(i);
            if (current < '0' || current > '9') {
                throw new IllegalArgumentException("CIDR前缀长度错误");
            }
            prefix = prefix * 10 + current - '0';
        }
        if (prefix < 0 || prefix > 32) {
            throw new IllegalArgumentException("CIDR前缀长度错误");
        }
        return prefix;
    }

    private static String formatIpv4(long value) {
        return ((value >> 24) & 0xFF) + "."
                + ((value >> 16) & 0xFF) + "."
                + ((value >> 8) & 0xFF) + "."
                + (value & 0xFF);
    }
}
