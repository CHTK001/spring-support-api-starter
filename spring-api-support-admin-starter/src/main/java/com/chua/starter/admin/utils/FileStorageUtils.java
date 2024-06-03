package com.chua.starter.admin.utils;

import com.chua.common.support.oss.FileStorage;
import com.chua.common.support.oss.extension.DefaultFileStorageFilterPlugin;
import com.chua.common.support.oss.extension.FileStorageConversionPlugin;
import com.chua.common.support.oss.extension.NamingResolver;
import com.chua.common.support.oss.options.FileStorageOption;
import com.chua.common.support.spi.ServiceProvider;
import com.chua.starter.admin.entity.SysFileStorage;
import com.chua.starter.common.support.filestorage.FileStorageService;

import java.nio.charset.StandardCharsets;

/**
 * 文件存储实用程序
 *
 * @author CH
 */
public class FileStorageUtils {


    /**
     * 打开文件存储
     *
     * @param fileStorageService 文件存储服务
     * @param sysFileStorage     sys文件存储
     */
    public static void openFileStorage(FileStorageService fileStorageService, SysFileStorage sysFileStorage) {
        fileStorageService.register(sysFileStorage.getFsBucket(), FileStorage.createStorage(
                sysFileStorage.getFsType(),
                FileStorageOption.builder()
                        .parentPath(sysFileStorage.getFsStoragePath())
                        .domain(sysFileStorage.getFsDomain())
                        .accessKeyId(sysFileStorage.getFsAccessKeyId())
                        .accessKeySecret(sysFileStorage.getFsAccessKeySecret())
                        .charset(StandardCharsets.UTF_8)
                        .bucket(sysFileStorage.getFsBucket())
                        .fileStorageNamingPlugin(ServiceProvider.of(NamingResolver.class).getNewExtension(sysFileStorage.getFsNamed()))
                        .fileStorageConversionPlugin(ServiceProvider.of(FileStorageConversionPlugin.class).getNewExtension(sysFileStorage.getFsPlugin()))
                        .fileStorageFilterPlugin(new DefaultFileStorageFilterPlugin(sysFileStorage.getFsFilter()))
                        .endpoint(sysFileStorage.getFsEndpoint())
                        .build()
        ));
    }
}
