package com.chua.starter.music.service;

import com.chua.protocol.support.network.http.HttpClient;
import com.chua.protocol.support.network.http.HttpResponse;
import com.chua.data.support.music.model.MusicTrackDetail;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.util.Optional;

/**
* 将外部音频资源统一转成当前服务的 HTTP 流。
* @author CH
* @since 4.0.0
 */
@Slf4j
@Service
public class MusicStreamProxyService {

    /**
    * 代理
    *
    * @param trackDetail trackDetail
    * @param rangeHeader 范围头部
    * @return 代理的结果
     */
    public ResponseEntity<StreamingResponseBody> proxy(MusicTrackDetail trackDetail, String rangeHeader) {
        if (trackDetail == null || !StringUtils.hasText(trackDetail.getStreamUrl())) {
            return ResponseEntity.notFound().build();
        }

        HttpResponse response;
        try {
            response = HttpClient.get(trackDetail.getStreamUrl())
                    .header(HttpHeaders.USER_AGENT, "spring-support-music-proxy/1.0")
                    .header(HttpHeaders.ACCEPT_ENCODING, "identity")
                    .header(StringUtils.hasText(rangeHeader), HttpHeaders.RANGE, rangeHeader)
                    .connectTimeout(10_000)
                    .readTimeout(60_000)
                    .retry(0)
                    .execute();
        } catch (Exception ex) {
            log.warn("Music stream upstream call failed, trackId={}, source={}, message={}",
                    trackDetail.getTrackId(), trackDetail.getSource(), ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).build();
        }
        int status = response.code();
        if (status >= HttpStatus.BAD_REQUEST.value()) {
            log.warn("Music stream upstream failed, trackId={}, source={}, status={}",
                    trackDetail.getTrackId(), trackDetail.getSource(), status);
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).build();
        }

        HttpHeaders headers = new HttpHeaders();
        copyHeader(response, headers, HttpHeaders.ACCEPT_RANGES);
        copyHeader(response, headers, HttpHeaders.CACHE_CONTROL);
        copyHeader(response, headers, HttpHeaders.CONTENT_DISPOSITION);
        copyHeader(response, headers, HttpHeaders.CONTENT_RANGE);
        copyHeader(response, headers, HttpHeaders.ETAG);
        copyHeader(response, headers, HttpHeaders.LAST_MODIFIED);

        MediaType mediaType = parseMediaType(response.header(HttpHeaders.CONTENT_TYPE));
        if (mediaType != null) {
            headers.setContentType(mediaType);
        }

        byte[] bodyBytes = response.getBodyBytes();
        if (bodyBytes == null || bodyBytes.length == 0) {
            log.warn("Music stream upstream returned empty body, trackId={}, source={}, status={}",
                    trackDetail.getTrackId(), trackDetail.getSource(), status);
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).build();
        }

        long contentLength = parseContentLength(Optional.ofNullable(response.header(HttpHeaders.CONTENT_LENGTH)));
        if (contentLength >= 0) {
            headers.setContentLength(contentLength);
        } else {
            headers.setContentLength(bodyBytes.length);
        }

        StreamingResponseBody body = outputStream -> {
            StreamUtils.copy(bodyBytes, outputStream);
            outputStream.flush();
        };

        return ResponseEntity.status(HttpStatusCode.valueOf(status))
                .headers(headers)
                .body(body);
    }

    /**
    * 复制头部
    *
    * @param response 响应
    * @param headers 头部
    * @param headerName 头部名称
     */
    private void copyHeader(HttpResponse response, HttpHeaders headers, String headerName) {
        String headerValue = response.header(headerName);
        if (StringUtils.hasText(headerValue)) {
            headers.set(headerName, headerValue);
        }
    }

    /**
    * 解析内容获取长度
    *
    * @param contentLength 内容长度
    * @return 解析内容长度的结果
     */
    private long parseContentLength(Optional<String> contentLength) {
        if (contentLength.isEmpty() || !StringUtils.hasText(contentLength.get())) {
            return -1L;
        }
        try {
            return Long.parseLong(contentLength.get());
        } catch (NumberFormatException ignored) {
            return -1L;
        }
    }

    /**
    * 解析media类型
    *
    * @param contentType 内容类型
    * @return 解析media类型的结果
     */
    private MediaType parseMediaType(String contentType) {
        if (!StringUtils.hasText(contentType)) {
            return null;
        }
        try {
            return MediaType.parseMediaType(contentType);
        } catch (Exception ignored) {
            return null;
        }
    }
}

