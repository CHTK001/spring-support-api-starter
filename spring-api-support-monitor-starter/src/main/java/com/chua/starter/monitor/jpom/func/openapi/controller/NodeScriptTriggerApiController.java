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

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.servlet.JakartaServletUtil;
import cn.keepbx.jpom.IJsonMessage;
import cn.keepbx.jpom.model.JsonMessage;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.chua.report.client.starter.jpom.common.BaseJpomController;
import com.chua.report.client.starter.jpom.common.ServerOpenApi;
import com.chua.report.client.starter.jpom.common.i18n.I18nMessageUtil;
import com.chua.starter.monitor.jpom.common.forward.NodeForward;
import com.chua.starter.monitor.jpom.common.forward.NodeUrl;
import com.chua.starter.monitor.jpom.common.interceptor.NotLogin;
import com.chua.starter.monitor.jpom.model.data.NodeModel;
import com.chua.starter.monitor.jpom.model.node.NodeScriptCacheModel;
import com.chua.starter.monitor.jpom.model.user.UserModel;
import com.chua.starter.monitor.jpom.service.node.NodeService;
import com.chua.starter.monitor.jpom.service.node.script.NodeScriptServer;
import com.chua.starter.monitor.jpom.service.user.TriggerTokenLogServer;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 节点脚本触发器
 *
 * @author bwcx_jzy
 * @since 2022/7/25
 */
@RestController
@NotLogin
@Slf4j
public class NodeScriptTriggerApiController extends BaseJpomController {

    private final NodeScriptServer nodeScriptServer;
    private final NodeService nodeService;
    private final TriggerTokenLogServer triggerTokenLogServer;

    public NodeScriptTriggerApiController(NodeScriptServer nodeScriptServer,
                                          NodeService nodeService,
                                          TriggerTokenLogServer triggerTokenLogServer) {
        this.nodeScriptServer = nodeScriptServer;
        this.nodeService = nodeService;
        this.triggerTokenLogServer = triggerTokenLogServer;
    }

    /**
     * 执行脚本
     *
     * @param id    构建ID
     * @param token 构建的token
     * @return json
     */
    @RequestMapping(value = ServerOpenApi.NODE_SCRIPT_TRIGGER_URL, produces = MediaType.APPLICATION_JSON_VALUE)
    public IJsonMessage<JSONObject> trigger2(@PathVariable String id, @PathVariable String token, HttpServletRequest request) {
        NodeScriptCacheModel item = nodeScriptServer.getByKey(id);
        Assert.notNull(item, I18nMessageUtil.get("i18n.no_data_found.4ffb"));
        Assert.state(StrUtil.equals(token, item.getTriggerToken()), I18nMessageUtil.get("i18n.trigger_token_error_or_expired.8976"));
        //
        UserModel userModel = triggerTokenLogServer.getUserByToken(token, nodeScriptServer.typeName());
        //
        Assert.notNull(userModel, I18nMessageUtil.get("i18n.trigger_token_error_or_expired_with_code.393b"));

        try {
            NodeModel nodeModel = nodeService.getByKey(item.getNodeId());
            JSONObject reqData = new JSONObject();
            reqData.put("id", item.getScriptId());
            reqData.put("params", JSONObject.toJSONString(JakartaServletUtil.getParamMap(request)));
            JsonMessage<String> jsonMessage = NodeForward.request(nodeModel, NodeUrl.SCRIPT_EXEC, reqData);
            //
            JSONObject jsonObject = new JSONObject();
            if (jsonMessage.getCode() == 200) {
                jsonObject.put("logId", jsonMessage.getData());
            } else {
                jsonObject.put("msg", jsonMessage.getMsg());
            }
            return JsonMessage.success(I18nMessageUtil.get("i18n.start_execution.00d7"), jsonObject);
        } catch (Exception e) {
            log.error(I18nMessageUtil.get("i18n.trigger_auto_execute_server_script_exception.8e84"), e);
            return new JsonMessage<>(500, I18nMessageUtil.get("i18n.general_execution_exception.62e9") + e.getMessage());
        }
    }


    /**
     * 构建触发器
     * <p>
     * 参数 <code>[
     * {
     * "id":"1",
     * "token":"a"
     * }
     * ]</code>
     * <p>
     * 响应 <code>[
     * {
     * "id":"1",
     * "token":"a",
     * "logId":"1",
     * "msg":"没有对应数据",
     * }
     * ]</code>
     *
     * @return json
     */
    @PostMapping(value = ServerOpenApi.NODE_SCRIPT_TRIGGER_BATCH, produces = MediaType.APPLICATION_JSON_VALUE)
    public IJsonMessage<List<Object>> triggerBatch(HttpServletRequest request) {
        try {
            String body = JakartaServletUtil.getBody(request);
            JSONArray jsonArray = JSONArray.parseArray(body);
            List<Object> collect = jsonArray.stream().peek(o -> {
                JSONObject jsonObject = (JSONObject) o;
                String id = jsonObject.getString("id");
                String token = jsonObject.getString("token");
                NodeScriptCacheModel item = nodeScriptServer.getByKey(id);
                if (item == null) {
                    String string = I18nMessageUtil.get("i18n.no_data_found.4ffb");
                    jsonObject.put("msg", string);
                    return;
                }
                UserModel userModel = triggerTokenLogServer.getUserByToken(token, nodeScriptServer.typeName());
                if (userModel == null) {
                    String string = I18nMessageUtil.get("i18n.user_not_exist_trigger_invalid.f375");
                    jsonObject.put("msg", string);
                    return;
                }
                //
                if (!StrUtil.equals(token, item.getTriggerToken())) {
                    String value = I18nMessageUtil.get("i18n.trigger_token_error_or_expired.8976");
                    jsonObject.put("msg", value);
                    return;
                }
                NodeModel nodeModel = nodeService.getByKey(item.getNodeId());
                JSONObject reqData = new JSONObject();
                reqData.put("id", item.getScriptId());
                JsonMessage<String> jsonMessage = NodeForward.request(nodeModel, NodeUrl.SCRIPT_EXEC, reqData);
                //
                if (jsonMessage.getCode() == 200) {
                    jsonObject.put("logId", jsonMessage.getData());
                } else {
                    jsonObject.put("msg", jsonMessage.getMsg());
                }
            }).collect(Collectors.toList());
            return JsonMessage.success(I18nMessageUtil.get("i18n.trigger_success.f9d1"), collect);
        } catch (Exception e) {
            log.error(I18nMessageUtil.get("i18n.batch_trigger_script_exception.8fb4"), e);
            return new JsonMessage<>(500, I18nMessageUtil.get("i18n.trigger_exception.d624") + e.getMessage());
        }
    }
}
