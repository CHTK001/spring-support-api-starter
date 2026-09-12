package com.chua.starter.geo.service;

import com.chua.starter.geo.model.TaskInfo;

import java.util.List;

/**
* 下载任务管理服务，负责任务的创建、状态追踪与生命周期管理。
*
* @author CH
* @since 1.0.0
 */
public interface TaskManagerService {

    /**
    * 创建下载任务。
    *
    * @param name      任务名称
    * @param source    图源 标识
    * @param sourceName 图源显示名称
    * @param zoom      缩放级别
    * @param format    输出格式
    * @param savePath  保存路径
    * @param total     总瓦片数
    * @return 任务 标识
     */
    String createTask(String name, String source, String sourceName, int zoom,
                      String format, String savePath, int total);

    /**
    * 获取任务信息。
    *
    * @param taskId 任务 标识
    * @return 任务信息，不存在返回 空
     */
    TaskInfo getTask(String taskId);

    /**
    * 列出所有任务。
    *
    * @return 任务列表
     */
    List<TaskInfo> listTasks();

    /**
    * 更新任务进度。
    *
    * @param taskId      任务 标识
    * @param completed   已完成数
    * @param failed      失败数
    * @param progress    进度百分比
    * @param message     状态消息
     */
    void updateProgress(String taskId, int completed, int failed, double progress, String message);

    /**
    * 设置任务状态。
    *
    * @param taskId  任务 标识
    * @param status  目标状态
     */
    void setStatus(String taskId, com.chua.starter.geo.model.TaskStatus status);

    /**
    * 取消任务。
    *
    * @param taskId 任务 标识
     */
    void cancelTask(String taskId);

    /**
    * 删除任务记录。
    *
    * @param taskId 任务 标识
     */
    void removeTask(String taskId);
}
