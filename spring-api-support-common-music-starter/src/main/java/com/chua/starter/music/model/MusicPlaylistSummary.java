package com.chua.starter.music.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MusicPlaylistSummary {
    private String playlistId;
    private String source;
    private String title;
    private String description;
    private String coverUrl;
    private String author;
    private Integer trackCount;
    private String accentColor;
    private Long playCount;
    private Long commentCount;
}
