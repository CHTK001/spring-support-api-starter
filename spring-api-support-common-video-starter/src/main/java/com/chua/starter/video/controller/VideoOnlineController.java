package com.chua.starter.video.controller;

import com.chua.common.support.lang.code.ReturnPageResult;
import com.chua.starter.video.pojo.VideoInfoResult;
import com.chua.starter.video.pojo.VideoSearch;
import com.chua.starter.video.service.VideoOnlineService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 在线视频控制器
 *
 * @author CH
 */
@RestController
@RequestMapping("v1/video/online")
@RequiredArgsConstructor
public class VideoOnlineController {

    private final VideoOnlineService videoOnlineService;

    /**
     * 根据视频名称查找视频
     *
     * @param videoSearch 视频名称
     * @return 视频列表
     */
    @PostMapping("/find")
    @Operation(summary = "根据视频名称查找视频")
    public ReturnPageResult<VideoInfoResult> findVideoByVideoName(@RequestBody VideoSearch videoSearch) {
        return videoOnlineService.findVideoByKeyword(videoSearch);
    }

}
