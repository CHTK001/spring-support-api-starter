/*
 * Copyright (c) 2019 Of Him Code Technology Studio
 * Jpom is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 * 			http://license.coscl.org.cn/MulanPSL2
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT, MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */
package com.chua.starter.monitor.jpom.common;

import cn.hutool.cache.Cache;
import cn.hutool.cache.CacheUtil;
import cn.hutool.core.util.StrUtil;
import cn.keepbx.jpom.model.JsonMessage;
import com.chua.report.client.starter.jpom.common.common.BaseJpomController;
import com.chua.report.client.starter.jpom.common.common.i18n.I18nMessageUtil;
import com.chua.starter.monitor.jpom.common.forward.NodeForward;
import com.chua.starter.monitor.jpom.common.forward.NodeUrl;
import com.chua.starter.monitor.jpom.func.assets.model.MachineNodeModel;
import com.chua.starter.monitor.jpom.func.assets.server.MachineNodeServer;
import com.chua.starter.monitor.jpom.model.data.NodeModel;
import com.chua.starter.monitor.jpom.model.user.UserModel;
import com.chua.starter.monitor.jpom.service.node.NodeService;
import com.chua.starter.monitor.jpom.util.StringUtil;
import com.chua.starter.oauth.client.support.contants.AuthConstant;
import com.chua.starter.oauth.client.support.execute.AuthClientExecute;
import com.chua.starter.oauth.client.support.user.UserResult;
import com.google.common.base.Joiner;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Jpom server 端
 *
 * @author bwcx_jzy
 * @since 2019/4/16
 */
public abstract class BaseServerController extends BaseJpomController {
    public static final Cache<String, String> SHARDING_IDS = CacheUtil.newLRUCache(10, TimeUnit.DAYS.toMillis(1));
    public static final String NODE_ID = "nodeId";
    private static final ThreadLocal<UserModel> USER_MODEL_THREAD_LOCAL = new ThreadLocal<>();
    @Resource
    protected NodeService nodeService;
    @Resource
    protected MachineNodeServer machineNodeServer;

    /**
     * 为线程设置 用户
     *
     * @param userModel 用户
     */
    public static void resetInfo(UserModel userModel) {
        UserModel userModel1 = USER_MODEL_THREAD_LOCAL.get();
        if (userModel1 != null && userModel == UserModel.EMPTY) {
            // 已经存在，更新为 empty 、跳过
            return;
        }
        USER_MODEL_THREAD_LOCAL.set(userModel);
    }

    /**
     * 从线程 缓存中获取 用户信息
     *
     * @return 用户
     */
    public static UserModel getUserByThreadLocal() {
        AuthClientExecute instance = AuthClientExecute.getInstance();
        UserResult userResult = instance.getSafeUserResult();
        if (null == userResult) {
            return null;
        }
        UserModel userModel = new UserModel();
        userModel.setId(userResult.getUserId());
        userModel.setName(userResult.getUsername());
        Set<String> roles = userResult.getRoles();
        Set<String> permission = userResult.getPermission();
        if (!CollectionUtils.isEmpty(roles)) {
            userModel.setSystemUser(AuthConstant.hasAdmin(roles) ? 1 : 0);
            userModel.setParent(AuthConstant.hasAdmin(roles) ? UserModel.SYSTEM_ADMIN : "");
        }

        if (!CollectionUtils.isEmpty(permission)) {
            userModel.setPermissionGroup(Joiner.on(",").join(permission));
        }
        userModel.setStatus(1);

        return userModel;
    }

    public static void removeAll() {
        USER_MODEL_THREAD_LOCAL.remove();
    }

    /**
     * 只清理 是 empty 对象
     */
    public static void removeEmpty() {
        UserModel userModel = USER_MODEL_THREAD_LOCAL.get();
        if (userModel == UserModel.EMPTY) {
            USER_MODEL_THREAD_LOCAL.remove();
        }
    }

    public static UserModel getUserModel() {
        ServletRequestAttributes servletRequestAttributes = tryGetRequestAttributes();
        if (servletRequestAttributes == null) {
            return null;
        }
//        return (UserModel) servletRequestAttributes.getAttribute(LoginInterceptor.SESSION_NAME, RequestAttributes.SCOPE_SESSION);
        UserResult userResult = AuthClientExecute.getInstance().getSafeUserResult();
        if (null == userResult) {
            return null;
        }
        UserModel userModel = new UserModel();
        userModel.setId(userResult.getUserId());
        userModel.setName(userResult.getUsername());
        Set<String> roles = userResult.getRoles();
        userModel.setSystemUser(AuthConstant.hasAdmin(roles) ? 1 : 0);
        Set<String> permission = userResult.getPermission();
        userModel.setPermissionGroup(Joiner.on(",").join(permission));
        userModel.setStatus(1);
        userModel.setParent(AuthConstant.hasAdmin(roles) ? UserModel.SYSTEM_ADMIN : "");
        return userModel;
    }

    protected NodeModel getNode() {
        NodeModel nodeModel = tryGetNode();
        Assert.notNull(nodeModel, I18nMessageUtil.get("i18n.incorrect_node_info_node_does_not_exist.2fd8"));
        return nodeModel;
    }

    protected NodeModel tryGetNode() {
        HttpServletRequest request = getRequest();
        return (NodeModel) request.getAttribute("node");
    }

    /**
     * 判断是否传入机器 id
     *
     * @param machineId 机器id
     * @param request   请求
     * @param nodeUrl   节点 url
     * @param pars      参数
     * @param <T>       泛型
     * @return data
     */
    protected <T> JsonMessage<T> tryRequestMachine(String machineId, HttpServletRequest request, NodeUrl nodeUrl, String... pars) {
        if (StrUtil.isNotEmpty(machineId)) {
            MachineNodeModel model = machineNodeServer.getByKey(machineId);
            Assert.notNull(model, I18nMessageUtil.get("i18n.no_machine_found.c16c"));
            return NodeForward.request(model, request, nodeUrl, new String[]{}, pars);
        }
        return null;
    }

    /**
     * 判断是否传入机器 id 或者节点id
     *
     * @param machineId 机器id
     * @param request   请求
     * @param nodeUrl   节点 url
     * @param pars      参数
     * @param <T>       泛型
     * @return data
     */
    protected <T> JsonMessage<T> tryRequestNode(String machineId, HttpServletRequest request, NodeUrl nodeUrl, String... pars) {
        NodeModel nodeModel = tryGetNode();
        if (nodeModel != null) {
            return NodeForward.request(nodeModel, request, nodeUrl, new String[]{}, pars);
        }
        if (StrUtil.isNotEmpty(machineId)) {
            MachineNodeModel model = machineNodeServer.getByKey(machineId);
            Assert.notNull(model, I18nMessageUtil.get("i18n.no_machine_found.c16c"));
            return NodeForward.request(model, request, nodeUrl, new String[]{}, pars);
        }
        return null;
    }

    /**
     * 验证 cron 表达式, demo 账号不能开启 cron
     *
     * @param cron cron
     * @return 原样返回
     */
    protected String checkCron(String cron) {
        return StringUtil.checkCron(cron, s -> {
            UserModel user = getUser();
            return s;
        });
    }

    protected UserModel getUser() {
        UserModel userByThreadLocal = getUserByThreadLocal();
        Assert.notNull(userByThreadLocal, ServerConst.AUTHORIZE_TIME_OUT_CODE + StrUtil.EMPTY);
        return userByThreadLocal;
    }

    @Override
    public void uploadSharding(MultipartFile file, String tempPath, String sliceId, Integer totalSlice, Integer nowSlice, String fileSumMd5, String... extNames) throws IOException {
        Assert.state(BaseServerController.SHARDING_IDS.containsKey(sliceId), I18nMessageUtil.get("i18n.invalid_shard_id.46fd"));
        super.uploadSharding(file, tempPath, sliceId, totalSlice, nowSlice, fileSumMd5, extNames);
    }

    @Override
    public File shardingTryMerge(String tempPath, String sliceId, Integer totalSlice, String fileSumMd5) throws IOException {
        Assert.state(BaseServerController.SHARDING_IDS.containsKey(sliceId), I18nMessageUtil.get("i18n.invalid_shard_id.46fd"));
        try {
            return super.shardingTryMerge(tempPath, sliceId, totalSlice, fileSumMd5);
        } finally {
            // 判断-删除分片id
            BaseServerController.SHARDING_IDS.remove(sliceId);
        }
    }
}
