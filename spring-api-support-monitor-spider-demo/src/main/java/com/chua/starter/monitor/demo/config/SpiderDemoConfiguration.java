package com.chua.starter.monitor.demo.config;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.AbstractWrapper;
import com.chua.starter.monitor.entity.MonitorSpiderConnection;
import com.chua.starter.monitor.entity.MonitorSpiderNode;
import com.chua.starter.monitor.entity.MonitorSpiderTask;
import com.chua.starter.monitor.entity.SystemDataSetting;
import com.chua.starter.monitor.pojo.data.ColumnInfo;
import com.chua.starter.monitor.pojo.data.TableStructureInfo;
import com.chua.starter.monitor.pojo.spider.SpiderOutputColumnDefinition;
import com.chua.starter.monitor.pojo.spider.SpiderOutputTableRequest;
import com.chua.starter.monitor.pojo.spider.SpiderTableStatus;
import com.chua.starter.monitor.service.data.SystemDataSettingService;
import com.chua.starter.monitor.service.spider.MonitorSpiderConnectionService;
import com.chua.starter.monitor.service.spider.MonitorSpiderDataService;
import com.chua.starter.monitor.service.spider.MonitorSpiderLogService;
import com.chua.starter.monitor.service.spider.MonitorSpiderNodeService;
import com.chua.starter.monitor.service.spider.MonitorSpiderTaskService;
import com.chua.starter.monitor.service.spider.SpiderOutputTableService;
import com.chua.starter.monitor.service.spider.SpiderSchemaService;
import org.springframework.boot.ApplicationRunner;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Demo 宿主 Bean 配置。
 */
@Configuration(proxyBeanMethods = false)
public class SpiderDemoConfiguration {

    @Bean
    DemoSpiderStore demoSpiderStore() {
        return DemoSpiderStore.createDefault();
    }

    @Bean
    MonitorSpiderTaskService monitorSpiderTaskService(DemoSpiderStore store) {
        return createEntityServiceProxy(MonitorSpiderTaskService.class, store.tasks());
    }

    @Bean
    MonitorSpiderNodeService monitorSpiderNodeService(DemoSpiderStore store) {
        return createEntityServiceProxy(MonitorSpiderNodeService.class, store.nodes());
    }

    @Bean
    MonitorSpiderConnectionService monitorSpiderConnectionService(DemoSpiderStore store) {
        return createEntityServiceProxy(MonitorSpiderConnectionService.class, store.connections());
    }

    @Bean
    MonitorSpiderDataService monitorSpiderDataService() {
        return createStaticServiceProxy(MonitorSpiderDataService.class, List.of());
    }

    @Bean
    MonitorSpiderLogService monitorSpiderLogService() {
        return createStaticServiceProxy(MonitorSpiderLogService.class, List.of());
    }

    @Bean
    SystemDataSettingService systemDataSettingService() {
        List<SystemDataSetting> demoSettings = List.of(createDemoSetting());
        return createStaticServiceProxy(SystemDataSettingService.class, demoSettings);
    }

    @Bean
    SpiderSchemaService spiderSchemaService() {
        return new DemoSpiderSchemaService();
    }

    @Bean
    SpiderOutputTableService spiderOutputTableService() {
        return new DemoSpiderOutputTableService();
    }

    @Bean
    ApplicationRunner demoStartupBanner(Environment environment) {
        return args -> {
            String port = environment.getProperty("local.server.port", environment.getProperty("server.port", "19171"));
            System.out.println("[SpiderDemo] 启动完成");
            System.out.println("[SpiderDemo] 演示页面: http://127.0.0.1:" + port + "/demo/page");
            System.out.println("[SpiderDemo] 页面预览: http://127.0.0.1:" + port + "/v1/spider/preview?url=http://127.0.0.1:" + port + "/demo/page");
            System.out.println("[SpiderDemo] DOM解析: http://127.0.0.1:" + port + "/v1/spider/parse-html?url=http://127.0.0.1:" + port + "/demo/page");
            System.out.println("[SpiderDemo] 爬虫控制台: http://127.0.0.1:" + port + "/spider-console/index.html");
        };
    }

    @SuppressWarnings("unchecked")
    private static <T> T createStaticServiceProxy(Class<T> type, List<?> fixedItems) {
        return (T) Proxy.newProxyInstance(
                type.getClassLoader(),
                new Class[]{type},
                (proxy, method, args) -> handleStaticServiceInvocation(type, method, args, fixedItems)
        );
    }

    @SuppressWarnings("unchecked")
    private static <T, E> T createEntityServiceProxy(Class<T> type, DemoEntityStore<E> store) {
        return (T) Proxy.newProxyInstance(
                type.getClassLoader(),
                new Class[]{type},
                (proxy, method, args) -> handleEntityServiceInvocation(type, method, args, store)
        );
    }

    private static Object handleStaticServiceInvocation(Class<?> type, Method method, Object[] args, List<?> fixedItems) {
        if (method.getDeclaringClass() == Object.class) {
            return switch (method.getName()) {
                case "toString" -> type.getSimpleName() + "DemoProxy";
                case "hashCode" -> System.identityHashCode(type);
                case "equals" -> args != null && args.length == 1 && args[0] != null && Proxy.isProxyClass(args[0].getClass());
                default -> null;
            };
        }

        String name = method.getName();
        int argCount = args == null ? 0 : args.length;
        if ("list".equals(name)) {
            return argCount == 0 ? fixedItems : List.of();
        }
        if ("page".equals(name)) {
            return new Page<>();
        }
        if ("count".equals(name)) {
            return (long) fixedItems.size();
        }
        if ("save".equals(name) || "saveBatch".equals(name) || "updateById".equals(name)
                || "remove".equals(name) || "removeById".equals(name) || "removeBatchByIds".equals(name)) {
            return true;
        }
        if ("getById".equals(name) || "getOne".equals(name) || "getObj".equals(name)) {
            return null;
        }

        Class<?> returnType = method.getReturnType();
        if (returnType == boolean.class || returnType == Boolean.class) {
            return false;
        }
        if (returnType == int.class || returnType == Integer.class) {
            return 0;
        }
        if (returnType == long.class || returnType == Long.class) {
            return 0L;
        }
        if (List.class.isAssignableFrom(returnType)) {
            return List.of();
        }
        if (Map.class.isAssignableFrom(returnType)) {
            return Map.of();
        }
        if (Page.class.isAssignableFrom(returnType)) {
            return new Page<>();
        }
        return null;
    }

    private static Object handleEntityServiceInvocation(Class<?> type, Method method, Object[] args, DemoEntityStore<?> store) {
        if (method.getDeclaringClass() == Object.class) {
            return switch (method.getName()) {
                case "toString" -> type.getSimpleName() + "DemoProxy";
                case "hashCode" -> System.identityHashCode(type);
                case "equals" -> args != null && args.length == 1 && args[0] != null && Proxy.isProxyClass(args[0].getClass());
                default -> null;
            };
        }

        String name = method.getName();
        if ("list".equals(name)) {
            Object query = args != null && args.length > 0 ? args[0] : null;
            return store.list(query);
        }
        if ("page".equals(name)) {
            Page<?> page = args != null && args.length > 0 && args[0] instanceof Page<?> value ? value : null;
            Object query = args != null && args.length > 1 ? args[1] : null;
            return store.page(page, query);
        }
        if ("count".equals(name)) {
            Object query = args != null && args.length > 0 ? args[0] : null;
            return store.count(query);
        }
        if ("save".equals(name)) {
            return args != null && args.length > 0 && store.save(args[0]);
        }
        if ("saveBatch".equals(name)) {
            if (args != null && args.length > 0 && args[0] instanceof Iterable<?> iterable) {
                for (Object item : iterable) {
                    store.save(item);
                }
            }
            return true;
        }
        if ("updateById".equals(name)) {
            return args != null && args.length > 0 && store.updateById(args[0]);
        }
        if ("remove".equals(name)) {
            return args != null && args.length > 0 ? store.remove(args[0]) : false;
        }
        if ("removeById".equals(name)) {
            return args != null && args.length > 0 && store.removeById(args[0]);
        }
        if ("removeBatchByIds".equals(name)) {
            if (args != null && args.length > 0 && args[0] instanceof Iterable<?> iterable) {
                for (Object item : iterable) {
                    store.removeById(item);
                }
            }
            return true;
        }
        if ("getById".equals(name)) {
            return args != null && args.length > 0 ? store.getById(args[0]) : null;
        }
        if ("getOne".equals(name)) {
            Object query = args != null && args.length > 0 ? args[0] : null;
            return store.first(query);
        }
        if ("getObj".equals(name)) {
            return null;
        }

        Class<?> returnType = method.getReturnType();
        if (returnType == boolean.class || returnType == Boolean.class) {
            return false;
        }
        if (returnType == int.class || returnType == Integer.class) {
            return 0;
        }
        if (returnType == long.class || returnType == Long.class) {
            return 0L;
        }
        if (List.class.isAssignableFrom(returnType)) {
            return List.of();
        }
        if (Map.class.isAssignableFrom(returnType)) {
            return Map.of();
        }
        if (Page.class.isAssignableFrom(returnType)) {
            return new Page<>();
        }
        return null;
    }

    private static SystemDataSetting createDemoSetting() {
        SystemDataSetting setting = new SystemDataSetting();
        setting.setSystemDataSettingId(1);
        setting.setSystemDataSettingName("示例采集库");
        setting.setSystemDataSettingType("JDBC");
        setting.setSystemDataSettingServer("jdbc:mysql://127.0.0.1:3306/spider_demo");
        setting.setSystemDataSettingDatabase("spider_demo");
        setting.setSystemDataSettingEnabled(true);
        return setting;
    }

    static class DemoSpiderStore {

        private final DemoEntityStore<MonitorSpiderTask> tasks = new DemoEntityStore<>(
                MonitorSpiderTask.class,
                MonitorSpiderTask::getSpiderTaskId,
                MonitorSpiderTask::setSpiderTaskId,
                Comparator.<MonitorSpiderTask, LocalDateTime>comparing(
                                item -> item.getUpdateTime() == null ? LocalDateTime.MIN : item.getUpdateTime())
                        .reversed()
                        .thenComparing(item -> item.getSpiderTaskId() == null ? 0 : item.getSpiderTaskId(), Comparator.reverseOrder()),
                null
        );
        private final DemoEntityStore<MonitorSpiderNode> nodes = new DemoEntityStore<>(
                MonitorSpiderNode.class,
                MonitorSpiderNode::getNodeId,
                MonitorSpiderNode::setNodeId,
                Comparator.<MonitorSpiderNode, Integer>comparing(
                                item -> item.getNodeOrder() == null ? Integer.MAX_VALUE : item.getNodeOrder())
                        .thenComparing(item -> item.getNodeId() == null ? 0 : item.getNodeId()),
                "spiderTaskId"
        );
        private final DemoEntityStore<MonitorSpiderConnection> connections = new DemoEntityStore<>(
                MonitorSpiderConnection.class,
                MonitorSpiderConnection::getConnectionId,
                MonitorSpiderConnection::setConnectionId,
                Comparator.comparing(item -> item.getConnectionId() == null ? 0 : item.getConnectionId()),
                "spiderTaskId"
        );

        static DemoSpiderStore createDefault() {
            DemoSpiderStore store = new DemoSpiderStore();
            store.seed();
            return store;
        }

        DemoEntityStore<MonitorSpiderTask> tasks() {
            return tasks;
        }

        DemoEntityStore<MonitorSpiderNode> nodes() {
            return nodes;
        }

        DemoEntityStore<MonitorSpiderConnection> connections() {
            return connections;
        }

        private void seed() {
            seedTask(
                    "数据库输出演示任务",
                    "默认选中数据库输出节点，直接验证字段 XPath 可视化回填。",
                    3,
                    1,
                    2,
                    4
            );
            seedTask(
                    "XPath 回填演示任务",
                    "默认选中 XPath 处理节点，直接验证参数 XPath 可视化回填。",
                    1,
                    4,
                    2,
                    3
            );
        }

        private void seedTask(
                String taskName,
                String taskDescription,
                int processorOrder,
                int pipelineOrder,
                int urlSourceOrder,
                int downloaderOrder) {
            MonitorSpiderTask task = createTask(taskName, taskDescription);
            tasks.save(task);

            Integer taskId = task.getSpiderTaskId();

            MonitorSpiderNode urlSource = createNode(
                    taskId,
                    "示例 URL 源",
                    "URL_SOURCE",
                    "simple",
                    "{\"urls\":\"http://127.0.0.1:19171/demo/page\"}",
                    "提供演示页面 URL",
                    40,
                    80,
                    urlSourceOrder
            );
            MonitorSpiderNode downloader = createNode(
                    taskId,
                    "HTTP 下载",
                    "DOWNLOADER",
                    "http",
                    "{\"timeout\":30}",
                    "标准 HTTP 抓取节点",
                    320,
                    80,
                    downloaderOrder
            );
            MonitorSpiderNode processor = createNode(
                    taskId,
                    "XPath 处理",
                    "PROCESSOR",
                    "xpath",
                    "{\"expression\":\"//*[@id='content']//li\"}",
                    "可视化选择页面节点并抽取内容",
                    600,
                    80,
                    processorOrder
            );
            MonitorSpiderNode pipeline = createNode(
                    taskId,
                    "数据库输出",
                    "PIPELINE",
                    "database",
                    "{\"tableName\":\"spider_demo_article\",\"tableComment\":\"Spider demo output\",\"columns\":[{\"name\":\"article_title\",\"type\":\"VARCHAR\",\"length\":255,\"nullable\":true,\"defaultValue\":\"\",\"primaryKey\":false,\"autoIncrement\":false,\"comment\":\"标题\",\"sourceField\":\"\",\"xpath\":\"//*[@id='content']//h2\"}]}",
                    "将提取结果落到数据库输出表",
                    880,
                    80,
                    pipelineOrder
            );

            nodes.save(urlSource);
            nodes.save(downloader);
            nodes.save(processor);
            nodes.save(pipeline);

            connections.save(createConnection(taskId, urlSource.getNodeId(), downloader.getNodeId()));
            connections.save(createConnection(taskId, downloader.getNodeId(), processor.getNodeId()));
            connections.save(createConnection(taskId, processor.getNodeId(), pipeline.getNodeId()));
        }

        private MonitorSpiderTask createTask(String taskName, String taskDescription) {
            MonitorSpiderTask task = new MonitorSpiderTask();
            task.setSpiderTaskName(taskName);
            task.setSpiderTaskDesc(taskDescription);
            task.setSpiderTaskUrl("http://127.0.0.1:19171/demo/page");
            task.setSpiderTaskStatus(1);
            task.setSpiderTaskScheduleType("NONE");
            task.setSpiderTaskCron("");
            task.setSpiderTaskThreadNum(1);
            task.setSpiderTaskSleepTime(500);
            task.setSpiderTaskRetryTimes(0);
            task.setSpiderTaskTimeout(30);
            task.setSpiderTaskMaxDepth(0);
            task.setSpiderTaskEnableUrlDiscover(false);
            task.setSpiderTaskPagePattern("");
            task.setSpiderTaskMaxPages(1);
            task.setSpiderTaskIncrementalMode("HASH");
            task.setSpiderTaskRateLimit(0);
            task.setSpiderTaskPipelineType("DATABASE");
            task.setSpiderTaskRunStatus("IDLE");
            task.setSpiderTaskTotalSuccess(0L);
            task.setSpiderTaskTotalFail(0L);
            return task;
        }

        private MonitorSpiderNode createNode(
                Integer taskId,
                String nodeName,
                String nodeType,
                String spiName,
                String nodeConfig,
                String description,
                int positionX,
                int positionY,
                int order) {
            MonitorSpiderNode node = new MonitorSpiderNode();
            node.setSpiderTaskId(taskId);
            node.setNodeName(nodeName);
            node.setNodeType(nodeType);
            node.setSpiName(spiName);
            node.setNodeConfig(nodeConfig);
            node.setNodeDescription(description);
            node.setPositionX(positionX);
            node.setPositionY(positionY);
            node.setNodeOrder(order);
            node.setNodeEnabled(true);
            node.setRequiresConfig(true);
            return node;
        }

        private MonitorSpiderConnection createConnection(Integer taskId, Integer sourceNodeId, Integer targetNodeId) {
            MonitorSpiderConnection connection = new MonitorSpiderConnection();
            connection.setSpiderTaskId(taskId);
            connection.setSourceNodeId(sourceNodeId);
            connection.setTargetNodeId(targetNodeId);
            connection.setSourcePort("output");
            connection.setTargetPort("input");
            connection.setConnectionType("FLOW");
            connection.setConnectionLabel("demo");
            return connection;
        }
    }

    static class DemoEntityStore<E> {
        private final Class<E> type;
        private final Function<E, Integer> idGetter;
        private final BiConsumer<E, Integer> idSetter;
        private final Comparator<E> comparator;
        private final String wrapperFilterProperty;
        private final AtomicInteger sequence = new AtomicInteger(1);
        private final Map<Integer, E> storage = new ConcurrentHashMap<>();

        DemoEntityStore(
                Class<E> type,
                Function<E, Integer> idGetter,
                BiConsumer<E, Integer> idSetter,
                Comparator<E> comparator,
                String wrapperFilterProperty) {
            this.type = type;
            this.idGetter = idGetter;
            this.idSetter = idSetter;
            this.comparator = comparator;
            this.wrapperFilterProperty = wrapperFilterProperty;
        }

        List<E> list(Object query) {
            return storage.values().stream()
                    .filter(item -> matches(item, query))
                    .sorted(comparator)
                    .map(this::copy)
                    .toList();
        }

        Page<E> page(Page<?> sourcePage, Object query) {
            long current = sourcePage == null ? 1L : Math.max(1L, sourcePage.getCurrent());
            long size = sourcePage == null ? 20L : Math.max(1L, sourcePage.getSize());
            List<E> records = list(query);
            int from = (int) Math.min((current - 1) * size, records.size());
            int to = (int) Math.min(from + size, records.size());

            Page<E> result = new Page<>(current, size, records.size());
            result.setRecords(records.subList(from, to));
            return result;
        }

        long count(Object query) {
            return storage.values().stream().filter(item -> matches(item, query)).count();
        }

        boolean save(Object value) {
            if (value == null) {
                return false;
            }
            E entity = copy(type.cast(value));
            Integer id = idGetter.apply(entity);
            if (id == null || id <= 0) {
                id = sequence.getAndIncrement();
                idSetter.accept(entity, id);
                idSetter.accept(type.cast(value), id);
            } else {
                int nextValue = id + 1;
                sequence.updateAndGet(current -> Math.max(current, nextValue));
            }
            touchCreateTime(entity);
            touchUpdateTime(entity);
            storage.put(id, entity);
            return true;
        }

        boolean updateById(Object value) {
            if (value == null) {
                return false;
            }
            E entity = type.cast(value);
            Integer id = idGetter.apply(entity);
            if (id == null || !storage.containsKey(id)) {
                return false;
            }
            E target = storage.get(id);
            copyNonNullProperties(entity, target);
            touchUpdateTime(target);
            return true;
        }

        boolean removeById(Object value) {
            Integer id = asInteger(value);
            return id != null && storage.remove(id) != null;
        }

        boolean remove(Object query) {
            List<Integer> ids = storage.values().stream()
                    .filter(item -> matches(item, query))
                    .map(idGetter)
                    .filter(Objects::nonNull)
                    .toList();
            ids.forEach(storage::remove);
            return !ids.isEmpty();
        }

        void clear() {
            storage.clear();
        }

        E getById(Object value) {
            Integer id = asInteger(value);
            return id == null ? null : copy(storage.get(id));
        }

        E first(Object query) {
            return list(query).stream().findFirst().orElse(null);
        }

        private boolean matches(E entity, Object query) {
            if (!(query instanceof AbstractWrapper<?, ?, ?> wrapper) || wrapperFilterProperty == null) {
                return true;
            }

            Map<String, Object> paramMap = wrapper.getParamNameValuePairs();
            Object expected = paramMap == null ? null : paramMap.values().stream().findFirst().orElse(null);
            if (expected == null && "spiderTaskId".equals(wrapperFilterProperty)) {
                expected = resolveTaskIdFromRequest();
            }
            if (expected == null) {
                return true;
            }

            BeanWrapper beanWrapper = new BeanWrapperImpl(entity);
            if (!beanWrapper.isReadableProperty(wrapperFilterProperty)) {
                return true;
            }
            Object actual = beanWrapper.getPropertyValue(wrapperFilterProperty);
            return Objects.equals(normalizeValue(actual), normalizeValue(expected));
        }

        private Object normalizeValue(Object value) {
            if (value instanceof Number number) {
                return number.longValue();
            }
            return value;
        }

        private Integer resolveTaskIdFromRequest() {
            var attributes = RequestContextHolder.getRequestAttributes();
            if (!(attributes instanceof ServletRequestAttributes servletRequestAttributes)) {
                return null;
            }

            HttpServletRequest request = servletRequestAttributes.getRequest();
            String uri = request.getRequestURI();
            if (uri == null || uri.isBlank()) {
                return null;
            }

            String[] segments = uri.split("/");
            for (int i = 0; i < segments.length - 1; i++) {
                if ("design".equals(segments[i])) {
                    return asInteger(segments[i + 1]);
                }
            }
            return null;
        }

        private void touchCreateTime(E entity) {
            BeanWrapper wrapper = new BeanWrapperImpl(entity);
            if (wrapper.isWritableProperty("createTime") && wrapper.getPropertyValue("createTime") == null) {
                wrapper.setPropertyValue("createTime", LocalDateTime.now());
            }
        }

        private void touchUpdateTime(E entity) {
            BeanWrapper wrapper = new BeanWrapperImpl(entity);
            if (wrapper.isWritableProperty("updateTime")) {
                wrapper.setPropertyValue("updateTime", LocalDateTime.now());
            }
        }

        private E copy(E source) {
            if (source == null) {
                return null;
            }
            try {
                E target = type.getDeclaredConstructor().newInstance();
                BeanUtils.copyProperties(source, target);
                return target;
            } catch (Exception e) {
                throw new IllegalStateException("复制演示实体失败: " + type.getSimpleName(), e);
            }
        }
    }

    private static void copyNonNullProperties(Object source, Object target) {
        BeanWrapper sourceWrapper = new BeanWrapperImpl(source);
        BeanWrapper targetWrapper = new BeanWrapperImpl(target);
        for (var descriptor : sourceWrapper.getPropertyDescriptors()) {
            String name = descriptor.getName();
            if ("class".equals(name) || !targetWrapper.isWritableProperty(name)) {
                continue;
            }
            Object value = sourceWrapper.getPropertyValue(name);
            if (value != null) {
                targetWrapper.setPropertyValue(name, value);
            }
        }
    }

    private static Integer asInteger(Object value) {
        if (value instanceof Integer number) {
            return number;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value instanceof String text && !text.isBlank()) {
            return Integer.parseInt(text);
        }
        return null;
    }

    static class DemoSpiderSchemaService extends SpiderSchemaService {

        private static final List<String> REQUIRED_TABLES = List.of(
                "monitor_spider_task",
                "monitor_spider_data",
                "monitor_spider_log",
                "monitor_spider_node",
                "monitor_spider_connection"
        );

        private final AtomicBoolean initialized = new AtomicBoolean(false);

        DemoSpiderSchemaService() {
            super(null, null);
        }

        @Override
        public SpiderTableStatus checkTableStatus() {
            SpiderTableStatus status = new SpiderTableStatus();
            Map<String, Boolean> tables = new LinkedHashMap<>();
            Map<String, List<String>> missingColumns = new LinkedHashMap<>();
            for (String tableName : REQUIRED_TABLES) {
                boolean exists = initialized.get();
                tables.put(tableName, exists);
                if (!exists) {
                    missingColumns.put(tableName, List.of("待初始化"));
                }
            }
            status.setTables(tables);
            status.setMissingTables(initialized.get() ? List.of() : REQUIRED_TABLES);
            status.setMissingColumns(initialized.get() ? Map.of() : missingColumns);
            status.setReady(initialized.get());
            return status;
        }

        @Override
        public boolean initializeTables(boolean force) {
            initialized.set(true);
            return true;
        }

        @Override
        public boolean tableExists(String tableName) {
            return initialized.get() && REQUIRED_TABLES.contains(tableName);
        }
    }

    static class DemoSpiderOutputTableService extends SpiderOutputTableService {

        private final Map<String, TableStructureInfo> tables = new ConcurrentHashMap<>();

        DemoSpiderOutputTableService() {
            super(null, null);
        }

        @Override
        public boolean tableExists(String tableName, Integer dataSourceSettingId) {
            validateTableName(tableName);
            return tables.containsKey(tableName.toLowerCase());
        }

        @Override
        public TableStructureInfo getTableStructure(String tableName, Integer dataSourceSettingId) {
            validateTableName(tableName);
            TableStructureInfo structureInfo = tables.get(tableName.toLowerCase());
            if (structureInfo == null) {
                throw new IllegalArgumentException("输出表不存在: " + tableName);
            }
            return structureInfo;
        }

        @Override
        public boolean createTable(String tableName, SpiderOutputTableRequest request, String dbType) {
            String ddl = previewCreateTableSql(tableName, request, dbType);
            tables.put(tableName.toLowerCase(), toStructureInfo(tableName, request, ddl));
            return true;
        }

        @Override
        public boolean syncStructure(String tableName, SpiderOutputTableRequest request, String dbType) {
            validateTableName(tableName);
            if (!tableExists(tableName, request == null ? null : request.getDataSourceSettingId())) {
                return createTable(tableName, request, dbType);
            }

            TableStructureInfo current = tables.get(tableName.toLowerCase());
            List<ColumnInfo> columns = new ArrayList<>(current.getColumns() == null ? List.of() : current.getColumns());
            Map<String, ColumnInfo> existing = new LinkedHashMap<>();
            for (ColumnInfo column : columns) {
                existing.put(column.getName().toLowerCase(), column);
            }

            if (request != null && request.getColumns() != null) {
                for (SpiderOutputColumnDefinition definition : request.getColumns()) {
                    if (!existing.containsKey(definition.getName().toLowerCase())) {
                        columns.add(toColumnInfo(definition, columns.size() + 1));
                    }
                }
            }

            String ddl = previewCreateTableSql(tableName, request, dbType);
            TableStructureInfo updated = TableStructureInfo.builder()
                    .tableName(current.getTableName())
                    .tableComment(request == null ? current.getTableComment() : request.getTableComment())
                    .tableType(current.getTableType())
                    .engine(current.getEngine())
                    .charset(current.getCharset())
                    .collation(current.getCollation())
                    .createTime(current.getCreateTime())
                    .updateTime(LocalDateTime.now().toString())
                    .columns(columns)
                    .primaryKeys(extractPrimaryKeys(columns))
                    .createTableDdl(ddl)
                    .build();
            tables.put(tableName.toLowerCase(), updated);
            return true;
        }

        private TableStructureInfo toStructureInfo(String tableName, SpiderOutputTableRequest request, String ddl) {
            List<ColumnInfo> columns = new ArrayList<>();
            if (request != null && request.getColumns() != null) {
                int index = 1;
                for (SpiderOutputColumnDefinition definition : request.getColumns()) {
                    columns.add(toColumnInfo(definition, index++));
                }
            }

            String now = LocalDateTime.now().toString();
            return TableStructureInfo.builder()
                    .tableName(tableName)
                    .tableComment(request == null ? null : request.getTableComment())
                    .tableType("BASE TABLE")
                    .engine("InnoDB")
                    .charset("utf8mb4")
                    .collation("utf8mb4_unicode_ci")
                    .createTime(now)
                    .updateTime(now)
                    .columns(columns)
                    .primaryKeys(extractPrimaryKeys(columns))
                    .createTableDdl(ddl)
                    .build();
        }

        private ColumnInfo toColumnInfo(SpiderOutputColumnDefinition definition, int ordinalPosition) {
            String dataType = definition.getType() == null ? "VARCHAR" : definition.getType().toUpperCase();
            String fullType = dataType;
            if ((dataType.equals("VARCHAR") || dataType.equals("CHAR")) && definition.getLength() != null) {
                fullType = dataType + "(" + definition.getLength() + ")";
            } else if ((dataType.equals("DECIMAL") || dataType.equals("NUMERIC"))
                    && definition.getLength() != null) {
                int scale = definition.getScale() == null ? 2 : definition.getScale();
                fullType = dataType + "(" + definition.getLength() + "," + scale + ")";
            }

            return ColumnInfo.builder()
                    .name(definition.getName())
                    .dataType(dataType)
                    .fullType(fullType)
                    .length(definition.getLength())
                    .scale(definition.getScale())
                    .nullable(definition.getNullable())
                    .primaryKey(definition.getPrimaryKey())
                    .autoIncrement(definition.getAutoIncrement())
                    .defaultValue(definition.getDefaultValue())
                    .comment(definition.getComment())
                    .ordinalPosition(ordinalPosition)
                    .build();
        }

        private List<String> extractPrimaryKeys(List<ColumnInfo> columns) {
            List<String> primaryKeys = new ArrayList<>();
            for (ColumnInfo column : columns) {
                if (Boolean.TRUE.equals(column.getPrimaryKey())) {
                    primaryKeys.add(column.getName());
                }
            }
            return primaryKeys;
        }

        private void validateTableName(String tableName) {
            if (tableName == null || !tableName.matches("[A-Za-z_][A-Za-z0-9_]*")) {
                throw new IllegalArgumentException("表名不合法: " + tableName);
            }
        }
    }
}
