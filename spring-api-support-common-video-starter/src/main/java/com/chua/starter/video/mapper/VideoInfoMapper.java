package com.chua.starter.video.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chua.starter.video.entity.VideoInfo;
import com.chua.starter.video.pojo.VideoInfoResponse;
import com.chua.starter.video.pojo.request.VideoSearchRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @author CH
 * @since 2025/9/16 17:35
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
    IPage<VideoInfoResponse> searchVideos(Page<VideoInfo> page, @Param("request") VideoSearchRequest request);

    /**
     * 按分类统计
     *
     * @return 分类统计列表
     */
    List<Map<String, Object>> countByCategory();

    /**
     * 按年份统计
     *
     * @return 年份统计列表
     */
    List<Map<String, Object>> countByYear();

    /**
     * 按地区统计
     *
     * @return 地区统计列表
     */
    List<Map<String, Object>> countByDistrict();

    /**
     * 按类型统计
     *
     * @return 类型统计列表
     */
    List<Map<String, Object>> countByType();

    /**
     * 最近7天新增统计
     *
     * @return 每日新增统计
     */
    List<Map<String, Object>> countRecentDaily();

    /**
     * 播放量前十
     *
     * @return 播放量前十视频
     */
    List<Map<String, Object>> selectTopViewed();

    /**
     * 评分前十
     *
     * @return 评分前十视频
     */
    List<Map<String, Object>> selectTopRated();
}