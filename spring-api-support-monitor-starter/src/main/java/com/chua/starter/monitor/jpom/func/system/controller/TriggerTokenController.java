/*
 * Copyright (c) 2019 Of Him Code Technology Studio
 * Jpom is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 * 			http://license.coscl.org.cn/MulanPSL2
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT, MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */
package com.chua.starter.monitor.jpom.func.system.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.keepbx.jpom.IJsonMessage;
import cn.keepbx.jpom.model.BaseIdModel;
import cn.keepbx.jpom.model.JsonMessage;
import com.alibaba.fastjson2.JSONObject;
import com.chua.report.client.starter.jpom.common.i18n.I18nMessageUtil;
import com.chua.starter.monitor.jpom.model.PageResultDto;
import com.chua.starter.monitor.jpom.model.user.TriggerTokenLogBean;
import com.chua.starter.monitor.jpom.permission.ClassFeature;
import com.chua.starter.monitor.jpom.permission.Feature;
import com.chua.starter.monitor.jpom.permission.MethodFeature;
import com.chua.starter.monitor.jpom.permission.SystemPermission;
import com.chua.starter.monitor.jpom.service.ITriggerToken;
import com.chua.starter.monitor.jpom.service.user.TriggerTokenLogServer;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author bwcx_jzy
 * @since 24/1/17 017
 */
@RestController
@RequestMapping(value = "system/trigger")
@Feature(cls = ClassFeature.SYSTEM_CACHE)
@SystemPermission
public class TriggerTokenController {

    private final TriggerTokenLogServer triggerTokenLogServer;

    public TriggerTokenController(TriggerTokenLogServer triggerTokenLogServer) {
        this.triggerTokenLogServer = triggerTokenLogServer;
    }

    @GetMapping(value = "all-type", produces = MediaType.APPLICATION_JSON_VALUE)
    @Feature(method = MethodFeature.LIST)
    public IJsonMessage<List<JSONObject>> allType() {
        List<JSONObject> jsonObjects = triggerTokenLogServer.allType();
        return JsonMessage.success("", jsonObjects);
    }

    @GetMapping(value = "delete", produces = MediaType.APPLICATION_JSON_VALUE)
    @Feature(method = MethodFeature.LIST)
    public IJsonMessage<String> delete(String id) {
        triggerTokenLogServer.delete(id);
        return JsonMessage.success(I18nMessageUtil.get("i18n.delete_success.0007"));
    }

    /**
     * 分页列表
     *
     * @return json
     */
    @PostMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    @Feature(method = MethodFeature.LIST)
    public IJsonMessage<PageResultDto<TriggerTokenLogBean>> list(HttpServletRequest request) {
        PageResultDto<TriggerTokenLogBean> listPage = triggerTokenLogServer.listPage(request);
        listPage.each(triggerTokenLogBean -> {
            String type = triggerTokenLogBean.getType();
            ITriggerToken byType = triggerTokenLogServer.getByType(type);
            if (byType == null) {
                triggerTokenLogBean.setDataName(I18nMessageUtil.get("i18n.type_not_exist_error.09de") + type);
            } else {
                BaseIdModel byKey = byType.getByKey(triggerTokenLogBean.getDataId());
                if (byKey == null) {
                    triggerTokenLogBean.setDataName(I18nMessageUtil.get("i18n.associated_data_lost_error.becb"));
                } else {
                    Object name = BeanUtil.getProperty(byKey, "name");
                    if (name == null) {
                        triggerTokenLogBean.setDataName(I18nMessageUtil.get("i18n.associated_data_name_not_exist_error.583e"));
                    } else {
                        triggerTokenLogBean.setDataName(name.toString());
                    }
                }
            }
        });
        return JsonMessage.success("", listPage);
    }
}
