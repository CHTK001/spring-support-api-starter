/// *
// * Copyright (c) 2019 Of Him Code Technology Studio
// * Jpom is licensed under Mulan PSL v2.
// * You can use this software according to the terms and conditions of the Mulan PSL v2.
// * You may obtain a copy of Mulan PSL v2 at:
// * 			http://license.coscl.org.cn/MulanPSL2
// * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT, MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
// * See the Mulan PSL v2 for more details.
// */
//package com.chua.starter.monitor.jpom.common.interceptor;
//
//import cn.hutool.core.util.ArrayUtil;
//import cn.hutool.core.util.StrUtil;
//import cn.hutool.extra.servlet.JakartaServletUtil;
//import cn.hutool.extra.spring.SpringUtil;
//import cn.keepbx.jpom.model.JsonMessage;
//import com.chua.starter.monitor.jpom.common.BaseServerController;
//import com.chua.starter.monitor.jpom.common.i18n.I18nMessageUtil;
//import com.chua.starter.monitor.jpom.exception.AgentException;
//import com.chua.starter.monitor.jpom.model.BaseNodeModel;
//import com.chua.starter.monitor.jpom.model.data.NodeModel;
//import com.chua.starter.monitor.jpom.model.user.UserBindWorkspaceModel;
//import com.chua.starter.monitor.jpom.model.user.UserModel;
//import com.chua.starter.monitor.jpom.permission.*;
//import com.chua.starter.monitor.jpom.service.h2db.BaseNodeService;
//import com.chua.starter.monitor.jpom.service.h2db.BaseWorkspaceService;
//import com.chua.starter.monitor.jpom.service.node.NodeService;
//import com.chua.starter.monitor.jpom.service.user.UserBindWorkspaceService;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.http.MediaType;
//import org.springframework.web.method.HandlerMethod;
//
//import jakarta.annotation.Resource;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import java.util.function.Supplier;
//
///**
// * 权限拦截器
// *
// * @author bwcx_jzy
// * @since 2019/03/16.
// */
//@Configuration
//public class PermissionInterceptor implements HandlerMethodInterceptor {
//
//    @Resource
//    private NodeService nodeService;
//    @Resource
//    private UserBindWorkspaceService userBindWorkspaceService;
//    public static final Supplier<String> DEMO_TIP = () -> I18nMessageUtil.get("i18n.demo_account_cannot_use_feature.a1a1");
//    /**
//     * demo 账号不能使用的功能
//     */
//    private static final MethodFeature[] DEMO = new MethodFeature[]{
//        MethodFeature.DEL,
//        MethodFeature.UPLOAD,
//        MethodFeature.REMOTE_DOWNLOAD,
//        MethodFeature.EXECUTE};
//
//
//    private SystemPermission getSystemPermission(HandlerMethod handlerMethod) {
//        SystemPermission systemPermission = handlerMethod.getMethodAnnotation(SystemPermission.class);
//        if (systemPermission == null) {
//            systemPermission = handlerMethod.getBeanType().getAnnotation(SystemPermission.class);
//        }
//        return systemPermission;
//    }
//
//    private NodeDataPermission getNodeDataPermission(HandlerMethod handlerMethod) {
//        NodeDataPermission nodeDataPermission = handlerMethod.getMethodAnnotation(NodeDataPermission.class);
//        if (nodeDataPermission == null) {
//            nodeDataPermission = handlerMethod.getBeanType().getAnnotation(NodeDataPermission.class);
//        }
//        return nodeDataPermission;
//    }
//
//    @Override
//    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, HandlerMethod handlerMethod) throws Exception {
//        this.addNode(request);
//        UserModel userModel = BaseServerController.getUserModel();
//        if (userModel == null || userModel.isSuperSystemUser()) {
//            // 没有登录、或者超级管理直接放过
//            return true;
//        }
//        //
//        boolean permission = this.checkSystemPermission(userModel, request, response, handlerMethod);
//        if (!permission) {
//            return false;
//        }
//        permission = this.checkNodeDataPermission(userModel, request, response, handlerMethod);
//        if (!permission) {
//            return false;
//        }
//        Feature feature = handlerMethod.getMethodAnnotation(Feature.class);
//        if (feature == null) {
//            return true;
//        }
//        MethodFeature method = feature.method();
//        if (ArrayUtil.contains(DEMO, method) && userModel.isDemoUser()) {
//            this.errorMsg(response, DEMO_TIP.get());
//            return false;
//        }
//        ClassFeature classFeature = feature.cls();
//        if (classFeature == ClassFeature.NULL) {
//            Feature feature1 = handlerMethod.getBeanType().getAnnotation(Feature.class);
//            if (feature1 != null && feature1.cls() != ClassFeature.NULL) {
//                classFeature = feature1.cls();
//            }
//        }
//        // 判断功能权限
//        if (method != MethodFeature.LIST) {
//            String workspaceId = BaseWorkspaceService.getWorkspaceId(request);
//            UserBindWorkspaceModel.PermissionResult permissionResult = userBindWorkspaceService.checkPermission(userModel, workspaceId + StrUtil.DASHED + method.name());
//            if (!permissionResult.isSuccess()) {
//                this.errorMsg(response, permissionResult.errorMsg(StrUtil.format(I18nMessageUtil.get("i18n.corresponding_function.5bb5"), I18nMessageUtil.get(classFeature.getName().get()), I18nMessageUtil.get(method.getName().get()))));
//                return false;
//            }
//        }
//        return true;
//    }
//
//    /**
//     * 检查管理员权限
//     *
//     * @param userModel     用户
//     * @param response      响应
//     * @param handlerMethod 拦截到到方法
//     * @return true 有权限
//     */
//    private boolean checkNodeDataPermission(UserModel userModel, HttpServletRequest request, HttpServletResponse response, HandlerMethod handlerMethod) {
//        NodeDataPermission nodeDataPermission = this.getNodeDataPermission(handlerMethod);
//        if (nodeDataPermission == null || userModel.isSuperSystemUser()) {
//            return true;
//        }
//        NodeModel node = (NodeModel) request.getAttribute("node");
//        if (node != null) {
//            String parameterName = nodeDataPermission.parameterName();
//            BaseNodeService<?> baseNodeService = SpringUtil.getBean(nodeDataPermission.cls());
//            String dataId = request.getParameter(parameterName);
//            if (StrUtil.isNotEmpty(dataId)) {
//                BaseNodeModel data = baseNodeService.getData(node.getId(), dataId);
//                if (data != null) {
//                    UserBindWorkspaceModel.PermissionResult permissionResult = userBindWorkspaceService.checkPermission(userModel, data.getWorkspaceId());
//
//                    if (!permissionResult.isSuccess()) {
//                        this.errorMsg(response, permissionResult.errorMsg());
//                        return false;
//                    }
//                }
//            }
//        }
//        return true;
//    }
//
//    /**
//     * 检查管理员权限
//     *
//     * @param userModel     用户
//     * @param response      响应
//     * @param handlerMethod 拦截到到方法
//     * @return true 有权限
//     */
//    private boolean checkSystemPermission(UserModel userModel, HttpServletRequest request, HttpServletResponse response, HandlerMethod handlerMethod) {
//        SystemPermission systemPermission = this.getSystemPermission(handlerMethod);
//        if (systemPermission == null) {
//            return true;
//        }
//        if (systemPermission.superUser() && !userModel.isSuperSystemUser()) {
//            this.errorMsg(response, I18nMessageUtil.get("i18n.not_super_admin.962e"));
//            return false;
//        }
//        if (!userModel.isSystemUser()) {
//            this.errorMsg(response, I18nMessageUtil.get("i18n.no_server_management_permission.ee19"));
//            return false;
//        }
//        return true;
//    }
//
//    private void addNode(HttpServletRequest request) {
//        String nodeId = request.getParameter("nodeId");
//        if (!StrUtil.isBlankOrUndefined(nodeId)) {
//            // 节点信息
//            NodeModel nodeModel = nodeService.getByKey(nodeId);
//            if (nodeModel != null && !nodeModel.isOpenStatus()) {
//                throw new AgentException(nodeModel.getName() + I18nMessageUtil.get("i18n.node_not_enabled.a14d"));
//            }
//            request.setAttribute("node", nodeModel);
//        }
//    }
//
//    private void errorMsg(HttpServletResponse response, String msg) {
//        JsonMessage<String> jsonMessage = new JsonMessage<>(302, msg);
//        JakartaServletUtil.write(response, jsonMessage.toString(), MediaType.APPLICATION_JSON_VALUE);
//    }
//}
