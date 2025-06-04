package com.chua.starter.monitor.jpom.common.interceptor;

import cn.hutool.core.util.StrUtil;
import com.chua.report.client.starter.jpom.common.common.i18n.I18nMessageUtil;
import com.chua.report.client.starter.jpom.common.common.interceptor.HandlerMethodInterceptor;
import com.chua.starter.monitor.jpom.exception.AgentException;
import com.chua.starter.monitor.jpom.model.data.NodeModel;
import com.chua.starter.monitor.jpom.service.node.NodeService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.HandlerMethod;

/**
 * @author CH
 * @since 2025/6/3 16:41
 */
@Configuration
public class NodeInterceptor implements HandlerMethodInterceptor {
    /**
     * 节点服务
     */
    @Resource
    private NodeService nodeService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, HandlerMethod handler) throws Exception {
        addNode(request);
        return true;
    }

    private void addNode(HttpServletRequest request) {
        String nodeId = request.getParameter("nodeId");

        if (!StrUtil.isBlankOrUndefined(nodeId)) {
            // 节点信息
            NodeModel nodeModel = nodeService.getByKey(nodeId);
            if (nodeModel != null && !nodeModel.isOpenStatus()) {
                throw new AgentException(nodeModel.getName() + I18nMessageUtil.get("i18n.node_not_enabled.a14d"));
            }
            request.setAttribute("node", nodeModel);
        }
    }
}
