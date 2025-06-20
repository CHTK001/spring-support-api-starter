package com.chua.starter.monitor.starter.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chua.starter.monitor.starter.entity.MonitorSysGenFileUploadRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

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
    int updateVerificationStatus(@Param("recordId") Long recordId,
                                 @Param("verificationStatus") String verificationStatus,
                                 @Param("remoteMd5") String remoteMd5);

    /**
     * 增加重试次数
     *
     * @param recordId 记录ID
     * @return 更新行数
     */
    int incrementRetryCount(@Param("recordId") Long recordId);

    /**
     * 获取任务的上传统计
     *
     * @param taskId 任务ID
     * @return 上传统计
     */
    Map<String, Object> getTaskUploadStatistics(@Param("taskId") Long taskId);

    /**
     * 获取服务器上传统计
     *
     * @param serverId  服务器ID
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 服务器上传统计
     */
    Map<String, Object> getServerUploadStatistics(@Param("serverId") Integer serverId,
                                                   @Param("startTime") LocalDateTime startTime,
                                                   @Param("endTime") LocalDateTime endTime);

    /**
     * 获取上传速度排行
     *
     * @param limit 限制数量
     * @return 上传速度排行
     */
    List<Map<String, Object>> getUploadSpeedRanking(@Param("limit") Integer limit);

    /**
     * 获取上传时间分布
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 上传时间分布
     */
    List<Map<String, Object>> getUploadTimeDistribution(@Param("startTime") LocalDateTime startTime,
                                                         @Param("endTime") LocalDateTime endTime);

    /**
     * 查询长时间上传的记录
     *
     * @param hours 小时数
     * @return 长时间上传的记录列表
     */
    List<MonitorSysGenFileUploadRecord> getLongTimeUploadingRecords(@Param("hours") Integer hours);

    /**
     * 删除过期记录
     *
     * @param expiredTime 过期时间
     * @return 删除数量
     */
    int deleteExpiredRecords(@Param("expiredTime") LocalDateTime expiredTime);
}
