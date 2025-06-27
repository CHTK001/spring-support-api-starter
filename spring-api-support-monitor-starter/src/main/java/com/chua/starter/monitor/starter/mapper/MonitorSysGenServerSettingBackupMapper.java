package com.chua.starter.monitor.starter.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.monitor.starter.entity.MonitorSysGenServerSettingBackup;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 服务器配置备份Mapper接口
 *
 * @author CH
 * @since 2024/12/27
 */
@Mapper
public interface MonitorSysGenServerSettingBackupMapper extends BaseMapper<MonitorSysGenServerSettingBackup> {

    /**
     * 根据备份名称查询备份
     *
     * @param backupName 备份名称
     * @return 备份记录
     */
    MonitorSysGenServerSettingBackup selectByBackupName(@Param("backupName") String backupName);

    /**
     * 根据备份类型查询备份列表
     *
     * @param backupType 备份类型
     * @return 备份列表
     */
    List<MonitorSysGenServerSettingBackup> selectByBackupType(@Param("backupType") String backupType);

    /**
     * 根据创建用户查询备份列表
     *
     * @param createUser 创建用户
     * @return 备份列表
     */
    List<MonitorSysGenServerSettingBackup> selectByCreateUser(@Param("createUser") String createUser);

    /**
     * 根据时间范围查询备份列表
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 备份列表
     */
    List<MonitorSysGenServerSettingBackup> selectByTimeRange(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    /**
     * 获取备份统计信息
     *
     * @return 统计信息
     */
    Map<String, Object> getBackupStatistics();

    /**
     * 删除指定时间之前的备份
     *
     * @param beforeTime 时间点
     * @return 删除数量
     */
    int deleteBeforeTime(@Param("beforeTime") LocalDateTime beforeTime);

    /**
     * 获取最新的自动备份
     *
     * @return 最新自动备份
     */
    MonitorSysGenServerSettingBackup selectLatestAutoBackup();

    /**
     * 根据备份状态查询备份列表
     *
     * @param backupStatus 备份状态
     * @return 备份列表
     */
    List<MonitorSysGenServerSettingBackup> selectByBackupStatus(@Param("backupStatus") String backupStatus);
}
