package com.chua.starter.monitor.starter.chain.filter;

import com.chua.common.support.function.Initializable;
import com.chua.common.support.task.limit.Limiter;
import com.chua.common.support.task.limit.LimiterProvider;
import com.chua.common.support.utils.StringUtils;
import com.chua.starter.monitor.starter.entity.MonitorProxyPluginLimit;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * QPS限流工厂类
 * <p>
 * 该类负责对API请求进行QPS(每秒查询率)限流，防止系统过载。
 * 可以针对不同的API路径设置不同的QPS限制，并提供实时统计和监控功能。
 *
 * @author CH
 * @since 2024/7/31
 */
public class ReportQpsLimitFactory implements Initializable {
    private final List<MonitorProxyPluginLimit> list;
    private final LimiterProvider limitProvider = new LimiterProvider();

    /**
     * 用于统计各路径的QPS
     */
    private final ConcurrentHashMap<String, AtomicLong> qpsCounter = new ConcurrentHashMap<>();
    /**
     * QPS统计重置间隔（毫秒），默认1秒
     */
    private final long resetInterval = 1000;
    /**
     * 上次统计时间戳
     */
    private volatile long lastResetTime = System.currentTimeMillis();

    public ReportQpsLimitFactory(List<MonitorProxyPluginLimit> list) {
        this.list = new LinkedList<>(list);
        initialize();
    }

    /**
     * 刷新配置
     *
     * @param list 限流配置列表
     */
    public synchronized void refresh(List<MonitorProxyPluginLimit> list) {
        limitProvider.clear();
        this.list.clear();
        this.list.addAll(list);
        initialize();
    }

    @Override
    public void initialize() {
        for (MonitorProxyPluginLimit monitorProxyLimit : list) {
            // 只处理QPS类型的限制
            if (monitorProxyLimit.getProxyConfigLimitDisabled() == 1 || !"QPS".equalsIgnoreCase(monitorProxyLimit.getProxyConfigLimitType()) ||
                    StringUtils.isEmpty(monitorProxyLimit.getProxyConfigLimitPathOrIp())) {
                continue;
            }

            String path = monitorProxyLimit.getProxyConfigLimitPathOrIp();
            int qpsLimit = monitorProxyLimit.getProxyConfigLimitPerSeconds();

            // 添加限流器
            limitProvider.addLimiter(path, Limiter.of(qpsLimit));

            // 初始化计数器
            qpsCounter.putIfAbsent(path, new AtomicLong(0));
        }
    }

    /**
     * 尝试获取访问权限
     *
     * @param path API路径
     * @return 是否允许访问
     */
    public boolean tryAcquire(String path) {
        // 检查是否需要重置计数器
        checkAndResetCounters();

        // 记录请求数
        qpsCounter.computeIfAbsent(path, k -> new AtomicLong(0)).incrementAndGet();

        // 检查是否超过限流阈值
        if (limitProvider.hasLimiter(path)) {
            return limitProvider.tryAcquire(path);
        }

        return true;
    }

    /**
     * 检查并重置计数器
     */
    private void checkAndResetCounters() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastResetTime >= resetInterval) {
            synchronized (this) {
                if (currentTime - lastResetTime >= resetInterval) {
                    // 重置所有计数器
                    qpsCounter.forEach((path, counter) -> counter.set(0));
                    lastResetTime = currentTime;
                }
            }
        }
    }

    /**
     * 获取指定路径的当前QPS
     *
     * @param path API路径
     * @return 当前QPS值
     */
    public long getCurrentQps(String path) {
        AtomicLong counter = qpsCounter.get(path);
        return counter != null ? counter.get() : 0;
    }

    /**
     * 获取所有路径的QPS统计
     *
     * @return 路径与QPS的映射
     */
    public ConcurrentHashMap<String, Long> getAllQpsStats() {
        ConcurrentHashMap<String, Long> stats = new ConcurrentHashMap<>();
        qpsCounter.forEach((path, counter) -> stats.put(path, counter.get()));
        return stats;
    }
} 