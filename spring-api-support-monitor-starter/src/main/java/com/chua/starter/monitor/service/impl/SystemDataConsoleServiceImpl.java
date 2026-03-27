package com.chua.starter.monitor.service.impl;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
// Backup 已重构为 Engine 的 BackupFeature 接口，相关功能需要通过 Engine 实例实现
import com.chua.common.support.base.collection.ConcurrentReferenceHashMap;
import com.chua.common.support.core.constant.EventType;
import com.chua.common.support.data.datasource.dialect.Dialect;
import com.chua.common.support.data.datasource.dialect.DialectFactory;
import com.chua.common.support.data.datasource.jdbc.option.DataSourceOptions;
import com.chua.common.support.data.query.FeatureSupportLevel;
import com.chua.common.support.document.parser.DocQuery;
import com.chua.common.support.document.parser.Document;
import com.chua.common.support.text.json.Json;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.metadata.base.ConstructMetadata;
import com.chua.common.support.metadata.command.CommandMetadata;
import com.chua.common.support.metadata.command.CommandResult;
import com.chua.common.support.metadata.command.ResultItem;
import com.chua.common.support.metadata.hierarchy.HierarchyMetadata;
import com.chua.common.support.metadata.hierarchy.HierarchyNode;
import com.chua.common.support.metadata.hierarchy.HierarchyResult;
import com.chua.common.support.metadata.listen.ListenMetadata;
import com.chua.common.support.metadata.query.MetadataQuery;
import com.chua.common.support.storage.oss.result.GetObjectResult;
import com.chua.common.support.core.spi.ServiceProvider;
import com.chua.common.support.core.utils.IoUtils;
import com.chua.common.support.core.utils.MapUtils;
import com.chua.common.support.storage.backup.Backup;
import com.chua.common.support.storage.backup.BackupSetting;
import com.chua.redis.support.constant.RedisConstant;
import com.chua.socket.support.session.SocketSessionTemplate;
import com.chua.starter.monitor.engine.SystemDataCapability;
import com.chua.starter.monitor.engine.SystemDataEngine;
import com.chua.starter.monitor.engine.SystemDataEngineFactory;
import com.chua.starter.monitor.engine.feature.SystemDataClusterFeature;
import com.chua.starter.monitor.engine.feature.SystemDataNodeFeature;
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
import org.springframework.boot.CommandLineRunner;
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
public class SystemDataConsoleServiceImpl implements SystemDataConsoleService, CommandLineRunner {

    private final SystemDataSettingMapper systemDataSettingMapper;
    private final SocketSessionTemplate socketSessionTemplate;
    private final RedisSearchService redisSearchService;
    private final SystemDataEngineFactory systemDataEngineFactory;
    private final Map<Integer, Backup> backupEngineMap = new ConcurrentReferenceHashMap<>();
    @Autowired
    private SystemDataFieldCommentService systemDataFieldCommentService;

    private SystemDataSetting getSettingOrThrow(Integer settingId) {
        SystemDataSetting setting = systemDataSettingMapper.selectById(settingId);
        if (setting == null) {
            throw new RuntimeException("配置不存在");
        }
        SystemDataSettingServiceImpl.decryptSensitiveData(setting);
        return setting;
    }

    private BackupSetting buildBackupSetting(SystemDataSetting setting) {
        String baseDir = System.getProperty("login.home", System.getProperty("user.dir", "."));
        return BackupSetting.builder()
                .path(baseDir + java.io.File.separator + "data-backup" + java.io.File.separator + "system-data"
                        + java.io.File.separator + setting.getSystemDataSettingId())
                .retentionDays(30)
                .expireDays(7)
                .build();
    }

    @Override
    public ReturnResult<HierarchyResult<HierarchyNode>> getRoot(Integer settingId, MetadataQuery query) {
        HierarchyMetadata metadata = null;
        try {
            metadata = getOrCreateHierarchy(settingId);
            return ReturnResult.ok(metadata.getRootNodes(query));
        } catch (Exception e) {
            log.error("获取根节点失败", e);
            if (metadata != null) {
                try {
                    metadata.close();
                } catch (Exception closeException) {
                    log.debug("关闭层级元数据失败", closeException);
                }
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
            } catch (Exception e) {
                log.debug("加载表字段节点失败: {}", nodePath, e);
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
            metadataQuery.setQueryId("1");
            ReturnResult<CommandResult> data1 = execute(settingId, "data", "SELECT * FROM " + schemaName + "." + tableName, metadataQuery);
            if (data1.isOk()) {
                CommandResult data = data1.getData();
                resp.setRows(data.getData().getRows());
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
        } catch (RuntimeException e) {
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
        SystemDataSetting setting = getSettingOrThrow(settingId);
        try (SystemDataEngine engine = systemDataEngineFactory.create(setting)) {
            if (!engine.supports(SystemDataCapability.STRUCTURE)) {
                return null;
            }
            return engine.renameTable(nodePath, newName);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Boolean backupTable(Integer settingId, String nodePath, String backupName) {
        SystemDataSetting setting = getSettingOrThrow(settingId);
        try (SystemDataEngine engine = systemDataEngineFactory.create(setting)) {
            if (!engine.supports(SystemDataCapability.TABLE_BACKUP)) {
                return null;
            }
            return engine.backupTable(nodePath, backupName);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ServerStatic getServerStatic() {
        return systemDataSettingMapper.getServerStatic();
    }

    @Override
    public ReturnResult<CommandResult> execute(Integer settingId, String type, String command, MetadataQuery query) {
        try {
            SystemDataSetting setting = getSettingOrThrow(settingId);

            if (command.startsWith("\"") && command.endsWith("\"")) {
                command = command.substring(1, command.length() - 1);
            }

            command = command.replace("\\n", " ");
            try (SystemDataEngine engine = systemDataEngineFactory.create(setting)) {
                CommandMetadata metadata = engine.command();
                CommandResult result = StringUtils.isBlank(type) ? metadata.executeCommand(command) : metadata.executeCommand(type, command, true);
                if (result.isSuccess() && query != null && StringUtils.isNotBlank(query.getQueryId())) {
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
            }
        } catch (Exception e) {
            log.error("执行命令失败", e);
            return ReturnResult.error(e.getMessage());
        }
    }

    @Override
    public ReturnResult<ResponseEntity<byte[]>> generateDocument(Integer settingId) {
        try {
            SystemDataSetting setting = getSettingOrThrow(settingId);
            try (SystemDataEngine engine = systemDataEngineFactory.create(setting)) {
                if (!engine.supports(SystemDataCapability.DOCUMENT)) {
                    return ReturnResult.error("文档不支持");
                }
                Document document = engine.document();
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
            }
        } catch (Exception e) {
            log.error("生成文档失败", e);
            return ReturnResult.error(e.getMessage());
        }
    }

    @Override
    public ReturnResult<Boolean> startBackup(Integer settingId) {
        try {
            SystemDataSetting setting = getSettingOrThrow(settingId);
            Backup old = backupEngineMap.get(settingId);
            if (old != null) {
                return ReturnResult.ok(true);
            }
            try (SystemDataEngine engine = systemDataEngineFactory.create(setting)) {
                if (!engine.supports(SystemDataCapability.BACKUP_LIFECYCLE)) {
                    return ReturnResult.error("当前数据源不支持备份生命周期");
                }
                Backup backup = engine.createBackup(buildBackupSetting(setting));
                backup.start();
                backupEngineMap.put(settingId, backup);
                setting.setSystemDataSettingBackupStatus(1);
                systemDataSettingMapper.updateById(setting);
                return ReturnResult.ok(true);
            }
        } catch (Exception e) {
            log.error("启动备份失败", e);
            return ReturnResult.error(e.getMessage());
        }
    }

    @Override
    public ReturnResult<Boolean> stopBackup(Integer settingId) {
        try {
            Backup backup = backupEngineMap.remove(settingId);
            if (backup != null) {
                backup.stop();
            }
            SystemDataSetting setting = systemDataSettingMapper.selectById(settingId);
            if (setting != null) {
                setting.setSystemDataSettingBackupStatus(0);
                systemDataSettingMapper.updateById(setting);
            }
            return ReturnResult.ok(true);
        } catch (Exception e) {
            log.error("停止备份失败", e);
            return ReturnResult.error(e.getMessage());
        }
    }

    @Override
    public ReturnResult<Boolean> backupStatus(Integer settingId) {
        if (backupEngineMap.containsKey(settingId)) {
            return ReturnResult.ok(true);
        }
        SystemDataSetting setting = systemDataSettingMapper.selectById(settingId);
        return ReturnResult.ok(setting != null && Integer.valueOf(1).equals(setting.getSystemDataSettingBackupStatus()));
    }

    @Override
    public HierarchyMetadata getOrCreateHierarchy(Integer settingId) {
        SystemDataSetting setting = getSettingOrThrow(settingId);
        SystemDataEngine engine = systemDataEngineFactory.create(setting);
        HierarchyMetadata hierarchy = engine.hierarchy();
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
        SystemDataSetting resolvedSetting = SystemDataSettingServiceImpl.copySetting(setting);
        SystemDataSettingServiceImpl.decryptSensitiveData(resolvedSetting);
        DataSourceOptions.DatabaseBuilder builder = DataSourceOptions.builder()
                .name(resolvedSetting.getSystemDataSettingName())
                .url(StringUtils.isNotBlank(resolvedSetting.getSystemDataSettingServer())
                        ? resolvedSetting.getSystemDataSettingServer()
                        : (resolvedSetting.getSystemDataSettingHost() + ":" + resolvedSetting.getSystemDataSettingPort()))
                .databaseName(resolvedSetting.getSystemDataSettingDatabase())
                .policy(resolvedSetting.getSystemDataSettingPolicy())
                .username(StringUtils.isNotBlank(resolvedSetting.getSystemDataSettingUsername()) ? resolvedSetting.getSystemDataSettingUsername() : null)
                .password(StringUtils.isNotBlank(resolvedSetting.getSystemDataSettingPassword()) ? resolvedSetting.getSystemDataSettingPassword() : null);

        // 如果上传过驱动文件，优先告知 DataSourceOptions 使用对应驱动
        if (StringUtils.isNotBlank(resolvedSetting.getSystemDataSettingDriverClass())) {
            builder.driverClassName(resolvedSetting.getSystemDataSettingDriverClass());
        }
        DataSourceOptions options = builder.build();
        if (StringUtils.isNotBlank(resolvedSetting.getSystemDataSettingDriverPath())) {
            options.setDriverPath(resolvedSetting.getSystemDataSettingDriverPath());
        }
        return options;
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
        SystemDataSetting setting = getSettingOrThrow(settingId);
        try (SystemDataEngine engine = systemDataEngineFactory.create(setting)) {
            return engine.addColumn(req);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Boolean modifyColumn(Integer settingId, StructureMetadata.ModifyColumnRequest req) {
        SystemDataSetting setting = getSettingOrThrow(settingId);
        try (SystemDataEngine engine = systemDataEngineFactory.create(setting)) {
            return engine.modifyColumn(req);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Boolean dropColumn(Integer settingId, StructureMetadata.DropColumnRequest req) {
        SystemDataSetting setting = getSettingOrThrow(settingId);
        try (SystemDataEngine engine = systemDataEngineFactory.create(setting)) {
            return engine.dropColumn(req);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Boolean batchDropColumn(Integer settingId, StructureMetadata.BatchDropColumnRequest req) {
        SystemDataSetting setting = getSettingOrThrow(settingId);
        try (SystemDataEngine engine = systemDataEngineFactory.create(setting)) {
            return engine.batchDropColumn(req);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Boolean batchAddColumn(Integer settingId, StructureMetadata.BatchAddColumnRequest req) {
        SystemDataSetting setting = getSettingOrThrow(settingId);
        try (SystemDataEngine engine = systemDataEngineFactory.create(setting)) {
            return engine.batchAddColumn(req);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Boolean batchModifyColumn(Integer settingId, StructureMetadata.BatchModifyColumnRequest req) {
        SystemDataSetting setting = getSettingOrThrow(settingId);
        try (SystemDataEngine engine = systemDataEngineFactory.create(setting)) {
            return engine.batchModifyColumn(req);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Boolean reorderColumn(Integer settingId, StructureMetadata.ReorderColumnRequest req) {
        SystemDataSetting setting = getSettingOrThrow(settingId);
        try (SystemDataEngine engine = systemDataEngineFactory.create(setting)) {
            return engine.reorderColumn(req);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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

    private Boolean executeParameterizedUpdate(SystemDataSetting setting, String sql, Object... args) {
        try (SystemDataEngine engine = systemDataEngineFactory.create(setting)) {
            if (engine.supports(SystemDataCapability.SQL_UPDATE)) {
                return engine.executeUpdate(sql, args) >= 0;
            }
            CommandMetadata commandMetadata = engine.command();
            CommandResult result = commandMetadata.executeCommand(sql);
            return result != null && result.isSuccess();
        } catch (Exception e) {
            log.error("执行参数化更新失败: {}", sql, e);
            return false;
        }
    }

    @Override
    public Boolean modifyTableComment(Integer settingId, StructureMetadata.ModifyTableCommentRequest req) {
        SystemDataSetting setting = getSettingOrThrow(settingId);
        try (SystemDataEngine engine = systemDataEngineFactory.create(setting)) {
            return engine.modifyTableComment(req);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Boolean batchModifyTableStructure(Integer settingId, StructureMetadata.BatchTableStructureRequest req) {
        SystemDataSetting setting = getSettingOrThrow(settingId);
        try (SystemDataEngine engine = systemDataEngineFactory.create(setting)) {
            return engine.batchModifyTableStructure(req);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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
            SystemDataSetting setting = getSettingOrThrow(settingId);
            try (SystemDataEngine engine = systemDataEngineFactory.create(setting)) {
                return engine.updateRow(req);
            }
        } catch (Exception e) {
            log.error("更新表数据行失败", e);
            return false;
        }
    }

    @Override
    public Boolean deleteTableRow(Integer settingId, DeleteRowRequest req) {
        try {
            SystemDataSetting setting = getSettingOrThrow(settingId);
            try (SystemDataEngine engine = systemDataEngineFactory.create(setting)) {
                return engine.deleteRow(req);
            }
        } catch (Exception e) {
            log.error("删除表数据行失败", e);
            return false;
        }
    }

    @Override
    public Boolean insertTableRow(Integer settingId, InsertRowRequest req) {
        try {
            SystemDataSetting setting = getSettingOrThrow(settingId);
            try (SystemDataEngine engine = systemDataEngineFactory.create(setting)) {
                return engine.insertRow(req);
            }
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
            SystemDataSetting setting = getSettingOrThrow(settingId);
            try (SystemDataEngine engine = systemDataEngineFactory.create(setting)) {
                if (!engine.supports(SystemDataCapability.INDEXES)) {
                    return Collections.emptyList();
                }
                List<Map<String, Object>> indexes = engine.getIndexes(tableName);
                log.debug("获取到 {} 个索引", indexes.size());
                return indexes;
            }
        } catch (Exception e) {
            log.error("获取表索引列表失败", e);
            return Collections.emptyList();
        }
    }

    @Override
    public Boolean createIndex(Integer settingId, StructureMetadata.CreateIndexRequest req) {
        try {
            log.info("创建索引: settingId={}, request={}", settingId, req);
            SystemDataSetting setting = getSettingOrThrow(settingId);
            try (SystemDataEngine engine = systemDataEngineFactory.create(setting)) {
                if (!engine.supports(SystemDataCapability.INDEXES)) {
                    return false;
                }
                boolean success = engine.createIndex(req);
                log.info("创建索引{}: {}", success ? "成功" : "失败", req.getIndexName());
                return success;
            }
        } catch (Exception e) {
            log.error("创建索引失败", e);
            return false;
        }
    }

    @Override
    public Boolean dropIndex(Integer settingId, String tableName, String indexName) {
        try {
            log.info("删除索引: settingId={}, tableName={}, indexName={}", settingId, tableName, indexName);
            SystemDataSetting setting = getSettingOrThrow(settingId);
            try (SystemDataEngine engine = systemDataEngineFactory.create(setting)) {
                if (!engine.supports(SystemDataCapability.INDEXES)) {
                    return false;
                }
                boolean success = engine.dropIndex(tableName, indexName);
                log.info("删除索引{}: {}", success ? "成功" : "失败", indexName);
                return success;
            }
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
            SystemDataSetting setting = getSettingOrThrow(settingId);
            try (SystemDataEngine engine = systemDataEngineFactory.create(setting)) {
                if (!engine.supports(SystemDataCapability.PARTITIONS)) {
                    return Collections.emptyList();
                }
                List<Map<String, Object>> partitions = engine.getPartitions(tableName);
                log.debug("获取到 {} 个分区", partitions.size());
                return partitions;
            }
        } catch (Exception e) {
            log.error("获取表分区列表失败", e);
            return Collections.emptyList();
        }
    }

    @Override
    public Boolean createPartition(Integer settingId, StructureMetadata.CreatePartitionRequest req) {
        try {
            log.info("创建分区: settingId={}, request={}", settingId, req);
            SystemDataSetting setting = getSettingOrThrow(settingId);
            try (SystemDataEngine engine = systemDataEngineFactory.create(setting)) {
                if (!engine.supports(SystemDataCapability.PARTITIONS)) {
                    return false;
                }
                boolean success = engine.createPartition(req);
                log.info("创建分区{}: {}", success ? "成功" : "失败", req.getPartitionName());
                return success;
            }
        } catch (Exception e) {
            log.error("创建分区失败", e);
            return false;
        }
    }

    @Override
    public Boolean dropPartition(Integer settingId, String tableName, String partitionName) {
        try {
            log.info("删除分区: settingId={}, tableName={}, partitionName={}", settingId, tableName, partitionName);
            SystemDataSetting setting = getSettingOrThrow(settingId);
            try (SystemDataEngine engine = systemDataEngineFactory.create(setting)) {
                if (!engine.supports(SystemDataCapability.PARTITIONS)) {
                    return false;
                }
                boolean success = engine.dropPartition(tableName, partitionName);
                log.info("删除分区{}: {}", success ? "成功" : "失败", partitionName);
                return success;
            }
        } catch (Exception e) {
            log.error("删除分区失败", e);
            return false;
        }
    }

    @Override
    public Map<String, Boolean> getDatabaseCapabilities(Integer settingId) {
        Map<String, Boolean> capabilities = new LinkedHashMap<>();
        try {
            log.info("检测数据库能力: settingId={}", settingId);
            SystemDataSetting setting = getSettingOrThrow(settingId);
            try (SystemDataEngine engine = systemDataEngineFactory.create(setting)) {
                capabilities.putAll(engine.capabilities());
                putLegacyCapabilityAliases(capabilities);
                return capabilities;
            }
        } catch (Exception e) {
            log.error("检测数据库能力失败", e);
            putDefaultCapabilities(capabilities, false);
            putLegacyCapabilityAliases(capabilities);
            return capabilities;
        }
    }

    @Override
    public Map<String, FeatureSupportLevel> getDatabaseCapabilityLevels(Integer settingId) {
        Map<String, FeatureSupportLevel> capabilityLevels = new LinkedHashMap<>();
        try {
            log.info("检测数据库能力级别: settingId={}", settingId);
            SystemDataSetting setting = getSettingOrThrow(settingId);
            try (SystemDataEngine engine = systemDataEngineFactory.create(setting)) {
                capabilityLevels.putAll(engine.capabilityLevels());
                return capabilityLevels;
            }
        } catch (Exception e) {
            log.error("检测数据库能力级别失败", e);
            putDefaultCapabilityLevels(capabilityLevels);
            return capabilityLevels;
        }
    }

    @Override
    public ReturnResult<Map<String, Object>> getClusterStatus(Integer settingId) {
        try {
            SystemDataSetting setting = getSettingOrThrow(settingId);
            try (SystemDataEngine engine = systemDataEngineFactory.create(setting)) {
                if (!engine.supports(SystemDataCapability.CLUSTER)) {
                    return ReturnResult.error("当前数据源不支持集群能力");
                }
                SystemDataClusterFeature clusterFeature = engine.requireFeature(
                        SystemDataCapability.CLUSTER,
                        SystemDataClusterFeature.class
                );
                return ReturnResult.ok(clusterFeature.cluster(null).getClusterStatus());
            }
        } catch (Exception e) {
            log.error("获取集群状态失败", e);
            return ReturnResult.error(e.getMessage());
        }
    }

    @Override
    public ReturnResult<String> getClusterHealth(Integer settingId) {
        try {
            SystemDataSetting setting = getSettingOrThrow(settingId);
            try (SystemDataEngine engine = systemDataEngineFactory.create(setting)) {
                if (!engine.supports(SystemDataCapability.CLUSTER)) {
                    return ReturnResult.error("当前数据源不支持集群能力");
                }
                SystemDataClusterFeature clusterFeature = engine.requireFeature(
                        SystemDataCapability.CLUSTER,
                        SystemDataClusterFeature.class
                );
                return ReturnResult.ok(clusterFeature.cluster(null).getClusterHealth());
            }
        } catch (Exception e) {
            log.error("获取集群健康状态失败", e);
            return ReturnResult.error(e.getMessage());
        }
    }

    @Override
    public ReturnResult<List<Map<String, Object>>> getClusterNodes(Integer settingId) {
        try {
            SystemDataSetting setting = getSettingOrThrow(settingId);
            try (SystemDataEngine engine = systemDataEngineFactory.create(setting)) {
                if (!engine.supports(SystemDataCapability.NODE)) {
                    return ReturnResult.error("当前数据源不支持节点能力");
                }
                SystemDataNodeFeature nodeFeature = engine.requireFeature(
                        SystemDataCapability.NODE,
                        SystemDataNodeFeature.class
                );
                return ReturnResult.ok(nodeFeature.node(null).list());
            }
        } catch (Exception e) {
            log.error("获取集群节点失败", e);
            return ReturnResult.error(e.getMessage());
        }
    }

    @Override
    public void run(String... args) throws Exception {
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

    private void putDefaultCapabilities(Map<String, Boolean> capabilities, boolean value) {
        for (SystemDataCapability capability : SystemDataCapability.values()) {
            capabilities.put(capability.getKey(), value);
        }
    }

    private void putLegacyCapabilityAliases(Map<String, Boolean> capabilities) {
        for (SystemDataCapability capability : SystemDataCapability.values()) {
            capabilities.put(
                    capability.getLegacyAliasKey(),
                    capabilities.getOrDefault(capability.getKey(), false)
            );
        }
    }

    private void putDefaultCapabilityLevels(Map<String, FeatureSupportLevel> capabilityLevels) {
        for (SystemDataCapability capability : SystemDataCapability.values()) {
            capabilityLevels.put(capability.getKey(), FeatureSupportLevel.NONE);
        }
    }
}


