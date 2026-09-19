package com.chua.starter.service.storage;

import com.chua.starter.service.properties.SysFileUploadProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * 本地磁盘文件存储
 * 落盘路径为 rootPath/bucket/随机文件名，文件名由服务端生成，不接受调用方命名，
 * 从根源上规避路径穿越与同名覆盖
 *
 * @author CH
 * @since 4.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SysLocalFileStorage {

    /**
     * 缺省桶名
    */
    private static final String DEFAULT_BUCKET = "default"; // 默认桶

    private final SysFileUploadProperties sysFileUploadProperties; // sys文件上传属性

    /**
     * 归一化桶名，仅保留小写字母数字与连字符
     *
     * @param bucket 原始桶名
     * @return 归一化后的桶名
     */
    public String normalizeBucket(String bucket) {
        if (!StringUtils.hasText(bucket)) {
            return DEFAULT_BUCKET;
        }
        String cleaned = bucket.trim().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9-]", "");
        if (cleaned.isEmpty()) {
            return DEFAULT_BUCKET;
        }
        return cleaned.length() > 32 ? cleaned.substring(0, 32) : cleaned;
    }

    /**
     * 取并校验扩展名
     *
     * @param originalFilename 上传时的原始文件名
     * @return 小写扩展名，不在白名单内返回 null
     */
    public String resolveExtension(String originalFilename) {
        String extension = StringUtils.getFilenameExtension(originalFilename);
        if (!StringUtils.hasText(extension)) {
            return null;
        }
        String lowered = extension.trim().toLowerCase(Locale.ROOT);
        List<String> allowed = sysFileUploadProperties.getAllowedExtensions();
        return null != allowed && allowed.contains(lowered) ? lowered : null;
    }

    /**
     * 写入本地磁盘
     *
     * @param bucket    归一化后的桶名
     * @param extension 已校验的扩展名
     * @param content   文件字节
     * @return 生成的文件名
     * @throws IOException 落盘失败
     */
    public String put(String bucket, String extension, byte[] content) throws IOException {
        Path directory = getRoot().resolve(bucket);
        Files.createDirectories(directory);
        String fileName = UUID.randomUUID().toString().replace("-", "") + "." + extension;
        Files.write(directory.resolve(fileName), content,
                StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
        return fileName;
    }

    /**
     * 解析并校验读取路径
     *
     * @param bucket   桶名
     * @param fileName 文件名
     * @return 可读的文件路径，越界或不存在返回 null
     */
    public Path resolve(String bucket, String fileName) {
        if (!StringUtils.hasText(bucket) || !StringUtils.hasText(fileName)) {
            return null;
        }
        if (fileName.indexOf('/') >= 0 || fileName.indexOf('\\') >= 0 || fileName.contains("..")) {
            return null;
        }
        Path root = getRoot();
        Path target = root.resolve(bucket).resolve(fileName).normalize();
        if (!target.startsWith(root) || !Files.isRegularFile(target)) {
            return null;
        }
        return target;
    }

    /**
     * 单文件大小上限（字节）
     *
     * @return 上限字节数
     */
    public long maxBytes() {
        return sysFileUploadProperties.getMaxSizeMb() * 1024L * 1024L;
    }

    /**
     * 存储根目录的绝对路径
     *
     * @return 根目录
     */
    public Path getRoot() {
        return Paths.get(sysFileUploadProperties.getRootPath()).toAbsolutePath().normalize();
    }
}
