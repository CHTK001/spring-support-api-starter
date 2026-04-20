package com.chua.starter.protocol.test.controller;

import com.chua.common.support.io.binary.ByteSourceArray;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.network.protocol.server.ProtocolServer;
import com.chua.common.support.storage.oss.FileStorage;
import com.chua.common.support.storage.oss.metadata.MapMetadata;
import com.chua.common.support.storage.oss.request.ListObjectRequest;
import com.chua.common.support.storage.oss.request.PutObjectRequest;
import com.chua.common.support.storage.oss.result.ListObjectResult;
import com.chua.common.support.storage.oss.result.PutObjectResult;
import com.chua.common.support.storage.oss.setting.BucketSetting;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Optional;

import static com.chua.starter.proxy.support.service.impl.SystemServerServiceImpl.runningServerInstances;

@Slf4j
@RestController
@RequestMapping("/file-storage")
@Tag(name = "文件存储预览")
public class FileStoragePreviewController {

    @PostMapping("/list")
    public ReturnResult<ListObjectResult> list(
            @RequestParam(required = false) Integer serverId,
            @RequestParam String type,
            @RequestParam(required = false) String endpoint,
            @RequestParam(required = false) String bucket,
            @RequestParam(required = false, name = "accessKey") String accessKeyId,
            @RequestParam(required = false, name = "secretKey") String accessKeySecret,
            @RequestParam(required = false) String region,
            @RequestParam(required = false, name = "basePath") String filePath,
            @RequestParam(required = false, defaultValue = "1000") Integer limit,
            @RequestParam(required = false) String marker
    ) {
        try {
            BucketSetting bucketSetting = BucketSetting.builder()
                    .endpoint(endpoint)
                    .bucket(bucket)
                    .accessKeyId(accessKeyId)
                    .accessKeySecret(accessKeySecret)
                    .region(region)
                    .build();
            ProtocolServer protocolServer = runningServerInstances.get(serverId);
            if (protocolServer == null) {
                return ReturnResult.error("服务未启动, 无法创建FileStorage实例");
            }
            Object filter = resolveFileStorageFilter(protocolServer);
            if (filter == null) {
                return ReturnResult.error("服务未安装 fileStorage 过滤器");
            }
            FileStorage fileStorage = resolveFileStorage(filter, bucket);
            if (fileStorage == null) {
                return ReturnResult.error("未找到 bucket 对应的文件存储: " + bucket);
            }
            ListObjectRequest request = ListObjectRequest.builder()
                    .filePath(Optional.ofNullable(filePath).orElse("/"))
                    .marker(marker)
                    .limit(limit == null ? 1000 : Math.max(1, limit))
                    .build();
            ListObjectResult result = fileStorage.listObject(request);
            return ReturnResult.of(result);
        } catch (Throwable e) {
            log.error("列出文件存储对象失败", e);
            return ReturnResult.of(ListObjectResult.EMPTY);
        }
    }

    @PostMapping("/upload")
    public ReturnResult<PutObjectResult> upload(
            @RequestParam Integer serverId,
            @RequestParam(required = false) String bucket,
            @RequestParam(required = false, defaultValue = "/") String filePath,
            @RequestParam("file") MultipartFile file
    ) {
        long maxSize = 10 * 1024 * 1024L;
        if (file.getSize() > maxSize) {
            return ReturnResult.error("文件大小不能超过10MB");
        }

        try {
            ProtocolServer protocolServer = runningServerInstances.get(serverId);
            if (protocolServer == null) {
                return ReturnResult.error("服务未启动，无法上传文件");
            }
            Object filter = resolveFileStorageFilter(protocolServer);
            if (filter == null) {
                return ReturnResult.error("服务未安装 fileStorage 过滤器");
            }
            FileStorage fileStorage = resolveFileStorage(filter, bucket);
            if (fileStorage == null) {
                return ReturnResult.error("未找到 bucket 对应的文件存储: " + bucket);
            }
            PutObjectRequest request = PutObjectRequest.builder()
                    .byteSource(new ByteSourceArray(file.getInputStream()))
                    .fileName(file.getOriginalFilename())
                    .filePath(Optional.ofNullable(filePath).orElse("/"))
                    .metadata(new MapMetadata(Collections.emptyMap()))
                    .build();
            PutObjectResult result = fileStorage.putObject(request);
            return ReturnResult.of(result);
        } catch (Exception e) {
            log.error("上传文件失败", e);
            return ReturnResult.error("上传文件失败: " + e.getMessage());
        }
    }

    private Object resolveFileStorageFilter(ProtocolServer protocolServer) {
        try {
            Class<?> filterClass = Class.forName("com.chua.common.support.network.protocol.filter.FileStorageServletFilter");
            Method method = protocolServer.getClass().getMethod("getFilter", Class.class);
            return method.invoke(protocolServer, filterClass);
        } catch (Exception e) {
            log.warn("解析 fileStorage 过滤器失败", e);
            return null;
        }
    }

    private FileStorage resolveFileStorage(Object filter, String bucket) {
        try {
            Method method = filter.getClass().getMethod("getFileStorage", String.class);
            Object target = method.invoke(filter, bucket);
            if (target instanceof FileStorage fileStorage) {
                return fileStorage;
            }
            return null;
        } catch (Exception e) {
            log.warn("获取 fileStorage 失败, bucket={}", bucket, e);
            return null;
        }
    }
}
