/*
 * Copyright (c) 2019 Of Him Code Technology Studio
 * Jpom is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 * 			http://license.coscl.org.cn/MulanPSL2
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT, MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */
package com.chua.starter.monitor.jpom.controller.script;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.keepbx.jpom.IJsonMessage;
import cn.keepbx.jpom.model.JsonMessage;
import com.alibaba.fastjson2.JSONObject;
import com.chua.report.client.starter.jpom.common.common.i18n.I18nMessageUtil;
import com.chua.report.client.starter.jpom.common.common.validator.ValidatorItem;
import com.chua.report.client.starter.jpom.common.common.validator.ValidatorRule;
import com.chua.report.client.starter.jpom.common.util.CommandUtil;
import com.chua.report.client.starter.jpom.common.util.FileUtils;
import com.chua.starter.monitor.jpom.common.BaseServerController;
import com.chua.starter.monitor.jpom.model.PageResultDto;
import com.chua.starter.monitor.jpom.model.script.ScriptExecuteLogModel;
import com.chua.starter.monitor.jpom.model.script.ScriptModel;
import com.chua.starter.monitor.jpom.permission.ClassFeature;
import com.chua.starter.monitor.jpom.permission.Feature;
import com.chua.starter.monitor.jpom.permission.MethodFeature;
import com.chua.starter.monitor.jpom.service.script.ScriptExecuteLogServer;
import com.chua.starter.monitor.jpom.service.script.ScriptServer;
import com.chua.starter.monitor.jpom.socket.ServerScriptProcessBuilder;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;

/**
 * @author bwcx_jzy
 * @since 2022/1/20
 */

@RestController
@RequestMapping(value = "/script_log")
@Feature(cls = ClassFeature.SCRIPT_LOG)
public class ScriptLogController extends BaseServerController {

    private final ScriptExecuteLogServer scriptExecuteLogServer;
    private final ScriptServer scriptServer;

    public ScriptLogController(ScriptExecuteLogServer scriptExecuteLogServer,
                               ScriptServer scriptServer) {
        this.scriptExecuteLogServer = scriptExecuteLogServer;
        this.scriptServer = scriptServer;
    }

    /**
     * get script log list
     *
     * @return json
     */
    @RequestMapping(value = "list", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @Feature(method = MethodFeature.LIST)
    public IJsonMessage<PageResultDto<ScriptExecuteLogModel>> scriptList(HttpServletRequest request) {
        PageResultDto<ScriptExecuteLogModel> pageResultDto = scriptExecuteLogServer.listPage(request);
        return JsonMessage.success("", pageResultDto);
    }

    /**
     * 删除日志
     *
     * @param id        id
     * @param executeId 执行ID
     * @return json
     */
    @RequestMapping(value = "del_log", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @Feature(method = MethodFeature.DEL)
    public IJsonMessage<Object> delLog(@ValidatorItem() String id,
                                       @ValidatorItem() String executeId,
                                       HttpServletRequest request) {
        ScriptModel item = null;
        try {
            item = scriptServer.getByKeyAndGlobal(id, request, "ignore");
        } catch (IllegalArgumentException | IllegalStateException e) {
            if (!StrUtil.equals("ignore", e.getMessage())) {
                throw e;
            }
        }
        File logFile = item == null ? ScriptModel.logFile(id, executeId) : item.logFile(executeId);
        boolean fastDel = CommandUtil.systemFastDel(logFile);
        Assert.state(!fastDel, I18nMessageUtil.get("i18n.delete_log_file_failure.bf0b"));
        scriptExecuteLogServer.delByKey(executeId);
        return JsonMessage.success(I18nMessageUtil.get("i18n.delete_success.0007"));
    }

    /**
     * 获取的日志
     *
     * @param id        id
     * @param executeId 执行ID
     * @param line      需要获取的行号
     * @return json
     */
    @RequestMapping(value = "log", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @Feature(method = MethodFeature.LIST)
    public IJsonMessage<JSONObject> getNowLog(@ValidatorItem() String id,
                                              @ValidatorItem() String executeId,
                                              @ValidatorItem(value = ValidatorRule.POSITIVE_INTEGER, msg = "i18n.line_number_error.c65d") int line,
                                              HttpServletRequest request) {
        ScriptModel item = scriptServer.getByKey(id, request);
        Assert.notNull(item, I18nMessageUtil.get("i18n.no_data_found.4ffb"));
        File logFile = item.logFile(executeId);
        Assert.state(FileUtil.isFile(logFile), I18nMessageUtil.get("i18n.log_file_error.473b"));
        JSONObject data = FileUtils.readLogFile(logFile, line);
        // 运行中
        data.put("run", ServerScriptProcessBuilder.isRun(executeId));
        return JsonMessage.success("", data);
    }
}
