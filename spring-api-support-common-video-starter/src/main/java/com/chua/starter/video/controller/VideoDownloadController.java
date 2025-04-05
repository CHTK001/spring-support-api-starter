package com.chua.starter.video.controller;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.validator.group.AddGroup;
import com.chua.starter.video.entity.VideoDownload;
import com.chua.starter.video.service.VideoDownloadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

import static com.chua.starter.common.support.constant.CacheConstant.REDIS_CACHE_ALWAYS;

/**
 * 视频下载地址控制器
 *
 * @author CH
 */
@RestController
@RequestMapping("v1/video/download")
public class VideoDownloadController {

    @Autowired
    private VideoDownloadService videoDownloadService;


    /**
     * 添加视频下载地址
     *
     * @param videoDownload 视频下载地址信息
     * @return 操作结果
     */
    @PostMapping("save")
    @CacheEvict(cacheManager = REDIS_CACHE_ALWAYS, cacheNames = {"video:info:list", "video:info:detail"}, allEntries = true, key = "#videoDownload.videoId")
    public ReturnResult<Void> add(@Validated(AddGroup.class) @RequestBody VideoDownload videoDownload, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ReturnResult.error(Objects.requireNonNull(bindingResult.getAllErrors().getFirst().getDefaultMessage()));
        }
        videoDownloadService.save(videoDownload);
        return ReturnResult.success();
    }

    /**
     * 更新视频下载地址
     *
     * @param videoDownload 视频下载地址信息
     * @return 操作结果
     */
    @PutMapping("update")
    @CacheEvict(cacheManager = REDIS_CACHE_ALWAYS, cacheNames = {"video:info:list", "video:info:detail"}, allEntries = true, key = "#videoDownload.videoId")
    public ReturnResult<Void> update(@RequestBody VideoDownload videoDownload) {
        videoDownloadService.updateById(videoDownload);
        return ReturnResult.success();
    }

    /**
     * 删除视频下载地址
     *
     * @param id 视频下载地址ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public ReturnResult<Void> delete(@PathVariable Long id) {
        videoDownloadService.removeById(id);
        return ReturnResult.success();
    }

}