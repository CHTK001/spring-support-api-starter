package com.chua.starter.video.controller;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.video.entity.VideoDownload;
import com.chua.starter.video.service.VideoDownloadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
    public ReturnResult<Void> add(@RequestBody VideoDownload videoDownload) {
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