package com.chua.starter.soft.support.entity;

import com.chua.starter.soft.support.model.SoftRepositorySource;
import java.util.List;
import lombok.Data;

@Data
public class SoftRepository {
    private Long softRepositoryId;
    private String repositoryCode;
    private String repositoryName;
    private String repositoryType;
    private String repositoryUrl;
    private String localDirectory;
    private String syncConfig;
    private Boolean enabled;
    private List<SoftRepositorySource> sourceConfigs;
}
