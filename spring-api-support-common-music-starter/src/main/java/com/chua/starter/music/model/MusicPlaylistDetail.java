package com.chua.starter.music.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class MusicPlaylistDetail {
    private String playlistId;
    private String source;
    private String title;
    private String description;
    private String coverUrl;
    private String author;
    private Integer trackCount;
    private List<MusicTrackSummary> tracks;
}
