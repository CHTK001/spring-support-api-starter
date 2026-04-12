package com.chua.starter.music.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MusicPlaylistCategory {
    private String tagId;
    private String name;
    private Boolean hot;
}
