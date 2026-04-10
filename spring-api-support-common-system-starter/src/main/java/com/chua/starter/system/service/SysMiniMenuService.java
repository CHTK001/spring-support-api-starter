package com.chua.starter.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.entity.system.SysMiniMenu;
import com.chua.starter.system.pojo.menu.SysMiniMenuVO;

import java.util.List;
import java.util.Set;

public interface SysMiniMenuService extends IService<SysMiniMenu> {

    ReturnResult<List<SysMiniMenu>> listManageMenus();

    ReturnResult<List<SysMiniMenuVO>> listUserMenus(Set<String> roles);

    ReturnResult<SysMiniMenu> saveMenu(SysMiniMenu sysMiniMenu, Set<String> roles);

    ReturnResult<Boolean> updateMenu(Set<String> roles, SysMiniMenu sysMiniMenu);

    ReturnResult<Boolean> deleteMenu(Long sysMiniMenuId);
}
