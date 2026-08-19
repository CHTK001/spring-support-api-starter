package com.chua.starter.system.configuration;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.chua.common.support.utils.StringUtils;
import com.chua.starter.entity.system.SysMenu;
import com.chua.starter.entity.system.SysRole;
import com.chua.starter.entity.system.SysRoleMenu;
import com.chua.starter.oauth.client.support.contants.AuthConstant;
import com.chua.starter.system.constant.SystemConstant;
import com.chua.starter.system.mapper.SysMenuMapper;
import com.chua.starter.system.mapper.SysRoleMapper;
import com.chua.starter.system.mapper.SysRoleMenuMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 反馈管理菜单初始化
 *
 * @since 2026/08/03
 * @author CH
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "plugin.system.feedback.menu", name = "enabled", havingValue = "true", matchIfMissing = true)
public class FeedbackMenuInitializer implements ApplicationRunner {

    /**
     * 父级菜单路径
     */
    private static final String PARENT_MENU_PATH = "/manage";

    /**
     * 反馈管理菜单路径
     */
    private static final String MENU_PATH = "/manage/feedback";

    /**
     * 菜单组件地址
     */
    private static final String MENU_COMPONENT = "system/feedback/index";

    /**
     * 菜单标题
     */
    private static final String MENU_TITLE = "反馈管理";

    /**
     * 菜单名称
     */
    private static final String MENU_NAME = "feedback";

    /** SYSmenumapper */
    private final SysMenuMapper sysMenuMapper;
    /** SYSrolemapper */
    private final SysRoleMapper sysRoleMapper;
    /** SYSrolemenumapper */
    private final SysRoleMenuMapper sysRoleMenuMapper;
    private final Map<String, CacheManager> cacheManagers;

    /**
     * 应用启动后执行反馈菜单初始化。
     *
     * @param args 启动参数
     */
    @Override
    public void run(ApplicationArguments args) {
        SysMenu parentMenu = sysMenuMapper.selectOne(
                Wrappers.<SysMenu>lambdaQuery()
                        .eq(SysMenu::getSysMenuPath, PARENT_MENU_PATH)
                        .eq(SysMenu::getSysMenuType, 0)
                        .last("limit 1")
        );
        if (parentMenu == null) {
            log.warn("[反馈管理] 未找到父级菜单 {}, 跳过初始化", PARENT_MENU_PATH);
            return;
        }

        boolean changed = false;
        SysMenu feedbackMenu = ensureMenu(parentMenu);
        changed |= feedbackMenu.getSysMenuId() != null;
        changed |= ensureRoleMenus(feedbackMenu.getSysMenuId());
        if (changed) {
            clearCache();
        }
    }

    /**
     * 确保反馈菜单存在并保持最新。
     *
     * @param parentMenu 父级菜单
     * @return 反馈菜单
     */
    private SysMenu ensureMenu(SysMenu parentMenu) {
        SysMenu existing = sysMenuMapper.selectOne(
                Wrappers.<SysMenu>lambdaQuery()
                        .eq(SysMenu::getSysMenuPath, MENU_PATH)
                        .last("limit 1")
        );
        if (existing != null) {
            boolean needUpdate = false;
            if (!Objects.equals(existing.getSysMenuPid(), parentMenu.getSysMenuId())) {
                existing.setSysMenuPid(parentMenu.getSysMenuId());
                needUpdate = true;
            }
            if (!StringUtils.equals(existing.getSysMenuComponent(), MENU_COMPONENT)) {
                existing.setSysMenuComponent(MENU_COMPONENT);
                needUpdate = true;
            }
            if (!StringUtils.equals(existing.getSysMenuTitle(), MENU_TITLE)) {
                existing.setSysMenuTitle(MENU_TITLE);
                needUpdate = true;
            }
            if (!StringUtils.equals(existing.getSysMenuName(), MENU_NAME)) {
                existing.setSysMenuName(MENU_NAME);
                needUpdate = true;
            }
            if (needUpdate) {
                existing.setSysMenuTreeId(buildTreeId(parentMenu, existing.getSysMenuId()));
                sysMenuMapper.updateById(existing);
            }
            return existing;
        }

        SysMenu menu = new SysMenu();
        menu.setSysMenuName(MENU_NAME);
        menu.setSysMenuTitle(MENU_TITLE);
        menu.setSysMenuPath(MENU_PATH);
        menu.setSysMenuPid(parentMenu.getSysMenuId());
        menu.setSysMenuComponent(MENU_COMPONENT);
        menu.setSysMenuIcon("ri:feedback-line");
        menu.setSysMenuType(0);
        menu.setSysMenuSort(85);
        menu.setSysMenuRole(String.join(",", AuthConstant.SUPER_ADMIN, AuthConstant.ADMIN));
        menu.setSysMenuKeepAlive(1);
        menu.setSysMenuHidden(0);
        menu.setSysMenuShowParent(0);
        menu.setSysMenuHiddenTag(0);
        menu.setSysMenuFixedTag(0);
        menu.setSysMenuHome(0);
        menu.setSysMenuEngine(0);
        sysMenuMapper.insert(menu);
        menu.setSysMenuTreeId(buildTreeId(parentMenu, menu.getSysMenuId()));
        sysMenuMapper.updateById(menu);
        log.info("[反馈管理] 已自动初始化菜单: {}", MENU_PATH);
        return menu;
    }

    /**
     * 确保管理员与超管角色已关联反馈菜单。
     *
     * @param menuId 菜单ID
     * @return true 表示发生了新增关联
     */
    private boolean ensureRoleMenus(Integer menuId) {
        boolean changed = false;
        List<SysRole> roles = sysRoleMapper.selectList(
                Wrappers.<SysRole>lambdaQuery()
                        .in(SysRole::getSysRoleCode, AuthConstant.SUPER_ADMIN, AuthConstant.ADMIN)
        );
        for (SysRole role : roles) {
            SysRoleMenu existed = sysRoleMenuMapper.selectOne(
                    Wrappers.<SysRoleMenu>lambdaQuery()
                            .eq(SysRoleMenu::getSysRoleId, role.getSysRoleId())
                            .eq(SysRoleMenu::getSysMenuId, menuId)
                            .last("limit 1")
            );
            if (existed != null) {
                continue;
            }
            SysRoleMenu roleMenu = new SysRoleMenu();
            roleMenu.setSysRoleId(role.getSysRoleId());
            roleMenu.setSysMenuId(menuId);
            sysRoleMenuMapper.insert(roleMenu);
            changed = true;
        }
        return changed;
    }

    /**
     * 构建菜单树ID。
     *
     * @param parentMenu 父级菜单
     * @param menuId     菜单ID
     * @return 树ID字符串
     */
    private String buildTreeId(SysMenu parentMenu, Integer menuId) {
        String parentTreeId = parentMenu.getSysMenuTreeId();
        if (StringUtils.isBlank(parentTreeId)) {
            return "0," + parentMenu.getSysMenuId() + "," + menuId;
        }
        return parentTreeId + "," + menuId;
    }

    /**
     * 清空所有缓存管理器下的缓存。
     */
    private void clearCache() {
        cacheManagers.forEach((cacheName, manager) -> {
            Cache namedCache = manager.getCache(cacheName);
            if (namedCache != null) {
                namedCache.clear();
            }
            manager.getCacheNames().forEach(existingCacheName -> {
                Cache cache = manager.getCache(existingCacheName);
                if (cache != null) {
                    cache.clear();
                }
            });
        });
    }
}
