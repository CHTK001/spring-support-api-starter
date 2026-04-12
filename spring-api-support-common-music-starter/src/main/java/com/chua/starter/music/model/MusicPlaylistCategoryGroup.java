package com.chua.starter.music.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class MusicPlaylistCategoryGroup {
    private String groupId;
    private String name;
    private List<MusicPlaylistCategory> tags;
}
