package com.chua.starter.monitor.job;

import com.chua.common.support.json.Json;
import com.chua.common.support.spi.ServiceProvider;
import com.chua.common.support.task.sync.*;
import com.chua.common.support.utils.StringUtils;
import com.chua.starter.job.support.handler.JobHandler;
import com.chua.starter.job.support.log.DefaultJobLog;
import com.chua.starter.job.support.thread.JobContext;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * 动态同步任务处理器
 * <p>
 * 支持通过 Groovy 脚本动态配置同步任务。
 * 在 monitor_job 表中配置 GLUE 脚本，实现动态的数据同步任务。
 * </p>
 * 
 * <p>使用示例（Groovy脚本）:</p>
 * <pre>{@code
 * import com.chua.starter.monitor.job.DynamicSyncTaskHandler
 * 
 * class MySyncTask extends DynamicSyncTaskHandler {
 *     @Override
 *     protected SyncFlowConfig buildConfig() {
 *         return SyncFlowConfig.builder()
 *             .inputType("mysql")
 *             .inputConfig([
 *                 "url": "jdbc:mysql://localhost:3306/source",
 *                 "username": "root",
 *                 "password": "123456",
 *                 "table": "users"
 *             ])
 *             .outputType("elasticsearch")
 *             .outputConfig([
 *                 "hosts": "localhost:9200",
 *                 "index": "users"
 *             ])
 *             .batchSize(100)
 *             .build()
 *     }
 * }
 * }</pre>
 *
 * @author CH
 * @since 2024/12/19
 */
@Slf4j
public abstract class DynamicSyncTaskHandler implements JobHandler {

    /**
     * 构建同步流配置
     * <p>子类需要实现此方法来定义同步任务的配置</p>
     *
     * @return 同步流配置
     */
    protected abstract SyncFlowConfig buildConfig();

    @Override
    public void execute() throws Exception {
        String param = JobContext.getJobParam();
        DefaultJobLog.log("开始执行动态同步任务, 参数: " + param);

        try {
            // 构建配置
            SyncFlowConfig config = buildConfig();
            if (config == null) {
                throw new IllegalArgumentException("同步配置不能为空");
            }

            // 合并运行时参数
            if (StringUtils.isNotEmpty(param)) {
                try {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> runtimeParams = Json.fromJson(param, Map.class);
                    config.mergeRuntimeParams(runtimeParams);
                } catch (Exception e) {
                    log.warn("解析运行时参数失败: {}", param);
                }
            }

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
    private SyncFlow buildSyncFlow(SyncFlowConfig config) {
        SyncFlow.SyncFlowBuilder builder = SyncFlow.builder();

        // 创建 Input
        if (config.getInputType() != null) {
            Input input = createInput(config.getInputType(), config.getInputConfig());
            builder.input(input);
        }

        // 创建 Output
        if (config.getOutputType() != null) {
            Output output = createOutput(config.getOutputType(), config.getOutputConfig());
            builder.output(output);
        }

        // 创建 DataCenter（可选）
        if (config.getDataCenterType() != null) {
            DataCenter dataCenter = createDataCenter(config.getDataCenterType(), config.getDataCenterConfig());
            builder.dataCenter(dataCenter);
        }

        // 创建过滤器（可选）
        if (config.getFilters() != null) {
            for (Map.Entry<String, Map<String, Object>> entry : config.getFilters().entrySet()) {
                DataFilter filter = createFilter(entry.getKey(), entry.getValue());
                builder.filter(filter);
            }
        }

        // 设置参数
        if (config.getBatchSize() != null) {
            builder.batchSize(config.getBatchSize());
        }
        if (config.getConsumeTimeout() != null) {
            builder.consumeTimeout(config.getConsumeTimeout());
        }
        if (config.getRetryCount() != null) {
            builder.retryCount(config.getRetryCount());
        }

        return builder.build();
    }

    private Input createInput(String type, Map<String, Object> config) {
        ServiceProvider<Input> provider = ServiceProvider.of(Input.class);
        return provider.getNewExtension(type, config != null ? config : new HashMap<>());
    }

    private Output createOutput(String type, Map<String, Object> config) {
        ServiceProvider<Output> provider = ServiceProvider.of(Output.class);
        return provider.getNewExtension(type, config != null ? config : new HashMap<>());
    }

    private DataCenter createDataCenter(String type, Map<String, Object> config) {
        ServiceProvider<DataCenter> provider = ServiceProvider.of(DataCenter.class);
        return provider.getNewExtension(type, config != null ? config : new HashMap<>());
    }

    private DataFilter createFilter(String type, Map<String, Object> config) {
        ServiceProvider<DataFilter> provider = ServiceProvider.of(DataFilter.class);
        return provider.getNewExtension(type, config != null ? config : new HashMap<>());
    }

    /**
     * 同步流配置
     */
    @lombok.Data
    @lombok.Builder
    public static class SyncFlowConfig {
        /**
         * 输入类型 (SPI名称)
         */
        private String inputType;
        /**
         * 输入配置
         */
        private Map<String, Object> inputConfig;
        /**
         * 输出类型 (SPI名称)
         */
        private String outputType;
        /**
         * 输出配置
         */
        private Map<String, Object> outputConfig;
        /**
         * 数据中心类型 (SPI名称)
         */
        private String dataCenterType;
        /**
         * 数据中心配置
         */
        private Map<String, Object> dataCenterConfig;
        /**
         * 过滤器配置 (类型 -> 配置)
         */
        private Map<String, Map<String, Object>> filters;
        /**
         * 批次大小
         */
        private Integer batchSize;
        /**
         * 消费超时
         */
        private Long consumeTimeout;
        /**
         * 重试次数
         */
        private Integer retryCount;
        /**
         * 重试间隔
         */
        private Long retryInterval;

        /**
         * 合并运行时参数
         */
        public void mergeRuntimeParams(Map<String, Object> params) {
            if (params == null) {
                return;
            }
            // 可以覆盖部分配置
            if (params.containsKey("batchSize")) {
                this.batchSize = (Integer) params.get("batchSize");
            }
            if (params.containsKey("retryCount")) {
                this.retryCount = (Integer) params.get("retryCount");
            }
            // 合并输入配置
            if (params.containsKey("inputConfig") && this.inputConfig != null) {
                @SuppressWarnings("unchecked")
                Map<String, Object> extra = (Map<String, Object>) params.get("inputConfig");
                this.inputConfig.putAll(extra);
            }
            // 合并输出配置
            if (params.containsKey("outputConfig") && this.outputConfig != null) {
                @SuppressWarnings("unchecked")
                Map<String, Object> extra = (Map<String, Object>) params.get("outputConfig");
                this.outputConfig.putAll(extra);
            }
        }
    }
}
