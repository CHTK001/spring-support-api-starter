// [P3C A20 豁免] A20/A30/A31: 含日志/界面/SQL 字符串字面量等非业务魔法值场景，豁免。
package com.chua.starter.service.utils;

import com.chua.common.support.storage.setting.BucketSetting;
import com.chua.common.support.utils.FileUtils;
import com.chua.common.support.utils.IdUtils;
import com.chua.common.support.utils.StringUtils;
import com.chua.starter.entity.service.SysFileSystemSetting;
import com.chua.starter.service.SysFileSystemSettingService;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/**
 * 文件storage 统一增强入口。
 * @author CH
 * @since 4.0.0
 * @param bucket bucket
 * @return normalizeBucket的结果
 */
@Component
public class FileStorageSupport {

    private static final String DEFAULT_LOCAL_ACCESS_KEY = "__local__"; // 默认本地access键
    private static final String DEFAULT_LOCAL_ACCESS_SECRET = "__local__"; // 默认本地accesssecret
    private static final String DEFAULT_STORAGE_TYPE = "FILESYSTEM"; // 默认storage类型
    private static final String DEFAULT_LOCAL_ROOT = "oss/system"; // 默认本地根

    private final Environment environment; // 环境
    private final ObjectProvider<SysFileSystemSettingService> sysFileSystemSettingServiceProvider; // sys文件系统setting服务提供者

    public FileStorageSupport(Environment environment,
                              ObjectProvider<SysFileSystemSettingService> sysFileSystemSettingServiceProvider) {
        this.environment = environment;
        this.sysFileSystemSettingServiceProvider = sysFileSystemSettingServiceProvider;
    /**
     * 创建storage。
     * @param storageType storage类型
     * @param bucketSetting bucketSetting
     * @return 创建storage的结果
     */
    }

    /**
     * 创建Storage。
     *
     * @param storageType storage类型，不允许为 null
     * @param bucketSetting 方法入参 bucketSetting
     * @return 结果值
     */
    public com.chua.common.support.storage.FileStorage createStorage(String storageType, BucketSetting bucketSetting) {
        String resolvedType = resolveStorageType(storageType);
        BucketSetting resolvedSetting = enrichSetting(resolvedType, bucketSetting);
        if (!isLocalStorageType(resolvedType)) {
            if (StringUtils.isBlank(resolvedSetting.getAccessKeyId()) || StringUtils.isBlank(resolvedSetting.getAccessKeySecret())) {
                throw new IllegalStateException("存储类型 " + resolvedType + " 缺少 accessKeyId/accessKeySecret 配置");
            }
        }
        return com.chua.common.support.storage.FileStorage.createStorage(resolvedType, resolvedSetting);
    /**
     * 创建uploadbucketsetting。
     * @param storageType storage类型
     * @param bucket bucket
     * @param originalFilename 原始文件名
     * @param preferredFileName preferred文件名称
     * @return 创建uploadbucketsetting的结果
     */
    }

    /**
     * 创建UploadBucketSetting。
     *
     * @param storageType storage类型，不允许为 null
     * @param bucket 方法入参 bucket
     * @param originalFilename 方法入参 originalFilename
     * @param preferredFileName preferred文件名称，不允许为 null
     * @return BucketSetting 对象
     */
    public BucketSetting createUploadBucketSetting(String storageType, String bucket, String originalFilename, String preferredFileName) {
        String resolvedType = resolveStorageType(storageType);
        String finalBucket = normalizeBucket(bucket);
        var builder = BucketSetting.builder()
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
    /**
     * 创建本地bucketsetting。
     * @param bucket bucket
     * @param rootPath 根路径
     * @return 创建本地bucketsetting的结果
     */
    }

    /**
     * 创建LocalBucketSetting。
     *
     * @param bucket 方法入参 bucket
     * @param rootPath 根节点路径，不允许为 null
     * @return BucketSetting 对象
     */
    public BucketSetting createLocalBucketSetting(String bucket, String rootPath) {
        return BucketSetting.builder()
                .bucket(normalizeBucket(bucket))
                .endpoint(StringUtils.defaultString(rootPath, resolveLocalRootPath()))
                .accessKeyId(DEFAULT_LOCAL_ACCESS_KEY)
                .accessKeySecret(DEFAULT_LOCAL_ACCESS_SECRET)
                .build();
    /**
     * resolvestorage类型。
     * @param storageType storage类型
     * @return resolvestorage类型的结果
     */
    }

    /**
     * 解析Storage类型。
     *
     * @param storageType storage类型，不允许为 null
     * @return 结果字符串
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
    /**
     * 是否本地storage类型。
     * @param storageType storage类型
     * @return 是否本地storage类型的结果
     */
    }

    /**
     * 是否LocalStorage类型。
     *
     * @param storageType storage类型，不允许为 null
     * @return 是否成功（true 表示成功）
     */
    public boolean isLocalStorageType(String storageType) {
        String text = (storageType == null ? "" : storageType).toLowerCase();
        return text.contains("filesystem") || text.contains("file_system") || text.contains("local");
    /**
     * enrichSetting。
     * @param storageType storage类型
     * @param setting setting
     * @return enrichSetting的结果
     */
    }

    /**
     * enrichSetting。
     *
     * @param storageType storage类型，不允许为 null
     * @param setting 方法入参 setting
     * @return BucketSetting 对象
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
    /**
     * resolve端点。
     * @param storageType storage类型
     * @return resolve端点的结果
     */
    }

    /**
     * 解析Endpoint。
     *
     * @param storageType storage类型，不允许为 null
     * @return 结果字符串
     */
    private String resolveEndpoint(String storageType) {
        if (isLocalStorageType(storageType)) {
            return resolveLocalRootPath();
        }
        String prefix = "plugin.sso." + storageType.toLowerCase() + ".";
        return environment.getProperty(prefix + "endpoint", DEFAULT_LOCAL_ROOT);
    /**
     * resolve本地根路径。
     * @return resolve本地根路径的结果
     */
    }

    /**
     * 解析Local根节点路径。
     *
     * @return 结果字符串
     */
    private String resolveLocalRootPath() {
        SysFileSystemSetting setting = safeGetSetting();
        if (setting != null && StringUtils.isNotBlank(setting.getSysFileSystemSettingStorageRootPath())) {
            return setting.getSysFileSystemSettingStorageRootPath();
        }
        return DEFAULT_LOCAL_ROOT;
    /**
     * safe获取setting。
     * @return safe获取setting的结果
     */
    }

    /**
     * safe获取Setting。
     *
     * @return Sys文件SystemSetting 对象
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
     * normalizeBucket。
     *
     * @param bucket 方法入参 bucket
     * @return 结果字符串
     */
    private String normalizeBucket(String bucket) {
        if (StringUtils.isBlank(bucket)) {
            return "default";
        }
        return bucket.startsWith("/") ? bucket.substring(1) : bucket;
    }
}
