/*
 * Copyright (c) 2019 Of Him Code Technology Studio
 * Jpom is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 * 			http://license.coscl.org.cn/MulanPSL2
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT, MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */
package com.chua.starter.monitor.jpom.func.files.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.SystemClock;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.StreamProgress;
import cn.hutool.core.io.unit.DataSize;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.db.Entity;
import cn.hutool.http.HttpUtil;
import cn.keepbx.jpom.event.ISystemTask;
import com.chua.report.client.starter.jpom.common.common.i18n.I18nMessageUtil;
import com.chua.report.client.starter.jpom.common.common.i18n.I18nThreadUtil;
import com.chua.starter.monitor.jpom.JpomApplication;
import com.chua.starter.monitor.jpom.common.ServerConst;
import com.chua.starter.monitor.jpom.configuration.BuildExtConfig;
import com.chua.starter.monitor.jpom.func.files.model.JpomFileStorageModel;
import com.chua.starter.monitor.jpom.service.IStatusRecover;
import com.chua.starter.monitor.jpom.service.ITriggerToken;
import com.chua.starter.monitor.jpom.service.h2db.BaseGlobalOrWorkspaceService;
import com.chua.starter.monitor.jpom.system.ServerConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author bwcx_jzy
 * @since 2023/3/16
 */
@Service
@Slf4j
public class FileStorageService extends BaseGlobalOrWorkspaceService<JpomFileStorageModel> implements ISystemTask, IStatusRecover, ITriggerToken {

    private final ServerConfig serverConfig;
    private final JpomApplication configBean;
    private final BuildExtConfig buildExtConfig;

    public FileStorageService(ServerConfig serverConfig,
                              JpomApplication configBean,
                              BuildExtConfig buildExtConfig) {
        this.serverConfig = serverConfig;
        this.configBean = configBean;
        this.buildExtConfig = buildExtConfig;
    }

    /**
     * 远程下载
     *
     * @param url         url
     * @param workspaceId 工作空间id
     * @param description 描述
     * @param global      是否为全局共享
     * @param keepDay     保留天数
     */
    public void download(String url, Boolean global, String workspaceId, Integer keepDay, String description, String aliasCode) {
        JpomFileStorageModel jpomFileStorageModel = new JpomFileStorageModel();
        // 临时使用 uuid，代替
        String uuid = IdUtil.fastSimpleUUID();
        long startTime = SystemClock.now();
        {
            jpomFileStorageModel.setId(uuid);
            jpomFileStorageModel.setName(I18nMessageUtil.get("i18n.file_downloading.7a8f"));
            String empty = StrUtil.emptyToDefault(description, StrUtil.EMPTY);

            jpomFileStorageModel.setDescription(StrUtil.format(I18nMessageUtil.get("i18n.remote_download_url.011f"), empty, url));
            String extName = "download";
            String path = StrUtil.format("/{}/{}.{}", DateTime.now().toString(DatePattern.PURE_DATE_FORMAT), uuid, extName);
            jpomFileStorageModel.setExtName("download");
            jpomFileStorageModel.setPath(path);
            jpomFileStorageModel.setAliasCode(aliasCode);
            jpomFileStorageModel.setSize(0L);
            jpomFileStorageModel.setStatus(0);
            jpomFileStorageModel.setSource(2);
            if (global != null && global) {
                jpomFileStorageModel.setWorkspaceId(ServerConst.WORKSPACE_GLOBAL);
            } else {
                jpomFileStorageModel.setWorkspaceId(workspaceId);
            }
            jpomFileStorageModel.validUntil(keepDay, null);
            this.insert(jpomFileStorageModel);
        }
        // 异步下载
        I18nThreadUtil.execute(() -> {
            try {
                File tempPath = configBean.getTempPath();
                File file = FileUtil.file(tempPath, "file-storage-download", uuid);
                FileUtil.mkdir(file);
                StreamProgress streamProgress = this.createStreamProgress(uuid);
                File fileFromUrl = HttpUtil.downloadFileFromUrl(url, file, -1, streamProgress);
                String md5 = SecureUtil.md5(fileFromUrl);
                JpomFileStorageModel storageModel = this.getByKey(md5);
                if (storageModel != null) {
                    this.updateError(uuid, I18nMessageUtil.get("i18n.file_already_exists.983d"));
                    FileUtil.del(fileFromUrl);
                    return;
                }
                String extName = FileUtil.extName(fileFromUrl);
                // 避免跨天数据
                String path = StrUtil.format("/{}/{}.{}", new DateTime(startTime).toString(DatePattern.PURE_DATE_FORMAT), md5, extName);
                File storageSavePath = serverConfig.fileStorageSavePath();
                File fileStorageFile = FileUtil.file(storageSavePath, path);
                FileUtil.mkParentDirs(fileStorageFile);
                FileUtil.move(fileFromUrl, fileStorageFile, true);
                //
                JpomFileStorageModel update = new JpomFileStorageModel();
                // 需要将 id 更新为真实 id
                update.setId(md5);
                update.setName(fileFromUrl.getName());
                update.setExtName(extName);
                update.setModifyTimeMillis(SystemClock.now());
                update.setPath(path);
                update.setStatus(1);
                update.setSize(FileUtil.size(fileStorageFile));
                Entity updateEntity = this.dataBeanToEntity(update);
                Entity id = Entity.create().set("id", uuid);
                this.update(updateEntity, id);
            } catch (Exception e) {
                log.error(I18nMessageUtil.get("i18n.download_failed_generic.be4f"), e);
                this.updateError(uuid, e.getMessage());
            }
        });
    }

    private StreamProgress createStreamProgress(String uuid) {
        int logReduceProgressRatio = buildExtConfig.getLogReduceProgressRatio();
        Set<Integer> progressRangeList = ConcurrentHashMap.newKeySet((int) Math.floor((float) 100 / logReduceProgressRatio));
        long bytes = DataSize.ofMegabytes(1).toBytes();
        return new StreamProgress() {
            @Override
            public void start() {

            }

            @Override
            public void progress(long total, long progressSize) {
                if (total > 0) {
                    double progressPercentage = Math.floor(((float) progressSize / total) * 100);
                    String percent = NumberUtil.formatPercent((float) progressSize / total, 0);
                    int progressRange = (int) Math.floor(progressPercentage / logReduceProgressRatio);
                    // 存在文件总大小
                    if (progressRangeList.add(progressRange)) {
                        //  total, progressSize
                        updateProgress(uuid, percent, total, progressSize);
                    }
                } else {
                    // 不存在文件总大小
                    if (progressSize % bytes == 0) {
                        updateProgress(uuid, null, total, progressSize);
                    }
                }
            }

            @Override
            public void finish() {

            }
        };
    }

    private void updateProgress(String id, String desc, long total, long progressSize) {
        JpomFileStorageModel jpomFileStorageModel = new JpomFileStorageModel();
        jpomFileStorageModel.setId(id);
        String fileSize = FileUtil.readableFileSize(progressSize);
        desc = StrUtil.emptyToDefault(desc, fileSize);
        jpomFileStorageModel.setName(I18nMessageUtil.get("i18n.file_downloading_status.c995") + desc);
        jpomFileStorageModel.setStatus(0);
        jpomFileStorageModel.setSize(progressSize);

        jpomFileStorageModel.setProgressDesc(StrUtil.format(I18nMessageUtil.get("i18n.download_progress.898a"), desc, FileUtil.readableFileSize(total), fileSize));
        this.updateById(jpomFileStorageModel);
    }

    /**
     * 更新进度
     *
     * @param id    数据id
     * @param error 错误信息
     */
    private void updateError(String id, String error) {
        JpomFileStorageModel jpomFileStorageModel = new JpomFileStorageModel();
        jpomFileStorageModel.setId(id);
        jpomFileStorageModel.setName(I18nMessageUtil.get("i18n.file_download_failed.7983") + StrUtil.maxLength(error, 200));
        jpomFileStorageModel.setStatus(2);
        jpomFileStorageModel.setProgressDesc(error);
        this.updateById(jpomFileStorageModel);
    }

    /**
     * 添加文件
     *
     * @param source      文件来源
     * @param file        要文件的文件
     * @param description 描述
     * @param workspaceId 工作空间id
     * @param aliasCode   别名码
     * @return 返回成功的文件id
     */
    public String addFile(File file, int source, String workspaceId, String description, String aliasCode) {
        return addFile(file, source, workspaceId, description, aliasCode, null);
    }

    /**
     * 添加文件
     *
     * @param source      文件来源
     * @param file        要文件的文件
     * @param description 描述
     * @param workspaceId 工作空间id
     * @param aliasCode   别名码
     * @return 返回成功的文件id
     */
    public String addFile(File file, int source, String workspaceId, String description, String aliasCode, Integer keepDay) {
        String md5 = SecureUtil.md5(file);
        File storageSavePath = serverConfig.fileStorageSavePath();
        String extName = FileUtil.extName(file);
        String path = StrUtil.format("/{}/{}.{}", DateTime.now().toString(DatePattern.PURE_DATE_FORMAT), md5, extName);
        JpomFileStorageModel storageModel = this.getByKey(md5);
        if (storageModel != null) {
            return null;
        }
        // 保存
        JpomFileStorageModel jpomFileStorageModel = new JpomFileStorageModel();
        jpomFileStorageModel.setId(md5);
        jpomFileStorageModel.setAliasCode(aliasCode);
        jpomFileStorageModel.setName(file.getName());
        jpomFileStorageModel.setDescription(description);
        jpomFileStorageModel.setExtName(extName);
        jpomFileStorageModel.setPath(path);
        jpomFileStorageModel.setSize(FileUtil.size(file));
        jpomFileStorageModel.setSource(source);
        jpomFileStorageModel.setWorkspaceId(workspaceId);
        jpomFileStorageModel.validUntil(keepDay, null);
        this.insert(jpomFileStorageModel);
        //
        File fileStorageFile = FileUtil.file(storageSavePath, path);
        FileUtil.mkParentDirs(fileStorageFile);
        FileUtil.copyFile(file, fileStorageFile, StandardCopyOption.REPLACE_EXISTING);
        return md5;
    }

    @Override
    public void executeTask() {
        // 定时删除文件
        Entity entity = Entity.create();
        entity.set("validUntil", " < " + SystemClock.now());
        List<JpomFileStorageModel> storageModels = this.listByEntity(entity);
        if (CollUtil.isEmpty(storageModels)) {
            return;
        }
        File storageSavePath = serverConfig.fileStorageSavePath();
        for (JpomFileStorageModel storageModel : storageModels) {
            log.info(I18nMessageUtil.get("i18n.start_deleting_files.210c"), storageModel.getName(), storageModel.getPath());
            File fileStorageFile = FileUtil.file(storageSavePath, storageModel.getPath());
            FileUtil.del(fileStorageFile);
            this.delByKey(storageModel.getId());
        }
    }

    @Override
    public int statusRecover() {
        JpomFileStorageModel update = new JpomFileStorageModel();
        update.setName(I18nMessageUtil.get("i18n.system_restart_cancel_download.444e"));
        update.setModifyTimeMillis(SystemClock.now());
        update.setStatus(2);
        Entity updateEntity = this.dataBeanToEntity(update);
        //
        Entity where = Entity.create()
                .set("source", 2)
                .set("status", 0);
        return this.update(updateEntity, where);
    }

    @Override
    public String typeName() {
        return getTableName();
    }
}
