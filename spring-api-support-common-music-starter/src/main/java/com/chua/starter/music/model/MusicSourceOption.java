package com.chua.starter.music.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MusicSourceOption {
    private String code;
    private String name;
    private String description;
    private Boolean enabled;
}
