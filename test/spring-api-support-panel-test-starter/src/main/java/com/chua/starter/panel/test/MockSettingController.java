package com.chua.starter.panel.test;

import com.chua.common.support.lang.code.ReturnResult;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 当 OAuth 禁用时提供模拟设置/菜单端点，
 * 使前端 UI 测试能够正常加载系统配置。
 */
@RestController
@ConditionalOnProperty(prefix = "plugin.oauth", name = "enable", havingValue = "false")
public class MockSettingController {

    @GetMapping("/v2/setting/list")
    public ReturnResult<List<Map<String, Object>>> list(
            @RequestParam(required = false) String sysSettingGroup,
            @RequestParam(required = false) String version) {
        return ReturnResult.ok(Collections.emptyList());
    }

    @GetMapping("/v2/setting/group/list")
    public ReturnResult<List<Map<String, Object>>> listForSettingGroup() {
        return ReturnResult.ok(Collections.emptyList());
    }

    @GetMapping("/v2/setting/default")
    public ReturnResult<List<Map<String, Object>>> listForDefault() {
        return ReturnResult.ok(Collections.emptyList());
    }

    @GetMapping("/v2/user/menu")
    public ReturnResult<List<Map<String, Object>>> userMenu(
            @RequestParam(required = false) String version) {
        // 返回模拟菜单数据，使前端能够加载 server/panel 页面
        List<Map<String, Object>> menus = new ArrayList<>();
        menus.add(Map.of(
            "id", 1, "parentId", 0, "name", "Server",
            "path", "/server", "component", "iframe",
            "meta", Map.of("title", "服务器管理", "icon", "server-line"),
            "children", List.of(Map.of(
                "id", 2, "parentId", 1, "name", "ServerList",
                "path", "/server/list", "component", "LAYOUT",
                "meta", Map.of(
                    "title", "物理服务器",
                    "icon", "server-line",
                    "frameSrc", "/server/index.html"
                )
            ))
        ));
        return ReturnResult.ok(menus);
    }
}
