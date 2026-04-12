package com.chua.starter.music.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class MusicTrackDetail {
    private String trackId;
    private String source;
    private String title;
    private String artist;
    private String album;
    private String coverUrl;
    private String streamUrl;
    private Integer durationSeconds;
    private String lyrics;
    private Long playCount;
    private Long commentCount;
    private List<MusicComment> comments;
}
