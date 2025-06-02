/*
 * Copyright (c) 2019 Of Him Code Technology Studio
 * Jpom is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 * 			http://license.coscl.org.cn/MulanPSL2
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT, MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */
package com.chua.starter.monitor.jpom.func;

import cn.keepbx.jpom.IJsonMessage;
import cn.keepbx.jpom.model.JsonMessage;
import com.chua.starter.monitor.jpom.common.BaseServerController;
import com.chua.starter.monitor.jpom.model.BaseGroupNameModel;
import com.chua.starter.monitor.jpom.permission.Feature;
import com.chua.starter.monitor.jpom.permission.MethodFeature;
import com.chua.starter.monitor.jpom.service.h2db.BaseDbService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Collection;

/**
 * @author bwcx_jzy
 * @since 2023/2/25
 */
public abstract class BaseGroupNameController extends BaseServerController {

    protected final BaseDbService<? extends BaseGroupNameModel> dbService;

    protected BaseGroupNameController(BaseDbService<? extends BaseGroupNameModel> dbService) {
        this.dbService = dbService;
    }


    @GetMapping(value = "list-group", produces = MediaType.APPLICATION_JSON_VALUE)
    @Feature(method = MethodFeature.LIST)
    public IJsonMessage<Collection<String>> listGroup() {
        Collection<String> list = dbService.listGroupName();
        return JsonMessage.success("", list);
    }
}
