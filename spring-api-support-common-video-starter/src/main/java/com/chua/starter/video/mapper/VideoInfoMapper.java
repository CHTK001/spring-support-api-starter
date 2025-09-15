package com.chua.starter.video.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chua.starter.video.entity.VideoInfo;
import com.chua.starter.video.pojo.VideoInfoResult;
import com.chua.starter.video.pojo.request.VideoSearchRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 视频信息Mapper
 *
 * @author CH
 */
@Mapper
public interface VideoInfoMapper extends BaseMapper<VideoInfo> {

    /**
     * 搜索视频
     *
     * @param page    分页参数
     * @param request 搜索请求
     * @return 视频列表
     */
    IPage<VideoInfoResult> searchVideos(Page<VideoInfo> page, @Param("request") VideoSearchRequest request);
}