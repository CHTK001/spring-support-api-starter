package com.chua.starter.monitor.service.impl;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.chua.common.support.backup.Backup;
import com.chua.common.support.datasource.jdbc.option.DataSourceOptions;
import com.chua.common.support.document.DocQuery;
import com.chua.common.support.document.Document;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.metadata.command.CommandMetadata;
import com.chua.common.support.metadata.command.CommandMetadataFactory;
import com.chua.common.support.metadata.command.CommandResult;
import com.chua.common.support.metadata.hierarchy.HierarchyMetadata;
import com.chua.common.support.metadata.hierarchy.HierarchyMetadataFactory;
import com.chua.common.support.metadata.hierarchy.HierarchyNode;
import com.chua.common.support.metadata.hierarchy.HierarchyResult;
import com.chua.common.support.metadata.query.MetadataQuery;
import com.chua.common.support.oss.result.GetObjectResult;
import com.chua.common.support.spi.ServiceProvider;
import com.chua.common.support.utils.IoUtils;
import com.chua.starter.monitor.entity.SystemDataSetting;
import com.chua.starter.monitor.mapper.SystemDataSettingMapper;
import com.chua.starter.monitor.service.DataConsoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class DataConsoleServiceImpl implements DataConsoleService {

    private final SystemDataSettingMapper systemDataSettingMapper;
    private final Map<Integer, Boolean> backupStatusMap = new ConcurrentHashMap<>();

    @Override
    public ReturnResult<HierarchyResult<HierarchyNode>> getRoot(Integer settingId, MetadataQuery query) {
        try {
            HierarchyMetadata metadata = getOrCreateHierarchy(settingId);
            return ReturnResult.ok(metadata.getRootNodes(query));
        } catch (Exception e) {
            log.error("获取根节点失败", e);
            return ReturnResult.error(e.getMessage());
        }
    }

    @Override
    public ReturnResult<HierarchyResult<HierarchyNode>> getChildren(Integer settingId, String parentPath, MetadataQuery query) {
        try {
            HierarchyMetadata metadata = getOrCreateHierarchy(settingId);
            return ReturnResult.ok(metadata.getChildNodes(parentPath, query));
        } catch (Exception e) {
            log.error("获取子节点失败", e);
            return ReturnResult.error(e.getMessage());
        }
    }

    @Override
    public ReturnResult<HierarchyNode> getNode(Integer settingId, String nodePath, MetadataQuery query) {
        try {
            HierarchyMetadata metadata = getOrCreateHierarchy(settingId);
            return ReturnResult.ok(metadata.getNodeDetails(nodePath, query));
        } catch (Exception e) {
            log.error("获取节点失败", e);
            return ReturnResult.error(e.getMessage());
        }
    }

    @Override
    public ReturnResult<HierarchyResult<HierarchyNode>> search(Integer settingId, String searchPattern, MetadataQuery query) {
        try {
            HierarchyMetadata metadata = getOrCreateHierarchy(settingId);
            return ReturnResult.ok(metadata.searchNodes(searchPattern, query));
        } catch (Exception e) {
            log.error("搜索节点失败", e);
            return ReturnResult.error(e.getMessage());
        }
    }

    @Override
    public ReturnResult<CommandResult> execute(Integer settingId, String type, String command, MetadataQuery query) {
        try {
            SystemDataSetting setting = systemDataSettingMapper.selectById(settingId);
            if (setting == null) return ReturnResult.error("配置不存在");

            CommandMetadata metadata;
            DataSourceOptions options = buildOptions(setting);
            if (StringUtils.isBlank(type)) {
                metadata = CommandMetadataFactory.createCommandByDataSourceOptions(options);
            } else {
                metadata = CommandMetadataFactory.createCommand(type, options);
            }
            CommandResult result = StringUtils.isBlank(type) ? metadata.executeCommand(command) : metadata.executeCommand(type, command);
            return ReturnResult.ok(result);
        } catch (Exception e) {
            log.error("执行命令失败", e);
            return ReturnResult.error(e.getMessage());
        }
    }

    @Override
    public ReturnResult<ResponseEntity<byte[]>> generateDocument(Integer settingId) {
        try {
            SystemDataSetting setting = systemDataSettingMapper.selectById(settingId);
            if (setting == null) return ReturnResult.error("配置不存在");
            boolean support = ServiceProvider.of(Document.class).isSupport(setting.getSystemDataSettingType());
            if (!support) return ReturnResult.error("文档不支持");
            Document document = ServiceProvider.of(Document.class).getNewExtension(setting.getSystemDataSettingType());
            if (document == null) return ReturnResult.error("文档组件不可用");
            GetObjectResult getObjectResult = document.create(DocQuery.builder()
                    .type(DocQuery.Type.HTML)
                    .build());
            try (InputStream inputStream = getObjectResult.getInputStream()) {
                return ReturnResult.ok(ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(getObjectResult.getMediaType().toString()))
                        .body(IoUtils.toByteArray(inputStream))
                );
            }
        } catch (Exception e) {
            log.error("生成文档失败", e);
            return ReturnResult.error(e.getMessage());
        }
    }

    @Override
    public ReturnResult<Boolean> startBackup(Integer settingId) {
        try {
            SystemDataSetting setting = systemDataSettingMapper.selectById(settingId);
            if (setting == null) return ReturnResult.error("配置不存在");
            boolean support = ServiceProvider.of(Backup.class).isSupport(setting.getSystemDataSettingType());
            if (!support) return ReturnResult.error("备份不支持");
            backupStatusMap.put(settingId, true);
            return ReturnResult.ok(true);
        } catch (Exception e) {
            log.error("启动备份失败", e);
            return ReturnResult.error(e.getMessage());
        }
    }

    @Override
    public ReturnResult<Boolean> stopBackup(Integer settingId) {
        backupStatusMap.put(settingId, false);
        return ReturnResult.ok(true);
    }

    @Override
    public ReturnResult<Boolean> backupStatus(Integer settingId) {
        return ReturnResult.ok(Boolean.TRUE.equals(backupStatusMap.get(settingId)));
    }

    @Override
    public HierarchyMetadata getOrCreateHierarchy(Integer settingId) {
        SystemDataSetting setting = systemDataSettingMapper.selectById(settingId);
        DataSourceOptions options = buildOptions(setting);
        if (setting.getSystemDataSettingServer() != null && setting.getSystemDataSettingServer().toLowerCase().startsWith("jdbc:")) {
            return HierarchyMetadataFactory.createJdbcHierarchy(options);
        }
        // 通用 SPI
        return HierarchyMetadataFactory.createHierarchy(setting.getSystemDataSettingType().toLowerCase(), options);
    }

    private DataSourceOptions buildOptions(SystemDataSetting setting) {
        DataSourceOptions.DatabaseBuilder builder = DataSourceOptions.builder()
                .name(setting.getSystemDataSettingName())
                .url(StringUtils.isNotBlank(setting.getSystemDataSettingServer())
                        ? setting.getSystemDataSettingServer()
                        : (setting.getSystemDataSettingHost() + ":" + setting.getSystemDataSettingPort()))
                .username(setting.getSystemDataSettingUsername())
                .password(setting.getSystemDataSettingPassword());

        // 如果上传过驱动文件，优先告知 DataSourceOptions 使用对应驱动
        if (StringUtils.isNotBlank(setting.getSystemDataSettingDriverClass())) {
            builder.driverClassName(setting.getSystemDataSettingDriverClass());
        }
        DataSourceOptions options = builder.build();
        if (StringUtils.isNotBlank(setting.getSystemDataSettingDriverPath())) {
            options.setDriverPath(setting.getSystemDataSettingDriverPath());
        }
        return options;
    }
}


