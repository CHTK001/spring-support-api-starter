package com.chua.starter.music.spi.support;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

public final class TencentSignSupport {

    private static final int[] PART_1_INDEXES = {23, 14, 6, 36, 16, 40, 7, 19};
    private static final int[] PART_2_INDEXES = {16, 1, 32, 12, 19, 27, 8, 5};
    private static final int[] SCRAMBLE_VALUES = {
            89, 39, 179, 150, 218, 82, 58, 252, 177, 52, 186, 123, 120, 64, 242, 133, 143, 161, 121, 179
    };

    private TencentSignSupport() {
    }

    public static String sign(String text) {
        String hash = sha1(text).toUpperCase();
        String part1 = pick(hash, PART_1_INDEXES);
        String part2 = pick(hash, PART_2_INDEXES);
        byte[] scramble = new byte[SCRAMBLE_VALUES.length];
        for (int index = 0; index < SCRAMBLE_VALUES.length; index++) {
            int value = Integer.parseInt(hash.substring(index * 2, index * 2 + 2), 16);
            scramble[index] = (byte) (SCRAMBLE_VALUES[index] ^ value);
        }
        String middle = Base64.getEncoder()
                .encodeToString(scramble)
                .replace("/", "")
                .replace("\\", "")
                .replace("+", "")
                .replace("=", "");
        return ("zzc" + part1 + middle + part2).toLowerCase();
    }

    private static String pick(String hash, int[] indexes) {
        StringBuilder builder = new StringBuilder();
        for (int index : indexes) {
            builder.append(hash.charAt(index));
        }
        return builder.toString();
    }

    private static String sha1(String text) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            byte[] bytes = digest.digest(text.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder(bytes.length * 2);
            for (byte value : bytes) {
                builder.append(String.format("%02x", value));
            }
            return builder.toString();
        } catch (Exception e) {
            throw new IllegalStateException("生成 QQMusic 签名失败", e);
        }
    }
}
