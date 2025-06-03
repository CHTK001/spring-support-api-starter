/*
 * Copyright (c) 2019 Of Him Code Technology Studio
 * Jpom is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 * 			http://license.coscl.org.cn/MulanPSL2
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT, MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */
package com.chua.starter.monitor.jpom.func.assets.controller.docker;

import com.chua.report.client.starter.jpom.common.common.i18n.I18nMessageUtil;
import com.chua.starter.monitor.jpom.controller.docker.base.BaseDockerSwarmServiceController;
import com.chua.starter.monitor.jpom.func.assets.model.MachineDockerModel;
import com.chua.starter.monitor.jpom.func.assets.server.MachineDockerServer;
import com.chua.starter.monitor.jpom.permission.ClassFeature;
import com.chua.starter.monitor.jpom.permission.Feature;
import com.chua.starter.monitor.jpom.system.ServerConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @author bwcx_jzy
 * @since 2022/2/14
 */
@RestController
@Feature(cls = ClassFeature.SYSTEM_ASSETS_MACHINE_DOCKER)
@RequestMapping(value = "/system/assets/docker/swarm-service")
@Slf4j
public class AssetsDockerSwarmServiceController extends BaseDockerSwarmServiceController {

    private final MachineDockerServer machineDockerServer;

    public AssetsDockerSwarmServiceController(ServerConfig serverConfig,
                                              MachineDockerServer machineDockerServer) {
        super(serverConfig);
        this.machineDockerServer = machineDockerServer;
    }

    @Override
    protected Map<String, Object> toDockerParameter(String id) {
        MachineDockerModel machineDockerModel = machineDockerServer.getByKey(id, false);
        Assert.notNull(machineDockerModel, I18nMessageUtil.get("i18n.no_corresponding_docker_info.c47a"));
        if (machineDockerModel.isControlAvailable()) {
            // 管理节点
            return machineDockerServer.toParameter(machineDockerModel);
        }
        // 非管理节点
        MachineDockerModel bySwarmId = machineDockerServer.getMachineDockerBySwarmId(machineDockerModel.getSwarmId());
        return machineDockerServer.toParameter(bySwarmId);
    }
}
