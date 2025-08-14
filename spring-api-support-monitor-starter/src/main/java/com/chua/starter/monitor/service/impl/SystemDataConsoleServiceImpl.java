package com.chua.starter.monitor.service.impl;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.chua.common.support.backup.Backup;
import com.chua.common.support.backup.BackupSetting;
import com.chua.common.support.backup.listener.BackupData;
import com.chua.common.support.backup.listener.BackupListener;
import com.chua.common.support.backup.strategy.DayBackupStrategy;
import com.chua.common.support.constant.EventType;
import com.chua.common.support.datasource.jdbc.option.DataSourceOptions;
import com.chua.common.support.document.DocQuery;
import com.chua.common.support.document.Document;
import com.chua.common.support.json.Json;
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
import com.chua.redis.support.constant.RedisConstant;
import com.chua.socketio.support.session.SocketSessionTemplate;
import com.chua.starter.monitor.entity.SystemDataBackupRecord;
import com.chua.starter.monitor.entity.SystemDataFieldComment;
import com.chua.starter.monitor.entity.SystemDataSetting;
import com.chua.starter.monitor.mapper.SystemDataSettingMapper;
import com.chua.starter.monitor.pojo.TableOpenResponse;
import com.chua.starter.monitor.pojo.ValueCount;
import com.chua.starter.monitor.service.DataConsoleService;
import com.chua.starter.monitor.service.SystemDataFieldCommentService;
import com.chua.starter.redis.support.service.RedisSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.beans.BeanMap;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SystemDataConsoleServiceImpl implements DataConsoleService, InitializingBean {

    private final SystemDataSettingMapper systemDataSettingMapper;
    private final SocketSessionTemplate socketSessionTemplate;
    private final RedisSearchService redisSearchService;
    private final Map<Integer, Backup> backupStatusMap = new ConcurrentHashMap<>();
    @Autowired
    private SystemDataFieldCommentService systemDataFieldCommentService;

    @Override
    public ReturnResult<HierarchyResult<HierarchyNode>> getRoot(Integer settingId, MetadataQuery query) {
        HierarchyMetadata metadata = null;
        try {
            metadata = getOrCreateHierarchy(settingId);
            return ReturnResult.ok(metadata.getRootNodes(query));
        } catch (Exception e) {
            log.error("获取根节点失败", e);
            try {
                metadata.close();
            } catch (Exception ignored) {
            }
            return ReturnResult.error(e.getMessage());
        }
    }

    @Override
    public ReturnResult<HierarchyResult<HierarchyNode>> getChildren(Integer settingId, String parentPath, MetadataQuery query) {
        try {
            HierarchyMetadata metadata = getOrCreateHierarchy(settingId);
            HierarchyResult<HierarchyNode> childNodes = metadata.getChildNodes(parentPath, query);
            List<HierarchyNode> data = childNodes.getData();
            Set<String> paths = data.stream().map(HierarchyNode::getPath).collect(Collectors.toSet());
            if(!paths.isEmpty()) {
                List<SystemDataFieldComment> list = systemDataFieldCommentService.list(
                        Wrappers.<SystemDataFieldComment>lambdaQuery()
                                .eq(SystemDataFieldComment::getSystemDataFieldCommentSettingId, settingId)
                                .in(SystemDataFieldComment::getSystemDataFieldCommentNodePath, paths)
                );
                Map<String, SystemDataFieldComment> collect = list.stream().collect(Collectors.toMap(SystemDataFieldComment::getSystemDataFieldCommentNodePath, item -> item));
                for (HierarchyNode datum : data) {
                    SystemDataFieldComment systemDataFieldComment = collect.get(datum.getPath());
                    if(systemDataFieldComment != null) {
                        datum.getProperties().put("comment", systemDataFieldComment.getSystemDataFieldCommentComment());
                    }
                }
            }
            return ReturnResult.ok(childNodes);
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
    public ReturnResult<TableOpenResponse> openTable(Integer settingId, String nodePath, Integer limit) {
        try {
            String[] pathParts = nodePath.split("/");
            String schemaName = pathParts[1];
            String tableName = pathParts[2];
            HierarchyMetadata metadata = getOrCreateHierarchy(settingId);
            MetadataQuery metadataQuery = new MetadataQuery();
            // 1) 基于层级获取列（表结构=字段列表），而非强依赖自定义 action
            List<HierarchyNode> columnNodes = new ArrayList<>();
            try {
                HierarchyResult<HierarchyNode> children = metadata.getChildNodes(nodePath, metadataQuery);
                if (children != null && children.getData() != null) {
                    columnNodes = children.getData();
                }
            } catch (Exception ignored) {
            }

            List<String> columns = new ArrayList<>();
            Map<String, String> autoComments = new java.util.HashMap<>();
            Map<String, String> pathToColumn = new java.util.HashMap<>();
            for (HierarchyNode cn : columnNodes) {
                String name = String.valueOf(cn.getName());
                columns.add(name);
                pathToColumn.put(cn.getPath(), name);
                String cmt = cn.getProperties() != null ? String.valueOf(cn.getProperties().getOrDefault("comment", "")) : "";
                if (StringUtils.isNotBlank(cmt)) {
                    autoComments.put(name, cmt);
                }
            }
            TableOpenResponse resp = new TableOpenResponse();
            // 2) 查询数据：优先使用 metadata 的 data 行为，不可用则回退到 SQL 执行
            try {
                ReturnResult<CommandResult> data1 = execute(settingId, "data", "SELECT * FROM " + schemaName + "." + tableName, metadataQuery);

                if (data1.isOk()) {
                    CommandResult data = data1.getData();
//                    resp.setRows(data.getData());
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            // 3) 合并字段注释：手动优先
            Map<String, String> columnComments = new java.util.HashMap<>(autoComments);
            if (!pathToColumn.isEmpty()) {
                List<SystemDataFieldComment> manual = systemDataFieldCommentService.list(
                        Wrappers.<SystemDataFieldComment>lambdaQuery()
                                .eq(SystemDataFieldComment::getSystemDataFieldCommentSettingId, settingId)
                                .in(SystemDataFieldComment::getSystemDataFieldCommentNodePath, pathToColumn.keySet())
                );
                for (SystemDataFieldComment item : manual) {
                    String path = item.getSystemDataFieldCommentNodePath();
                    String col = pathToColumn.getOrDefault(path, item.getSystemDataFieldCommentColumnName());
                    if (StringUtils.isNotBlank(col) && StringUtils.isNotBlank(item.getSystemDataFieldCommentComment())) {
                        columnComments.put(col, item.getSystemDataFieldCommentComment());
                    }
                }
            }

            resp.setColumns(columns);
            resp.setColumnComments(columnComments);
            return ReturnResult.ok(resp);
        } catch (Exception e) {
            log.error("打开表失败", e);
            return ReturnResult.error(e.getMessage());
        }
    }

    @Override
    public ReturnResult<Map<String, List<ValueCount>>> analyzeTable(Integer settingId, String nodePath, Integer limit) {
        try {
            // 复用 openTable 的数据获取逻辑，保证一致性
            ReturnResult<TableOpenResponse> open = openTable(settingId, nodePath, limit);
            if (open == null || open.getData() == null) {
                return ReturnResult.error("无法获取表数据");
            }
            List<String> columns = open.getData().getColumns();
            List<Map<String, Object>> rows = open.getData().getRows();
            Map<String, List<ValueCount>> result = new java.util.HashMap<>();
            for (String col : columns) {
                Map<String, Long> freq = rows.stream()
                        .collect(Collectors.groupingBy(o -> String.valueOf(o.get(col)), Collectors.counting()));
                List<ValueCount> list = freq.entrySet().stream()
                        .map(e -> new ValueCount(e.getKey(), e.getValue()))
                        .sorted((a, b) -> Long.compare(b.getCount(), a.getCount()))
                        .collect(Collectors.toList());
                result.put(col, list);
            }
            return ReturnResult.ok(result);
        } catch (Exception e) {
            log.error("表分析失败", e);
            return ReturnResult.error(e.getMessage());
        }
    }

    @Override
    public ReturnResult<CommandResult> execute(Integer settingId, String type, String command, MetadataQuery query) {
        try {
            SystemDataSetting setting = systemDataSettingMapper.selectById(settingId);
            if (setting == null) {
                return ReturnResult.error("配置不存在");
            }

            if (command.startsWith("\"") && command.endsWith("\"")) {
                command = command.substring(1, command.length() - 1);
            }

            command = command.replace("\\n", " ");
            DataSourceOptions options = buildOptions(setting);
            CommandMetadata metadata = CommandMetadataFactory.create(setting.getSystemDataSettingId(), setting.getSystemDataSettingType(), options);
            CommandResult result = StringUtils.isBlank(type) ? metadata.executeCommand(command) : metadata.executeCommand(type, command, true);
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
            if (setting == null) {
                return ReturnResult.error("配置不存在");
            }
            boolean support = ServiceProvider.of(Document.class).isSupport(setting.getSystemDataSettingType());
            if (!support) {
                return ReturnResult.error("文档不支持");
            }

            DataSourceOptions options = buildOptions(setting);
            Document document = ServiceProvider.of(Document.class).getNewExtension(setting.getSystemDataSettingType(), options);
            if (document == null) {
                return ReturnResult.error("文档组件不可用");
            }
            try ( GetObjectResult getObjectResult = document.create(DocQuery.builder()
                    .type(DocQuery.Type.HTML)
                    .build());
                  InputStream inputStream = getObjectResult.getInputStream()) {
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
            if (setting == null) {
                return ReturnResult.error("配置不存在");
            }
            boolean support = ServiceProvider.of(Backup.class).isSupport(setting.getSystemDataSettingProtocol());
            if (!support) {
                return ReturnResult.error("备份不支持");
            }
            DataSourceOptions databaseOptions = new DataSourceOptions();
            databaseOptions.setDriverClass(setting.getSystemDataSettingDriverClass());
            databaseOptions.setUrl(setting.getSystemDataSettingServer());
            databaseOptions.setUsername(setting.getSystemDataSettingUsername());
            databaseOptions.setPassword(setting.getSystemDataSettingPassword());
            databaseOptions.setTimeout(setting.getSystemDataSettingTimeoutMs());
            BackupSetting backupOptions = BackupSetting.builder()
                    .build();
            Backup backup = ServiceProvider.of(Backup.class).getNewExtension(setting.getSystemDataSettingProtocol(), databaseOptions, backupOptions);
            backup.addStrategy(new DayBackupStrategy());
            backup.addListener(new BackupListener() {
                @Override
                public void listen(EventType event, BackupData backupData) {
                    try {
                        // 构建记录实体
                        SystemDataBackupRecord record = SystemDataBackupRecord.builder()
                                .id(backupData.getId())
                                .settingId(settingId)
                                .event(event != null ? event.name() : "UNKNOWN")
                                .timestamp(System.currentTimeMillis())
                                .name(backupData.getMessage())
                                .path(backupData.getAddress())
                                .size(-1L)
                                .message(backupData.getMessage())
                                .build();

                        // 保存到 RedisSearch
                        String index = RedisConstant.REDIS_SEARCH_PREFIX + "system:data:backup";
                        Map<String, String> doc = BeanMap.create(record);
                        redisSearchService.addDocument(index, doc);

                        // 推送到前端
                        try {
                            if (socketSessionTemplate != null) {
                                socketSessionTemplate.send("system/data/backup", Json.toJson(record));
                            }
                        } catch (Exception ex) {
                            log.warn("推送备份事件失败: {}", ex.getMessage());
                        }
                    } catch (Exception ex) {
                        log.warn("处理备份事件失败: {}", ex.getMessage());
                    }
                }
            });
            backup.start();
            log.info("启动备份成功: {}", setting.getSystemDataSettingName());
            setting.setSystemDataSettingBackupStatus(1);
            systemDataSettingMapper.updateById(setting);
            backupStatusMap.put(settingId, backup);
            return ReturnResult.ok(true);
        } catch (Exception e) {
            log.error("启动备份失败", e);
            return ReturnResult.error(e.getMessage());
        }
    }

    @Override
    public ReturnResult<Boolean> stopBackup(Integer settingId) {
        SystemDataSetting setting = systemDataSettingMapper.selectById(settingId);
        if (setting == null) {
            return ReturnResult.error("配置不存在");
        }
        Backup backup = backupStatusMap.get(settingId);
        if (backup != null) {
            try {
                backup.stop();
                setting.setSystemDataSettingBackupStatus(0);
                systemDataSettingMapper.updateById(setting);
            } catch (Exception e) {
                log.error("停止备份失败", e);
            }
        }
        return ReturnResult.ok(true);
    }

    @Override
    public ReturnResult<Boolean> backupStatus(Integer settingId) {
        return ReturnResult.ok(backupStatusMap.containsKey(settingId));
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

    @Override
    public void afterPropertiesSet() throws Exception {
        Thread.startVirtualThread(() -> {
            for (SystemDataSetting systemDataSetting : systemDataSettingMapper.selectList(
                    Wrappers.<SystemDataSetting>lambdaQuery()
                            .eq(SystemDataSetting::getSystemDataSettingBackupStatus, 1)
            )) {

                try {
                    startBackup(systemDataSetting.getSystemDataSettingId());
                } catch (Exception ignored) {
                }
            }
        });
    }
}


