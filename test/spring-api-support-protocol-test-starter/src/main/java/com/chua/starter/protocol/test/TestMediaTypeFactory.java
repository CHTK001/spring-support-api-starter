package com.chua.starter.protocol.test;

import com.chua.common.support.core.utils.IoUtils;
import com.chua.common.support.media.MediaTypeFactory;

import java.io.InputStream;

/**
 * 验证 MediaTypeFactory 是否正确加载 mime.types
 */
public class TestMediaTypeFactory {
    public static void main(String[] args) throws Exception {
        System.out.println("=== MediaTypeFactory Test ===");

        // 1. Test IoUtils.newClassPathStream
        System.out.println("\n1. IoUtils.newClassPathStream:");
        InputStream is = IoUtils.newClassPathStream("/mime/types/mime.types");
        System.out.println("  InputStream: " + (is != null ? "OK" : "NULL"));
        if (is != null) {
            byte[] data = new byte[500];
            int len = is.read(data);
            System.out.println("  First bytes: " + new String(data, 0, len));
            is.close();
        }

        // 2. Test MediaTypeFactory
        System.out.println("\n2. MediaTypeFactory.getMediaType:");
        String[] testFiles = {"pom.xml", "test.html", "test.js", "test.css", "test.png", "test.json", "test.py"};
        for (String f : testFiles) {
            System.out.println("  " + f + " -> " + MediaTypeFactory.getMediaType(f).map(Object::toString).orElse("*/*"));
        }

        // 3. Check the internal FILE_EXTENSION_TO_MEDIA_TYPES map size
        System.out.println("\n3. Static analysis:");
        System.out.println("  Check getMediaType('xml'): " + MediaTypeFactory.getMediaType("test.xml").map(Object::toString).orElse("*/*"));
        System.out.println("  Check getMediaType('txt'): " + MediaTypeFactory.getMediaType("test.txt").map(Object::toString).orElse("*/*"));

        System.out.println("\n=== Test Complete ===");
    }
}
