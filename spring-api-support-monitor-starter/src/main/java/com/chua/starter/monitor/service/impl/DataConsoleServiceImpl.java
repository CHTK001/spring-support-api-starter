package com.chua.starter.monitor.service.impl;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.chua.common.support.backup.Backup;
import com.chua.common.support.datasource.jdbc.option.DataSourceOptions;
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
import com.chua.common.support.spi.ServiceProvider;
import com.chua.starter.monitor.entity.SystemDataSetting;
import com.chua.starter.monitor.mapper.SystemDataSettingMapper;
import com.chua.starter.monitor.service.DataConsoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DataConsoleServiceImpl implements DataConsoleService {

    private final SystemDataSettingMapper systemDataSettingMapper;

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
    public ReturnResult<Object> generateDocument(Integer settingId) {
        try {
            SystemDataSetting setting = systemDataSettingMapper.selectById(settingId);
            if (setting == null) return ReturnResult.error("配置不存在");
            boolean support = ServiceProvider.of(Document.class).isSupport(setting.getSystemDataSettingType());
            if (!support) return ReturnResult.error("文档不支持");
            Object document = ServiceProvider.of(Document.class).getNewExtension(setting.getSystemDataSettingType());
            return ReturnResult.ok(document != null);
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
            // 启动备份的具体实现可在后续加上任务管理
            return ReturnResult.ok(true);
        } catch (Exception e) {
            log.error("启动备份失败", e);
            return ReturnResult.error(e.getMessage());
        }
    }

    @Override
    public ReturnResult<Boolean> stopBackup(Integer settingId) {
        return ReturnResult.ok(true);
    }

    @Override
    public ReturnResult<Boolean> backupStatus(Integer settingId) {
        return ReturnResult.ok(false);
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
        return builder.build();
    }
}


