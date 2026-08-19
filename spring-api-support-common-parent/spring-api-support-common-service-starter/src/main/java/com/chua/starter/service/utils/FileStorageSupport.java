package com.chua.starter.service.utils;

import com.chua.common.support.utils.StringUtils;
import com.chua.common.support.storage.FileStorage;
import com.chua.common.support.storage.setting.BucketSetting;
import com.chua.common.support.storage.setting.BucketSetting.BucketSettingBuilder;
import com.chua.starter.entity.service.SysFileSystemSetting;
import com.chua.starter.service.SysFileSystemSettingService;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * FileStorage 统一增强入口。
 *
 * @since 2026/08/03
 * @author CH
 */
@Component
public class FileStorageSupport {

    /**
     * 本地存储默认 AccessKey
     */
    private static final String DEFAULT_LOCAL_ACCESS_KEY = "__local__";

    /**
     * 本地存储默认 AccessSecret
     */
    private static final String DEFAULT_LOCAL_ACCESS_SECRET = "__local__";

    /**
     * 默认存储类型
     */
    private static final String DEFAULT_STORAGE_TYPE = "FILESYSTEM";

    /**
     * 本地存储默认根路径
     */
    private static final String DEFAULT_LOCAL_ROOT = "oss/system";

    /** environment */
    private final Environment environment;
    /** sysFileSystem设置服务Provider */
    private final ObjectProvider<SysFileSystemSettingService> sysFileSystemSettingServiceProvider;

    /**
     * 构造文件存储支持服务。
     *
     * @param environment                          Spring 环境对象，用于读取 /plugin.sso 前缀配置
     * @param sysFileSystemSettingServiceProvider  文件系统设置服务提供者（允许缺省）
     */
    public FileStorageSupport(Environment environment,
                              ObjectProvider<SysFileSystemSettingService> sysFileSystemSettingServiceProvider) {
        this.environment = environment;
        this.sysFileSystemSettingServiceProvider = sysFileSystemSettingServiceProvider;
    }

    /**
     * 创建存储对象。
     *
     * @param storageType   存储类型（可为空，为空时从配置解析默认类型）
     * @param bucketSetting 存储桶配置
     * @return 存储对象
     * @throws IllegalStateException 非本地类型且缺少 accessKeyId/accessKeySecret 时抛出
     */
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

    /**
     * 创建文件上传所需的存储桶配置。
     *
     * @param storageType       存储类型
     * @param bucket            存储桶名称
     * @param originalFilename  原始文件名
     * @param preferredFileName 期望文件名
     * @return 存储桶配置
     */
    public BucketSetting createUploadBucketSetting(String storageType, String bucket, String originalFilename, String preferredFileName) {
        String resolvedType = resolveStorageType(storageType);
        String finalBucket = normalizeBucket(bucket);
        BucketSettingBuilder builder = BucketSetting.builder()
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

    /**
     * 创建本地存储桶配置。
     *
     * @param bucket   存储桶名称
     * @param rootPath 本地根路径（为空时使用默认路径）
     * @return 本地存储桶配置
     */
    public BucketSetting createLocalBucketSetting(String bucket, String rootPath) {
        return BucketSetting.builder()
                .bucket(normalizeBucket(bucket))
                .endpoint(StringUtils.defaultString(rootPath, resolveLocalRootPath()))
                .accessKeyId(DEFAULT_LOCAL_ACCESS_KEY)
                .accessKeySecret(DEFAULT_LOCAL_ACCESS_SECRET)
                .build();
    }

    /**
     * 解析存储类型，为空时回退到配置或默认类型。
     *
     * @param storageType 存储类型
     * @return 解析后的存储类型
     */
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

    /**
     * 判断是否为本地存储类型。
     *
     * @param storageType 存储类型
     * @return true 表示为本地存储
     */
    public boolean isLocalStorageType(String storageType) {
        String text = (storageType == null ? "" : storageType).toLowerCase();
        return text.contains("filesystem") || text.contains("file_system") || text.contains("local");
    }

    /**
     * 补齐存储桶配置的缺省值（本地类型自动填充 accessKey 与根路径）。
     *
     * @param storageType 存储类型
     * @param setting     存储桶配置
     * @return 补齐后的存储桶配置
     */
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

    /**
     * 解析存储端点地址。
     *
     * @param storageType 存储类型
     * @return 端点地址
     */
    private String resolveEndpoint(String storageType) {
        if (isLocalStorageType(storageType)) {
            return resolveLocalRootPath();
        }
        String prefix = "plugin.sso." + storageType.toLowerCase() + ".";
        return environment.getProperty(prefix + "endpoint", DEFAULT_LOCAL_ROOT);
    }

    /**
     * 解析本地存储根路径。
     *
     * @return 本地存储根路径
     */
    private String resolveLocalRootPath() {
        SysFileSystemSetting setting = safeGetSetting();
        if (setting != null && StringUtils.isNotBlank(setting.getSysFileSystemSettingStorageRootPath())) {
            return setting.getSysFileSystemSettingStorageRootPath();
        }
        return DEFAULT_LOCAL_ROOT;
    }

    /**
     * 安全获取文件系统设置（不可用时返回 null，避免启动失败）。
     *
     * @return 启用的文件系统设置
     */
    private SysFileSystemSetting safeGetSetting() {
        try {
            SysFileSystemSettingService service = sysFileSystemSettingServiceProvider.getIfAvailable();
            return service == null ? null : service.getEnabledSettingForFileSystem();
        } catch (Exception ignored) {
            return null;
        }
    }

    /**
     * 规范化存储桶名称（去除开头斜杠）。
     *
     * @param bucket 存储桶名称
     * @return 规范化后的名称
     */
    private String normalizeBucket(String bucket) {
        if (StringUtils.isBlank(bucket)) {
            return "default";
        }
        return bucket.startsWith("/") ? bucket.substring(1) : bucket;
    }
}
