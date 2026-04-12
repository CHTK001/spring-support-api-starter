package com.chua.starter.music.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class MusicSearchResult {
    private String source;
    private String keyword;
    private Integer page;
    private Integer pageSize;
    private Long total;
    private List<MusicTrackSummary> tracks;
}
