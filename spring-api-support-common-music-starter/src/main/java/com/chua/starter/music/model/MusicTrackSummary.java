package com.chua.starter.music.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MusicTrackSummary {
    private String trackId;
    private String source;
    private String title;
    private String artist;
    private String album;
    private String coverUrl;
    private Integer durationSeconds;
    private Long playCount;
    private Long commentCount;
}
