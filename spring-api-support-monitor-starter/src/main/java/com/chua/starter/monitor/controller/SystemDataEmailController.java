package com.chua.starter.monitor.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.entity.SystemDataEmailHistory;
import com.chua.starter.monitor.pojo.EmailFetch;
import com.chua.starter.monitor.pojo.response.EmailBackupResponse;
import com.chua.starter.monitor.pojo.response.EmailFetchResponse;
import com.chua.starter.monitor.service.SystemDataEmailHistoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 邮件数据管理控制器
 * <p>
 * 提供邮件拉取、云备份、云同步等功能。
 *
 * @author CH
 * @since 2025-01-20
 */
@Slf4j
@RestController
@RequestMapping("/system/data/email")
@Api(tags = "邮件数据管理")
@Tag(name = "邮件数据管理")
@RequiredArgsConstructor
public class SystemDataEmailController {

    private final SystemDataEmailHistoryService emailHistoryService;

    /**
     * 拉取邮件
     *
     * @param settingId  数据源配置ID
     * @param folderName 邮件文件夹名称，默认为INBOX
     * @param limit      拉取邮件数量限制，默认为50
     * @return 拉取结果
     */
    @PostMapping("/{settingId}/fetch")
    @ApiOperation("拉取邮件")
    public ReturnResult<EmailFetchResponse> fetchEmails(
            @PathVariable Integer settingId,
            EmailFetch emailFetch
    ) {

        return emailHistoryService.fetchEmails(settingId, emailFetch);
    }

    /**
     * 分页查询邮件历史
     *
     * @param settingId 数据源配置ID
     * @param current   当前页码
     * @param size      每页大小
     * @param isRead    是否已读（可选）
     * @param isStarred 是否收藏（可选）
     * @param label     标签（可选）
     * @return 分页结果
     */
    @GetMapping("/{settingId}/history")
    @ApiOperation("分页查询邮件历史")
    public ReturnResult<Page<SystemDataEmailHistory>> getEmailHistory(
            @PathVariable Integer settingId,
            @RequestParam(required = false, defaultValue = "1") long current,
            @RequestParam(required = false, defaultValue = "20") long size,
            @RequestParam(required = false) Boolean isRead,
            @RequestParam(required = false) Boolean isStarred,
            @RequestParam(required = false) String label) {

        return emailHistoryService.getEmailHistory(settingId, current, size, isRead, isStarred, label);
    }

    /**
     * 邮件云备份（前端上传邮件保存到数据库）
     *
     * @param settingId 数据源配置ID
     * @param file      邮件文件
     * @return 备份结果
     */
    @PostMapping(value = "/{settingId}/backup", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ApiOperation("邮件云备份")
    public ReturnResult<EmailBackupResponse> backupEmail(
            @PathVariable Integer settingId,
            @RequestParam("file") MultipartFile file) {

        return emailHistoryService.backupEmail(settingId, file);
    }

    /**
     * 同步邮件
     *
     * @param settingId  数据源配置ID
     * @param folderName 邮件文件夹名称，默认为INBOX
     * @return 同步结果
     */
    @PostMapping("/{settingId}/sync")
    @ApiOperation("同步邮件")
    public ReturnResult<List<SystemDataEmailHistory>> syncEmails(
            @PathVariable Integer settingId,
            @RequestParam(required = false, defaultValue = "INBOX") String folderName) {

        return emailHistoryService.syncEmails(settingId, folderName);
    }


}