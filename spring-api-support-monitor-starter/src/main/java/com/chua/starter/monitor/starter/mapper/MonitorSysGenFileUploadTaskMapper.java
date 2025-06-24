package com.chua.starter.monitor.starter.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chua.starter.monitor.starter.entity.MonitorSysGenFileUploadTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 文件上传任务Mapper接口
 *
 * @author CH
 * @since 2024/12/20
 */
@Mapper
public interface MonitorSysGenFileUploadTaskMapper extends BaseMapper<MonitorSysGenFileUploadTask> {

    /**
     * 分页查询文件上传任务
     *
     * @param page       分页参数
     * @param taskName   任务名称
     * @param status     任务状态
     * @param mode       上传模式
     * @param type       上传类型
     * @param createUser 创建用户
     * @param startTime  开始时间
     * @param endTime    结束时间
     * @return 分页结果
     */
    IPage<MonitorSysGenFileUploadTask> selectTaskPage(Page<MonitorSysGenFileUploadTask> page,
                                                      @Param("taskName") String taskName,
                                                      @Param("status") String status,
                                                      @Param("mode") String mode,
                                                      @Param("type") String type,
                                                      @Param("createUser") String createUser,
                                                      @Param("startTime") LocalDateTime startTime,
                                                      @Param("endTime") LocalDateTime endTime);

    /**
     * 根据状态查询任务
     *
     * @param status 任务状态
     * @return 任务列表
     */
    List<MonitorSysGenFileUploadTask> selectTasksByStatus(@Param("status") String status);

    /**
     * 查询待处理的定时任务
     *
     * @param currentTime 当前时间
     * @return 待处理的定时任务列表
     */
    List<MonitorSysGenFileUploadTask> selectPendingScheduledTasks(@Param("currentTime") LocalDateTime currentTime);

    /**
     * 查询可重试的失败任务
     *
     * @return 可重试的失败任务列表
     */
    List<MonitorSysGenFileUploadTask> selectRetryableTasks();

    /**
     * 查询超时的处理中任务
     *
     * @param timeoutMinutes 超时分钟数
     * @return 超时的处理中任务列表
     */
    List<MonitorSysGenFileUploadTask> selectTimeoutTasks(@Param("timeoutMinutes") Integer timeoutMinutes);

    /**
     * 更新任务状态
     *
     * @param taskId    任务ID
     * @param status    新状态
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 更新行数
     */
    int updateTaskStatus(@Param("taskId") Long taskId,
                         @Param("status") String status,
                         @Param("startTime") LocalDateTime startTime,
                         @Param("endTime") LocalDateTime endTime);

    /**
     * 增加重试次数
     *
     * @param taskId 任务ID
     * @return 更新行数
     */
    int incrementRetryCount(@Param("taskId") Long taskId);

    /**
     * 获取任务统计信息
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 统计信息
     */
    List<Map<String, Object>> getTaskStatistics(@Param("startTime") LocalDateTime startTime,
                                                 @Param("endTime") LocalDateTime endTime);

    /**
     * 根据用户查询任务统计
     *
     * @param createUser 创建用户
     * @param startTime  开始时间
     * @param endTime    结束时间
     * @return 用户任务统计
     */
    Map<String, Object> getUserTaskStatistics(@Param("createUser") String createUser,
                                               @Param("startTime") LocalDateTime startTime,
                                               @Param("endTime") LocalDateTime endTime);

    /**
     * 查询热门文件类型
     *
     * @param limit 限制数量
     * @return 热门文件类型统计
     */
    List<Map<String, Object>> getPopularFileTypes(@Param("limit") Integer limit);

    /**
     * 查询大文件任务
     *
     * @param minSizeMB 最小文件大小(MB)
     * @param limit     限制数量
     * @return 大文件任务列表
     */
    List<MonitorSysGenFileUploadTask> getLargeFileTasks(@Param("minSizeMB") Integer minSizeMB,
                                                        @Param("limit") Integer limit);

    /**
     * 查询最近的任务
     *
     * @param limit 限制数量
     * @return 最近的任务列表
     */
    List<MonitorSysGenFileUploadTask> getRecentTasks(@Param("limit") Integer limit);

    /**
     * 根据文件MD5查询任务
     *
     * @param fileMd5 文件MD5
     * @return 任务列表
     */
    List<MonitorSysGenFileUploadTask> selectTasksByFileMd5(@Param("fileMd5") String fileMd5);

    /**
     * 查询队列中的任务数量
     *
     * @return 队列中的任务数量
     */
    Long getQueueTaskCount();

    /**
     * 查询今日任务统计
     *
     * @return 今日任务统计
     */
    Map<String, Object> getTodayTaskStatistics();

    /**
     * 删除过期任务
     *
     * @param expiredTime 过期时间
     * @return 删除数量
     */
    int deleteExpiredTasks(@Param("expiredTime") LocalDateTime expiredTime);
}
