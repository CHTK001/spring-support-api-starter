package com.chua.starter.video.controller;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.video.entity.VideoInfo;
import com.chua.starter.video.service.VideoOnlineService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
     * @param videoName 视频名称
     * @return 视频列表
     */
    @GetMapping("/find")
    public ReturnResult<List<VideoInfo>> findVideoByVideoName(@RequestParam String videoName) {
        return videoOnlineService.findVideoByVideoName(videoName);
    }

}
