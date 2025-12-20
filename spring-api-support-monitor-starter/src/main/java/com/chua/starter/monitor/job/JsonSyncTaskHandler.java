package com.chua.starter.monitor.job;

import com.chua.common.support.json.Json;
import com.chua.common.support.spi.ServiceProvider;
import com.chua.common.support.task.sync.*;
import com.chua.common.support.utils.StringUtils;
import com.chua.starter.job.support.handler.JobHandler;
import com.chua.starter.job.support.log.DefaultJobLog;
import com.chua.starter.job.support.thread.JobContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * JSON配置同步任务处理器
 * <p>
 * 支持通过 JSON 参数配置同步任务，无需编写 Groovy 脚本。
 * 在 monitor_job 表中配置：
 * - job_execute_bean: jsonSyncTaskHandler
 * - job_execute_param: JSON配置
 * </p>
 * 
 * <p>JSON配置示例:</p>
 * <pre>{@code
 * {
 *   "inputType": "mysql",
 *   "inputConfig": {
 *     "url": "jdbc:mysql://localhost:3306/source",
 *     "username": "root",
 *     "password": "123456",
 *     "table": "users",
 *     "query": "SELECT * FROM users WHERE status = 1"
 *   },
 *   "outputType": "elasticsearch",
 *   "outputConfig": {
 *     "hosts": "localhost:9200",
 *     "index": "users"
 *   },
 *   "filters": {
 *     "field-mapper": {
 *       "mappings": {"user_name": "userName", "create_time": "createTime"}
 *     }
 *   },
 *   "batchSize": 100,
 *   "retryCount": 3
 * }
 * }</pre>
 *
 * @author CH
 * @since 2024/12/19
 */
@Slf4j
@Component("jsonSyncTaskHandler")
public class JsonSyncTaskHandler implements JobHandler {

    @Override
    public void execute() throws Exception {
        String param = JobContext.getJobParam();
        if (StringUtils.isEmpty(param)) {
            throw new IllegalArgumentException("同步配置参数不能为空，请在 job_execute_param 中配置 JSON");
        }

        DefaultJobLog.log("开始解析同步配置...");

        try {
            // 解析配置
            @SuppressWarnings("unchecked")
            Map<String, Object> config = Json.fromJson(param, Map.class);

            // 创建 SyncFlow
            SyncFlow syncFlow = buildSyncFlow(config);

            // 执行同步
            DefaultJobLog.log("开始数据同步...");
            long startTime = System.currentTimeMillis();
            syncFlow.start();
            long cost = System.currentTimeMillis() - startTime;

            // 获取指标
            SyncMetrics metrics = syncFlow.getMetrics();
            if (metrics != null) {
                DefaultJobLog.log(String.format(
                        "同步完成, 读取: %d, 写入: %d, 成功: %d, 失败: %d, 耗时: %dms",
                        metrics.getInputCount().get(),
                        metrics.getOutputCount().get(),
                        metrics.getSuccessCount().get(),
                        metrics.getFailCount().get(),
                        cost
                ));
            } else {
                DefaultJobLog.log("同步完成, 耗时: " + cost + "ms");
            }

        } catch (Exception e) {
            DefaultJobLog.log("同步任务执行失败: " + e.getMessage());
            throw e;
        }
    }

    /**
     * 构建 SyncFlow
     */
    @SuppressWarnings("unchecked")
    private SyncFlow buildSyncFlow(Map<String, Object> config) {
        SyncFlow.SyncFlowBuilder builder = SyncFlow.builder();

        // 创建 Input
        String inputType = (String) config.get("inputType");
        if (inputType != null) {
            Map<String, Object> inputConfig = (Map<String, Object>) config.getOrDefault("inputConfig", new HashMap<>());
            Input input = createInput(inputType, inputConfig);
            builder.input(input);
            DefaultJobLog.log("配置输入: " + inputType);
        } else {
            throw new IllegalArgumentException("必须配置 inputType");
        }

        // 创建 Output
        String outputType = (String) config.get("outputType");
        if (outputType != null) {
            Map<String, Object> outputConfig = (Map<String, Object>) config.getOrDefault("outputConfig", new HashMap<>());
            Output output = createOutput(outputType, outputConfig);
            builder.output(output);
            DefaultJobLog.log("配置输出: " + outputType);
        } else {
            throw new IllegalArgumentException("必须配置 outputType");
        }

        // 创建 DataCenter（可选）
        String dataCenterType = (String) config.get("dataCenterType");
        if (dataCenterType != null) {
            Map<String, Object> dataCenterConfig = (Map<String, Object>) config.getOrDefault("dataCenterConfig", new HashMap<>());
            DataCenter dataCenter = createDataCenter(dataCenterType, dataCenterConfig);
            builder.dataCenter(dataCenter);
            DefaultJobLog.log("配置数据中心: " + dataCenterType);
        }

        // 创建过滤器（可选）
        Map<String, Object> filters = (Map<String, Object>) config.get("filters");
        if (filters != null) {
            for (Map.Entry<String, Object> entry : filters.entrySet()) {
                String filterType = entry.getKey();
                Map<String, Object> filterConfig = entry.getValue() instanceof Map 
                        ? (Map<String, Object>) entry.getValue() 
                        : new HashMap<>();
                DataFilter filter = createFilter(filterType, filterConfig);
                builder.filter(filter);
                DefaultJobLog.log("配置过滤器: " + filterType);
            }
        }

        // 设置参数
        if (config.containsKey("batchSize")) {
            builder.batchSize(((Number) config.get("batchSize")).intValue());
        }
        if (config.containsKey("consumeTimeout")) {
            builder.consumeTimeout(((Number) config.get("consumeTimeout")).longValue());
        }
        if (config.containsKey("retryCount")) {
            builder.retryCount(((Number) config.get("retryCount")).intValue());
        }
        if (config.containsKey("retryInterval")) {
            builder.retryInterval(((Number) config.get("retryInterval")).longValue());
        }
        if (config.containsKey("ackEnabled")) {
            builder.ackEnabled((Boolean) config.get("ackEnabled"));
        }

        return builder.build();
    }

    private Input createInput(String type, Map<String, Object> config) {
        ServiceProvider<Input> provider = ServiceProvider.of(Input.class);
        return provider.getNewExtension(type, config);
    }

    private Output createOutput(String type, Map<String, Object> config) {
        ServiceProvider<Output> provider = ServiceProvider.of(Output.class);
        return provider.getNewExtension(type, config);
    }

    private DataCenter createDataCenter(String type, Map<String, Object> config) {
        ServiceProvider<DataCenter> provider = ServiceProvider.of(DataCenter.class);
        return provider.getNewExtension(type, config);
    }

    private DataFilter createFilter(String type, Map<String, Object> config) {
        ServiceProvider<DataFilter> provider = ServiceProvider.of(DataFilter.class);
        return provider.getNewExtension(type, config);
    }
}
