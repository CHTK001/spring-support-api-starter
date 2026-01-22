package com.chua.starter.video.provider.impl;

import com.chua.common.support.core.annotation.Spi;
import com.chua.common.support.network.http.HttpClient;
import com.chua.common.support.network.http.HttpResponse;
import com.chua.common.support.lang.code.ReturnPageResult;
import com.chua.common.support.time.date.DateUtils;
import com.chua.starter.mybatis.utils.ReturnPageResultUtils;
import com.chua.starter.video.entity.VideoMark;
import com.chua.starter.video.entity.VideoSource;
import com.chua.starter.video.pojo.VideoInfoResult;
import com.chua.starter.video.pojo.VideoSearch;
import com.chua.starter.video.provider.data.TmdbGenre;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * TMDB接口
 *
 * @author CH
 * @since 2025/9/16 16:26
 */
@Spi("tmdb")
public class TmdbResourceProvider extends AbstractResourceProvider {
    static final String IMAGE_URL = "https://image.tmdb.org/t/p/w500";

    public TmdbResourceProvider(VideoSource videoSource) {
        super(videoSource);
    }

    @Override
    public ReturnPageResult<VideoInfoResult> searchResource(VideoSearch videoSearch) {
        HttpResponse httpResponse = HttpClient.get(videoSource.getVideoSourceUrl())
                .json()
                .query("include_adult", true)
                .query("include_video", true)
                .query("page", videoSearch.getPage())
                .query("year", videoSearch.getYear())
                .query("sort_by", videoSearch.getOrder())
                .header("Authorization", "Bearer %s".formatted(videoSource.getVideoSourceToken()))
                .execute();

        TmdbResult content = httpResponse.content(TmdbResult.class);
        if (null == content || null == content.getResults()) {
            return ReturnPageResult.empty();
        }

        List<TmdbResult.ResultsDTO> results = content.getResults();
        if (results.isEmpty()) {
            return ReturnPageResult.empty();
        }
        Integer totalPages = content.getTotalPages();
        return ReturnPageResultUtils.ok(
                results.stream().map(item -> {
                    VideoInfoResult videoInfoResult = new VideoInfoResult();
                    videoInfoResult.setVideoOriginId(String.valueOf(item.getId()));
                    videoInfoResult.setVideoTitle(item.getOriginalTitle());
                    videoInfoResult.setVideoLanguage(item.getOriginalLanguage());
                    videoInfoResult.setVideoDescription(item.getOverview());
                    try {
                        videoInfoResult.setVideoPublishDate(DateUtils.toLocalDateTime(item.getReleaseDate()));
                    } catch (ParseException ignored) {
                    }
                    videoInfoResult.setVideoVoteCount(item.getVoteCount());
                    videoInfoResult.setVideoPlatform("TMDB");
                    videoInfoResult.setVideoPopularity(String.valueOf(item.getPopularity()));
                    videoInfoResult.setVideoCategory("MV");
                    videoInfoResult.setVideoScore(BigDecimal.valueOf(item.getVoteAverage()));
                    videoInfoResult.setVideoCover(IMAGE_URL + item.getPosterPath());
                    List<Integer> genreIds = item.getGenreIds();
                    videoInfoResult.setVideoType(genreIds.stream().map(TmdbGenre::getNameById).collect(Collectors.joining()));

                    VideoMark videoMark = new VideoMark();
                    videoMark.setVideoMarkType("TMDB");
                    videoMark.setVideoMarkPeople(item.getVoteCount());
                    videoMark.setVideoMarkScore(BigDecimal.valueOf(item.getVoteAverage()));

                    videoInfoResult.setVideoMarkList(Collections.singletonList(videoMark));
                    return videoInfoResult;
                }).collect(Collectors.toList()), videoSearch.getPage(), videoSearch.getPageSize(), totalPages);
    }

    /**
     * TMDB搜索结果封装类
     * <p>
     * {
     * "adult": false,
     * "backdrop_path": "/1RgPyOhN4DRs225BGTlHJqCudII.jpg",
     * "genre_ids": [
     * 16,
     * 28,
     * 14,
     * 53
     * ],
     * "id": 1311031,
     * "original_language": "ja",
     * "original_title": "劇場版「鬼滅の刃」無限城編 第一章 猗窩座再来",
     * "overview": "The Demon Slayer Corps are drawn into the Infinity Castle, where Tanjiro, Nezuko, and the Hashira face terrifying Upper Rank demons in a desperate fight as the final battle against Muzan Kibutsuji begins.",
     * "popularity": 820.4782,
     * "poster_path": "/aFRDH3P7TX61FVGpaLhKr6QiOC1.jpg",
     * "release_date": "2025-07-18",
     * "title": "Demon Slayer: Kimetsu no Yaiba Infinity Castle",
     * "video": false,
     * "vote_average": 7.7,
     * "vote_count": 207
     * },
     *
     * @author CH
     * @since 2025/9/16 16:26
     */
    @NoArgsConstructor
    @Data
    static class TmdbResult {

        /**
         * 当前页码
         *
         * @example <pre><code>1</code></pre>
         */
        @JsonProperty("page")
        private Integer page;

        /**
         * 搜索结果列表
         *
         * @see ResultsDTO
         */
        @JsonProperty("results")
        private List<ResultsDTO> results;

        /**
         * 总页数
         *
         * @example <pre><code>100</code></pre>
         */
        @JsonProperty("total_pages")
        private Integer totalPages;

        /**
         * 总结果数
         *
         * @example <pre><code>2000</code></pre>
         */
        @JsonProperty("total_results")
        private Integer totalResults;

        /**
         * TMDB搜索结果详情封装类
         *
         * @author CH
         * @since 2025/9/16 16:26
         */
        @NoArgsConstructor
        @Data
        public static class ResultsDTO {

            /**
             * 是否为成人内容
             *
             * @example <pre><code>false</code></pre>
             */
            @JsonProperty("adult")
            private Boolean adult;

            /**
             * 背景图片路径
             *
             * @example <pre><code>/example/path.jpg</code></pre>
             */
            @JsonProperty("backdrop_path")
            private String backdropPath;

            /**
             * 类型ID列表
             *
             * @example <pre><code>[28, 12, 878]</code></pre>
             */
            @JsonProperty("genre_ids")
            private List<Integer> genreIds;

            /**
             * 影片ID
             *
             * @example <pre><code>12345</code></pre>
             */
            @JsonProperty("id")
            private Integer id;

            /**
             * 原始语言
             *
             * @example <pre><code>en</code></pre>
             */
            @JsonProperty("original_language")
            private String originalLanguage;

            /**
             * 原始标题
             *
             * @example <pre><code>Original Movie Title</code></pre>
             */
            @JsonProperty("original_title")
            private String originalTitle;

            /**
             * 影片概述
             *
             * @example <pre><code>This is a great movie about...</code></pre>
             */
            @JsonProperty("overview")
            private String overview;

            /**
             * 流行度评分
             *
             * @example <pre><code>8.5</code></pre>
             */
            @JsonProperty("popularity")
            private Double popularity;

            /**
             * 海报图片路径
             *
             * @example <pre><code>/poster/path.jpg</code></pre>
             */
            @JsonProperty("poster_path")
            private String posterPath;

            /**
             * 发布日期
             *
             * @example <pre><code>2025-12-25</code></pre>
             */
            @JsonProperty("release_date")
            private String releaseDate;

            /**
             * 标题
             *
             * @example <pre><code>Movie Title</code></pre>
             */
            @JsonProperty("title")
            private String title;

            /**
             * 是否有视频
             *
             * @example <pre><code>true</code></pre>
             */
            @JsonProperty("video")
            private Boolean video;

            /**
             * 平均评分
             *
             * @example <pre><code>7.8</code></pre>
             */
            @JsonProperty("vote_average")
            private Double voteAverage;

            /**
             * 评分人数
             *
             * @example <pre><code>1234</code></pre>
             */
            @JsonProperty("vote_count")
            private Integer voteCount;
        }
    }
}
