/*
 * Copyright (c) 2019 Of Him Code Technology Studio
 * Jpom is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 * 			http://license.coscl.org.cn/MulanPSL2
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT, MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */
package com.chua.starter.monitor.jpom.controller.monitor;

import cn.keepbx.jpom.IJsonMessage;
import cn.keepbx.jpom.model.JsonMessage;
import com.chua.report.client.starter.jpom.common.common.i18n.I18nMessageUtil;
import com.chua.starter.monitor.jpom.common.BaseServerController;
import com.chua.starter.monitor.jpom.model.PageResultDto;
import com.chua.starter.monitor.jpom.model.log.MonitorNotifyLog;
import com.chua.starter.monitor.jpom.permission.ClassFeature;
import com.chua.starter.monitor.jpom.permission.Feature;
import com.chua.starter.monitor.jpom.permission.MethodFeature;
import com.chua.starter.monitor.jpom.service.dblog.DbMonitorNotifyLogService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * 监控列表
 *
 * @author bwcx_jzy
 * @since 2019/7/16
 */
@RestController
@RequestMapping(value = "/monitor")
@Feature(cls = ClassFeature.MONITOR_LOG)
public class MonitorLogController extends BaseServerController {

    private final DbMonitorNotifyLogService dbMonitorNotifyLogService;

    public MonitorLogController(DbMonitorNotifyLogService dbMonitorNotifyLogService) {
        this.dbMonitorNotifyLogService = dbMonitorNotifyLogService;
    }

    /**
     * 展示用户列表
     *
     * @return json
     */
    @RequestMapping(value = "list_data.json", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @Feature(method = MethodFeature.LIST)
    public IJsonMessage<PageResultDto<MonitorNotifyLog>> listData(HttpServletRequest request) {
        PageResultDto<MonitorNotifyLog> pageResult = dbMonitorNotifyLogService.listPage(request);
        return JsonMessage.success(I18nMessageUtil.get("i18n.get_success.fb55"), pageResult);
    }
}
