/*
 * Copyright (c) 2019 Of Him Code Technology Studio
 * Jpom is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 * 			http://license.coscl.org.cn/MulanPSL2
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT, MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */
package com.chua.starter.monitor.jpom.socket.handler;

import cn.hutool.core.util.ArrayUtil;
import com.alibaba.fastjson2.JSONObject;
import com.chua.report.client.starter.jpom.common.socket.ConsoleCommandOp;
import com.chua.starter.monitor.jpom.common.forward.NodeUrl;
import com.chua.starter.monitor.jpom.permission.ClassFeature;
import com.chua.starter.monitor.jpom.permission.Feature;
import com.chua.starter.monitor.jpom.permission.MethodFeature;
import com.chua.starter.monitor.jpom.socket.BaseProxyHandler;
import com.chua.starter.monitor.jpom.transport.IProxyWebSocket;

import java.io.IOException;
import java.util.Map;

/**
 * 控制台消息处理器
 *
 * @author bwcx_jzy
 * @since 2019/4/19
 */
@Feature(cls = ClassFeature.PROJECT_CONSOLE, method = MethodFeature.EXECUTE)
public class ConsoleHandler extends BaseProxyHandler {

    public ConsoleHandler() {
        super(NodeUrl.TopSocket);
    }

    @Override
    protected Object[] getParameters(Map<String, Object> attributes) {
        return new Object[]{"projectId", attributes.get("projectId")};
    }

    @Override
    protected String handleTextMessage(Map<String, Object> attributes,
                                       IProxyWebSocket proxySession,
                                       JSONObject json,
                                       ConsoleCommandOp consoleCommandOp) throws IOException {
        //ProjectInfoCacheModel dataItem = (ProjectInfoCacheModel) attributes.get("dataItem");
//		UserModel userModel = (UserModel) attributes.get("userInfo");
//		if (RunMode.Dsl.name().equals(dataItem.getRunMode()) && userModel.isDemoUser()) {
//			if (consoleCommandOp == ConsoleCommandOp.stop || consoleCommandOp == ConsoleCommandOp.start || consoleCommandOp == ConsoleCommandOp.restart) {
//				return PermissionInterceptor.DEMO_TIP;
//			}
//		}
        ConsoleCommandOp[] commandOps = new ConsoleCommandOp[]{ConsoleCommandOp.heart, ConsoleCommandOp.showlog};
        if (!ArrayUtil.contains(commandOps, consoleCommandOp)) {
            super.logOpt(this.getClass(), attributes, json);
        }
        proxySession.send(json.toString());
        return null;
    }
}
