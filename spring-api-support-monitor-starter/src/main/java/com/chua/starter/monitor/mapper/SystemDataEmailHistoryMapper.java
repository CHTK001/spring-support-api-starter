package com.chua.starter.monitor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.monitor.entity.SystemDataEmailHistory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 邮件历史记录Mapper
 *
 * @author CH
 * @since 2025-01-20
 */
@Mapper
public interface SystemDataEmailHistoryMapper extends BaseMapper<SystemDataEmailHistory> {

    /**
     * 根据数据源配置ID查询邮件列表
     *
     * @param settingId 数据源配置ID
     * @return 邮件列表
     */
    List<SystemDataEmailHistory> selectBySettingId(@Param("settingId") Integer settingId);

    /**
     * 根据邮件ID查询邮件
     *
     * @param messageId 邮件ID
     * @param settingId 数据源配置ID
     * @return 邮件信息
     */
    SystemDataEmailHistory selectByMessageId(@Param("messageId") String messageId, @Param("settingId") Integer settingId);

    /**
     * 更新邮件已读状态
     *
     * @param id 邮件历史记录ID
     * @param isRead 是否已读
     * @return 更新行数
     */
    int updateReadStatus(@Param("id") Integer id, @Param("isRead") Boolean isRead);

    /**
     * 更新邮件收藏状态
     *
     * @param id 邮件历史记录ID
     * @param isStarred 是否收藏
     * @return 更新行数
     */
    int updateStarredStatus(@Param("id") Integer id, @Param("isStarred") Boolean isStarred);

    /**
     * 更新邮件标签
     *
     * @param id 邮件历史记录ID
     * @param labels 标签
     * @return 更新行数
     */
    int updateLabels(@Param("id") Integer id, @Param("labels") String labels);

    /**
     * 根据标签查询邮件
     *
     * @param settingId 数据源配置ID
     * @param label 标签
     * @return 邮件列表
     */
    List<SystemDataEmailHistory> selectByLabel(@Param("settingId") Integer settingId, @Param("label") String label);

    /**
     * 查询未读邮件
     *
     * @param settingId 数据源配置ID
     * @return 未读邮件列表
     */
    List<SystemDataEmailHistory> selectUnreadEmails(@Param("settingId") Integer settingId);

    /**
     * 查询收藏邮件
     *
     * @param settingId 数据源配置ID
     * @return 收藏邮件列表
     */
    List<SystemDataEmailHistory> selectStarredEmails(@Param("settingId") Integer settingId);

    /**
     * 更新同步状态
     *
     * @param id 邮件历史记录ID
     * @param syncStatus 同步状态
     * @param errorMessage 错误信息
     * @return 更新行数
     */
    int updateSyncStatus(@Param("id") Integer id, @Param("syncStatus") Integer syncStatus, @Param("errorMessage") String errorMessage);
}