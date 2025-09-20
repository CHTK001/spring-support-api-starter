package com.chua.starter.video.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.lang.code.ListReturnResult;
import com.chua.starter.video.entity.VideoDownload;
import com.chua.starter.video.entity.VideoInfo;
import com.chua.starter.video.mapper.VideoDownloadMapper;
import com.chua.starter.video.pojo.FunctionType;
import com.chua.starter.video.provider.DownloadLinkProvider;
import com.chua.starter.video.provider.ResourceProvider;
import com.chua.starter.video.service.VideoDownloadService;
import com.chua.starter.video.service.VideoSourceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 视频下载服务实现
 *
 * @author CH
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class VideoDownloadServiceImpl extends ServiceImpl<VideoDownloadMapper, VideoDownload> implements VideoDownloadService {


    final VideoSourceService videoSourceService;
    @Override
    public List<VideoDownload> getDownloadsByVideoId(String videoId) {
        return baseMapper.selectList(Wrappers.<VideoDownload>lambdaQuery().eq(VideoDownload::getVideoId, videoId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public VideoDownload saveDownload(VideoInfo videoInfo, String downloadUrl, String downloadType, String quality) {
        // 检查是否已存在相同URL的下载记录
        LambdaQueryWrapper<VideoDownload> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(VideoDownload::getVideoId, videoInfo.getVideoId())
                .eq(VideoDownload::getVideoDownloadUrl, downloadUrl);

        VideoDownload existingDownload = this.getOne(queryWrapper);

        if (existingDownload != null) {
            // 更新已存在的记录
            existingDownload.setVideoDownloadType(downloadType);
            existingDownload.setVideoDownloadQuality(quality);
            existingDownload.setVideoDownloadPlatform(videoInfo.getVideoPlatform());
            this.updateById(existingDownload);
            return existingDownload;
        } else {
            // 创建新记录
            VideoDownload download = new VideoDownload();
            download.setVideoId(videoInfo.getVideoId());
            download.setVideoDownloadUrl(downloadUrl);
            download.setVideoDownloadType(downloadType);
            download.setVideoDownloadPlatform(videoInfo.getVideoPlatform());
            download.setVideoDownloadQuality(quality);

            this.save(download);
            return download;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchSaveDownloads(List<VideoDownload> downloads) {
        if (downloads == null || downloads.isEmpty()) {
            return false;
        }

        try {
            return saveBatch(downloads);
        } catch (Exception e) {
            log.error("批量保存视频下载信息失败", e);
            return false;
        }
    }

    @Override
    public boolean updateDownloadStatus(String downloadId, Integer status) {
        VideoDownload download = this.getById(downloadId);
        if (download != null) {
            return this.updateById(download);
        }
        return false;
    }

    @Override
    public ListReturnResult<String> searchDownloadUrls(String keyword) {
        if (keyword == null) {
            return ListReturnResult.empty();
        }

        List<ResourceProvider> availableProviders = videoSourceService.getAvailableProviders(FunctionType.SEARCH);
        ListReturnResult<String> listReturnResult = new ListReturnResult<>();
        for (ResourceProvider resourceProvider : availableProviders) {
            if (resourceProvider instanceof DownloadLinkProvider downloadLinkProvider) {
                ListReturnResult<String> searchDownloadUrls = downloadLinkProvider.searchDownloadUrls(keyword);
                listReturnResult.addAll(searchDownloadUrls.getData());
            }
        }
        return listReturnResult;
    }
}