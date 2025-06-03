/*
 * Copyright (c) 2019 Of Him Code Technology Studio
 * Jpom is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 * 			http://license.coscl.org.cn/MulanPSL2
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT, MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */
package com.chua.starter.monitor.jpom.func.assets.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Opt;
import cn.hutool.core.util.StrUtil;
import com.chua.report.client.starter.jpom.common.common.i18n.I18nMessageUtil;
import com.chua.report.client.starter.jpom.common.util.FileUtils;
import com.chua.starter.monitor.jpom.func.assets.model.MachineSshModel;
import com.chua.starter.monitor.jpom.permission.ClassFeature;
import com.chua.starter.monitor.jpom.permission.Feature;
import com.chua.starter.monitor.jpom.permission.SystemPermission;
import com.chua.starter.monitor.jpom.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.function.BiFunction;

/**
 * @author bwcx_jzy
 * @since 2023/2/27
 */
@RestController
@RequestMapping(value = "/system/assets/ssh-file")
@Feature(cls = ClassFeature.SSH_FILE)
@Slf4j
@SystemPermission
public class MachineSshFileController extends BaseSshFileController {
    @Override
    protected <T> T checkConfigPath(String id, BiFunction<MachineSshModel, ItemConfig, T> function) {
        MachineSshModel machineSshModel = machineSshServer.getByKey(id, false);
        Assert.notNull(machineSshModel, I18nMessageUtil.get("i18n.no_corresponding_ssh.aa68"));
        return function.apply(machineSshModel, new ItemConfig() {
            @Override
            public List<String> allowEditSuffix() {
                return StringUtil.jsonConvertArray(machineSshModel.getAllowEditSuffix(), String.class);
            }

            @Override
            public List<String> fileDirs() {
                return CollUtil.newArrayList(StrUtil.SLASH);
            }
        });
    }

    @Override
    protected <T> T checkConfigPathChildren(String id, String path, String children, BiFunction<MachineSshModel, ItemConfig, T> function) {
        FileUtils.checkSlip(path);
        Opt.ofBlankAble(children).ifPresent(FileUtils::checkSlip);
        //
        MachineSshModel machineSshModel = machineSshServer.getByKey(id, false);
        return function.apply(machineSshModel, new ItemConfig() {
            @Override
            public List<String> allowEditSuffix() {
                return StringUtil.jsonConvertArray(machineSshModel.getAllowEditSuffix(), String.class);
            }

            @Override
            public List<String> fileDirs() {
                return CollUtil.newArrayList(StrUtil.SLASH);
            }
        });
    }
}
