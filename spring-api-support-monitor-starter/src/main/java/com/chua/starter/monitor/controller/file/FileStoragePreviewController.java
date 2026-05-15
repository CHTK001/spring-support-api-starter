package com.chua.starter.monitor.controller.file;

import com.chua.common.support.io.binary.ByteSourceArray;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.media.MediaType;
import com.chua.common.support.storage.oss.FileStorage;
import com.chua.common.support.storage.oss.metadata.MapMetadata;
import com.chua.common.support.storage.oss.request.GetObjectRequest;
import com.chua.common.support.storage.oss.request.ListObjectRequest;
import com.chua.common.support.storage.oss.request.PutObjectRequest;
import com.chua.common.support.storage.oss.result.GetObjectResult;
import com.chua.common.support.storage.oss.result.ListObjectResult;
import com.chua.common.support.storage.oss.result.PutObjectResult;
import com.chua.common.support.storage.oss.setting.BucketSetting;
import com.chua.common.support.core.utils.FileUtils;
import com.chua.common.support.core.utils.IoUtils;
import com.chua.common.support.network.protocol.filter.FileStorageServletFilter;
import com.chua.common.support.network.protocol.server.ProtocolServer;
import com.chua.starter.service.utils.FileStorageSupport;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Optional;

import static com.chua.starter.monitor.service.impl.SystemServerServiceImpl.runningServerInstances;

/**
 * 文件存储预览接口
 *
 * 使用 FileStorage#listObject 列出对象，供前端预览（列表/卡片/大图）。
 * 注意：该接口仅做对象列出，不做下载/预览内容。
 *
 * @author CH
 */
@Slf4j
@RestController
@RequestMapping("/file-storage")
@Api(tags = "文件存储预览")
@Tag(name = "文件存储预览")
public class FileStoragePreviewController {

    @Autowired
    private FileStorageSupport fileStorageSupport;

    /**
     * 列出对象（表单提交，便于直接由前端调用）
     * 返回简化后的对象元数据列表，便于前端直接渲染。
     */
    @PostMapping("/list")
    @ApiOperation("列出文件存储对象")
    public ReturnResult<ListObjectResult> list(
            @ApiParam("服务器ID(可选)") @RequestParam(required = false) Integer serverId,
            @ApiParam("存储类型，如 FILESYSTEM/S3/MINIO/ALIYUN 等") @RequestParam String type,
            @ApiParam("端点") @RequestParam(required = false) String endpoint,
            @ApiParam("Bucket") @RequestParam(required = false) String bucket,
            @ApiParam("AccessKey") @RequestParam(required = false, name = "accessKey") String accessKeyId,
            @ApiParam("SecretKey") @RequestParam(required = false, name = "secretKey") String accessKeySecret,
            @ApiParam("区域") @RequestParam(required = false) String region,
            @ApiParam("路径(目录)") @RequestParam(required = false, name = "basePath") String filePath,
            @ApiParam("分页大小") @RequestParam(required = false, defaultValue = "1000") Integer limit,
            @ApiParam("分页游标") @RequestParam(required = false) String marker
    ) {
        try {
            // 构建BucketSetting
            BucketSetting bucketSetting = BucketSetting.builder()
                    .endpoint(endpoint)
                    .bucket(bucket)
                    .accessKeyId(accessKeyId)
                    .accessKeySecret(accessKeySecret)
                    .region(region)
                    .build();

            ProtocolServer protocolServer = runningServerInstances.get(serverId);
            if (protocolServer == null) {
                log.warn("服务未启动, server={}, type={}", serverId, type);
                return ReturnResult.error("服务未启动, 无法创建FileStorage实例");
            }
            FileStorageServletFilter filter = protocolServer.getFilter(FileStorageServletFilter.class);
            FileStorage fileStorage = filter.getFileStorage(bucket);
            // 组装请求
            ListObjectRequest request = ListObjectRequest.builder()
                    .filePath(Optional.ofNullable(filePath).orElse("/"))
                    .marker(marker)
                    .limit(limit == null ? 1000 : Math.max(1, limit))
                    .build();

            // 查询
            ListObjectResult result = fileStorage.listObject(request);
            // 直接返回数组，方便前端 axios 直接使用 res.data 作为数组
            return ReturnResult.of(result);

        } catch (Throwable e) {
            log.error("列出文件存储对象失败", e);
            // 失败时返回空数组，保证前端容错
            return ReturnResult.of(ListObjectResult.EMPTY);
        }
    }

    /**
     * 上传文件到文件存储
     * 支持小文件直接上传（建议不超过10MB）
     *
     * @param serverId 服务器ID
     * @param bucket Bucket名称
     * @param filePath 上传目标路径
     * @param file 上传的文件
     * @return 上传结果
     * @author CH
     * @since 1.0
     */
    @PostMapping("/upload")
    @ApiOperation("上传文件")
    public ReturnResult<PutObjectResult> upload(
            @ApiParam("服务器ID") @RequestParam Integer serverId,
            @ApiParam("Bucket") @RequestParam(required = false) String bucket,
            @ApiParam("上传目标路径") @RequestParam(required = false, defaultValue = "/") String filePath,
            @ApiParam("上传文件") @RequestParam("file") MultipartFile file
    ) {
        // 文件大小限制：10MB
        long maxSize = 10 * 1024 * 1024L;
        if (file.getSize() > maxSize) {
            log.warn("上传文件过大, size={}, maxSize={}", file.getSize(), maxSize);
            return ReturnResult.error("文件大小不能超过10MB，请使用分片上传");
        }

        try {
            log.debug("开始上传文件, serverId={}, bucket={}, filePath={}, fileName={}, size={}",
                    serverId, bucket, filePath, file.getOriginalFilename(), file.getSize());

            ProtocolServer protocolServer = runningServerInstances.get(serverId);
            if (protocolServer == null) {
                log.warn("服务未启动, serverId={}", serverId);
                return ReturnResult.error("服务未启动，无法上传文件");
            }

            FileStorageServletFilter filter = protocolServer.getFilter(FileStorageServletFilter.class);
            FileStorage fileStorage = filter.getFileStorage(bucket);

            if (fileStorage == null) {
                log.warn("无法获取FileStorage实例, bucket={}", bucket);
                return ReturnResult.error("无法获取文件存储实例");
            }

            // 构建上传请求
            PutObjectRequest request = PutObjectRequest.builder()
                    .byteSource(new ByteSourceArray(file.getInputStream()))
                    .fileName(file.getOriginalFilename())
                    .filePath(Optional.ofNullable(filePath).orElse("/"))
                    .metadata(new MapMetadata(Collections.emptyMap()))
                    .build();

            // 执行上传
            PutObjectResult result = fileStorage.putObject(request);
            log.info("文件上传成功, fileName={}, filePath={}", file.getOriginalFilename(), filePath);

            return ReturnResult.of(result);

        } catch (Exception e) {
            log.error("上传文件失败, fileName={}", file.getOriginalFilename(), e);
            return ReturnResult.error("上传文件失败: " + e.getMessage());
        }
    }

    /**
     * 获取文件内容（预览/下载）
     * 通过文件存储系统获取文件内容并返回HTTP流，支持浏览器直接打开预览
     */
    @GetMapping("/file")
    @ApiOperation("获取文件内容")
    public void getFile(
            @ApiParam("存储类型(默认FILESYSTEM)") @RequestParam(required = false, defaultValue = "FILESYSTEM") String type,
            @ApiParam("服务器ID(可选，为空则直接创建存储)") @RequestParam(required = false) Integer serverId,
            @ApiParam("存储Bucket") @RequestParam String bucket,
            @ApiParam("文件路径(key)") @RequestParam String key,
            HttpServletResponse response) {
        try {
            FileStorage fileStorage = resolveFileStorage(serverId, type, bucket);
            if (fileStorage == null) {
                log.warn("文件存储不存在: bucket={}", bucket);
                response.sendError(404, "文件存储不存在: " + bucket);
                return;
            }

            // 获取文件对象
            GetObjectResult getResult = fileStorage.getObject(GetObjectRequest.builder()
                    .fileName(FileUtils.getName(key))
                    .filePath(FileUtils.getPath(key))
                    .build());

            if (!getResult.getResultCode().isSuccessful()) {
                log.warn("获取文件失败: bucket={}, key={}, msg={}", bucket, key, getResult.getMessage());
                response.sendError(400, getResult.getMessage());
                return;
            }

            // 设置Content-Type
            MediaType mediaType = getResult.getMediaType();
            if (mediaType != null) {
                response.setContentType(mediaType.toString());
            }

            // 流式输出文件内容
            try (InputStream is = getResult.getInputStream();
                 OutputStream os = response.getOutputStream()) {
                IoUtils.copy(is, os);
                os.flush();
            }
        } catch (Exception e) {
            log.error("获取文件失败: bucket={}, key={}", bucket, key, e);
            try {
                response.sendError(500, "获取文件失败");
            } catch (IOException ignored) {
                // ignore
            }
        }
    }

    /**
     * 解析文件存储实例
     * 优先从协议服务器获取，若 serverId 为空则通过 FileStorageSupport 直接创建
     */
    private FileStorage resolveFileStorage(Integer serverId, String type, String bucket) {
        if (serverId != null) {
            ProtocolServer protocolServer = runningServerInstances.get(serverId);
            if (protocolServer != null) {
                FileStorageServletFilter filter = protocolServer.getFilter(FileStorageServletFilter.class);
                FileStorage fileStorage = filter.getFileStorage(bucket);
                if (fileStorage != null) {
                    return fileStorage;
                }
            }
        }
        // 回退：通过 FileStorageSupport 直接创建
        if (fileStorageSupport != null) {
            log.debug("通过FileStorageSupport直接创建文件存储: type={}, bucket={}", type, bucket);
            BucketSetting bucketSetting = fileStorageSupport.createUploadBucketSetting(type, bucket, null, null);
            return fileStorageSupport.createStorage(type, bucketSetting);
        }
        // 最后的回退：直接使用 FileStorage API 创建本地存储
        log.debug("直接创建本地文件存储: bucket={}", bucket);
        return FileStorage.createStorage("FILESYSTEM", BucketSetting.builder()
                .bucket(bucket)
                .endpoint("oss/system")
                .accessKeyId("__local__")
                .accessKeySecret("__local__")
                .build());
    }

}
