package com.chua.starter.monitor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.datasource.jdbc.option.DataSourceOptions;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.metadata.command.CommandMetadataFactory;
import com.chua.common.support.metadata.command.CommandResult;
import com.chua.common.support.metadata.command.ObjectResultItem;
import com.chua.common.support.utils.SimpleRetry;
import com.chua.common.support.utils.StringUtils;
import com.chua.email.support.metadata.EmailServiceMetadata;
import com.chua.email.support.metadata.MailMessage;
import com.chua.starter.monitor.entity.SystemDataEmailHistory;
import com.chua.starter.monitor.entity.SystemDataSetting;
import com.chua.starter.monitor.mapper.SystemDataEmailHistoryMapper;
import com.chua.starter.monitor.pojo.response.EmailBackupResponse;
import com.chua.starter.monitor.pojo.response.EmailFetchResponse;
import com.chua.starter.monitor.service.SystemDataEmailHistoryService;
import com.chua.starter.monitor.service.SystemDataSettingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 邮件历史数据服务实现类
 *
 * @author CH
 * @since 2025/01/20
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SystemDataEmailHistoryServiceImpl extends ServiceImpl<SystemDataEmailHistoryMapper, SystemDataEmailHistory>
        implements SystemDataEmailHistoryService {

    private final SystemDataSettingService systemDataSettingService;
    private final SystemDataEmailHistoryMapper emailHistoryMapper;

    @Override
    public ReturnResult<EmailFetchResponse> fetchEmails(Integer settingId, String folderName, String command, Integer pageNumber, Integer limit) {
        try {
            SystemDataSetting setting = systemDataSettingService.getById(settingId);
            if (setting == null) {
                return ReturnResult.error("数据源配置不存在");
            }
            DataSourceOptions options = SystemDataConsoleServiceImpl.buildOptions(setting);
            EmailServiceMetadata emailService = (EmailServiceMetadata) CommandMetadataFactory.create(setting.getSystemDataSettingId(),
                    setting.getSystemDataSettingType(), options);

            folderName = "收件箱".equals(folderName) ? "INBOX" : folderName;
            // 获取邮件
            String finalFolderName = folderName;
            CommandResult commandResult = SimpleRetry.with(
                            it -> {
                                try {
                                    return it.executeCommand(command + " " + finalFolderName + " " + pageNumber + " " + limit);
                                } catch (Exception e) {
                                    throw new RuntimeException(e);
                                }
                            },
                            () -> (EmailServiceMetadata) CommandMetadataFactory.create(setting.getSystemDataSettingId(),
                                    setting.getSystemDataSettingType(), options))
                    .retryCount(3)
                    .sleepTime(1000)
                    .onException((it, ex) -> {
                        try {
                            it.close();
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .buildAndRun();
            ObjectResultItem data = (ObjectResultItem) commandResult.getData();
            if (null == data) {
                return ReturnResult.error("拉取邮件失败: 获取邮件失败");
            }
            com.chua.common.support.lang.page.Page<MailMessage> page = (com.chua.common.support.lang.page.Page<MailMessage>) data.getNewRows();
            // 保存到数据库
            int savedCount = 0;
            int updatedCount = 0;
            List<MailMessage> emails = page.getData();
            for (MailMessage email : emails) {
                SystemDataEmailHistory existing = emailHistoryMapper.selectByMessageId(
                        email.getMessageId(), settingId);

                if (existing == null) {
                    existing = new SystemDataEmailHistory();
                    existing.setSystemDataEmailHistorySettingId(settingId);
                    existing.setSystemDataEmailHistoryMessageId(email.getMessageId());
                    existing.setSystemDataEmailHistorySubject(email.getSubject());
                    existing.setSystemDataEmailHistoryFrom(email.getFrom());
                    existing.setSystemDataEmailHistoryTo(StringUtils.join(email.getTo(), ","));
                    existing.setSystemDataEmailHistoryCc(StringUtils.join(email.getCc(), ","));
                    existing.setSystemDataEmailHistoryBcc(StringUtils.join(email.getBcc(), ","));
                    existing.setSystemDataEmailHistoryContent(email.getContent());
                    existing.setSystemDataEmailHistorySentDate(email.getSentDate());
                    existing.setSystemDataEmailHistoryReceivedDate(email.getReceivedDate());
                    emailHistoryMapper.insert(existing);
                    savedCount++;
                } else {
                    updatedCount++;
                }
            }

            EmailFetchResponse response = EmailFetchResponse.builder()
                    .fetchedCount(emails.size())
                    .newCount(savedCount)
                    .duplicateCount(updatedCount)
                    .status("success")
                    .message("邮件拉取成功")
                    .record(page)
                    .folderName(folderName)
                    .settingId(settingId)
                    .build();

            return ReturnResult.ok(response);

        } catch (Exception e) {
            log.error("拉取邮件失败", e);
            return ReturnResult.error("拉取邮件失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Page<SystemDataEmailHistory>> getEmailHistory(Integer settingId, long current, long size,
                                                                      Boolean isRead, Boolean isStarred, String label) {
        try {
            Page<SystemDataEmailHistory> page = new Page<>(current, size);

            // 构建查询条件
            QueryWrapper<SystemDataEmailHistory> queryWrapper = new QueryWrapper<>();

            queryWrapper.eq("system_data_email_history_setting_id", settingId);

            if (isRead != null) {
                queryWrapper.eq("system_data_email_history_is_read", isRead);
            }

            if (isStarred != null) {
                queryWrapper.eq("system_data_email_history_is_starred", isStarred);
            }

            if (label != null && !label.trim().isEmpty()) {
                queryWrapper.like("system_data_email_history_labels", label);
            }

            queryWrapper.orderByDesc("system_data_email_history_received_date");

            Page<SystemDataEmailHistory> result = emailHistoryMapper.selectPage(page, queryWrapper);
            return ReturnResult.ok(result);

        } catch (Exception e) {
            log.error("查询邮件历史失败", e);
            return ReturnResult.error("查询邮件历史失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<EmailBackupResponse> backupEmail(Integer settingId, MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ReturnResult.error("上传文件不能为空");
            }

            SystemDataSetting setting = systemDataSettingService.getById(settingId);
            if (setting == null) {
                return ReturnResult.error("数据源配置不存在");
            }

            // 解析邮件文件内容
            SystemDataEmailHistory emailHistory = parseEmailFile(file, settingId);

            if (emailHistory == null) {
                return ReturnResult.error("邮件文件解析失败");
            }

            // 检查是否已存在
            SystemDataEmailHistory existing = emailHistoryMapper.selectByMessageId(
                    emailHistory.getSystemDataEmailHistoryMessageId(), settingId);

            if (existing != null) {
                return ReturnResult.error("邮件已存在，无需重复备份");
            }

            // 保存到数据库
            emailHistory.setSystemDataEmailHistorySyncStatus(1); // 已同步
            emailHistory.setSystemDataEmailHistorySyncTime(LocalDateTime.now());
            emailHistoryMapper.insert(emailHistory);

            EmailBackupResponse response = EmailBackupResponse.builder()
                    .status("success")
                    .message("邮件备份成功")
                    .emailId(emailHistory.getSystemDataEmailHistoryId())
                    .subject(emailHistory.getSystemDataEmailHistorySubject())
                    .sender(emailHistory.getSystemDataEmailHistoryFrom())
                    .fileName(file.getOriginalFilename())
                    .fileSize(file.getSize())
                    .settingId(settingId)
                    .build();

            return ReturnResult.ok(response);

        } catch (Exception e) {
            log.error("邮件云备份失败", e);
            return ReturnResult.error("邮件云备份失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<List<SystemDataEmailHistory>> syncEmails(Integer settingId, String syncType) {
        try {
            List<SystemDataEmailHistory> emails;

            switch (syncType.toLowerCase()) {
                case "unread":
                    emails = emailHistoryMapper.selectUnreadEmails(settingId);
                    break;
                case "starred":
                    emails = emailHistoryMapper.selectStarredEmails(settingId);
                    break;
                case "all":
                default:
                    emails = emailHistoryMapper.selectBySettingId(settingId);
                    break;
            }

            // 更新同步状态
            for (SystemDataEmailHistory email : emails) {
                emailHistoryMapper.updateSyncStatus(email.getSystemDataEmailHistoryId(), 1, null);
            }

            return ReturnResult.ok(emails);

        } catch (Exception e) {
            log.error("云同步拉取数据失败", e);
            return ReturnResult.error("云同步拉取数据失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Boolean> updateEmailStatus(Integer emailId, Boolean isRead, Boolean isStarred, String labels) {
        try {
            boolean updated = false;

            if (isRead != null) {
                emailHistoryMapper.updateReadStatus(emailId, isRead);
                updated = true;
            }

            if (isStarred != null) {
                emailHistoryMapper.updateStarredStatus(emailId, isStarred);
                updated = true;
            }

            if (labels != null) {
                emailHistoryMapper.updateLabels(emailId, labels);
                updated = true;
            }

            return ReturnResult.ok(updated);

        } catch (Exception e) {
            log.error("更新邮件状态失败", e);
            return ReturnResult.error("更新邮件状态失败: " + e.getMessage());
        }
    }


    /**
     * 解析邮件文件
     */
    private SystemDataEmailHistory parseEmailFile(MultipartFile file, Integer settingId) throws IOException {
        try {
            // 创建邮件历史记录
            SystemDataEmailHistory emailHistory = new SystemDataEmailHistory();
            emailHistory.setSystemDataEmailHistorySettingId(settingId);

            // 生成唯一的消息ID
            emailHistory.setSystemDataEmailHistoryMessageId("upload-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8));

            // 设置基本信息
            emailHistory.setSystemDataEmailHistorySubject("上传的邮件文件: " + file.getOriginalFilename());
            emailHistory.setSystemDataEmailHistoryFrom("upload@system");
            emailHistory.setSystemDataEmailHistoryTo("system@local");
            emailHistory.setSystemDataEmailHistoryContent(new String(file.getBytes()));
            emailHistory.setSystemDataEmailHistorySize(file.getSize());
            emailHistory.setSystemDataEmailHistoryReceivedDate(LocalDateTime.now());
            emailHistory.setSystemDataEmailHistorySentDate(LocalDateTime.now());
            emailHistory.setSystemDataEmailHistoryIsRead(false);
            emailHistory.setSystemDataEmailHistoryIsStarred(false);
            emailHistory.setSystemDataEmailHistoryIsImportant(false);
            emailHistory.setSystemDataEmailHistoryIsDeleted(false);
            emailHistory.setSystemDataEmailHistoryFolder("UPLOAD");
            emailHistory.setSystemDataEmailHistoryType("UPLOAD");
            emailHistory.setSystemDataEmailHistoryStatus("ACTIVE");
            emailHistory.setSystemDataEmailHistoryAttachmentCount(0);

            return emailHistory;

        } catch (Exception e) {
            log.error("解析邮件文件失败", e);
            return null;
        }
    }
}