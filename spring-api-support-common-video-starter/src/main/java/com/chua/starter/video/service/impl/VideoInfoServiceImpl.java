package com.chua.starter.video.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.bean.BeanUtils;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.datasource.annotation.DS;
import com.chua.starter.video.entity.*;
import com.chua.starter.video.mapper.*;
import com.chua.starter.video.pojo.VideoInfoResponse;
import com.chua.starter.video.pojo.VideoInfoResult;
import com.chua.starter.video.pojo.request.VideoSearchRequest;
import com.chua.starter.video.service.VideoInfoService;
import com.chua.starter.video.service.VideoPlayAddressChannelService;
import com.chua.starter.video.service.VideoPlayAddressService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
    final VideoSyncConfigMapper videoSyncConfigMapper;
    final VideoSyncConfigRecordMapper videoSyncConfigRecordMapper;
    final VideoPlayAddressService videoPlayAddressService;
    final VideoPlayAddressChannelService videoPlayAddressChannelService;

    @Override
    @DS("video")
    public IPage<VideoInfoResponse> searchVideos(VideoSearchRequest request) {
        // 使用自定义Mapper方法进行查询
        IPage<VideoInfoResponse> searchVideos = baseMapper.searchVideos(request.createPage(), request);
        List<VideoInfoResponse> records = searchVideos.getRecords();
        if (records.isEmpty()) {
            return searchVideos;
        }

        List<Integer> videoIds = records.stream().map(VideoInfo::getVideoId).toList();
        List<VideoMark> videoMarks = videoMarkMapper.selectList(Wrappers.<VideoMark>lambdaQuery().in(VideoMark::getVideoId, videoIds));
        Map<Integer, List<VideoMark>> videoMarkMap = videoMarks.stream().collect(java.util.stream.Collectors.groupingBy(VideoMark::getVideoId));

        records.forEach(videoInfo -> {
            videoInfo.setVideoMarkList(videoMarkMap.getOrDefault(videoInfo.getVideoId(), List.of()));
        });

        return searchVideos;
    }

    @Override
    public ReturnResult<VideoInfoResult> getVideoById(String id) {
        VideoInfo videoInfo = this.getById(id);
        if (videoInfo == null) {
            return ReturnResult.error("视频不存在");
        }

        VideoInfoResult videoInfoResult = new VideoInfoResult();
        BeanUtils.copyProperties(videoInfo, videoInfoResult);
        List<VideoDownload> videoDownloads = videoDownloadMapper.selectList(Wrappers.<VideoDownload>lambdaQuery().eq(VideoDownload::getVideoId, id));
        List<VideoMark> videoMarks = videoMarkMapper.selectList(Wrappers.<VideoMark>lambdaQuery().eq(VideoMark::getVideoId, id));
        videoInfoResult.setDownloadList(videoDownloads);
        videoInfoResult.setVideoMarkList(videoMarks);
        return ReturnResult.success(videoInfoResult);
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

    @Override
    public List<VideoPlayAddress> playAddress(Integer videoId) {
        List<VideoPlayAddress> list = videoPlayAddressService.list(Wrappers.lambdaQuery(VideoPlayAddress.class).eq(VideoPlayAddress::getVideoId, videoId));
        Set<Integer> ids = list.stream().map(VideoPlayAddress::getVideoPlayAddressId).collect(Collectors.toSet());
        List<VideoPlayAddressChannel> list1 = videoPlayAddressChannelService.list(Wrappers.lambdaQuery(VideoPlayAddressChannel.class).in(VideoPlayAddressChannel::getVideoPlayAddressId, ids));
        list.forEach(item -> item.setVideoPlayAddressChannels(list1.stream().filter(item1 -> item1.getVideoPlayAddressId().equals(item.getVideoPlayAddressId())).collect(Collectors.toList())));
        return list;
    }
}
