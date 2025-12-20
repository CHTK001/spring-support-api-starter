package com.chua.starter.monitor.service.impl;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.spi.ServiceProvider;
import com.chua.common.support.task.sync.*;
import com.chua.starter.monitor.annotation.SpiMeta;
import com.chua.starter.monitor.pojo.sync.SpiInfo;
import com.chua.starter.monitor.pojo.sync.SpiParameter;
import com.chua.starter.monitor.service.sync.MonitorSyncSpiService;
import com.chua.starter.monitor.util.SpiParameterResolver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * SPI 服务实现类
 *
 * @author CH
 * @since 2024/12/19
 */
@Slf4j
@Service
public class MonitorSyncSpiServiceImpl implements MonitorSyncSpiService {

    /**
     * SPI 类型常量
     */
    private static final String TYPE_INPUT = "INPUT";
    private static final String TYPE_OUTPUT = "OUTPUT";
    private static final String TYPE_DATA_CENTER = "DATA_CENTER";
    private static final String TYPE_FILTER = "FILTER";

    /**
     * SPI 配置缓存
     */
    private static final Map<String, Map<String, SpiInfo>> SPI_CACHE = new HashMap<>();

    @Override
    public ReturnResult<List<SpiInfo>> getInputList() {
        try {
            List<SpiInfo> list = getSpiList(Input.class, TYPE_INPUT);
            return ReturnResult.ok(list);
        } catch (Exception e) {
            log.error("获取 Input SPI 列表失败", e);
            return ReturnResult.error("获取失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<List<SpiInfo>> getOutputList() {
        try {
            List<SpiInfo> list = getSpiList(Output.class, TYPE_OUTPUT);
            return ReturnResult.ok(list);
        } catch (Exception e) {
            log.error("获取 Output SPI 列表失败", e);
            return ReturnResult.error("获取失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<List<SpiInfo>> getDataCenterList() {
        try {
            List<SpiInfo> list = getSpiList(DataCenter.class, TYPE_DATA_CENTER);
            return ReturnResult.ok(list);
        } catch (Exception e) {
            log.error("获取 DataCenter SPI 列表失败", e);
            return ReturnResult.error("获取失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<List<SpiInfo>> getDataFilterList() {
        try {
            List<SpiInfo> list = getSpiList(DataFilter.class, TYPE_FILTER);
            return ReturnResult.ok(list);
        } catch (Exception e) {
            log.error("获取 DataFilter SPI 列表失败", e);
            return ReturnResult.error("获取失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<List<SpiParameter>> getParameters(String spiType, String spiName) {
        try {
            List<SpiParameter> parameters = getParametersForSpi(spiType, spiName);
            return ReturnResult.ok(parameters);
        } catch (Exception e) {
            log.error("获取 SPI 参数失败, type: {}, name: {}", spiType, spiName, e);
            return ReturnResult.error("获取失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Boolean> validateConfig(String spiType, String spiName, Map<String, Object> config) {
        try {
            List<SpiParameter> parameters = getParametersForSpi(spiType, spiName);

            // 检查必填参数
            for (SpiParameter param : parameters) {
                if (Boolean.TRUE.equals(param.getRequired())) {
                    Object value = config.get(param.getName());
                    if (value == null || (value instanceof String && ((String) value).isEmpty())) {
                        return ReturnResult.error("参数 " + param.getLabel() + " 不能为空");
                    }
                }
            }

            return ReturnResult.ok(true);
        } catch (Exception e) {
            log.error("验证配置失败, type: {}, name: {}", spiType, spiName, e);
            return ReturnResult.error("验证失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<String> testConnection(String spiType, String spiName, Map<String, Object> config) {
        try {
            // 根据类型创建实例并测试
            switch (spiType.toUpperCase()) {
                case TYPE_INPUT:
                    return testInputConnection(spiName, config);
                case TYPE_OUTPUT:
                    return testOutputConnection(spiName, config);
                case TYPE_DATA_CENTER:
                    return testDataCenterConnection(spiName, config);
                default:
                    return ReturnResult.ok("该类型不支持连接测试");
            }
        } catch (Exception e) {
            log.error("测试连接失败, type: {}, name: {}", spiType, spiName, e);
            return ReturnResult.error("测试失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Map<String, List<SpiInfo>>> getAllSpiTypes() {
        try {
            Map<String, List<SpiInfo>> result = new LinkedHashMap<>();
            result.put(TYPE_INPUT, getSpiList(Input.class, TYPE_INPUT));
            result.put(TYPE_OUTPUT, getSpiList(Output.class, TYPE_OUTPUT));
            result.put(TYPE_DATA_CENTER, getSpiList(DataCenter.class, TYPE_DATA_CENTER));
            result.put(TYPE_FILTER, getSpiList(DataFilter.class, TYPE_FILTER));
            return ReturnResult.ok(result);
        } catch (Exception e) {
            log.error("获取所有 SPI 类型失败", e);
            return ReturnResult.error("获取失败: " + e.getMessage());
        }
    }

    /**
     * 获取 SPI 列表
     */
    private <T> List<SpiInfo> getSpiList(Class<T> spiClass, String type) {
        List<SpiInfo> result = new ArrayList<>();
        ServiceProvider<T> provider = ServiceProvider.of(spiClass);

        Set<String> extensions = provider.getExtensions();
        int order = 0;
        for (String name : extensions) {
            SpiInfo info = new SpiInfo();
            info.setName(name);
            info.setType(type);
            info.setAvailable(true);

            // 尝试从注解解析元信息
            SpiInfo annotatedInfo = SpiParameterResolver.resolveSpiInfo(spiClass, name, type);
            if (annotatedInfo.getDisplayName() != null && !annotatedInfo.getDisplayName().isEmpty()) {
                info.setDisplayName(annotatedInfo.getDisplayName());
            } else {
                info.setDisplayName(getDisplayName(name, type));
            }
            if (annotatedInfo.getDescription() != null && !annotatedInfo.getDescription().isEmpty()) {
                info.setDescription(annotatedInfo.getDescription());
            } else {
                info.setDescription(getDescription(name, type));
            }
            if (annotatedInfo.getIcon() != null && !annotatedInfo.getIcon().isEmpty()) {
                info.setIcon(annotatedInfo.getIcon());
            } else {
                info.setIcon(getIcon(name, type));
            }
            if (annotatedInfo.getColor() != null && !annotatedInfo.getColor().isEmpty()) {
                info.setColor(annotatedInfo.getColor());
            } else {
                info.setColor(getColor(name, type));
            }
            if (annotatedInfo.getClassName() != null) {
                info.setClassName(annotatedInfo.getClassName());
            }
            info.setOrder(annotatedInfo.getOrder() != null ? annotatedInfo.getOrder() : order++);

            // 获取参数定义（优先从注解解析）
            info.setParameters(getParametersForSpi(type, name, spiClass));

            result.add(info);
        }

        // 按 order 排序
        result.sort(Comparator.comparingInt(i -> i.getOrder() != null ? i.getOrder() : Integer.MAX_VALUE));

        return result;
    }

    /**
     * 获取 SPI 参数定义
     */
    private List<SpiParameter> getParametersForSpi(String spiType, String spiName) {
        Class<?> spiClass = getSpiClass(spiType);
        if (spiClass != null) {
            return getParametersForSpi(spiType, spiName, spiClass);
        }
        return new ArrayList<>();
    }

    /**
     * 获取 SPI 参数定义（带类型参数）
     */
    private <T> List<SpiParameter> getParametersForSpi(String spiType, String spiName, Class<T> spiClass) {
        // 首先尝试从注解解析参数
        List<SpiParameter> annotatedParams = SpiParameterResolver.resolveParameters(spiClass, spiName);
        if (!annotatedParams.isEmpty()) {
            return annotatedParams;
        }

        // 如果注解中没有定义参数，使用硬编码的默认参数
        List<SpiParameter> parameters = new ArrayList<>();
        switch (spiType.toUpperCase()) {
            case TYPE_INPUT:
                parameters = getInputParameters(spiName);
                break;
            case TYPE_OUTPUT:
                parameters = getOutputParameters(spiName);
                break;
            case TYPE_DATA_CENTER:
                parameters = getDataCenterParameters(spiName);
                break;
            case TYPE_FILTER:
                parameters = getFilterParameters(spiName);
                break;
        }

        return parameters;
    }

    /**
     * 根据类型获取 SPI 接口类
     */
    private Class<?> getSpiClass(String spiType) {
        switch (spiType.toUpperCase()) {
            case TYPE_INPUT:
                return Input.class;
            case TYPE_OUTPUT:
                return Output.class;
            case TYPE_DATA_CENTER:
                return DataCenter.class;
            case TYPE_FILTER:
                return DataFilter.class;
            default:
                return null;
        }
    }

    /**
     * 获取 Input 参数定义
     */
    private List<SpiParameter> getInputParameters(String spiName) {
        List<SpiParameter> params = new ArrayList<>();

        switch (spiName.toLowerCase()) {
            case "jdbc":
                params.add(createParameter("url", "数据库URL", "string", true, "jdbc:mysql://localhost:3306/test", "数据库连接URL"));
                params.add(createParameter("username", "用户名", "string", true, null, "数据库用户名"));
                params.add(createParameter("password", "密码", "password", true, null, "数据库密码"));
                params.add(createParameter("sql", "查询SQL", "textarea", true, null, "SELECT语句"));
                params.add(createParameter("fetchSize", "批次大小", "number", false, 1000, "每次获取的记录数"));
                break;
            case "csv":
                params.add(createParameter("path", "文件路径", "string", true, null, "CSV文件路径"));
                params.add(createParameter("encoding", "编码", "string", false, "UTF-8", "文件编码"));
                params.add(createParameter("delimiter", "分隔符", "string", false, ",", "字段分隔符"));
                params.add(createParameter("hasHeader", "包含表头", "boolean", false, true, "是否包含表头"));
                break;
            case "tsv":
                params.add(createParameter("path", "文件路径", "string", true, null, "TSV文件路径"));
                params.add(createParameter("encoding", "编码", "string", false, "UTF-8", "文件编码"));
                params.add(createParameter("hasHeader", "包含表头", "boolean", false, true, "是否包含表头"));
                break;
            case "mock":
                params.add(createParameter("count", "数据量", "number", false, 100, "生成的数据条数"));
                params.add(createParameter("interval", "间隔(ms)", "number", false, 0, "每条数据生成间隔"));
                break;
            default:
                // 默认参数
                break;
        }

        return params;
    }

    /**
     * 获取 Output 参数定义
     */
    private List<SpiParameter> getOutputParameters(String spiName) {
        List<SpiParameter> params = new ArrayList<>();

        switch (spiName.toLowerCase()) {
            case "jdbc":
                params.add(createParameter("url", "数据库URL", "string", true, "jdbc:mysql://localhost:3306/test", "数据库连接URL"));
                params.add(createParameter("username", "用户名", "string", true, null, "数据库用户名"));
                params.add(createParameter("password", "密码", "password", true, null, "数据库密码"));
                params.add(createParameter("table", "目标表", "string", true, null, "目标表名"));
                params.add(createParameter("batchSize", "批次大小", "number", false, 1000, "批量写入大小"));
                params.add(createParameter("writeMode", "写入模式", "select", false, "INSERT", "写入模式"));
                break;
            case "csv":
                params.add(createParameter("path", "文件路径", "string", true, null, "CSV输出文件路径"));
                params.add(createParameter("encoding", "编码", "string", false, "UTF-8", "文件编码"));
                params.add(createParameter("delimiter", "分隔符", "string", false, ",", "字段分隔符"));
                params.add(createParameter("writeHeader", "写入表头", "boolean", false, true, "是否写入表头"));
                break;
            case "tsv":
                params.add(createParameter("path", "文件路径", "string", true, null, "TSV输出文件路径"));
                params.add(createParameter("encoding", "编码", "string", false, "UTF-8", "文件编码"));
                params.add(createParameter("writeHeader", "写入表头", "boolean", false, true, "是否写入表头"));
                break;
            case "console":
                params.add(createParameter("format", "输出格式", "select", false, "JSON", "输出格式"));
                break;
            default:
                break;
        }

        return params;
    }

    /**
     * 获取 DataCenter 参数定义
     */
    private List<SpiParameter> getDataCenterParameters(String spiName) {
        List<SpiParameter> params = new ArrayList<>();

        switch (spiName.toLowerCase()) {
            case "local":
                params.add(createParameter("capacity", "队列容量", "number", false, 10000, "内存队列容量"));
                params.add(createParameter("ackEnabled", "启用ACK", "boolean", false, false, "是否启用消息确认"));
                params.add(createParameter("deadLetterEnabled", "启用死信", "boolean", false, false, "是否启用死信队列"));
                break;
            case "redis":
                params.add(createParameter("host", "Redis主机", "string", true, "localhost", "Redis服务器地址"));
                params.add(createParameter("port", "端口", "number", true, 6379, "Redis端口"));
                params.add(createParameter("password", "密码", "password", false, null, "Redis密码"));
                params.add(createParameter("database", "数据库", "number", false, 0, "Redis数据库索引"));
                params.add(createParameter("queueKey", "队列Key", "string", false, "sync:queue", "队列存储Key"));
                break;
            case "kafka":
                params.add(createParameter("bootstrapServers", "服务器地址", "string", true, "localhost:9092", "Kafka服务器地址"));
                params.add(createParameter("topic", "Topic", "string", true, null, "Kafka Topic"));
                params.add(createParameter("groupId", "消费组", "string", false, "sync-group", "消费组ID"));
                break;
            default:
                break;
        }

        return params;
    }

    /**
     * 获取 Filter 参数定义
     */
    private List<SpiParameter> getFilterParameters(String spiName) {
        List<SpiParameter> params = new ArrayList<>();

        switch (spiName.toLowerCase()) {
            case "script":
                params.add(createParameter("scriptType", "脚本类型", "select", true, "javascript", "脚本语言类型"));
                params.add(createParameter("script", "脚本内容", "textarea", true, null, "过滤脚本"));
                break;
            case "field":
                params.add(createParameter("includeFields", "包含字段", "string", false, null, "需要的字段(逗号分隔)"));
                params.add(createParameter("excludeFields", "排除字段", "string", false, null, "排除的字段(逗号分隔)"));
                break;
            case "condition":
                params.add(createParameter("condition", "过滤条件", "textarea", true, null, "过滤条件表达式"));
                break;
            default:
                break;
        }

        return params;
    }

    /**
     * 创建参数定义
     */
    private SpiParameter createParameter(String name, String label, String type,
                                          boolean required, Object defaultValue, String description) {
        SpiParameter param = new SpiParameter();
        param.setName(name);
        param.setLabel(label);
        param.setType(type);
        param.setRequired(required);
        param.setDefaultValue(defaultValue);
        param.setDescription(description);
        param.setSensitive("password".equals(type));
        return param;
    }

    /**
     * 获取显示名称
     */
    private String getDisplayName(String name, String type) {
        Map<String, String> displayNames = new HashMap<>();
        displayNames.put("jdbc", "JDBC数据库");
        displayNames.put("csv", "CSV文件");
        displayNames.put("tsv", "TSV文件");
        displayNames.put("mock", "模拟数据");
        displayNames.put("console", "控制台");
        displayNames.put("local", "本地队列");
        displayNames.put("redis", "Redis队列");
        displayNames.put("kafka", "Kafka队列");
        displayNames.put("script", "脚本过滤");
        displayNames.put("field", "字段过滤");
        displayNames.put("condition", "条件过滤");

        return displayNames.getOrDefault(name.toLowerCase(), name);
    }

    /**
     * 获取描述
     */
    private String getDescription(String name, String type) {
        Map<String, String> descriptions = new HashMap<>();
        descriptions.put("jdbc", "支持MySQL、PostgreSQL、Oracle等关系数据库");
        descriptions.put("csv", "逗号分隔值文件");
        descriptions.put("tsv", "制表符分隔值文件");
        descriptions.put("mock", "生成模拟测试数据");
        descriptions.put("console", "输出到控制台");
        descriptions.put("local", "本地内存队列，基于Reactor");
        descriptions.put("redis", "Redis消息队列");
        descriptions.put("kafka", "Apache Kafka消息队列");
        descriptions.put("script", "使用脚本进行数据过滤");
        descriptions.put("field", "按字段名过滤数据");
        descriptions.put("condition", "按条件表达式过滤数据");

        return descriptions.getOrDefault(name.toLowerCase(), "");
    }

    /**
     * 获取图标
     */
    private String getIcon(String name, String type) {
        Map<String, String> icons = new HashMap<>();
        icons.put("jdbc", "database");
        icons.put("csv", "file-text");
        icons.put("tsv", "file-text");
        icons.put("mock", "code");
        icons.put("console", "terminal");
        icons.put("local", "inbox");
        icons.put("redis", "server");
        icons.put("kafka", "message-square");
        icons.put("script", "code");
        icons.put("field", "filter");
        icons.put("condition", "git-branch");

        return icons.getOrDefault(name.toLowerCase(), "box");
    }

    /**
     * 获取颜色
     */
    private String getColor(String name, String type) {
        switch (type.toUpperCase()) {
            case TYPE_INPUT:
                return "#52c41a"; // 绿色
            case TYPE_OUTPUT:
                return "#1890ff"; // 蓝色
            case TYPE_DATA_CENTER:
                return "#722ed1"; // 紫色
            case TYPE_FILTER:
                return "#faad14"; // 橙色
            default:
                return "#666666";
        }
    }

    /**
     * 测试 Input 连接
     */
    private ReturnResult<String> testInputConnection(String spiName, Map<String, Object> config) {
        try {
            ServiceProvider<Input> provider = ServiceProvider.of(Input.class);
            Input input = provider.getNewExtension(spiName, config);
            if (input != null) {
                return ReturnResult.ok("连接测试成功");
            }
            return ReturnResult.error("无法创建 Input 实例");
        } catch (Exception e) {
            return ReturnResult.error("连接测试失败: " + e.getMessage());
        }
    }

    /**
     * 测试 Output 连接
     */
    private ReturnResult<String> testOutputConnection(String spiName, Map<String, Object> config) {
        try {
            ServiceProvider<Output> provider = ServiceProvider.of(Output.class);
            Output output = provider.getNewExtension(spiName, config);
            if (output != null) {
                return ReturnResult.ok("连接测试成功");
            }
            return ReturnResult.error("无法创建 Output 实例");
        } catch (Exception e) {
            return ReturnResult.error("连接测试失败: " + e.getMessage());
        }
    }

    /**
     * 测试 DataCenter 连接
     */
    private ReturnResult<String> testDataCenterConnection(String spiName, Map<String, Object> config) {
        try {
            ServiceProvider<DataCenter> provider = ServiceProvider.of(DataCenter.class);
            DataCenter dataCenter = provider.getNewExtension(spiName, config);
            if (dataCenter != null) {
                return ReturnResult.ok("连接测试成功");
            }
            return ReturnResult.error("无法创建 DataCenter 实例");
        } catch (Exception e) {
            return ReturnResult.error("连接测试失败: " + e.getMessage());
        }
    }
}
