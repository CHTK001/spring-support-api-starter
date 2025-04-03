package com.chua.starter.video.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.common.support.annotations.DS;
import com.chua.starter.video.entity.VideoDownload;
import com.chua.starter.video.entity.VideoInfo;
import com.chua.starter.video.entity.VideoMark;
import com.chua.starter.video.mapper.VideoDownloadMapper;
import com.chua.starter.video.mapper.VideoInfoMapper;
import com.chua.starter.video.mapper.VideoMarkMapper;
import com.chua.starter.video.pojo.request.VideoSearchRequest;
import com.chua.starter.video.service.VideoInfoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 视频信息服务实现
 *
 * @author CH
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class VideoInfoServiceImpl extends ServiceImpl<VideoInfoMapper, VideoInfo> implements VideoInfoService {

    final VideoDownloadMapper videoDownloadMapper;
    final VideoMarkMapper videoMarkMapper;
    @Override
    @DS("video")
    public IPage<VideoInfo> searchVideos(VideoSearchRequest request) {
        // 使用自定义Mapper方法进行查询
        IPage<VideoInfo> videoInfoIPage = baseMapper.searchVideos(request.createPage(), request);
        List<VideoInfo> records = videoInfoIPage.getRecords();
        if (records.isEmpty()) {
            return videoInfoIPage;
        }

        List<Integer> videoIds = records.stream().map(VideoInfo::getVideoId).toList();
        List<VideoDownload> videoDownloads = videoDownloadMapper.selectList(Wrappers.<VideoDownload>lambdaQuery().in(VideoDownload::getVideoId, videoIds));
        Map<Integer, List<VideoDownload>> videoDownloadMap = videoDownloads.stream().collect(java.util.stream.Collectors.groupingBy(VideoDownload::getVideoId));
        List<VideoMark> videoMarks = videoMarkMapper.selectList(Wrappers.<VideoMark>lambdaQuery().in(VideoMark::getVideoId, videoIds));
        Map<Integer, List<VideoMark>> videoMarkMap = videoMarks.stream().collect(java.util.stream.Collectors.groupingBy(VideoMark::getVideoId));
        records.forEach(videoInfo -> {
            videoInfo.setDownloadList(videoDownloadMap.getOrDefault(videoInfo.getVideoId(), List.of()));
            videoInfo.setVideoMarkList(videoMarkMap.getOrDefault(videoInfo.getVideoId(), List.of()));
        });

        return videoInfoIPage;
    }

    @Override
    public ReturnResult<VideoInfo> getVideoById(String id) {
        VideoInfo videoInfo = this.getById(id);
        if (videoInfo == null) {
            return ReturnResult.error("视频不存在");
        }

        List<VideoDownload> videoDownloads = videoDownloadMapper.selectList(Wrappers.<VideoDownload>lambdaQuery().eq(VideoDownload::getVideoId, id));
        List<VideoMark> videoMarks = videoMarkMapper.selectList(Wrappers.<VideoMark>lambdaQuery().eq(VideoMark::getVideoId, id));
        videoInfo.setDownloadList(videoDownloads);
        videoInfo.setVideoMarkList(videoMarks);
        return ReturnResult.success(videoInfo);
    }

    @Override
    public boolean addVideo(VideoInfo videoInfo) {
        return this.save(videoInfo);
    }

    @Override
    public boolean updateVideo(VideoInfo videoInfo) {
        return this.updateById(videoInfo);
    }

    @Override
    public boolean deleteVideo(String id) {
        return this.removeById(id);
    }
}
