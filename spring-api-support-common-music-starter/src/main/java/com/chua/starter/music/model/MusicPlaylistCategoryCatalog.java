package com.chua.starter.music.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class MusicPlaylistCategoryCatalog {
    private String source;
    private List<MusicPlaylistCategory> hotTags;
    private List<MusicPlaylistCategoryGroup> groups;
}
