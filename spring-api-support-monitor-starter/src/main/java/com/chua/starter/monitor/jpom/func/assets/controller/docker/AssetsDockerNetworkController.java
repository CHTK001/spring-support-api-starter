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
import com.chua.starter.monitor.jpom.controller.docker.base.BaseDockerNetworkController;
import com.chua.starter.monitor.jpom.func.assets.model.MachineDockerModel;
import com.chua.starter.monitor.jpom.func.assets.server.MachineDockerServer;
import com.chua.starter.monitor.jpom.permission.ClassFeature;
import com.chua.starter.monitor.jpom.permission.Feature;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @author bwcx_jzy
 * @since 2022/2/15
 */
@RestController
@Feature(cls = ClassFeature.SYSTEM_ASSETS_MACHINE_DOCKER)
@RequestMapping(value = "/system/assets/docker/networks")
public class AssetsDockerNetworkController extends BaseDockerNetworkController {

    private final MachineDockerServer machineDockerServer;

    public AssetsDockerNetworkController(MachineDockerServer machineDockerServer) {
        this.machineDockerServer = machineDockerServer;
    }

    @Override
    protected Map<String, Object> toDockerParameter(String id) {
        MachineDockerModel machineDockerModel = machineDockerServer.getByKey(id);
        Assert.notNull(machineDockerModel, I18nMessageUtil.get("i18n.no_corresponding_docker_asset.6f06"));
        return machineDockerServer.toParameter(machineDockerModel);
    }
}
