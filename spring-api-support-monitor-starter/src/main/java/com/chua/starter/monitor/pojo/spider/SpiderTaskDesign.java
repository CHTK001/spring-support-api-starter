package com.chua.starter.monitor.pojo.spider;

import com.chua.starter.monitor.entity.MonitorSpiderConnection;
import com.chua.starter.monitor.entity.MonitorSpiderNode;
import lombok.Data;

import java.util.List;

/**
 * 任务设计数据
 *
 * @author CH
 * @since 2024/12/28
 */
@Data
public class SpiderTaskDesign {
    private Integer taskId;
    private List<MonitorSpiderNode> nodes;
    private List<MonitorSpiderConnection> connections;
}
