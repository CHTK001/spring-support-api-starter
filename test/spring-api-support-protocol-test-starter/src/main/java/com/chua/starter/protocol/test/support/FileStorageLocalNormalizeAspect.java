package com.chua.starter.protocol.test.support;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.proxy.support.entity.SystemServerSettingFileStorage;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

/**
 * 协议测试环境下的本地存储兜底规范化：
 * 1. LOCAL -> FILESYSTEM
 * 2. 根目录不存在时自动创建
 * 3. 本地存储补齐占位 accessKey/secretKey
 */
@Slf4j
@Aspect
@Component
public class FileStorageLocalNormalizeAspect {

    @Around("execution(* com.chua.starter.proxy.support.service.server.SystemServerSettingFileStorageService.saveOne(..))")
    public Object aroundSaveOne(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();
        if (args.length == 1 && args[0] instanceof SystemServerSettingFileStorage storage) {
            normalize(storage);
        }
        return joinPoint.proceed(args);
    }

    private void normalize(SystemServerSettingFileStorage storage) {
        String type = normalizeType(storage.getFileStorageType());
        storage.setFileStorageType(type);
        if (!"FILESYSTEM".equals(type)) {
            return;
        }
        String endpoint = storage.getFileStorageEndpoint();
        if (hasText(endpoint)) {
            try {
                Files.createDirectories(Path.of(endpoint));
            } catch (Exception e) {
                log.warn("创建本地存储目录失败: {}", endpoint, e);
            }
        }
        if (!hasText(storage.getFileStorageBucket())) {
            storage.setFileStorageBucket("local");
        }
        if (!hasText(storage.getFileStorageAccessKey())) {
            storage.setFileStorageAccessKey("local");
        }
        if (!hasText(storage.getFileStorageSecretKey())) {
            storage.setFileStorageSecretKey("local");
        }
    }

    private String normalizeType(String rawType) {
        String normalized = rawType == null ? "" : rawType.trim().toUpperCase(Locale.ROOT);
        return "LOCAL".equals(normalized) ? "FILESYSTEM" : normalized;
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}

