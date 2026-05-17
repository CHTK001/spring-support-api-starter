package com.chua.starter.project.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.chua.common.support.lang.code.ReturnResult;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;

/**
 * 当 OAuth 禁用时，拦截系统设置/菜单请求并返回模拟数据，
 * 使前端 UI 测试能够正常加载，避免与 SysSettingController 冲突。
 */
@Component
@Order(1)
@ConditionalOnProperty(prefix = "plugin.oauth", name = "enable", havingValue = "false")
public class MockSettingFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(MockSettingFilter.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final Set<String> MOCK_PATHS = Set.of(
        "/v2/setting/list",
        "/v2/setting/group/list",
        "/v2/setting/default",
        "/v2/user/menu"
    );

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        String uri = req.getRequestURI();
        String method = req.getMethod();

        if ("GET".equalsIgnoreCase(method) && MOCK_PATHS.contains(uri)) {
            log.debug("[MockSettingFilter] 拦截 mock 请求: {} {}", method, uri);
            resp.setContentType("application/json;charset=UTF-8");

            Object result;
            if (uri.equals("/v2/user/menu")) {
                result = buildMenuResponse();
            } else {
                result = ReturnResult.ok(Collections.emptyList());
            }

            resp.getWriter().write(MAPPER.writeValueAsString(result));
            return;
        }

        chain.doFilter(request, response);
    }

    private ReturnResult<List<Map<String, Object>>> buildMenuResponse() {
        List<Map<String, Object>> menus = new ArrayList<>();
        menus.add(Map.of(
            "id", 1, "parentId", 0, "name", "Project",
            "path", "/project", "component", "LAYOUT",
            "meta", Map.of("title", "项目管理", "icon", "ep:folder"),
            "children", List.of(Map.of(
                "id", 2, "parentId", 1, "name", "ProjectManage",
                "path", "/project/manage", "component", "LAYOUT",
                "meta", Map.of("title", "项目管理", "icon", "ep:folder")
            ))
        ));
        return ReturnResult.ok(menus);
    }
}
