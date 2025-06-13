/*
 * Copyright (c) 2019 Of Him Code Technology Studio
 * Jpom is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 * 			http://license.coscl.org.cn/MulanPSL2
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT, MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */
package com.chua.starter.monitor.jpom.func.openapi.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Opt;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.db.sql.Direction;
import cn.hutool.db.sql.Order;
import cn.hutool.extra.servlet.JakartaServletUtil;
import com.chua.report.client.starter.jpom.common.common.ServerOpenApi;
import com.chua.report.client.starter.jpom.common.common.i18n.I18nMessageUtil;
import com.chua.report.client.starter.jpom.common.util.FileUtils;
import com.chua.starter.monitor.jpom.common.interceptor.NotLogin;
import com.chua.starter.monitor.jpom.func.files.model.JpomFileStorageModel;
import com.chua.starter.monitor.jpom.func.files.service.FileStorageService;
import com.chua.starter.monitor.jpom.model.user.UserModel;
import com.chua.starter.monitor.jpom.service.user.TriggerTokenLogServer;
import com.chua.starter.monitor.jpom.system.ServerConfig;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author bwcx_jzy
 * @since 2023/3/17
 */
@RestController
@NotLogin
@Slf4j
public class FileStorageApiController extends BaseDownloadApiController {

    private final TriggerTokenLogServer triggerTokenLogServer;
    private final FileStorageService fileStorageService;
    private final ServerConfig serverConfig;

    public FileStorageApiController(TriggerTokenLogServer triggerTokenLogServer,
                                    FileStorageService fileStorageService,
                                    ServerConfig serverConfig) {
        this.triggerTokenLogServer = triggerTokenLogServer;
        this.fileStorageService = fileStorageService;
        this.serverConfig = serverConfig;
    }


    /**
     * 解析别名参数
     *
     * @param id      别名
     * @param token   token
     * @param sort    排序
     * @param request 请求
     * @return 数据
     */
    private JpomFileStorageModel queryByAliasCode(String id, String token, String sort, HttpServletRequest request) {
        // 先验证 token 和 id 是否都存在
        {
            JpomFileStorageModel data = new JpomFileStorageModel();
            data.setAliasCode(id);
            data.setTriggerToken(token);
            Assert.state(fileStorageService.exists(data), I18nMessageUtil.get("i18n.alias_or_token_error.d5c6"));
        }
        List<Order> orders = Opt.ofBlankAble(sort)
                .map(s -> StrUtil.splitTrim(s, StrUtil.COMMA))
                .map(strings ->
                        strings.stream()
                                .map(s -> {
                                    List<String> list = StrUtil.splitTrim(s, StrUtil.COLON);
                                    Order order = new Order();
                                    order.setField(CollUtil.getFirst(list));
                                    String s1 = CollUtil.get(list, 1);
                                    order.setDirection(ObjectUtil.defaultIfNull(Direction.fromString(s1), Direction.DESC));
                                    return order;
                                })
                                .collect(Collectors.toList()))
                .orElse(Collections.emptyList());
        Map<String, String> paramMap = JakartaServletUtil.getParamMap(request);
        JpomFileStorageModel where = new JpomFileStorageModel();
        for (Map.Entry<String, String> entry : paramMap.entrySet()) {
            String key = entry.getKey();
            if (StrUtil.startWith(key, "filter_")) {
                key = StrUtil.removePrefix(key, "filter_");
                ReflectUtil.setFieldValue(where, key, entry.getValue());
            }
        }
        where.setAliasCode(id);
        List<JpomFileStorageModel> jpomFileStorageModels = fileStorageService.queryList(where, 1, orders.toArray(new Order[]{}));
        return CollUtil.getFirst(jpomFileStorageModels);
    }

    @GetMapping(value = ServerOpenApi.FILE_STORAGE_DOWNLOAD, produces = MediaType.APPLICATION_JSON_VALUE)
    public void download(@PathVariable String id,
                         @PathVariable String token,
                         String sort,
                         HttpServletRequest request,
                         HttpServletResponse response) throws IOException {
        JpomFileStorageModel storageModel = fileStorageService.getByKey(id);
        if (storageModel == null) {
            // 根据别名查询
            storageModel = this.queryByAliasCode(id, token, sort, request);
            Assert.notNull(storageModel, I18nMessageUtil.get("i18n.no_data_found.4ffb"));
        } else {
            Assert.state(StrUtil.equals(token, storageModel.getTriggerToken()), I18nMessageUtil.get("i18n.invalid_or_expired_token.bc43"));
        }
        //
        UserModel userModel = triggerTokenLogServer.getUserByToken(token, fileStorageService.typeName());
        //
        Assert.notNull(userModel, I18nMessageUtil.get("i18n.token_invalid_or_expired.cb96"));
        //
        File storageSavePath = serverConfig.fileStorageSavePath();
        File fileStorageFile = FileUtil.file(storageSavePath, storageModel.getPath());
        // 需要考虑文件名中存在非法字符
        String name = FileUtils.safeFileName(storageModel.getName(), storageModel.getExtName(), fileStorageFile.getName());
        //    解析断点续传相关信息
        long fileSize = FileUtil.size(fileStorageFile);
        long[] resolveRange = this.resolveRange(request, fileSize, storageModel.getId(), storageModel.getName(), response);
        this.download(fileStorageFile, fileSize, name, resolveRange, response);
    }
}
