package com.chua.starter.video.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.video.entity.VideoInfo;
import com.chua.starter.video.entity.VideoPlayAddress;
import com.chua.starter.video.pojo.VideoInfoResult;
import com.chua.starter.video.pojo.request.VideoSearchRequest;

import java.util.List;

/**
 * 视频信息服务接口
 *
 * @author CH
 */
public interface VideoInfoService extends IService<VideoInfo> {

    /**
     * 视频搜索与分页查询
     *
     * @param request 搜索参数
     * @return 搜索结果
     */
    IPage<VideoInfoResult> searchVideos(VideoSearchRequest request);

    /**
     * 获取视频详情
     *
     * @param id 视频ID
     * @return 视频详情
     */
    ReturnResult<VideoInfoResult> getVideoById(String id);

    /**
     * 添加视频
     *
     * @param videoInfo 视频信息
     * @return 添加结果
     */
    boolean addVideo(VideoInfo videoInfo);

    /**
     * 修改视频
     *
     * @param videoInfo 视频信息
     * @return 修改结果
     */
    boolean updateVideo(VideoInfo videoInfo);

    /**
     * 删除视频
     *
     * @param id 视频ID
     * @return 删除结果
     */
    boolean deleteVideo(String id);

    /**
     * 获取视频播放地址
     *
     * @param videoId 视频ID
     * @return 播放地址
     */
    List<VideoPlayAddress> playAddress(Integer videoId);
}
