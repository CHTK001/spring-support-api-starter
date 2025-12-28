package com.chua.starter.monitor.pojo.spider;

import lombok.Data;

import java.util.List;

/**
 * 批量任务请求
 *
 * @author CH
 * @since 2024/12/28
 */
@Data
public class BatchTaskRequest {
    private List<Integer> taskIds;
    private Integer status;
}
