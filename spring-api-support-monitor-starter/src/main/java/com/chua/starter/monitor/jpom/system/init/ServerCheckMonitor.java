/*
 * Copyright (c) 2019 Of Him Code Technology Studio
 * Jpom is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 * 			http://license.coscl.org.cn/MulanPSL2
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT, MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */
package com.chua.starter.monitor.jpom.system.init;

import cn.keepbx.jpom.event.ISystemTask;
import cn.keepbx.jpom.model.JsonMessage;
import com.alibaba.fastjson2.JSONObject;
import com.chua.report.client.starter.jpom.common.ILoadEvent;
import com.chua.report.client.starter.jpom.common.RemoteVersion;
import com.chua.report.client.starter.jpom.common.i18n.I18nMessageUtil;
import com.chua.report.client.starter.jpom.cron.CronUtils;
import com.chua.report.client.starter.jpom.script.BaseRunScript;
import com.chua.starter.monitor.jpom.common.forward.NodeForward;
import com.chua.starter.monitor.jpom.common.forward.NodeUrl;
import com.chua.starter.monitor.jpom.func.assets.AssetsExecutorPoolService;
import com.chua.starter.monitor.jpom.model.data.NodeModel;
import com.chua.starter.monitor.jpom.service.node.NodeService;
import com.chua.starter.monitor.jpom.service.node.script.NodeScriptExecuteLogServer;
import com.chua.starter.monitor.jpom.service.node.script.NodeScriptServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;

import java.util.Collection;
import java.util.List;

/**
 * 检查监控数据状态
 *
 * @author bwcx_jzy
 * @since 2019/7/14
 */
@Configuration
@Slf4j
public class ServerCheckMonitor implements ILoadEvent, ISystemTask {

    private final NodeService nodeService;
    private final NodeScriptServer nodeScriptServer;
    private final NodeScriptExecuteLogServer nodeScriptExecuteLogServer;
    private final AssetsExecutorPoolService assetsExecutorPoolService;

    public ServerCheckMonitor(NodeService nodeService,
                              NodeScriptServer nodeScriptServer,
                              NodeScriptExecuteLogServer nodeScriptExecuteLogServer,
                              AssetsExecutorPoolService assetsExecutorPoolService) {
        this.nodeService = nodeService;
        this.nodeScriptServer = nodeScriptServer;
        this.nodeScriptExecuteLogServer = nodeScriptExecuteLogServer;
        this.assetsExecutorPoolService = assetsExecutorPoolService;
    }

    /**
     * 同步 节点的脚本模版日志
     *
     * @param nodeModel 节点
     */
    private void pullScriptLogItem(NodeModel nodeModel) {
        try {
            //NodeScriptExecuteLogServer nodeScriptExecuteLogServer = SpringUtil.getBean(NodeScriptExecuteLogServer.class);
            Collection<String> strings = nodeScriptExecuteLogServer.syncExecuteNodeInc(nodeModel);
            if (strings == null) {
                return;
            }
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("ids", strings);
            JsonMessage<Object> jsonMessage = NodeForward.requestBody(nodeModel, NodeUrl.SCRIPT_DEL_EXEC_LOG, jsonObject);
            if (!jsonMessage.success()) {
                log.error(I18nMessageUtil.get("i18n.delete_script_template_execution_error.8bc5"), jsonMessage);
            }
        } catch (Exception e) {
            log.error(I18nMessageUtil.get("i18n.synchronization_script_exception.9c70"), e);
        }
    }


    @Override
    public void afterPropertiesSet(ApplicationContext applicationContext) throws Exception {
        // 拉取 脚本模版日志
        CronUtils.upsert("pull_script_log", "0 0/1 * * * ?", () -> {
            //NodeService nodeService = SpringUtil.getBean(NodeService.class);
            //NodeScriptServer nodeScriptServer = SpringUtil.getBean(NodeScriptServer.class);
            List<String> nodeIds = nodeScriptServer.hasScriptNode();
            if (nodeIds == null) {
                return;
            }
            for (String nodeId : nodeIds) {
                NodeModel nodeModel = nodeService.getByKey(nodeId);
                if (nodeModel == null) {
                    continue;
                }
                assetsExecutorPoolService.execute(() -> this.pullScriptLogItem(nodeModel));
            }
        });
    }

    @Override
    public void executeTask() {
        // 尝试获取版本更新信息
        RemoteVersion.loadRemoteInfo();
        // 清空脚本缓存
        BaseRunScript.clearRunScript();
    }
}
