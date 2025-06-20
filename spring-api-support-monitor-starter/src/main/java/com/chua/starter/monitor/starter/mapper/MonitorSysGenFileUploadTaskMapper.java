package com.chua.starter.monitor.starter.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chua.starter.monitor.starter.entity.MonitorSysGenFileUploadTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

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
    @Select("<script>" +
            "SELECT * FROM monitor_sys_gen_file_upload_tasks " +
            "WHERE monitor_sys_gen_file_upload_task_status = 1 " +
            "<if test='taskName != null and taskName != \"\"'>" +
            "AND monitor_sys_gen_file_upload_task_name LIKE CONCAT('%', #{taskName}, '%') " +
            "</if>" +
            "<if test='status != null and status != \"\"'>" +
            "AND monitor_sys_gen_file_upload_status = #{status} " +
            "</if>" +
            "<if test='mode != null and mode != \"\"'>" +
            "AND monitor_sys_gen_file_upload_mode = #{mode} " +
            "</if>" +
            "<if test='type != null and type != \"\"'>" +
            "AND monitor_sys_gen_file_upload_type = #{type} " +
            "</if>" +
            "<if test='createUser != null and createUser != \"\"'>" +
            "AND monitor_sys_gen_file_upload_create_user = #{createUser} " +
            "</if>" +
            "<if test='startTime != null'>" +
            "AND monitor_sys_gen_file_upload_create_time >= #{startTime} " +
            "</if>" +
            "<if test='endTime != null'>" +
            "AND monitor_sys_gen_file_upload_create_time <= #{endTime} " +
            "</if>" +
            "ORDER BY monitor_sys_gen_file_upload_priority ASC, monitor_sys_gen_file_upload_create_time DESC" +
            "</script>")
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
    @Select("SELECT * FROM monitor_sys_gen_file_upload_tasks " +
            "WHERE monitor_sys_gen_file_upload_status = #{status} " +
            "AND monitor_sys_gen_file_upload_task_status = 1 " +
            "ORDER BY monitor_sys_gen_file_upload_priority ASC, monitor_sys_gen_file_upload_create_time ASC")
    List<MonitorSysGenFileUploadTask> selectTasksByStatus(@Param("status") String status);

    /**
     * 查询待处理的定时任务
     *
     * @param currentTime 当前时间
     * @return 待处理的定时任务列表
     */
    @Select("SELECT * FROM monitor_sys_gen_file_upload_tasks " +
            "WHERE monitor_sys_gen_file_upload_status = 'PENDING' " +
            "AND monitor_sys_gen_file_upload_mode = 'SCHEDULED' " +
            "AND monitor_sys_gen_file_upload_scheduled_time <= #{currentTime} " +
            "AND monitor_sys_gen_file_upload_task_status = 1 " +
            "ORDER BY monitor_sys_gen_file_upload_priority ASC, monitor_sys_gen_file_upload_scheduled_time ASC")
    List<MonitorSysGenFileUploadTask> selectPendingScheduledTasks(@Param("currentTime") LocalDateTime currentTime);

    /**
     * 查询可重试的失败任务
     *
     * @return 可重试的失败任务列表
     */
    @Select("SELECT * FROM monitor_sys_gen_file_upload_tasks " +
            "WHERE monitor_sys_gen_file_upload_status = 'FAILED' " +
            "AND monitor_sys_gen_file_upload_retry_count < monitor_sys_gen_file_upload_max_retry " +
            "AND monitor_sys_gen_file_upload_task_status = 1 " +
            "ORDER BY monitor_sys_gen_file_upload_priority ASC, monitor_sys_gen_file_upload_update_time ASC")
    List<MonitorSysGenFileUploadTask> selectRetryableTasks();

    /**
     * 查询超时的处理中任务
     *
     * @param timeoutMinutes 超时分钟数
     * @return 超时的处理中任务列表
     */
    @Select("SELECT * FROM monitor_sys_gen_file_upload_tasks " +
            "WHERE monitor_sys_gen_file_upload_status = 'PROCESSING' " +
            "AND monitor_sys_gen_file_upload_start_time < DATE_SUB(NOW(), INTERVAL #{timeoutMinutes} MINUTE) " +
            "AND monitor_sys_gen_file_upload_task_status = 1")
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
    @Update("UPDATE monitor_sys_gen_file_upload_tasks SET " +
            "monitor_sys_gen_file_upload_status = #{status}, " +
            "monitor_sys_gen_file_upload_start_time = #{startTime}, " +
            "monitor_sys_gen_file_upload_end_time = #{endTime}, " +
            "monitor_sys_gen_file_upload_update_time = NOW() " +
            "WHERE monitor_sys_gen_file_upload_task_id = #{taskId}")
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
    @Update("UPDATE monitor_sys_gen_file_upload_tasks SET " +
            "monitor_sys_gen_file_upload_retry_count = monitor_sys_gen_file_upload_retry_count + 1, " +
            "monitor_sys_gen_file_upload_update_time = NOW() " +
            "WHERE monitor_sys_gen_file_upload_task_id = #{taskId}")
    int incrementRetryCount(@Param("taskId") Long taskId);

    /**
     * 获取任务统计信息
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 统计信息
     */
    @Select("SELECT " +
            "monitor_sys_gen_file_upload_status as status, " +
            "COUNT(*) as count, " +
            "SUM(monitor_sys_gen_file_upload_file_size) as totalSize " +
            "FROM monitor_sys_gen_file_upload_tasks " +
            "WHERE monitor_sys_gen_file_upload_task_status = 1 " +
            "AND monitor_sys_gen_file_upload_create_time BETWEEN #{startTime} AND #{endTime} " +
            "GROUP BY monitor_sys_gen_file_upload_status")
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
    @Select("SELECT " +
            "COUNT(*) as totalTasks, " +
            "SUM(CASE WHEN monitor_sys_gen_file_upload_status = 'COMPLETED' THEN 1 ELSE 0 END) as completedTasks, " +
            "SUM(CASE WHEN monitor_sys_gen_file_upload_status = 'FAILED' THEN 1 ELSE 0 END) as failedTasks, " +
            "SUM(monitor_sys_gen_file_upload_file_size) as totalSize " +
            "FROM monitor_sys_gen_file_upload_tasks " +
            "WHERE monitor_sys_gen_file_upload_create_user = #{createUser} " +
            "AND monitor_sys_gen_file_upload_task_status = 1 " +
            "AND monitor_sys_gen_file_upload_create_time BETWEEN #{startTime} AND #{endTime}")
    Map<String, Object> getUserTaskStatistics(@Param("createUser") String createUser,
                                               @Param("startTime") LocalDateTime startTime,
                                               @Param("endTime") LocalDateTime endTime);

    /**
     * 查询热门文件类型
     *
     * @param limit 限制数量
     * @return 热门文件类型统计
     */
    @Select("SELECT " +
            "SUBSTRING_INDEX(monitor_sys_gen_file_upload_file_name, '.', -1) as fileType, " +
            "COUNT(*) as count, " +
            "SUM(monitor_sys_gen_file_upload_file_size) as totalSize " +
            "FROM monitor_sys_gen_file_upload_tasks " +
            "WHERE monitor_sys_gen_file_upload_task_status = 1 " +
            "AND monitor_sys_gen_file_upload_file_name LIKE '%.%' " +
            "GROUP BY fileType " +
            "ORDER BY count DESC " +
            "LIMIT #{limit}")
    List<Map<String, Object>> getPopularFileTypes(@Param("limit") Integer limit);

    /**
     * 查询大文件任务
     *
     * @param minSizeMB 最小文件大小(MB)
     * @param limit     限制数量
     * @return 大文件任务列表
     */
    @Select("SELECT * FROM monitor_sys_gen_file_upload_tasks " +
            "WHERE monitor_sys_gen_file_upload_file_size >= #{minSizeMB} * 1024 * 1024 " +
            "AND monitor_sys_gen_file_upload_task_status = 1 " +
            "ORDER BY monitor_sys_gen_file_upload_file_size DESC " +
            "LIMIT #{limit}")
    List<MonitorSysGenFileUploadTask> getLargeFileTasks(@Param("minSizeMB") Integer minSizeMB,
                                                        @Param("limit") Integer limit);

    /**
     * 查询最近的任务
     *
     * @param limit 限制数量
     * @return 最近的任务列表
     */
    @Select("SELECT * FROM monitor_sys_gen_file_upload_tasks " +
            "WHERE monitor_sys_gen_file_upload_task_status = 1 " +
            "ORDER BY monitor_sys_gen_file_upload_create_time DESC " +
            "LIMIT #{limit}")
    List<MonitorSysGenFileUploadTask> getRecentTasks(@Param("limit") Integer limit);

    /**
     * 根据文件MD5查询任务
     *
     * @param fileMd5 文件MD5
     * @return 任务列表
     */
    @Select("SELECT * FROM monitor_sys_gen_file_upload_tasks " +
            "WHERE monitor_sys_gen_file_upload_file_md5 = #{fileMd5} " +
            "AND monitor_sys_gen_file_upload_task_status = 1 " +
            "ORDER BY monitor_sys_gen_file_upload_create_time DESC")
    List<MonitorSysGenFileUploadTask> selectTasksByFileMd5(@Param("fileMd5") String fileMd5);

    /**
     * 查询队列中的任务数量
     *
     * @return 队列中的任务数量
     */
    @Select("SELECT COUNT(*) FROM monitor_sys_gen_file_upload_tasks " +
            "WHERE monitor_sys_gen_file_upload_status IN ('PENDING', 'PROCESSING') " +
            "AND monitor_sys_gen_file_upload_task_status = 1")
    Long getQueueTaskCount();

    /**
     * 查询今日任务统计
     *
     * @return 今日任务统计
     */
    @Select("SELECT " +
            "COUNT(*) as totalTasks, " +
            "SUM(CASE WHEN monitor_sys_gen_file_upload_status = 'COMPLETED' THEN 1 ELSE 0 END) as completedTasks, " +
            "SUM(CASE WHEN monitor_sys_gen_file_upload_status = 'FAILED' THEN 1 ELSE 0 END) as failedTasks, " +
            "SUM(CASE WHEN monitor_sys_gen_file_upload_status = 'PROCESSING' THEN 1 ELSE 0 END) as processingTasks, " +
            "SUM(monitor_sys_gen_file_upload_file_size) as totalSize " +
            "FROM monitor_sys_gen_file_upload_tasks " +
            "WHERE DATE(monitor_sys_gen_file_upload_create_time) = CURDATE() " +
            "AND monitor_sys_gen_file_upload_task_status = 1")
    Map<String, Object> getTodayTaskStatistics();

    /**
     * 删除过期任务
     *
     * @param expiredTime 过期时间
     * @return 删除数量
     */
    @Update("UPDATE monitor_sys_gen_file_upload_tasks SET " +
            "monitor_sys_gen_file_upload_task_status = 0, " +
            "monitor_sys_gen_file_upload_update_time = NOW() " +
            "WHERE monitor_sys_gen_file_upload_create_time < #{expiredTime} " +
            "AND monitor_sys_gen_file_upload_status IN ('COMPLETED', 'FAILED', 'CANCELLED')")
    int deleteExpiredTasks(@Param("expiredTime") LocalDateTime expiredTime);
}
