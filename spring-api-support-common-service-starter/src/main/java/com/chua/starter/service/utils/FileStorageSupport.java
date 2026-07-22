package com.chua.starter.service.utils;

import com.chua.common.support.utils.StringUtils;
import com.chua.common.support.storage.FileStorage;
import com.chua.common.support.storage.setting.BucketSetting;
import com.chua.starter.entity.service.SysFileSystemSetting;
import com.chua.starter.service.SysFileSystemSettingService;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * FileStorage 统一增强入口。
 */
@Component
public class FileStorageSupport {

    private static final String DEFAULT_LOCAL_ACCESS_KEY = "__local__";
    private static final String DEFAULT_LOCAL_ACCESS_SECRET = "__local__";
    private static final String DEFAULT_STORAGE_TYPE = "FILESYSTEM";
    private static final String DEFAULT_LOCAL_ROOT = "oss/system";

    private final Environment environment;
    private final ObjectProvider<SysFileSystemSettingService> sysFileSystemSettingServiceProvider;

    public FileStorageSupport(Environment environment,
                              ObjectProvider<SysFileSystemSettingService> sysFileSystemSettingServiceProvider) {
        this.environment = environment;
        this.sysFileSystemSettingServiceProvider = sysFileSystemSettingServiceProvider;
    }

    public FileStorage createStorage(String storageType, BucketSetting bucketSetting) {
        String resolvedType = resolveStorageType(storageType);
        BucketSetting resolvedSetting = enrichSetting(resolvedType, bucketSetting);
        if (!isLocalStorageType(resolvedType)) {
            if (StringUtils.isBlank(resolvedSetting.getAccessKeyId()) || StringUtils.isBlank(resolvedSetting.getAccessKeySecret())) {
                throw new IllegalStateException("存储类型 " + resolvedType + " 缺少 accessKeyId/accessKeySecret 配置");
            }
        }
        return FileStorage.createStorage(resolvedType, resolvedSetting);
    }

    public BucketSetting createUploadBucketSetting(String storageType, String bucket, String originalFilename, String preferredFileName) {
        String resolvedType = resolveStorageType(storageType);
        String finalBucket = normalizeBucket(bucket);
        BucketSetting.BucketSettingBuilder<?, ?> builder = BucketSetting.builder()
                .bucket(finalBucket)
                .endpoint(resolveEndpoint(resolvedType));

        if (isLocalStorageType(resolvedType)) {
            builder.accessKeyId(DEFAULT_LOCAL_ACCESS_KEY);
            builder.accessKeySecret(DEFAULT_LOCAL_ACCESS_SECRET);
        } else {
            String prefix = "plugin.sso." + resolvedType.toLowerCase() + ".";
            builder.accessKeyId(environment.getProperty(prefix + "app-id"));
            builder.accessKeySecret(environment.getProperty(prefix + "app-secret"));
            builder.region(environment.getProperty(prefix + "region"));
        }
        return builder.build();
    }

    public BucketSetting createLocalBucketSetting(String bucket, String rootPath) {
        return BucketSetting.builder()
                .bucket(normalizeBucket(bucket))
                .endpoint(StringUtils.defaultString(rootPath, resolveLocalRootPath()))
                .accessKeyId(DEFAULT_LOCAL_ACCESS_KEY)
                .accessKeySecret(DEFAULT_LOCAL_ACCESS_SECRET)
                .build();
    }

    public String resolveStorageType(String storageType) {
        if (StringUtils.isNotBlank(storageType)) {
            return storageType;
        }
        SysFileSystemSetting setting = safeGetSetting();
        if (setting != null && StringUtils.isNotBlank(setting.getSysFileSystemSettingDefaultStorageType())) {
            return setting.getSysFileSystemSettingDefaultStorageType();
        }
        return DEFAULT_STORAGE_TYPE;
    }

    public boolean isLocalStorageType(String storageType) {
        String text = (storageType == null ? "" : storageType).toLowerCase();
        return text.contains("filesystem") || text.contains("file_system") || text.contains("local");
    }

    private BucketSetting enrichSetting(String storageType, BucketSetting setting) {
        if (setting == null) {
            return isLocalStorageType(storageType)
                    ? createLocalBucketSetting("default", resolveLocalRootPath())
                    : BucketSetting.builder().bucket("default").endpoint(resolveEndpoint(storageType)).build();
        }
        if (!isLocalStorageType(storageType)) {
            return setting;
        }
        return BucketSetting.builder()
                .bucket(normalizeBucket(setting.getBucket()))
                .region(setting.getRegion())
                .endpoint(StringUtils.isBlank(setting.getEndpoint()) ? resolveLocalRootPath() : setting.getEndpoint())
                .accessKeyId(StringUtils.isBlank(setting.getAccessKeyId()) ? DEFAULT_LOCAL_ACCESS_KEY : setting.getAccessKeyId())
                .accessKeySecret(StringUtils.isBlank(setting.getAccessKeySecret()) ? DEFAULT_LOCAL_ACCESS_SECRET : setting.getAccessKeySecret())
                .connectionTimeoutMills(setting.getConnectionTimeoutMills())
                .sessionTimeoutMills(setting.getSessionTimeoutMills())
                .build();
    }

    private String resolveEndpoint(String storageType) {
        if (isLocalStorageType(storageType)) {
            return resolveLocalRootPath();
        }
        String prefix = "plugin.sso." + storageType.toLowerCase() + ".";
        return environment.getProperty(prefix + "endpoint", DEFAULT_LOCAL_ROOT);
    }

    private String resolveLocalRootPath() {
        SysFileSystemSetting setting = safeGetSetting();
        if (setting != null && StringUtils.isNotBlank(setting.getSysFileSystemSettingStorageRootPath())) {
            return setting.getSysFileSystemSettingStorageRootPath();
        }
        return DEFAULT_LOCAL_ROOT;
    }

    private SysFileSystemSetting safeGetSetting() {
        try {
            SysFileSystemSettingService service = sysFileSystemSettingServiceProvider.getIfAvailable();
            return service == null ? null : service.getEnabledSettingForFileSystem();
        } catch (Exception ignored) {
            return null;
        }
    }

    private String normalizeBucket(String bucket) {
        if (StringUtils.isBlank(bucket)) {
            return "default";
        }
        return bucket.startsWith("/") ? bucket.substring(1) : bucket;
    }
}
