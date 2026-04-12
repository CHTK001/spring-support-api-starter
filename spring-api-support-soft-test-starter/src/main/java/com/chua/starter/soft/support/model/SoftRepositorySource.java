package com.chua.starter.soft.support.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SoftRepositorySource {
    private String sourceName;
    private String sourceType;
    private String sourceUrl;
    private String localDirectory;
    private boolean enabled;
    private String sourceConfig;
}
