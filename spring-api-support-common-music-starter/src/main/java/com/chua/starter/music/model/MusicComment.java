package com.chua.starter.music.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MusicComment {
    private String commentId;
    private String author;
    private String avatar;
    private String content;
    private Long likedCount;
    private String time;
}
