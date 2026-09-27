package com.chua.starter.video.controller;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.video.live.service.LiveChannelService;
import com.chua.starter.video.setting.VideoConfigSnapshot;
import com.chua.starter.video.setting.VideoSettingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 视频配置接口。
 *
 * <p>视频/直播配置存储于配置数据库（{@code sys_setting}，group=video），由
 * {@code /v2/setting} 管理端维护；本控制器提供当前生效配置的读取与热加载。</p>
 *
 * @author CH
 * @since 4.0.0.42
 */
@RestController
@RequestMapping("/video/api/config")
@Tag(name = "Video Config", description = "Video configuration read and reload")
@RequiredArgsConstructor
public class VideoConfigController {

    /**
     * 视频配置数据库服务。
     */
    private final VideoSettingService videoSettingService;

    /**
     * 直播频道目录（直播关闭时为空）。
     */
    private final LiveChannelService liveChannelService;

    /**
     * 当前生效配置概览。
     *
     * @return 配置概览
     */
    @Operation(summary = "Get effective video config")
    @GetMapping
    public ReturnResult<Map<String, Object>> current() {
        Map<String, Object> config = new LinkedHashMap<>();
        config.put("storage", videoSettingService.available() ? "db" : "json");
        config.put("catalogUrl", videoSettingService.resolve(VideoSettingService.NAME_CATALOG_URL, null));
        config.put("defaultKeyword",
                videoSettingService.resolve(VideoSettingService.NAME_DEFAULT_KEYWORD, null));
        config.put("browseKeywords",
                videoSettingService.resolveMap(VideoSettingService.NAME_BROWSE_KEYWORDS, Map.of()));
        config.put("liveChannelCount",
                liveChannelService == null ? 0 : liveChannelService.channels(null, null).size());
        return ReturnResult.ok(config);
    }

    /**
     * 导出视频配置快照（标量 + 整段直播目录），用于备份或跨环境迁移。
     *
     * @return 配置快照
     */
    @Operation(summary = "Export video config snapshot")
    @GetMapping("/export")
    public ReturnResult<VideoConfigSnapshot> exportConfig() {
        return ReturnResult.ok(videoSettingService.exportConfig());
    }

    /**
     * 导入视频配置快照并热加载。
     *
     * @param snapshot  配置快照
     * @param overwrite 是否覆盖已存在配置项，默认 true
     * @return 是否成功
     */
    @Operation(summary = "Import video config snapshot")
    @PostMapping("/import")
    public ReturnResult<Boolean> importConfig(@RequestBody VideoConfigSnapshot snapshot,
                                              @RequestParam(defaultValue = "true") boolean overwrite) {
        if (!videoSettingService.available()) {
            return ReturnResult.error("配置数据库不可用，无法导入");
        }
        boolean success = videoSettingService.importConfig(snapshot, overwrite);
        if (success && liveChannelService != null) {
            liveChannelService.reload();
        }
        return ReturnResult.ok(success);
    }

    /**
     * 热加载配置（配置数据库变更后调用）。
     *
     * @return 是否成功
     */
    @Operation(summary = "Reload video config from database")
    @PostMapping("/reload")
    public ReturnResult<Boolean> reload() {
        videoSettingService.reload();
        if (liveChannelService != null) {
            liveChannelService.reload();
        }
        return ReturnResult.ok(true);
    }
}
