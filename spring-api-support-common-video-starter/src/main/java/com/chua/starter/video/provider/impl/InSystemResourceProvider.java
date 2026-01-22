package com.chua.starter.video.provider.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chua.common.support.core.annotation.Spi;
import com.chua.common.support.core.annotation.SpiDefault;
import com.chua.common.support.lang.code.ReturnPageResult;
import com.chua.common.support.objects.annotation.AutoInject;
import com.chua.common.support.core.utils.StringUtils;
import com.chua.starter.mybatis.utils.ReturnPageResultUtils;
import com.chua.starter.video.entity.VideoInfo;
import com.chua.starter.video.entity.VideoSource;
import com.chua.starter.video.mapper.VideoInfoMapper;
import com.chua.starter.video.pojo.VideoInfoResult;
import com.chua.starter.video.pojo.VideoSearch;

/**
 * @author CH
 * @since 2025/9/19 10:03
 */
@Spi("default")
@SpiDefault
public class InSystemResourceProvider extends AbstractResourceProvider {

    @AutoInject
    VideoInfoMapper videoInfoMapper;

    public InSystemResourceProvider(VideoSource videoSource) {
        super(videoSource);
    }

    @Override
    public ReturnPageResult<VideoInfoResult> searchResource(VideoSearch videoSearch) {
        return ReturnPageResultUtils.ok(
                videoInfoMapper.selectPage(
                        new Page<>(videoSearch.getPage(), videoSearch.getPageSize()),
                        createWrapper(videoSearch)
                ),
                VideoInfoResult.class
        );
    }

    /**
     * 创建查询条件
     * <pre>
     * { value: "original_title.asc", label: "标题升序" },
     * { value: "original_title.desc", label: "标题降序" },
     * { value: "popularity.asc", label: "受欢迎程度升序" },
     * { value: "popularity.desc", label: "受欢迎程度降序" },
     * { value: "revenue.asc", label: "票房升序" },
     * { value: "revenue.desc", label: "票房降序" },
     * { value: "primary_release_date.asc", label: "上映日期升序" },
     * { value: "primary_release_date.desc", label: "上映日期降序" },
     * { value: "vote_count.asc", label: "评分升序" },
     * { value: "vote_count.desc", label: "评分降序" },
     * </pre>
     *
     * @param videoSearch
     * @return
     */
    private Wrapper<VideoInfo> createWrapper(VideoSearch videoSearch) {
        LambdaQueryWrapper<VideoInfo> eq = Wrappers.lambdaQuery(VideoInfo.class)
                .like(StringUtils.isNotBlank(videoSearch.getKeyword()), VideoInfo::getVideoName, videoSearch.getKeyword())
                .eq(StringUtils.isNotBlank(videoSearch.getVideoType()), VideoInfo::getVideoType, videoSearch.getVideoType())
                .eq(StringUtils.isNotBlank(videoSearch.getPlatform()), VideoInfo::getVideoPlatform, videoSearch.getPlatform())
                .eq(StringUtils.isNotBlank(videoSearch.getCategory()), VideoInfo::getVideoCategory, videoSearch.getCategory())
                .eq(null != videoSearch.getYear(), VideoInfo::getVideoYear, videoSearch.getYear());


        String order = videoSearch.getOrder();
        if ("original_title.asc".equals(order)) {
            eq.orderByAsc(VideoInfo::getVideoTitle);
            return eq;
        }

        if ("original_title.desc".equals(order)) {
            eq.orderByDesc(VideoInfo::getVideoTitle);
            return eq;
        }

        if ("popularity.asc".equals(order)) {
            eq.orderByAsc(VideoInfo::getVideoPopularity);
            return eq;
        }

        if ("popularity.desc".equals(order)) {
            eq.orderByDesc(VideoInfo::getVideoPopularity);
            return eq;
        }

        if ("primary_release_date.asc".equals(order)) {
            eq.orderByAsc(VideoInfo::getVideoRelease);
            return eq;
        }

        if ("primary_release_date.desc".equals(order)) {
            eq.orderByDesc(VideoInfo::getVideoRelease);
            return eq;
        }

        if ("vote_count.asc".equals(order)) {
            eq.orderByAsc(VideoInfo::getVideoScore);
            return eq;
        }

        if ("vote_count.desc".equals(order)) {
            eq.orderByDesc(VideoInfo::getVideoScore);
            return eq;
        }
        return eq;
    }
}
