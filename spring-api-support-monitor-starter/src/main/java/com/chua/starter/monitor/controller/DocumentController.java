package com.chua.starter.monitor.controller;

import com.chua.common.support.utils.MapUtils;
import com.chua.starter.common.support.annotations.ReturnOrigin;
import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.util.Map;

/**
 * 文档转发
 *
 * @author CH
 */
@RestController
@Slf4j
@Api(tags = "文档转发接口")
@Tag(name = "文档转发接口")
@RequestMapping("/v2/document")
@RequiredArgsConstructor
@SuppressWarnings("ALL")
public class DocumentController {

    private final RestTemplate restTemplate;

    /**
     * 转发文档请求（POST）
     *
     * @param remoteUrl 远程地址
     * @param request   请求参数
     * @return 响应结果
     */
    @PostMapping("/forward")
    @ReturnOrigin
    @Operation(summary = "文档请求转发（POST）")
    public ResponseEntity<byte[]> forwardDocument(@RequestParam String remoteUrl, @RequestBody Object request) {
        // 发送POST请求到远程地址
        Map headers = (Map) MapUtils.getObject((Map) request, "headers");
        Object data = MapUtils.getObject((Map) request, "data");
        Object params = MapUtils.getObject((Map) request, "params");
        MultiValueMap<String, String> headers1 = new LinkedMultiValueMap<>();
        headers.forEach((key, value) -> headers1.add(key.toString(), (String) value));
        HttpEntity<Object> request1 = new HttpEntity<>(data == null ? new byte[0] : data, headers1);
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(remoteUrl);
        ((Map) params).forEach((key, value) -> uriComponentsBuilder.queryParam(key.toString(), value));
        URI build = uriComponentsBuilder.encode().build().toUri();
        ResponseEntity<byte[]> responseEntity = restTemplate.exchange(
                build,
                HttpMethod.valueOf(MapUtils.getString(request, "method").toUpperCase()),
                request1, byte[].class);

        return ResponseEntity.status(responseEntity.getStatusCode())
                .headers(responseEntity.getHeaders())
                .body(responseEntity.getBody());
    }

    /**
     * 转发文档请求（GET）
     *
     * @param remoteUrl 远程地址
     * @param params    GET参数
     * @return 响应结果
     */
    @GetMapping("/forward")
    @Operation(summary = "文档请求转发（GET）")
    public ResponseEntity<byte[]> forwardDocumentGet(@RequestParam String remoteUrl, @RequestParam Map<String, String> params) {
        // 构建请求URL with parameters
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(remoteUrl);

        // 添加GET参数
        params.forEach(builder::queryParam);

        // 发送GET请求
        ResponseEntity<byte[]> responseEntity = restTemplate.getForEntity(builder.toUriString(), byte[].class);

        return ResponseEntity.status(responseEntity.getStatusCode())
                .headers(responseEntity.getHeaders())
                .body(responseEntity.getBody());
    }

    /**
     * 转发文档请求（DELETE）
     *
     * @param remoteUrl 远程地址
     * @param params    DELETE参数
     * @return 响应结果
     */
    @DeleteMapping("/forward")
    @Operation(summary = "文档请求转发（DELETE）")
    public ResponseEntity<byte[]> forwardDocumentDelete(@RequestParam String remoteUrl, @RequestParam Map<String, String> params) {
        // 构建请求URL with parameters
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(remoteUrl);

        // 添加DELETE参数
        params.forEach((key, value) -> builder.queryParam(key, value));

        // 创建DELETE请求
        RequestEntity<Void> requestEntity = RequestEntity.delete(builder.build().encode().toUri()).build();

        // 发送DELETE请求
        ResponseEntity<byte[]> responseEntity = restTemplate.exchange(requestEntity, byte[].class);

        return ResponseEntity.status(responseEntity.getStatusCode())
                .headers(responseEntity.getHeaders())
                .body(responseEntity.getBody());
    }

    /**
     * 转发文档请求（PUT）
     *
     * @param remoteUrl 远程地址
     * @param request   请求参数
     * @return 响应结果
     */
    @PutMapping("/forward")
    @Operation(summary = "文档请求转发（PUT）")
    public ResponseEntity<byte[]> forwardDocumentPut(@RequestParam String remoteUrl, @RequestBody Object request) {
        // 创建PUT请求
        RequestEntity<Object> requestEntity = RequestEntity.put(remoteUrl).body(request);

        // 发送PUT请求
        ResponseEntity<byte[]> responseEntity = restTemplate.exchange(requestEntity, byte[].class);

        return ResponseEntity.status(responseEntity.getStatusCode())
                .headers(responseEntity.getHeaders())
                .body(responseEntity.getBody());
    }

    /**
     * 转发文件上传请求
     *
     * @param remoteUrl 远程地址
     * @param file      上传的文件
     * @param params    其他参数
     * @return 响应结果
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "文件上传转发")
    public ResponseEntity<byte[]> forwardUpload(@RequestParam String remoteUrl, @RequestParam("file") MultipartFile file, @RequestParam Map<String, String> params) {
        try {
            // 创建多部分请求体
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

            // 添加文件参数
            body.add("file", new ByteArrayResource(file.getBytes()));

            // 添加其他参数
            params.forEach((key, value) -> body.add(key, value));

            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            // 创建请求实体
            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            // 发送POST请求
            ResponseEntity<byte[]> responseEntity = restTemplate.postForEntity(remoteUrl, requestEntity, byte[].class);

            return ResponseEntity.status(responseEntity.getStatusCode())
                    .headers(responseEntity.getHeaders())
                    .body(responseEntity.getBody());
        } catch (IOException e) {
            log.error("文件上传转发失败: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}