package com.chua.starter.music.service;

import com.chua.starter.music.model.MusicTrackDetail;
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

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;

/**
 * 将外部音频资源统一转成当前服务的 HTTP 流。
 */
@Slf4j
@Service
public class MusicStreamProxyService {

    public ResponseEntity<StreamingResponseBody> proxy(MusicTrackDetail trackDetail, String rangeHeader) {
        if (trackDetail == null || !StringUtils.hasText(trackDetail.getStreamUrl())) {
            return ResponseEntity.notFound().build();
        }

        try {
            HttpURLConnection connection = openConnection(trackDetail.getStreamUrl(), rangeHeader);
            int status = connection.getResponseCode();
            if (status >= HttpStatus.BAD_REQUEST.value()) {
                log.warn("Music stream upstream failed, trackId={}, source={}, status={}",
                        trackDetail.getTrackId(), trackDetail.getSource(), status);
                connection.disconnect();
                return ResponseEntity.status(HttpStatus.BAD_GATEWAY).build();
            }

            HttpHeaders headers = new HttpHeaders();
            copyHeader(connection, headers, HttpHeaders.ACCEPT_RANGES);
            copyHeader(connection, headers, HttpHeaders.CACHE_CONTROL);
            copyHeader(connection, headers, HttpHeaders.CONTENT_DISPOSITION);
            copyHeader(connection, headers, HttpHeaders.CONTENT_RANGE);
            copyHeader(connection, headers, HttpHeaders.ETAG);
            copyHeader(connection, headers, HttpHeaders.LAST_MODIFIED);

            MediaType mediaType = parseMediaType(connection.getContentType());
            if (mediaType != null) {
                headers.setContentType(mediaType);
            }

            long contentLength = connection.getContentLengthLong();
            if (contentLength >= 0) {
                headers.setContentLength(contentLength);
            }

            StreamingResponseBody body = outputStream -> {
                try (InputStream inputStream = connection.getInputStream()) {
                    StreamUtils.copy(inputStream, outputStream);
                    outputStream.flush();
                } finally {
                    connection.disconnect();
                }
            };

            return ResponseEntity.status(HttpStatusCode.valueOf(status))
                    .headers(headers)
                    .body(body);
        } catch (IOException ex) {
            log.warn("Music stream proxy failed, trackId={}, source={}",
                    trackDetail.getTrackId(), trackDetail.getSource(), ex);
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).build();
        }
    }

    private HttpURLConnection openConnection(String streamUrl, String rangeHeader) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) URI.create(streamUrl).toURL().openConnection();
        connection.setConnectTimeout(10_000);
        connection.setReadTimeout(60_000);
        connection.setInstanceFollowRedirects(true);
        connection.setRequestMethod("GET");
        connection.setRequestProperty(HttpHeaders.USER_AGENT, "spring-support-music-proxy/1.0");
        if (StringUtils.hasText(rangeHeader)) {
            connection.setRequestProperty(HttpHeaders.RANGE, rangeHeader);
        }
        return connection;
    }

    private void copyHeader(HttpURLConnection connection, HttpHeaders headers, String headerName) {
        String headerValue = connection.getHeaderField(headerName);
        if (StringUtils.hasText(headerValue)) {
            headers.set(headerName, headerValue);
        }
    }

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
