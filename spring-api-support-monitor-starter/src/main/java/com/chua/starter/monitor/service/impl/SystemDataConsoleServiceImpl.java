package com.chua.starter.monitor.service.impl;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.chua.common.support.backup.Backup;
import com.chua.common.support.backup.BackupSetting;
import com.chua.common.support.backup.listener.BackupData;
import com.chua.common.support.backup.listener.BackupListener;
import com.chua.common.support.backup.strategy.DayBackupStrategy;
import com.chua.common.support.collection.ConcurrentReferenceHashMap;
import com.chua.common.support.constant.EventType;
import com.chua.common.support.datasource.dialect.Dialect;
import com.chua.common.support.datasource.dialect.DialectFactory;
import com.chua.common.support.datasource.jdbc.option.DataSourceOptions;
import com.chua.common.support.document.DocQuery;
import com.chua.common.support.document.Document;
import com.chua.common.support.json.Json;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.metadata.base.ConstructMetadata;
import com.chua.common.support.metadata.command.CommandMetadata;
import com.chua.common.support.metadata.command.CommandMetadataFactory;
import com.chua.common.support.metadata.command.CommandResult;
import com.chua.common.support.metadata.command.ResultItem;
import com.chua.common.support.metadata.hierarchy.HierarchyMetadata;
import com.chua.common.support.metadata.hierarchy.HierarchyMetadataFactory;
import com.chua.common.support.metadata.hierarchy.HierarchyNode;
import com.chua.common.support.metadata.hierarchy.HierarchyResult;
import com.chua.common.support.metadata.listen.ListenMetadata;
import com.chua.common.support.metadata.query.MetadataQuery;
import com.chua.common.support.oss.result.GetObjectResult;
import com.chua.common.support.spi.ServiceProvider;
import com.chua.common.support.utils.IoUtils;
import com.chua.common.support.utils.MapUtils;
import com.chua.redis.support.constant.RedisConstant;
import com.chua.socket.support.session.SocketSessionTemplate;
import com.chua.starter.monitor.entity.SystemDataBackupRecord;
import com.chua.starter.monitor.entity.SystemDataFieldComment;
import com.chua.starter.monitor.entity.SystemDataSetting;
import com.chua.starter.monitor.mapper.SystemDataSettingMapper;
import com.chua.starter.monitor.pojo.ServerStatic;
import com.chua.starter.monitor.pojo.data.ValueCount;
import com.chua.starter.monitor.pojo.data.TableOpenResponse;
import com.chua.starter.monitor.pojo.data.ColumnInfo;
import com.chua.starter.monitor.pojo.data.TableStructureInfo;
import com.chua.starter.monitor.pojo.data.UpdateRowRequest;
import com.chua.starter.monitor.pojo.data.DeleteRowRequest;
import com.chua.starter.monitor.pojo.data.InsertRowRequest;
import com.chua.starter.monitor.service.data.SystemDataConsoleService;
import com.chua.starter.monitor.spi.metadata.StructureMetadata;
import com.chua.starter.monitor.service.data.SystemDataFieldCommentService;
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
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SystemDataConsoleServiceImpl implements SystemDataConsoleService, InitializingBean {

    private final SystemDataSettingMapper systemDataSettingMapper;
    private final SocketSessionTemplate socketSessionTemplate;
    private final RedisSearchService redisSearchService;
    private final Map<Integer, Backup> backupStatusMap = new ConcurrentReferenceHashMap<>();
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
            if (!paths.isEmpty()) {
                List<SystemDataFieldComment> list = systemDataFieldCommentService.list(
                        Wrappers.<SystemDataFieldComment>lambdaQuery()
                                .eq(SystemDataFieldComment::getSystemDataFieldCommentSettingId, settingId)
                                .in(SystemDataFieldComment::getSystemDataFieldCommentNodePath, paths)
                );
                Map<String, SystemDataFieldComment> collect = list.stream().collect(Collectors.toMap(SystemDataFieldComment::getSystemDataFieldCommentNodePath, item -> item));
                for (HierarchyNode datum : data) {
                    SystemDataFieldComment systemDataFieldComment = collect.get(datum.getPath());
                    if (systemDataFieldComment != null) {
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
            HierarchyNode node = metadata.getNodeDetails(nodePath, query);

            // 增强节点：为时序数据和 JSON 数据注入 valueType
            if (node != null && node.getProperties() != null) {
                enhanceNodeWithValueType(node, nodePath);
            }

            return ReturnResult.ok(node);
        } catch (Exception e) {
            log.error("获取节点失败", e);
            return ReturnResult.error(e.getMessage());
        }
    }

    /**
     * 为节点增强 valueType 信息，支持时序数据和 JSON 数据的检测
     */
    private void enhanceNodeWithValueType(HierarchyNode node, String nodePath) {
        try {
            String path = nodePath != null ? nodePath : "";
            Map<String, Object> properties = node.getProperties();

            // 检测时序数据前缀
            if (path.startsWith("monitor:timeseries:") || path.startsWith("monitor:simple:series:")) {
                properties.put("valueType", "timeserial");
                properties.put("chartType", "line");
                return;
            }

            // 检测 JSON 数据
            Object value = properties.get("value");
            if (value == null) {
                value = properties.get("data");
            }

            if (value instanceof String strValue) {
                if (isJsonLike(strValue)) {
                    properties.put("valueType", "json");
                    return;
                }

                // 检测其他类型
                if (strValue.equalsIgnoreCase("true") || strValue.equalsIgnoreCase("false")) {
                    properties.put("valueType", "boolean");
                } else if (isEmailLike(strValue)) {
                    properties.put("valueType", "email");
                } else if (isColorLike(strValue)) {
                    properties.put("valueType", "color");
                } else if (isPasswordLike(strValue)) {
                    properties.put("valueType", "password");
                }
            } else if (value instanceof Number) {
                properties.put("valueType", "number");
            } else if (value instanceof Boolean) {
                properties.put("valueType", "boolean");
            } else if (value instanceof java.util.List) {
                properties.put("valueType", "array");
            } else if (value instanceof java.util.Map) {
                properties.put("valueType", "object");
            }

        } catch (Exception e) {
            log.debug("增强节点 valueType 失败: {}", e.getMessage());
        }
    }

    /**
     * 检测是否为 JSON 格式字符串
     */
    private boolean isJsonLike(String str) {
        if (str == null || str.trim().isEmpty()) return false;
        String trimmed = str.trim();
        return (trimmed.startsWith("{") && trimmed.endsWith("}")) ||
                (trimmed.startsWith("[") && trimmed.endsWith("]"));
    }

    /**
     * 检测是否为邮箱格式
     */
    private boolean isEmailLike(String str) {
        if (str == null) return false;
        return str.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
    }

    /**
     * 检测是否为颜色格式
     */
    private boolean isColorLike(String str) {
        if (str == null) return false;
        return str.matches("^#?([0-9a-fA-F]{3}|[0-9a-fA-F]{6})$");
    }

    /**
     * 检测是否为密码格式（简单检测包含特定关键词）
     */
    private boolean isPasswordLike(String str) {
        if (str == null) return false;
        String lower = str.toLowerCase();
        return lower.contains("password") || lower.contains("secret") || lower.contains("key");
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

            List<ColumnInfo> columns = new ArrayList<>();
            Map<String, String> autoComments = new HashMap<>();
            Map<String, String> pathToColumn = new HashMap<>();
            for (HierarchyNode cn : columnNodes) {
                String name = String.valueOf(cn.getName());
                Map<String, Object> props = cn.getProperties();

                // 构建 ColumnInfo
                ColumnInfo columnInfo = new ColumnInfo();
                columnInfo.setName(name);
                if (props != null) {
                    columnInfo.setDataType(String.valueOf(props.getOrDefault("columnType", props.getOrDefault("dataType", ""))));
                    columnInfo.setComment(String.valueOf(props.getOrDefault("comment", "")));
                    columnInfo.setPrimaryKey(Boolean.TRUE.equals(props.get("primaryKey")) || Boolean.TRUE.equals(props.get("isPrimary")));
                    columnInfo.setNullable(Boolean.TRUE.equals(props.get("nullable")));
                    Object size = props.get("columnSize");
                    if (size != null) {
                        try {
                            columnInfo.setLength(Integer.parseInt(String.valueOf(size)));
                        } catch (NumberFormatException ignored) {}
                    }
                }
                columns.add(columnInfo);

                pathToColumn.put(cn.getPath(), name);
                String cmt = props != null ? String.valueOf(props.getOrDefault("comment", "")) : "";
                if (StringUtils.isNotBlank(cmt)) {
                    autoComments.put(name, cmt);
                }
            }
            TableOpenResponse resp = new TableOpenResponse();
            // 2) 查询数据：优先使用 metadata 的 data 行为，不可用则回退到 SQL 执行
            try {
                metadataQuery.setQueryId("1");
                ReturnResult<CommandResult> data1 = execute(settingId, "data", "SELECT * FROM " + schemaName + "." + tableName, metadataQuery);

                if (data1.isOk()) {
                    CommandResult data = data1.getData();
                    resp.setRows(data.getData().getRows());
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            // 3) 合并字段注释：手动优先
            Map<String, String> columnComments = new HashMap<>(autoComments);
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
            // 从 ColumnInfo 中提取列名
            List<ColumnInfo> columnInfos = open.getData().getColumns();
            List<String> columns = columnInfos.stream()
                    .map(ColumnInfo::getName)
                    .collect(Collectors.toList());
            List<Map<String, Object>> rows = open.getData().getRows();
            if (null == rows || rows.isEmpty()) {
                return ReturnResult.error("表数据为空");
            }
            Map<String, List<ValueCount>> result = new HashMap<>(columns.size());
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
    public Boolean renameTable(Integer settingId, String nodePath, String newName) {
        HierarchyMetadata orCreateHierarchy = getOrCreateHierarchy(settingId);
        if (orCreateHierarchy instanceof ConstructMetadata constructMetaData) {
            String[] pathParts = nodePath.split("/");
            String schemaName = pathParts[1];
            String tableName = pathParts[2];
            try {
                return !constructMetaData.renameTable(schemaName, tableName, newName).hasError();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    @Override
    public Boolean backupTable(Integer settingId, String nodePath, String backupName) {
        HierarchyMetadata orCreateHierarchy = getOrCreateHierarchy(settingId);
        if (orCreateHierarchy instanceof ConstructMetadata constructMetaData) {
            String[] pathParts = nodePath.split("/");
            String schemaName = pathParts[1];
            String tableName = pathParts[2];
            try {
                return !constructMetaData.copyTableStructure(schemaName, tableName, backupName).hasError();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    @Override
    public ServerStatic getServerStatic() {
        return systemDataSettingMapper.getServerStatic();
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
            if (result.isSuccess() && StringUtils.isNotBlank(query.getQueryId())) {
                ResultItem data = result.getData();
                if(null == data.getColumns()) {
                    return ReturnResult.ok(result);
                }
                List<SystemDataFieldComment> list = systemDataFieldCommentService.list(
                        Wrappers.<SystemDataFieldComment>lambdaQuery()
                                .eq(SystemDataFieldComment::getSystemDataFieldCommentSettingId, settingId)
                                .likeRight(SystemDataFieldComment::getSystemDataFieldCommentNodePath, query.getQueryId())
                );
                Map<String, String> columnComments = list.stream().collect(Collectors.toMap(SystemDataFieldComment::getSystemDataFieldCommentColumnName, SystemDataFieldComment::getSystemDataFieldCommentComment));
                data.getColumns().forEach(column -> {
                    column.setComment(columnComments.getOrDefault(column.getName(), column.getComment()));
                });
                return ReturnResult.ok(result);
            }
            return ReturnResult.fail(result.getErrorMessage());
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
            try (GetObjectResult getObjectResult = document.create(DocQuery.builder()
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
                        Map<String, String> doc = new HashMap<>(BeanMap.create(record));
                        MapUtils.clearNullValue(doc);
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
        HierarchyMetadata hierarchy = HierarchyMetadataFactory.createHierarchy(String.valueOf(setting.getSystemDataSettingId()), setting.getSystemDataSettingType(), options);
        registerIdle(hierarchy, setting);
        return hierarchy;
    }

    /**
     * 注册idle检测
     *
     * @param hierarchy hierarchy
     * @param setting   setting
     */
    private void registerIdle(HierarchyMetadata hierarchy, SystemDataSetting setting) {
        Boolean dataSettingIdle = setting.getSystemDataSettingIdle();
        if (null == dataSettingIdle || !dataSettingIdle) {
            return;
        }

        if (hierarchy instanceof ListenMetadata listenMetadata) {
            listenMetadata.enableSupported((p1, p2, actionType, it) -> {
                socketSessionTemplate.send("system/data/listen/" + actionType + "/" + setting.getSystemDataSettingId(),
                        Json.toJSONString(it));
            });
        }

    }

    public static DataSourceOptions buildOptions(SystemDataSetting setting) {
        SystemDataSettingServiceImpl.decryptSensitiveData(setting);
        DataSourceOptions.DatabaseBuilder builder = DataSourceOptions.builder()
                .name(setting.getSystemDataSettingName())
                .url(StringUtils.isNotBlank(setting.getSystemDataSettingServer())
                        ? setting.getSystemDataSettingServer()
                        : (setting.getSystemDataSettingHost() + ":" + setting.getSystemDataSettingPort()))
                .databaseName(setting.getSystemDataSettingDatabase())
                .policy(setting.getSystemDataSettingPolicy())
                .username(StringUtils.isNotBlank(setting.getSystemDataSettingUsername()) ? setting.getSystemDataSettingUsername() : null)
                .password(StringUtils.isNotBlank(setting.getSystemDataSettingPassword()) ? setting.getSystemDataSettingPassword() : null);

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
                    Wrappers.lambdaQuery()
            )) {

                Integer systemDataSettingId = systemDataSetting.getSystemDataSettingId();
                try {
                    if (systemDataSetting.getSystemDataSettingBackupStatus() == 1) {
                        startBackup(systemDataSettingId);
                        continue;
                    }
                } catch (Exception ignored) {
                }

                Boolean systemDataSettingIdle = systemDataSetting.getSystemDataSettingIdle();
                if (null != systemDataSettingIdle && systemDataSettingIdle) {
                    getRoot(systemDataSettingId, new MetadataQuery());
                }
            }
        });
    }

    @Override
    public List<String> listTables(Integer settingId) {
        HierarchyMetadata metadata = getOrCreateHierarchy(settingId);
        List<String> tables = new ArrayList<>();
        try {
            // 获取根节点（通常是数据库/schema）
            HierarchyResult<HierarchyNode> rootResult = metadata.getRootNodes(new MetadataQuery());
            if (rootResult != null && rootResult.getData() != null) {
                for (HierarchyNode rootNode : rootResult.getData()) {
                    // 获取表节点
                    HierarchyResult<HierarchyNode> childResult = metadata.getChildNodes(rootNode.getPath(), new MetadataQuery());
                    if (childResult != null && childResult.getData() != null) {
                        for (HierarchyNode child : childResult.getData()) {
                            HierarchyNode.NodeType nodeType = child.getType();
                            if (nodeType != null && nodeType == HierarchyNode.NodeType.TABLE) {
                                tables.add(child.getName());
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("获取表名列表失败", e);
        }
        return tables;
    }

    @Override
    public List<ColumnInfo> listColumns(Integer settingId, String tableName) {
        HierarchyMetadata metadata = getOrCreateHierarchy(settingId);
        List<ColumnInfo> columns = new ArrayList<>();
        try {
            // 查找表节点
            String tablePath = findTablePath(metadata, tableName);
            if (tablePath == null) {
                return columns;
            }
            // 获取字段节点
            HierarchyResult<HierarchyNode> childResult = metadata.getChildNodes(tablePath, new MetadataQuery());
            if (childResult != null && childResult.getData() != null) {
                int position = 1;
                for (HierarchyNode child : childResult.getData()) {
                    HierarchyNode.NodeType nodeType = child.getType();
                    if (nodeType != null && (nodeType == HierarchyNode.NodeType.COLUMN || nodeType == HierarchyNode.NodeType.FIELD)) {
                        Map<String, Object> props = child.getProperties();
                        ColumnInfo info = ColumnInfo.builder()
                                .name(child.getName())
                                .dataType(MapUtils.getString(props, "columnType", ""))
                                .fullType(MapUtils.getString(props, "columnType", ""))
                                .length(MapUtils.getInteger(props, "columnSize", null))
                                .nullable("YES".equalsIgnoreCase(MapUtils.getString(props, "nullable", "YES")))
                                .primaryKey(MapUtils.getBoolean(props, "primaryKey", false))
                                .autoIncrement(MapUtils.getBoolean(props, "autoIncrement", false))
                                .defaultValue(MapUtils.getString(props, "defaultValue", null))
                                .comment(MapUtils.getString(props, "comment", ""))
                                .ordinalPosition(position++)
                                .build();
                        columns.add(info);
                    }
                }
            }
        } catch (Exception e) {
            log.error("获取字段列表失败", e);
        }
        return columns;
    }

    @Override
    public TableStructureInfo getTableStructure(Integer settingId, String tableName) {
        List<ColumnInfo> columns = listColumns(settingId, tableName);
        List<String> primaryKeys = columns.stream()
                .filter(c -> Boolean.TRUE.equals(c.getPrimaryKey()))
                .map(ColumnInfo::getName)
                .collect(Collectors.toList());

        // 获取建表DDL
        String ddl = getCreateTableDdl(settingId, tableName);

        // 从DDL中解析表注释
        String tableComment = parseTableCommentFromDdl(ddl);

        return TableStructureInfo.builder()
                .tableName(tableName)
                .tableComment(tableComment)
                .columns(columns)
                .primaryKeys(primaryKeys)
                .createTableDdl(ddl)
                .indexes(new ArrayList<>())
                .build();
    }

    /**
     * 从DDL中解析表注释
     */
    private String parseTableCommentFromDdl(String ddl) {
        if (StringUtils.isBlank(ddl)) {
            return "";
        }
        // 匹配 COMMENT='xxx' 或 COMMENT 'xxx'
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(
                "COMMENT\\s*=?\\s*['\"]([^'\"]*)['\"]",
                java.util.regex.Pattern.CASE_INSENSITIVE
        );
        java.util.regex.Matcher matcher = pattern.matcher(ddl);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "";
    }

    @Override
    public Boolean addColumn(Integer settingId, StructureMetadata.AddColumnRequest req) {
        SystemDataSetting setting = systemDataSettingMapper.selectById(settingId);
        if (setting == null) {
            throw new RuntimeException("数据源配置不存在");
        }

        Dialect dialect = getDialect(setting);
        char openQuote = dialect.openQuote();
        char closeQuote = dialect.closeQuote();

        StringBuilder sql = new StringBuilder();
        sql.append("ALTER TABLE ").append(quoteIdentifier(req.getTableName(), openQuote, closeQuote));
        sql.append(" ADD COLUMN ").append(quoteIdentifier(req.getColumnName(), openQuote, closeQuote));
        sql.append(" ").append(req.getDataType());

        if (Boolean.FALSE.equals(req.getNullable())) {
            sql.append(" NOT NULL");
        }
        if (StringUtils.isNotBlank(req.getDefaultValue())) {
            sql.append(" DEFAULT ").append(req.getDefaultValue());
        }
        if (StringUtils.isNotBlank(req.getComment())) {
            sql.append(" COMMENT '").append(req.getComment().replace("'", "''")).append("'");
        }
        // 位置控制
        if ("FIRST".equalsIgnoreCase(req.getPosition())) {
            sql.append(" FIRST");
        } else if ("AFTER".equalsIgnoreCase(req.getPosition()) && StringUtils.isNotBlank(req.getAfterColumn())) {
            sql.append(" AFTER ").append(quoteIdentifier(req.getAfterColumn(), openQuote, closeQuote));
        }

        return executeAlterSql(settingId, sql.toString());
    }

    @Override
    public Boolean modifyColumn(Integer settingId, StructureMetadata.ModifyColumnRequest req) {
        SystemDataSetting setting = systemDataSettingMapper.selectById(settingId);
        if (setting == null) {
            throw new RuntimeException("数据源配置不存在");
        }

        Dialect dialect = getDialect(setting);
        char openQuote = dialect.openQuote();
        char closeQuote = dialect.closeQuote();

        StringBuilder sql = new StringBuilder();
        sql.append("ALTER TABLE ").append(quoteIdentifier(req.getTableName(), openQuote, closeQuote));

        // 判断是否需要重命名
        if (StringUtils.isNotBlank(req.getNewColumnName()) && !req.getNewColumnName().equals(req.getOldColumnName())) {
            // MySQL使用CHANGE COLUMN重命名
            sql.append(" CHANGE COLUMN ").append(quoteIdentifier(req.getOldColumnName(), openQuote, closeQuote));
            sql.append(" ").append(quoteIdentifier(req.getNewColumnName(), openQuote, closeQuote));
        } else {
            // MySQL使用MODIFY COLUMN修改字段
            sql.append(" MODIFY COLUMN ").append(quoteIdentifier(req.getOldColumnName(), openQuote, closeQuote));
        }

        sql.append(" ").append(req.getDataType());

        if (Boolean.FALSE.equals(req.getNullable())) {
            sql.append(" NOT NULL");
        } else {
            sql.append(" NULL");
        }
        if (StringUtils.isNotBlank(req.getDefaultValue())) {
            sql.append(" DEFAULT ").append(req.getDefaultValue());
        }
        if (StringUtils.isNotBlank(req.getComment())) {
            sql.append(" COMMENT '").append(req.getComment().replace("'", "''")).append("'");
        }

        return executeAlterSql(settingId, sql.toString());
    }

    @Override
    public Boolean dropColumn(Integer settingId, StructureMetadata.DropColumnRequest req) {
        SystemDataSetting setting = systemDataSettingMapper.selectById(settingId);
        if (setting == null) {
            throw new RuntimeException("数据源配置不存在");
        }

        Dialect dialect = getDialect(setting);
        char openQuote = dialect.openQuote();
        char closeQuote = dialect.closeQuote();

        String sql = "ALTER TABLE " + quoteIdentifier(req.getTableName(), openQuote, closeQuote)
                + " DROP COLUMN " + quoteIdentifier(req.getColumnName(), openQuote, closeQuote);
        return executeAlterSql(settingId, sql);
    }

    @Override
    public Boolean batchDropColumn(Integer settingId, StructureMetadata.BatchDropColumnRequest req) {
        if (req.getColumnNames() == null || req.getColumnNames().isEmpty()) {
            return true;
        }
        for (String columnName : req.getColumnNames()) {
            StructureMetadata.DropColumnRequest dropReq = new StructureMetadata.DropColumnRequest();
            dropReq.setTableName(req.getTableName());
            dropReq.setColumnName(columnName);
            dropColumn(settingId, dropReq);
        }
        return true;
    }

    @Override
    public Boolean batchAddColumn(Integer settingId, StructureMetadata.BatchAddColumnRequest req) {
        if (req.getColumns() == null || req.getColumns().isEmpty()) {
            return true;
        }
        for (StructureMetadata.AddColumnRequest addReq : req.getColumns()) {
            addReq.setTableName(req.getTableName());
            addColumn(settingId, addReq);
        }
        return true;
    }

    @Override
    public Boolean batchModifyColumn(Integer settingId, StructureMetadata.BatchModifyColumnRequest req) {
        if (req.getColumns() == null || req.getColumns().isEmpty()) {
            return true;
        }
        for (StructureMetadata.ModifyColumnRequest modifyReq : req.getColumns()) {
            modifyReq.setTableName(req.getTableName());
            modifyColumn(settingId, modifyReq);
        }
        return true;
    }

    @Override
    public Boolean reorderColumn(Integer settingId, StructureMetadata.ReorderColumnRequest req) {
        SystemDataSetting setting = systemDataSettingMapper.selectById(settingId);
        if (setting == null) {
            throw new RuntimeException("数据源配置不存在");
        }

        Dialect dialect = getDialect(setting);
        char openQuote = dialect.openQuote();
        char closeQuote = dialect.closeQuote();

        // 先获取字段信息
        List<ColumnInfo> columns = listColumns(settingId, req.getTableName());
        ColumnInfo targetColumn = columns.stream()
                .filter(c -> c.getName().equals(req.getColumnName()))
                .findFirst()
                .orElse(null);

        if (targetColumn == null) {
            throw new RuntimeException("字段不存在: " + req.getColumnName());
        }

        StringBuilder sql = new StringBuilder();
        sql.append("ALTER TABLE ").append(quoteIdentifier(req.getTableName(), openQuote, closeQuote));
        sql.append(" MODIFY COLUMN ").append(quoteIdentifier(req.getColumnName(), openQuote, closeQuote));
        sql.append(" ").append(targetColumn.getFullType());

        if (Boolean.FALSE.equals(targetColumn.getNullable())) {
            sql.append(" NOT NULL");
        }
        if (StringUtils.isNotBlank(targetColumn.getDefaultValue())) {
            sql.append(" DEFAULT ").append(targetColumn.getDefaultValue());
        }
        if (StringUtils.isNotBlank(targetColumn.getComment())) {
            sql.append(" COMMENT '").append(targetColumn.getComment().replace("'", "''")).append("'");
        }

        // 位置控制
        if ("FIRST".equalsIgnoreCase(req.getPosition())) {
            sql.append(" FIRST");
        } else if ("AFTER".equalsIgnoreCase(req.getPosition()) && StringUtils.isNotBlank(req.getAfterColumn())) {
            sql.append(" AFTER ").append(quoteIdentifier(req.getAfterColumn(), openQuote, closeQuote));
        }

        return executeAlterSql(settingId, sql.toString());
    }

    @Override
    public String getCreateTableDdl(Integer settingId, String tableName) {
        try {
            ReturnResult<CommandResult> result = execute(settingId, "sql", "SHOW CREATE TABLE " + tableName, new MetadataQuery());
            if (result != null && result.isSuccess() && result.getData() != null) {
                CommandResult commandResult = result.getData();
                ResultItem resultItem = commandResult.getData();
                if (resultItem != null) {
                    List<Map<String, Object>> rows = resultItem.getRows();
                    if (rows != null && !rows.isEmpty()) {
                        Map<String, Object> rowData = rows.get(0);
                        // MySQL 返回的是 "Create Table" 列
                        Object ddl = rowData.get("Create Table");
                        if (ddl == null) {
                            // 尝试其他可能的列名
                            ddl = rowData.get("CREATE TABLE");
                            if (ddl == null && !rowData.isEmpty()) {
                                // 取第二个值（第一个是表名）
                                Object[] values = rowData.values().toArray();
                                if (values.length > 1) {
                                    ddl = values[1];
                                }
                            }
                        }
                        return ddl != null ? ddl.toString() : "";
                    }
                }
            }
        } catch (Exception e) {
            log.error("获取建表DDL失败", e);
        }
        return "";
    }

    /**
     * 执行 ALTER SQL
     */
    private Boolean executeAlterSql(Integer settingId, String sql) {
        try {
            log.info("执行DDL: {}", sql);
            MetadataQuery metadataQuery = new MetadataQuery();
            metadataQuery.setQueryId("executeAlterSql");
            ReturnResult<CommandResult> result = execute(settingId, "sql", sql, metadataQuery);
            if (result == null) {
                throw new RuntimeException("执行失败: 返回结果为空");
            }
            if (!result.isSuccess()) {
                String errorMsg = result.getMsg();
                // 提供更友好的错误提示
                if (errorMsg != null && errorMsg.contains("Data truncated")) {
                    throw new RuntimeException("数据截断错误: 现有数据长度超过新定义的字段长度，请先检查并处理超长数据");
                }
                if (errorMsg != null && errorMsg.contains("cannot be null")) {
                    throw new RuntimeException("空值约束错误: 现有数据包含NULL值，无法设置为NOT NULL，请先处理NULL数据");
                }
                throw new RuntimeException("执行失败: " + errorMsg);
            }
            return true;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            log.error("执行DDL失败: {}", sql, e);
            throw new RuntimeException("执行失败: " + e.getMessage());
        }
    }

    @Override
    public Boolean modifyTableComment(Integer settingId, StructureMetadata.ModifyTableCommentRequest req) {
        SystemDataSetting setting = systemDataSettingMapper.selectById(settingId);
        if (setting == null) {
            throw new RuntimeException("数据源配置不存在");
        }

        Dialect dialect = getDialect(setting);
        char openQuote = dialect.openQuote();
        char closeQuote = dialect.closeQuote();

        String tableName = req.getTableName();
        String comment = req.getComment();
        String escapedComment = comment != null ? comment.replace("'", "''") : "";

        String sql = "ALTER TABLE " + quoteIdentifier(tableName, openQuote, closeQuote)
                + " COMMENT '" + escapedComment + "'";
        return executeAlterSql(settingId, sql);
    }

    @Override
    public Boolean batchModifyTableStructure(Integer settingId, StructureMetadata.BatchTableStructureRequest req) {
        SystemDataSetting setting = systemDataSettingMapper.selectById(settingId);
        if (setting == null) {
            throw new RuntimeException("数据源配置不存在");
        }

        log.info("开始批量修改表结构: {}", req.getTableName());

        // 1. 先删除字段
        if (req.getDropColumns() != null && !req.getDropColumns().isEmpty()) {
            for (String columnName : req.getDropColumns()) {
                StructureMetadata.DropColumnRequest dropReq = new StructureMetadata.DropColumnRequest();
                dropReq.setTableName(req.getTableName());
                dropReq.setColumnName(columnName);
                dropColumn(settingId, dropReq);
            }
        }

        // 2. 添加新字段
        if (req.getAddColumns() != null && !req.getAddColumns().isEmpty()) {
            for (StructureMetadata.AddColumnRequest addReq : req.getAddColumns()) {
                addReq.setTableName(req.getTableName());
                addColumn(settingId, addReq);
            }
        }

        // 3. 修改已有字段
        if (req.getModifyColumns() != null && !req.getModifyColumns().isEmpty()) {
            for (StructureMetadata.ModifyColumnRequest modifyReq : req.getModifyColumns()) {
                modifyReq.setTableName(req.getTableName());
                modifyColumn(settingId, modifyReq);
            }
        }

        // 4. 调整字段顺序
        if (req.getReorderColumns() != null && !req.getReorderColumns().isEmpty()) {
            for (StructureMetadata.ReorderColumnRequest reorderReq : req.getReorderColumns()) {
                reorderReq.setTableName(req.getTableName());
                reorderColumn(settingId, reorderReq);
            }
        }

        // 5. 修改表注释
        if (StringUtils.isNotBlank(req.getTableComment())) {
            StructureMetadata.ModifyTableCommentRequest commentReq = new StructureMetadata.ModifyTableCommentRequest();
            commentReq.setTableName(req.getTableName());
            commentReq.setComment(req.getTableComment());
            modifyTableComment(settingId, commentReq);
        }

        log.info("批量修改表结构完成: {}", req.getTableName());
        return true;
    }

    /**
     * 获取数据库方言
     */
    private Dialect getDialect(SystemDataSetting setting) {
        String url = setting.getSystemDataSettingServer();
        String driver = setting.getSystemDataSettingDriverClass();
        return DialectFactory.createDialect(url, driver);
    }

    /**
     * 引用标识符（表名、字段名等）
     */
    private String quoteIdentifier(String identifier, char openQuote, char closeQuote) {
        if (identifier == null) {
            return "";
        }
        // 如果引号是空格，则不添加引号
        if (openQuote == ' ' && closeQuote == ' ') {
            return identifier;
        }
        return openQuote + identifier + closeQuote;
    }

    /**
     * 查找表路径
     */
    private String findTablePath(HierarchyMetadata metadata, String tableName) {
        try {
            HierarchyResult<HierarchyNode> rootResult = metadata.getRootNodes(new MetadataQuery());
            if (rootResult != null && rootResult.getData() != null) {
                for (HierarchyNode rootNode : rootResult.getData()) {
                    HierarchyResult<HierarchyNode> childResult = metadata.getChildNodes(rootNode.getPath(), new MetadataQuery());
                    if (childResult != null && childResult.getData() != null) {
                        for (HierarchyNode child : childResult.getData()) {
                            if (tableName.equalsIgnoreCase(child.getName())) {
                                return child.getPath();
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("查找表路径失败", e);
        }
        return null;
    }

    @Override
    public Boolean updateTableRow(Integer settingId, UpdateRowRequest req) {
        try {
            SystemDataSetting setting = systemDataSettingMapper.selectById(settingId);
            if (setting == null) {
                log.error("数据源配置不存在: {}", settingId);
                return false;
            }
            Dialect dialect = getDialect(setting);
            char openQuote = dialect.openQuote();
            char closeQuote = dialect.closeQuote();

            // 构建 UPDATE SQL
            String tableName = quoteIdentifier(req.getTableName(), openQuote, closeQuote);
            String columnName = quoteIdentifier(req.getColumnName(), openQuote, closeQuote);
            String primaryKey = quoteIdentifier(req.getPrimaryKey(), openQuote, closeQuote);

            String sql;
            if (req.getNewValue() == null) {
                sql = String.format("UPDATE %s SET %s = NULL WHERE %s = '%s'",
                        tableName, columnName, primaryKey, req.getPrimaryValue());
            } else {
                sql = String.format("UPDATE %s SET %s = '%s' WHERE %s = '%s'",
                        tableName, columnName, req.getNewValue(), primaryKey, req.getPrimaryValue());
            }

            log.info("执行更新SQL: {}", sql);
            DataSourceOptions options = buildOptions(setting);
            CommandMetadata commandMetadata = CommandMetadataFactory.create(setting.getSystemDataSettingId(), setting.getSystemDataSettingType(), options);
            CommandResult result = commandMetadata.executeCommand(sql);

            return result != null && result.isSuccess();
        } catch (Exception e) {
            log.error("更新表数据行失败", e);
            return false;
        }
    }

    @Override
    public Boolean deleteTableRow(Integer settingId, DeleteRowRequest req) {
        try {
            SystemDataSetting setting = systemDataSettingMapper.selectById(settingId);
            if (setting == null) {
                log.error("数据源配置不存在: {}", settingId);
                return false;
            }
            Dialect dialect = getDialect(setting);
            char openQuote = dialect.openQuote();
            char closeQuote = dialect.closeQuote();

            // 构建 DELETE SQL
            String tableName = quoteIdentifier(req.getTableName(), openQuote, closeQuote);
            String primaryKey = quoteIdentifier(req.getPrimaryKey(), openQuote, closeQuote);

            String sql = String.format("DELETE FROM %s WHERE %s = '%s'",
                    tableName, primaryKey, req.getPrimaryValue());

            log.info("执行删除SQL: {}", sql);
            DataSourceOptions options = buildOptions(setting);
            CommandMetadata commandMetadata = CommandMetadataFactory.create(setting.getSystemDataSettingId(), setting.getSystemDataSettingType(), options);
            CommandResult result = commandMetadata.executeCommand(sql);

            return result != null && result.isSuccess();
        } catch (Exception e) {
            log.error("删除表数据行失败", e);
            return false;
        }
    }

    @Override
    public Boolean insertTableRow(Integer settingId, InsertRowRequest req) {
        try {
            SystemDataSetting setting = systemDataSettingMapper.selectById(settingId);
            if (setting == null) {
                log.error("数据源配置不存在: {}", settingId);
                return false;
            }
            Dialect dialect = getDialect(setting);
            char openQuote = dialect.openQuote();
            char closeQuote = dialect.closeQuote();

            Map<String, Object> data = req.getData();
            if (data == null || data.isEmpty()) {
                log.error("插入数据为空");
                return false;
            }

            // 构建 INSERT SQL
            String tableName = quoteIdentifier(req.getTableName(), openQuote, closeQuote);
            StringBuilder columns = new StringBuilder();
            StringBuilder values = new StringBuilder();

            boolean first = true;
            for (Map.Entry<String, Object> entry : data.entrySet()) {
                if (!first) {
                    columns.append(", ");
                    values.append(", ");
                }
                columns.append(quoteIdentifier(entry.getKey(), openQuote, closeQuote));
                if (entry.getValue() == null) {
                    values.append("NULL");
                } else {
                    values.append("'").append(entry.getValue()).append("'");
                }
                first = false;
            }

            String sql = String.format("INSERT INTO %s (%s) VALUES (%s)",
                    tableName, columns, values);

            log.info("执行插入SQL: {}", sql);
            DataSourceOptions options = buildOptions(setting);
            CommandMetadata commandMetadata = CommandMetadataFactory.create(setting.getSystemDataSettingId(), setting.getSystemDataSettingType(), options);
            CommandResult result = commandMetadata.executeCommand(sql);

            return result != null && result.isSuccess();
        } catch (Exception e) {
            log.error("插入表数据行失败", e);
            return false;
        }
    }

    // ==================== 索引管理 ====================

    @Override
    public List<Map<String, Object>> getTableIndexes(Integer settingId, String tableName) {
        try {
            log.info("获取表索引列表: settingId={}, tableName={}", settingId, tableName);
            SystemDataSetting setting = systemDataSettingMapper.selectById(settingId);
            if (setting == null) {
                log.warn("数据源设置不存在: settingId={}", settingId);
                return Collections.emptyList();
            }

            DataSourceOptions options = buildOptions(setting);
            StructureMetadata structureMetadata = ServiceProvider.of(StructureMetadata.class)
                    .getNewExtension(setting.getSystemDataSettingType(), options);

            if (structureMetadata == null) {
                log.warn("不支持的数据源类型: {}", setting.getSystemDataSettingType());
                return Collections.emptyList();
            }

            List<Map<String, Object>> indexes = structureMetadata.getIndexes(tableName);
            log.debug("获取到 {} 个索引", indexes.size());
            return indexes;
        } catch (Exception e) {
            log.error("获取表索引列表失败", e);
            return Collections.emptyList();
        }
    }

    @Override
    public Boolean createIndex(Integer settingId, StructureMetadata.CreateIndexRequest req) {
        try {
            log.info("创建索引: settingId={}, request={}", settingId, req);
            SystemDataSetting setting = systemDataSettingMapper.selectById(settingId);
            if (setting == null) {
                log.warn("数据源设置不存在: settingId={}", settingId);
                return false;
            }

            DataSourceOptions options = buildOptions(setting);
            StructureMetadata structureMetadata = ServiceProvider.of(StructureMetadata.class)
                    .getNewExtension(setting.getSystemDataSettingType(), options);

            if (structureMetadata == null) {
                log.warn("不支持的数据源类型: {}", setting.getSystemDataSettingType());
                return false;
            }

            boolean success = structureMetadata.createIndex(req);
            log.info("创建索引{}: {}", success ? "成功" : "失败", req.getIndexName());
            return success;
        } catch (Exception e) {
            log.error("创建索引失败", e);
            return false;
        }
    }

    @Override
    public Boolean dropIndex(Integer settingId, String tableName, String indexName) {
        try {
            log.info("删除索引: settingId={}, tableName={}, indexName={}", settingId, tableName, indexName);
            SystemDataSetting setting = systemDataSettingMapper.selectById(settingId);
            if (setting == null) {
                log.warn("数据源设置不存在: settingId={}", settingId);
                return false;
            }

            DataSourceOptions options = buildOptions(setting);
            StructureMetadata structureMetadata = ServiceProvider.of(StructureMetadata.class)
                    .getNewExtension(setting.getSystemDataSettingType(), options);

            if (structureMetadata == null) {
                log.warn("不支持的数据源类型: {}", setting.getSystemDataSettingType());
                return false;
            }

            boolean success = structureMetadata.dropIndex(tableName, indexName);
            log.info("删除索引{}: {}", success ? "成功" : "失败", indexName);
            return success;
        } catch (Exception e) {
            log.error("删除索引失败", e);
            return false;
        }
    }

    // ==================== 分区管理 ====================

    @Override
    public List<Map<String, Object>> getTablePartitions(Integer settingId, String tableName) {
        try {
            log.info("获取表分区列表: settingId={}, tableName={}", settingId, tableName);
            SystemDataSetting setting = systemDataSettingMapper.selectById(settingId);
            if (setting == null) {
                log.warn("数据源设置不存在: settingId={}", settingId);
                return Collections.emptyList();
            }

            DataSourceOptions options = buildOptions(setting);
            StructureMetadata structureMetadata = ServiceProvider.of(StructureMetadata.class)
                    .getNewExtension(setting.getSystemDataSettingType(), options);

            if (structureMetadata == null) {
                log.warn("不支持的数据源类型: {}", setting.getSystemDataSettingType());
                return Collections.emptyList();
            }

            List<Map<String, Object>> partitions = structureMetadata.getPartitions(tableName);
            log.debug("获取到 {} 个分区", partitions.size());
            return partitions;
        } catch (Exception e) {
            log.error("获取表分区列表失败", e);
            return Collections.emptyList();
        }
    }

    @Override
    public Boolean createPartition(Integer settingId, StructureMetadata.CreatePartitionRequest req) {
        try {
            log.info("创建分区: settingId={}, request={}", settingId, req);
            SystemDataSetting setting = systemDataSettingMapper.selectById(settingId);
            if (setting == null) {
                log.warn("数据源设置不存在: settingId={}", settingId);
                return false;
            }

            DataSourceOptions options = buildOptions(setting);
            StructureMetadata structureMetadata = ServiceProvider.of(StructureMetadata.class)
                    .getNewExtension(setting.getSystemDataSettingType(), options);

            if (structureMetadata == null) {
                log.warn("不支持的数据源类型: {}", setting.getSystemDataSettingType());
                return false;
            }

            boolean success = structureMetadata.createPartition(req);
            log.info("创建分区{}: {}", success ? "成功" : "失败", req.getPartitionName());
            return success;
        } catch (Exception e) {
            log.error("创建分区失败", e);
            return false;
        }
    }

    @Override
    public Boolean dropPartition(Integer settingId, String tableName, String partitionName) {
        try {
            log.info("删除分区: settingId={}, tableName={}, partitionName={}", settingId, tableName, partitionName);
            SystemDataSetting setting = systemDataSettingMapper.selectById(settingId);
            if (setting == null) {
                log.warn("数据源设置不存在: settingId={}", settingId);
                return false;
            }

            DataSourceOptions options = buildOptions(setting);
            StructureMetadata structureMetadata = ServiceProvider.of(StructureMetadata.class)
                    .getNewExtension(setting.getSystemDataSettingType(), options);

            if (structureMetadata == null) {
                log.warn("不支持的数据源类型: {}", setting.getSystemDataSettingType());
                return false;
            }

            boolean success = structureMetadata.dropPartition(tableName, partitionName);
            log.info("删除分区{}: {}", success ? "成功" : "失败", partitionName);
            return success;
        } catch (Exception e) {
            log.error("删除分区失败", e);
            return false;
        }
    }

    @Override
    public Map<String, Boolean> getDatabaseCapabilities(Integer settingId) {
        Map<String, Boolean> capabilities = new HashMap<>();
        try {
            log.info("检测数据库能力: settingId={}", settingId);
            SystemDataSetting setting = systemDataSettingMapper.selectById(settingId);
            if (setting == null) {
                log.warn("数据源设置不存在: settingId={}", settingId);
                capabilities.put("supportsIndexes", false);
                capabilities.put("supportsPartitions", false);
                return capabilities;
            }

            String dbType = setting.getSystemDataSettingType();
            log.debug("数据库类型: {}", dbType);

            // 根据数据库类型判断能力
            boolean supportsIndexes = true; // 大部分数据库都支持索引
            boolean supportsPartitions = false;

            if (dbType != null) {
                String type = dbType.toLowerCase();
                // MySQL 5.7+ 支持分区
                if (type.contains("mysql")) {
                    supportsPartitions = true;
                }
                // PostgreSQL 10+ 支持分区
                else if (type.contains("postgresql") || type.contains("postgres")) {
                    supportsPartitions = true;
                }
                // Oracle 支持分区
                else if (type.contains("oracle")) {
                    supportsPartitions = true;
                }
                // SQL Server 2016+ 支持分区
                else if (type.contains("sqlserver") || type.contains("mssql")) {
                    supportsPartitions = true;
                }
            }

            capabilities.put("supportsIndexes", supportsIndexes);
            capabilities.put("supportsPartitions", supportsPartitions);
            log.info("数据库能力检测结果: supportsIndexes={}, supportsPartitions={}", 
                    supportsIndexes, supportsPartitions);
            return capabilities;
        } catch (Exception e) {
            log.error("检测数据库能力失败", e);
            capabilities.put("supportsIndexes", false);
            capabilities.put("supportsPartitions", false);
            return capabilities;
        }
    }
}


