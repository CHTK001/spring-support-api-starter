package com.chua.starter.service.controller;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.storage.result.PutObjectResult;
import com.chua.starter.oauth.client.support.annotation.TokenForIgnore;
import com.chua.starter.service.storage.SysLocalFileStorage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 本地文件上传与回显接口
 * 承载头像等小文件的落盘存储：上传需登录态，回显（view）为图片标签的匿名访问开放
 *
 * @author CH
 * @since 4.0.0
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v2/file")
@Tag(name = "本地文件上传接口")
public class SysFileUploadController {

    /** 回显路径前缀，与 view 映射保持一致 */
    private static final String VIEW_PATH = "/v2/file/view/"; // 查看路径

    private final SysLocalFileStorage sysLocalFileStorage; // sys本地文件存储

    /**
     * 上传文件到本地磁盘
     *
     * @param file       文件内容
     * @param fileBucket 存储桶，为空时使用 default
     * @param ossType    存储类型，仅支持本地(FILESYSTEM)
     * @param request    HTTP请求对象，用于拼接回显URL
     * @return 上传结果，data 内含 url 与 key
     */
    @Operation(summary = "上传文件")
    @RequestMapping(value = "/upload", method = {RequestMethod.PUT, RequestMethod.POST})
    public ReturnResult<PutObjectResult> upload(
            @RequestPart(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "fileBucket", required = false) String fileBucket,
            @RequestParam(value = "ossType", required = false) String ossType,
            HttpServletRequest request) {
        if (null == file || file.isEmpty()) {
            return ReturnResult.illegal("文件不能为空");
        }
        if (StringUtils.hasText(ossType) && !isLocalOssType(ossType)) {
            return ReturnResult.illegal("暂不支持的存储类型: " + ossType);
        }
        if (file.getSize() > sysLocalFileStorage.maxBytes()) {
            return ReturnResult.illegal("文件大小超过上限 " + (sysLocalFileStorage.maxBytes() / 1024 / 1024) + "MB");
        }
        String contentType = StringUtils.hasText(file.getContentType()) ? file.getContentType().toLowerCase() : "";
        if (!contentType.startsWith("image/")) {
            return ReturnResult.illegal("仅支持图片文件");
        }
        String extension = sysLocalFileStorage.resolveExtension(file.getOriginalFilename());
        if (null == extension) {
            return ReturnResult.illegal("不支持的文件类型");
        }

        String bucket = sysLocalFileStorage.normalizeBucket(fileBucket);
        try {
            String fileName = sysLocalFileStorage.put(bucket, extension, file.getBytes());
            String url = request.getContextPath() + VIEW_PATH + bucket + "/" + fileName;
            PutObjectResult result = PutObjectResult.builder()
                    .url(url)
                    .key(bucket + "/" + fileName)
                    .build();
            return ReturnResult.ok(result);
        } catch (IOException e) {
            log.error("文件上传失败: bucket={}", bucket, e);
            return ReturnResult.error("文件保存失败");
        }
    }

    /**
     * 回显已上传的文件，供 img 标签匿名加载
     *
     * @param bucket   存储桶
     * @param fileName 服务端生成的文件名
     * @return 文件字节流
     */
    @Operation(summary = "查看文件")
    @TokenForIgnore
    @GetMapping("/view/{bucket}/{fileName}")
    public void view(@PathVariable("bucket") String bucket,
                     @PathVariable("fileName") String fileName,
                     HttpServletResponse response) throws IOException {
        Path path = sysLocalFileStorage.resolve(sysLocalFileStorage.normalizeBucket(bucket), fileName);
        if (null == path) {
            response.setStatus(HttpStatus.NOT_FOUND.value());
            return;
        }
        byte[] body = Files.readAllBytes(path);
        response.setStatus(HttpStatus.OK.value());
        response.setContentType(resolveMediaType(fileName).toString());
        response.setContentLength(body.length);
        response.setHeader(HttpHeaders.CACHE_CONTROL, "private, max-age=31536000, immutable");
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.getOutputStream().write(body);
        response.flushBuffer();
    }

    /**
     * 按文件扩展名映射响应类型，未命中时退回下载语义
     *
     * @param fileName 文件名
     * @return MediaType
     */
    private MediaType resolveMediaType(String fileName) {
        String extension = StringUtils.getFilenameExtension(fileName);
        if (null == extension) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
        return switch (extension.toLowerCase()) {
            case "jpg", "jpeg" -> MediaType.IMAGE_JPEG;
            case "png" -> MediaType.IMAGE_PNG;
            case "gif" -> MediaType.IMAGE_GIF;
            case "bmp" -> MediaType.parseMediaType("image/bmp");
            case "webp" -> MediaType.parseMediaType("image/webp");
            default -> MediaType.APPLICATION_OCTET_STREAM;
        };
    }

    /**
     * 是否本地存储类型
     *
     * @param ossType 存储类型
     * @return 是否本地存储
     */
    private boolean isLocalOssType(String ossType) {
        String lowered = ossType.toLowerCase();
        return lowered.contains("filesystem") || lowered.contains("local");
    }
}
