package com.chua.starter.monitor.starter.chain.filter;

import com.chua.starter.monitor.starter.entity.MonitorProxyPluginLimit;
import com.chua.starter.monitor.starter.entity.MonitorProxyPluginList;
import lombok.Data;
import lombok.Setter;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 监控工厂
 * @author CH
 * @since 2024/7/30
 */
@Data
@Setter
public class ReportFactory {

    private ReportUrlLimitFactory reportUrlLimitFactory;
    private ReportIpLimitFactory reportIpLimitFactory;
    private ReportBlackLimitFactory reportBlackLimitFactory;
    private ReportWhiteLimitFactory reportWhiteLimitFactory;
    private ReportQpsLimitFactory reportQpsLimitFactory;


    /**
     * 升级
     * @param list list
     */
    public void upgrade(List<MonitorProxyPluginLimit> list) {
        if(null != reportUrlLimitFactory) {
            reportUrlLimitFactory.refresh(list);
        }

        if(null != reportIpLimitFactory) {
            reportIpLimitFactory.refresh(list);
        }

        if (null != reportQpsLimitFactory) {
            reportQpsLimitFactory.refresh(list);
        }
    }

    /**
     * 升级
     * @param list list
     */
    public void upgradeList(List<MonitorProxyPluginList> list) {
        if(null != reportBlackLimitFactory) {
            reportBlackLimitFactory.refresh(list);
        }

        if(null != reportWhiteLimitFactory) {
            reportWhiteLimitFactory.refresh(list);
        }
    }

    /**
     * 检查路径是否允许通过QPS限流
     *
     * @param path API路径
     * @return 是否允许访问
     */
    public boolean tryAcquireQps(String path) {
        if (null == reportQpsLimitFactory) {
            return true;
        }
        return reportQpsLimitFactory.tryAcquire(path);
    }

    /**
     * 获取所有路径的QPS统计数据
     *
     * @return 路径与QPS的映射
     */
    public Map<String, Long> getQpsStats() {
        if (null == reportQpsLimitFactory) {
            return new ConcurrentHashMap<>();
        }
        return reportQpsLimitFactory.getAllQpsStats();
    }

    /**
     * 获取指定路径的当前QPS
     *
     * @param path API路径
     * @return 当前QPS值
     */
    public long getCurrentQps(String path) {
        if (null == reportQpsLimitFactory) {
            return 0;
        }
        return reportQpsLimitFactory.getCurrentQps(path);
    }
}
