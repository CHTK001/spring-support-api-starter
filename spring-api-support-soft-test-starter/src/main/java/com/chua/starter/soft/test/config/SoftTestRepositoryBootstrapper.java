package com.chua.starter.soft.test.config;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.chua.starter.soft.support.entity.SoftRepository;
import com.chua.starter.soft.support.mapper.SoftRepositoryMapper;
import com.chua.starter.soft.support.model.SoftRepositorySource;
import com.chua.starter.soft.support.service.SoftManagementService;
import java.util.List;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class SoftTestRepositoryBootstrapper implements ApplicationRunner {

    private static final String STATIC_ROOT = "http://127.0.0.1:18901";
    private static final String ARTIFACT_ROOT = "H:/workspace/2/tmp/soft-artifacts";

    private final SoftRepositoryMapper repositoryMapper;
    private final SoftManagementService softManagementService;

    public SoftTestRepositoryBootstrapper(
            SoftRepositoryMapper repositoryMapper,
            SoftManagementService softManagementService
    ) {
        this.repositoryMapper = repositoryMapper;
        this.softManagementService = softManagementService;
    }

    @Override
    public void run(ApplicationArguments args) {
        upsert(repository(
                "real-soft-repo",
                "综合软件仓库",
                "LOCAL_DIR",
                null,
                ARTIFACT_ROOT,
                "本地安装包目录 + 在线索引 + RPM 目录 + 镜像目录的综合定义，用于软件搜索、补充版本和运维联调。",
                List.of(
                        source("主索引仓库", "HTTP_JSON", STATIC_ROOT + "/soft-repo-main/index.json", null, true, "主软件索引"),
                        source("补充索引仓库", "HTTP_JSON", STATIC_ROOT + "/soft-repo-extra/index.json", null, true, "补充软件与版本"),
                        source("RPM 远程仓库", "RPM_REPO", STATIC_ROOT + "/soft-artifacts/rpm/", null, true, "远程 RPM 安装包目录"),
                        source("镜像安装目录", "MIRROR_REPO", STATIC_ROOT + "/soft-artifacts/mirror/", null, true, "镜像安装包目录"),
                        source("禁用镜像目录", "MIRROR_REPO", STATIC_ROOT + "/soft-artifacts/disabled/", null, false, "禁用源示例")
                )
        ));
        upsert(repository(
                "remote-artifacts-http",
                "远程安装包目录",
                "HTTP_DIR",
                STATIC_ROOT + "/soft-artifacts/",
                null,
                "远程 HTTP 安装包目录，适合通用二进制、zip、exe、rpm 等直接入库。",
                List.of()
        ));
        upsert(repository(
                "rpm-third-party",
                "RPM 远程仓库",
                "RPM_REPO",
                STATIC_ROOT + "/soft-artifacts/rpm/",
                null,
                "用于搜索和同步 RPM 安装包版本信息。",
                List.of()
        ));
        upsert(repository(
                "mirror-third-party",
                "镜像安装包仓库",
                "MIRROR_REPO",
                STATIC_ROOT + "/soft-artifacts/mirror/",
                null,
                "用于同步镜像站安装包目录，适合多源分发场景。",
                List.of()
        ));
    }

    private void upsert(SoftRepository repository) {
        SoftRepository current = repositoryMapper.selectOne(
                Wrappers.<SoftRepository>lambdaQuery()
                        .eq(SoftRepository::getRepositoryCode, repository.getRepositoryCode())
                        .last("limit 1")
        );
        if (current != null) {
            repository.setSoftRepositoryId(current.getSoftRepositoryId());
        }
        softManagementService.saveRepository(repository);
    }

    private SoftRepository repository(
            String code,
            String name,
            String type,
            String url,
            String localDirectory,
            String syncConfig,
            List<SoftRepositorySource> sources
    ) {
        SoftRepository repository = new SoftRepository();
        repository.setRepositoryCode(code);
        repository.setRepositoryName(name);
        repository.setRepositoryType(type);
        repository.setRepositoryUrl(url);
        repository.setLocalDirectory(localDirectory);
        repository.setSyncConfig(syncConfig);
        repository.setEnabled(Boolean.TRUE);
        repository.setSourceConfigs(sources);
        return repository;
    }

    private SoftRepositorySource source(
            String name,
            String type,
            String url,
            String localDirectory,
            boolean enabled,
            String config
    ) {
        return SoftRepositorySource.builder()
                .sourceName(name)
                .sourceType(type)
                .sourceUrl(url)
                .localDirectory(localDirectory)
                .enabled(enabled)
                .sourceConfig(config)
                .build();
    }
}
