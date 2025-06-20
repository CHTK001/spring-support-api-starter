package com.chua.starter.monitor.starter.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chua.starter.monitor.starter.entity.MonitorSysGenFileUploadRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 文件上传记录Mapper接口
 *
 * @author CH
 * @since 2024/12/20
 */
@Mapper
public interface MonitorSysGenFileUploadRecordMapper extends BaseMapper<MonitorSysGenFileUploadRecord> {

    /**
     * 分页查询文件上传记录
     *
     * @param page      分页参数
     * @param taskId    任务ID
     * @param serverId  服务器ID
     * @param status    上传状态
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 分页结果
     */
    IPage<MonitorSysGenFileUploadRecord> selectRecordPage(Page<MonitorSysGenFileUploadRecord> page,
                                                          @Param("taskId") Long taskId,
                                                          @Param("serverId") Integer serverId,
                                                          @Param("status") String status,
                                                          @Param("startTime") LocalDateTime startTime,
                                                          @Param("endTime") LocalDateTime endTime);

    /**
     * 根据任务ID查询上传记录
     *
     * @param taskId 任务ID
     * @return 上传记录列表
     */
    List<MonitorSysGenFileUploadRecord> selectRecordsByTaskId(@Param("taskId") Long taskId);

    /**
     * 根据服务器ID查询上传记录
     *
     * @param serverId 服务器ID
     * @param limit    限制数量
     * @return 上传记录列表
     */
    List<MonitorSysGenFileUploadRecord> selectRecordsByServerId(@Param("serverId") Integer serverId,
                                                                @Param("limit") Integer limit);

    /**
     * 根据状态查询上传记录
     *
     * @param status 上传状态
     * @return 上传记录列表
     */
    List<MonitorSysGenFileUploadRecord> selectRecordsByStatus(@Param("status") String status);

    /**
     * 查询正在上传的记录
     *
     * @return 正在上传的记录列表
     */
    List<MonitorSysGenFileUploadRecord> selectUploadingRecords();

    /**
     * 查询失败的上传记录
     *
     * @param limit 限制数量
     * @return 失败的上传记录列表
     */
    List<MonitorSysGenFileUploadRecord> selectFailedRecords(@Param("limit") Integer limit);

    /**
     * 更新上传进度
     *
     * @param recordId          记录ID
     * @param progress          进度
     * @param transferredBytes  已传输字节数
     * @param speed             上传速度
     * @return 更新行数
     */
    @Update("UPDATE monitor_sys_gen_file_upload_records SET " +
            "monitor_sys_gen_file_upload_progress = #{progress}, " +
            "monitor_sys_gen_file_upload_transferred_bytes = #{transferredBytes}, " +
            "monitor_sys_gen_file_upload_speed = #{speed}, " +
            "monitor_sys_gen_file_upload_update_time = NOW() " +
            "WHERE monitor_sys_gen_file_upload_record_id = #{recordId}")
    int updateUploadProgress(@Param("recordId") Long recordId,
                             @Param("progress") Integer progress,
                             @Param("transferredBytes") Long transferredBytes,
                             @Param("speed") Long speed);

    /**
     * 更新上传状态
     *
     * @param recordId     记录ID
     * @param status       状态
     * @param errorMessage 错误信息
     * @param errorCode    错误代码
     * @param endTime      结束时间
     * @param durationMs   耗时
     * @return 更新行数
     */
    @Update("UPDATE monitor_sys_gen_file_upload_records SET " +
            "monitor_sys_gen_file_upload_status = #{status}, " +
            "monitor_sys_gen_file_upload_error_message = #{errorMessage}, " +
            "monitor_sys_gen_file_upload_error_code = #{errorCode}, " +
            "monitor_sys_gen_file_upload_end_time = #{endTime}, " +
            "monitor_sys_gen_file_upload_duration_ms = #{durationMs}, " +
            "monitor_sys_gen_file_upload_update_time = NOW() " +
            "WHERE monitor_sys_gen_file_upload_record_id = #{recordId}")
    int updateUploadStatus(@Param("recordId") Long recordId,
                           @Param("status") String status,
                           @Param("errorMessage") String errorMessage,
                           @Param("errorCode") String errorCode,
                           @Param("endTime") LocalDateTime endTime,
                           @Param("durationMs") Long durationMs);

    /**
     * 更新验证状态
     *
     * @param recordId           记录ID
     * @param verificationStatus 验证状态
     * @param remoteMd5          远程文件MD5
     * @return 更新行数
     */
    @Update("UPDATE monitor_sys_gen_file_upload_records SET " +
            "monitor_sys_gen_file_upload_verification_status = #{verificationStatus}, " +
            "monitor_sys_gen_file_upload_file_md5_remote = #{remoteMd5}, " +
            "monitor_sys_gen_file_upload_update_time = NOW() " +
            "WHERE monitor_sys_gen_file_upload_record_id = #{recordId}")
    int updateVerificationStatus(@Param("recordId") Long recordId,
                                 @Param("verificationStatus") String verificationStatus,
                                 @Param("remoteMd5") String remoteMd5);

    /**
     * 增加重试次数
     *
     * @param recordId 记录ID
     * @return 更新行数
     */
    @Update("UPDATE monitor_sys_gen_file_upload_records SET " +
            "monitor_sys_gen_file_upload_retry_count = monitor_sys_gen_file_upload_retry_count + 1, " +
            "monitor_sys_gen_file_upload_update_time = NOW() " +
            "WHERE monitor_sys_gen_file_upload_record_id = #{recordId}")
    int incrementRetryCount(@Param("recordId") Long recordId);

    /**
     * 获取任务的上传统计
     *
     * @param taskId 任务ID
     * @return 上传统计
     */
    @Select("SELECT " +
            "COUNT(*) as totalRecords, " +
            "SUM(CASE WHEN monitor_sys_gen_file_upload_status = 'COMPLETED' THEN 1 ELSE 0 END) as completedRecords, " +
            "SUM(CASE WHEN monitor_sys_gen_file_upload_status = 'FAILED' THEN 1 ELSE 0 END) as failedRecords, " +
            "SUM(CASE WHEN monitor_sys_gen_file_upload_status = 'UPLOADING' THEN 1 ELSE 0 END) as uploadingRecords, " +
            "AVG(monitor_sys_gen_file_upload_progress) as avgProgress, " +
            "SUM(monitor_sys_gen_file_upload_transferred_bytes) as totalTransferred, " +
            "AVG(monitor_sys_gen_file_upload_speed) as avgSpeed " +
            "FROM monitor_sys_gen_file_upload_records " +
            "WHERE monitor_sys_gen_file_upload_task_id = #{taskId} " +
            "AND monitor_sys_gen_file_upload_record_status = 1")
    Map<String, Object> getTaskUploadStatistics(@Param("taskId") Long taskId);

    /**
     * 获取服务器上传统计
     *
     * @param serverId  服务器ID
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 服务器上传统计
     */
    @Select("SELECT " +
            "COUNT(*) as totalRecords, " +
            "SUM(CASE WHEN monitor_sys_gen_file_upload_status = 'COMPLETED' THEN 1 ELSE 0 END) as completedRecords, " +
            "SUM(CASE WHEN monitor_sys_gen_file_upload_status = 'FAILED' THEN 1 ELSE 0 END) as failedRecords, " +
            "SUM(monitor_sys_gen_file_upload_transferred_bytes) as totalTransferred, " +
            "AVG(monitor_sys_gen_file_upload_speed) as avgSpeed, " +
            "AVG(monitor_sys_gen_file_upload_duration_ms) as avgDuration " +
            "FROM monitor_sys_gen_file_upload_records " +
            "WHERE monitor_sys_gen_server_id = #{serverId} " +
            "AND monitor_sys_gen_file_upload_record_status = 1 " +
            "AND monitor_sys_gen_file_upload_create_time BETWEEN #{startTime} AND #{endTime}")
    Map<String, Object> getServerUploadStatistics(@Param("serverId") Integer serverId,
                                                   @Param("startTime") LocalDateTime startTime,
                                                   @Param("endTime") LocalDateTime endTime);

    /**
     * 获取上传速度排行
     *
     * @param limit 限制数量
     * @return 上传速度排行
     */
    @Select("SELECT " +
            "monitor_sys_gen_server_id as serverId, " +
            "AVG(monitor_sys_gen_file_upload_speed) as avgSpeed, " +
            "MAX(monitor_sys_gen_file_upload_speed) as maxSpeed, " +
            "COUNT(*) as recordCount " +
            "FROM monitor_sys_gen_file_upload_records " +
            "WHERE monitor_sys_gen_file_upload_status = 'COMPLETED' " +
            "AND monitor_sys_gen_file_upload_record_status = 1 " +
            "AND monitor_sys_gen_file_upload_speed > 0 " +
            "GROUP BY monitor_sys_gen_server_id " +
            "ORDER BY avgSpeed DESC " +
            "LIMIT #{limit}")
    List<Map<String, Object>> getUploadSpeedRanking(@Param("limit") Integer limit);

    /**
     * 获取上传时间分布
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 上传时间分布
     */
    @Select("SELECT " +
            "DATE_FORMAT(monitor_sys_gen_file_upload_start_time, '%Y-%m-%d %H:00:00') as hour, " +
            "COUNT(*) as count, " +
            "SUM(monitor_sys_gen_file_upload_transferred_bytes) as totalBytes " +
            "FROM monitor_sys_gen_file_upload_records " +
            "WHERE monitor_sys_gen_file_upload_start_time BETWEEN #{startTime} AND #{endTime} " +
            "AND monitor_sys_gen_file_upload_record_status = 1 " +
            "GROUP BY DATE_FORMAT(monitor_sys_gen_file_upload_start_time, '%Y-%m-%d %H:00:00') " +
            "ORDER BY hour")
    List<Map<String, Object>> getUploadTimeDistribution(@Param("startTime") LocalDateTime startTime,
                                                         @Param("endTime") LocalDateTime endTime);

    /**
     * 查询长时间上传的记录
     *
     * @param hours 小时数
     * @return 长时间上传的记录列表
     */
    @Select("SELECT * FROM monitor_sys_gen_file_upload_records " +
            "WHERE monitor_sys_gen_file_upload_status = 'UPLOADING' " +
            "AND monitor_sys_gen_file_upload_start_time < DATE_SUB(NOW(), INTERVAL #{hours} HOUR) " +
            "AND monitor_sys_gen_file_upload_record_status = 1 " +
            "ORDER BY monitor_sys_gen_file_upload_start_time ASC")
    List<MonitorSysGenFileUploadRecord> getLongTimeUploadingRecords(@Param("hours") Integer hours);

    /**
     * 删除过期记录
     *
     * @param expiredTime 过期时间
     * @return 删除数量
     */
    @Update("UPDATE monitor_sys_gen_file_upload_records SET " +
            "monitor_sys_gen_file_upload_record_status = 0, " +
            "monitor_sys_gen_file_upload_update_time = NOW() " +
            "WHERE monitor_sys_gen_file_upload_create_time < #{expiredTime} " +
            "AND monitor_sys_gen_file_upload_status IN ('COMPLETED', 'FAILED', 'CANCELLED')")
    int deleteExpiredRecords(@Param("expiredTime") LocalDateTime expiredTime);
}
