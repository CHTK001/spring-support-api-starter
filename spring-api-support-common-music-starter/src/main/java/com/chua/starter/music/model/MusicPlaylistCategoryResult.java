package com.chua.starter.music.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class MusicPlaylistCategoryResult {
    private String source;
    private String tagId;
    private String categoryName;
    private Integer page;
    private Integer pageSize;
    private Long total;
    private List<MusicPlaylistSummary> playlists;
}
