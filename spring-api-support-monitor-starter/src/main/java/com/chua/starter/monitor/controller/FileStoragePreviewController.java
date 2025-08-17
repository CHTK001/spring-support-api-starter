package com.chua.starter.monitor.controller;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.oss.FileStorage;
import com.chua.common.support.oss.request.ListObjectRequest;
import com.chua.common.support.oss.result.ListObjectResult;
import com.chua.common.support.oss.setting.BucketSetting;
import com.chua.common.support.protocol.filter.FileStorageServletFilter;
import com.chua.common.support.protocol.server.ProtocolServer;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
                return ReturnResult.error("无法创建FileStorage实例");
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
//            return ReturnResult.error("无法创建FileStorage实例");

        } catch (Throwable e) {
            log.error("列出文件存储对象失败", e);
            // 失败时返回空数组，保证前端容错
            return ReturnResult.of(ListObjectResult.EMPTY);
        }
    }
}

