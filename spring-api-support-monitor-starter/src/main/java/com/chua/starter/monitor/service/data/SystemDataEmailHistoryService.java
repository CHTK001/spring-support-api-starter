package com.chua.starter.monitor.service.data;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.entity.SystemDataEmailHistory;
import com.chua.starter.monitor.pojo.data.EmailFetch;
import com.chua.starter.monitor.pojo.response.EmailBackupResponse;
import com.chua.starter.monitor.pojo.response.EmailFetchResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 邮件历史数据服务接口
 *
 * @author CH
 * @since 2025/01/20
 */
public interface SystemDataEmailHistoryService extends IService<SystemDataEmailHistory> {

    /**
     * 拉取邮件
     *
     * @param settingId  数据源配置ID
     * @param emailFetch 邮件文件夹名称
     * @return 拉取结果
     */
    ReturnResult<EmailFetchResponse> fetchEmails(Integer settingId, EmailFetch emailFetch);

    /**
     * 分页查询邮件历史
     *
     * @param settingId 数据源配置ID
     * @param current 当前页码
     * @param size 每页大小
     * @param isRead 是否已读（可选）
     * @param isStarred 是否收藏（可选）
     * @param label 标签（可选）
     * @return 分页结果
     */
    ReturnResult<Page<SystemDataEmailHistory>> getEmailHistory(Integer settingId, long current, long size, 
                                                               Boolean isRead, Boolean isStarred, String label);

    /**
     * 邮件云备份（前端上传邮件保存到数据库）
     *
     * @param settingId 数据源配置ID
     * @param file 邮件文件
     * @return 备份结果
     */
    ReturnResult<EmailBackupResponse> backupEmail(Integer settingId, MultipartFile file);

    /**
     * 云同步拉取数据到前端
     *
     * @param settingId 数据源配置ID
     * @param syncType 同步类型：all-全部，unread-未读，starred-收藏
     * @return 同步结果
     */
    ReturnResult<List<SystemDataEmailHistory>> syncEmails(Integer settingId, String syncType);

    /**
     * 更新邮件状态
     *
     * @param emailId 邮件ID
     * @param isRead 是否已读
     * @param isStarred 是否收藏
     * @param labels 标签
     * @return 更新结果
     */
    ReturnResult<Boolean> updateEmailStatus(Integer emailId, Boolean isRead, Boolean isStarred, String labels);
}