package com.chua.starter.video.live.controller;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.video.live.model.LiveChannel;
import com.chua.starter.video.live.model.LiveGroup;
import com.chua.starter.video.live.service.HlsProxyService;
import com.chua.starter.video.live.service.LiveChannelService;
import com.chua.starter.video.properties.VideoProperties;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 直播频道接口。
 *
 * <p>提供直播频道目录（央视 / 卫视 / 特色）以及可直连浏览器的 HLS 反向代理。</p>
 *
 * @author CH
 * @since 4.0.0.42
 */
@RestController
@RequestMapping("/video/api/live")
@Tag(name = "Live Channel", description = "Live TV channel catalog and HLS proxy")
@RequiredArgsConstructor
public class LiveChannelController {

    /**
     * 直播频道目录服务。
     */
    private final LiveChannelService liveChannelService;

    /**
     * HLS 反向代理服务。
     */
    private final HlsProxyService hlsProxyService;

    /**
     * 直播分组列表。
     *
     * @return 分组列表
     */
    @Operation(summary = "List live channel groups")
    @GetMapping("/groups")
    public ReturnResult<List<LiveGroup>> groups() {
        return ReturnResult.ok(liveChannelService.groups());
    }

    /**
     * 直播频道列表。
     *
     * @param group 分组标识，可空
     * @param cctv  仅央视分组，可空
     * @return 频道列表
     */
    @Operation(summary = "List live channels")
    @GetMapping("/channels")
    public ReturnResult<List<LiveChannel>> channels(
            @RequestParam(required = false) String group,
            @RequestParam(required = false) Boolean cctv) {
        return ReturnResult.ok(liveChannelService.channels(group, cctv));
    }

    /**
     * 直播频道详情。
     *
     * @param id 频道标识
     * @return 频道详情
     */
    @Operation(summary = "Get live channel detail")
    @GetMapping("/channel/{id}")
    public ReturnResult<LiveChannel> channel(@PathVariable String id) {
        LiveChannel channel = liveChannelService.get(id);
        if (channel == null) {
            return ReturnResult.error("直播频道不存在: " + id);
        }
        return ReturnResult.ok(channel);
    }

    /**
     * 频道主播放列表（重写后的代理地址）。
     *
     * @param id  频道标识
     * @param src 候选源下标，默认 0
     * @return m3u8 内容
     */
    @Operation(summary = "Proxy live m3u8 playlist")
    @GetMapping("/{id}/play.m3u8")
    public ResponseEntity<byte[]> play(@PathVariable String id,
                                       @RequestParam(defaultValue = "0") int src) {
        return toResponse(hlsProxyService.playlist(id, src));
    }

    /**
     * 代理子清单或分片。
     *
     * @param id    频道标识
     * @param src   候选源下标，默认 0
     * @param url   目标地址（Base64URL）
     * @param range Range 头，可空
     * @return 资源内容
     */
    @Operation(summary = "Proxy live HLS resource")
    @GetMapping("/{id}/resource")
    public ResponseEntity<byte[]> resource(@PathVariable String id,
                                           @RequestParam(defaultValue = "0") int src,
                                           @RequestParam("u") String url,
                                           @RequestHeader(value = HttpHeaders.RANGE, required = false) String range) {
        return toResponse(hlsProxyService.resource(id, src, url, range));
    }

    /**
     * 代理响应转 HTTP 响应。
     *
     * @param response 代理响应
     * @return HTTP 响应
     */
    private ResponseEntity<byte[]> toResponse(HlsProxyService.ProxyResponse response) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_TYPE,
                response.getContentType() == null ? "application/octet-stream" : response.getContentType());
        headers.set(HttpHeaders.CACHE_CONTROL, "no-cache");
        headers.set(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
        if (response.getContentRange() != null) {
            headers.set(HttpHeaders.CONTENT_RANGE, response.getContentRange());
        }
        HttpStatus status = HttpStatus.resolve(response.getStatus());
        return new ResponseEntity<>(response.getBody(),
                headers, status == null ? HttpStatus.BAD_GATEWAY : status);
    }
}
