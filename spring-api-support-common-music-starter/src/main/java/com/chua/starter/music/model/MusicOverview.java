package com.chua.starter.music.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class MusicOverview {
    private List<MusicSourceOption> sources;
    private String defaultSource;
    private List<String> hotKeywords;
    private List<MusicPlaylistSummary> featuredPlaylists;
}
