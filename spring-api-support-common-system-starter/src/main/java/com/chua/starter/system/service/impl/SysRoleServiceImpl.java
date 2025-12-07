package com.chua.starter.system.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.utils.CollectionUtils;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.oauth.client.support.contants.AuthConstant;
import com.chua.starter.entity.system.SysRole;
import com.chua.starter.entity.system.SysRoleMenu;
import com.chua.starter.system.mapper.SysRoleMapper;
import com.chua.starter.system.pojo.role.RoleMenuUpdate;
import com.chua.starter.system.service.SysRoleMenuService;
import com.chua.starter.system.service.SysRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 * @since 2024/8/7
 * @author CH    
 */
@Service
@RequiredArgsConstructor
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements SysRoleService{

    final TransactionTemplate transactionTemplate;
    final SysRoleMenuService sysRoleMenuService;

    @Override
    public IPage<SysRole> pageForRole(Query<SysRole> query, SysRole sysRole, Set<String> roles) {
        return baseMapper.selectPage(query.createPage(), Wrappers.<SysRole>lambdaQuery()
                .like(StringUtils.isNotBlank(sysRole.getSysRoleCode()), SysRole::getSysRoleCode, sysRole.getSysRoleCode())
                .like(StringUtils.isNotBlank(sysRole.getSysRoleName()), SysRole::getSysRoleName, sysRole.getSysRoleName())
                .like(StringUtils.isNotBlank(sysRole.getSysRoleRemark()), SysRole::getSysRoleRemark, sysRole.getSysRoleRemark())
                .ne(SysRole::getSysRoleCode, AuthConstant.SUPER_ADMIN)
        );
    }

    @Override
    public ReturnResult<Boolean> delete(Long roleId) {
        if(null == roleId) {
            return ReturnResult.fail("瑙掕壊ID涓嶈兘涓虹┖");
        }

        SysRole sysRole = baseMapper.selectById(roleId);
        if(null == sysRole) {
            return ReturnResult.fail("瑙掕壊涓嶅瓨鍦，璇峰埛鏂板悗閲嶈瘯!");
        }

        Integer sysRoleInSystem = sysRole.getSysRoleInSystem();

        if(null != sysRoleInSystem && sysRoleInSystem == 1) {
            return ReturnResult.fail("鍐呯疆瑙掕壊涓嶅厑璁稿垹闄");
        }

        return ReturnResult.of(baseMapper.deleteById(roleId) > 0);
    }

    @Override
    public ReturnResult<Boolean> updateRole(Set<String> roles, SysRole sysRole) {
        SysRole sysRole2 = baseMapper.selectById(sysRole.getSysRoleId());
        Integer sysRoleInSystem = sysRole2.getSysRoleInSystem();
        if(null != sysRoleInSystem && sysRoleInSystem == 1 && !AuthConstant.isSuperAdmin(roles)) {
            return ReturnResult.fail("鍐呯疆瑙掕壊涓嶅厑璁镐慨鏀");
        }

        sysRole.setSysRoleInSystem(null);
        String sysRoleCode = sysRole.getSysRoleCode();
        if(StringUtils.isNotBlank(sysRoleCode)) {
            SysRole sysRole1 = baseMapper.selectOne(Wrappers.<SysRole>lambdaQuery()
                    .eq(SysRole::getSysRoleCode, sysRoleCode.toUpperCase())
                    .ne(SysRole::getSysRoleId, sysRole.getSysRoleId()));
            if(null != sysRole1) {
                return ReturnResult.fail("瑙掕壊缂栫爜宸插瓨鍦");
            }
        }

        return ReturnResult.of(baseMapper.updateById(sysRole) > 0);
    }

    @Override
    public ReturnResult<SysRole> saveRole(SysRole sysRole) {
        sysRole.setSysRoleInSystem(0);
        String sysRoleCode = sysRole.getSysRoleCode();
        if(AuthConstant.ADMIN.equalsIgnoreCase(sysRoleCode) ||
                AuthConstant.SUPER_ADMIN.equalsIgnoreCase(sysRoleCode) ||
                AuthConstant.OPS.equalsIgnoreCase(sysRoleCode)
        ) {
            return ReturnResult.fail("涓嶅厑璁告柊寤虹壒娈婅鑹");
        }

        SysRole sysRole1 = baseMapper.selectOne(Wrappers.<SysRole>lambdaQuery()
                .eq(SysRole::getSysRoleCode, sysRoleCode.toUpperCase()));
        if(null != sysRole1) {
            return ReturnResult.fail("瑙掕壊缂栫爜宸插瓨鍦");
        }
        if(baseMapper.insert(sysRole) > 0) {
            return ReturnResult.success(sysRole);
        }
        return ReturnResult.error("鏂板澶辫触");
    }

    @Override
    public ReturnResult<Boolean> updateMenu(Set<String> roles, RoleMenuUpdate roleMenuUpdate) {
        return ReturnResult.of(Optional.ofNullable(transactionTemplate.execute(status -> {
            sysRoleMenuService.remove(Wrappers.<SysRoleMenu>lambdaQuery().eq(SysRoleMenu::getSysRoleId, roleMenuUpdate.getRoleId()));
            List<Integer> menuId = roleMenuUpdate.getMenuId();
            if(CollectionUtils.isNotEmpty(menuId)) {
                return sysRoleMenuService.saveBatch(menuId.stream().map(menuId1 -> {
                    SysRoleMenu sysRoleMenu = new SysRoleMenu();
                    sysRoleMenu.setSysRoleId(roleMenuUpdate.getRoleId());
                    sysRoleMenu.setSysMenuId(menuId1);
                    return sysRoleMenu;
                }).toList());
            }
            return true;
        })));
    }

    @Override
    public ReturnResult<Set<Integer>> getMenu(RoleMenuUpdate roleMenuUpdate) {
        List<SysRoleMenu> list = sysRoleMenuService.list(Wrappers.<SysRoleMenu>lambdaQuery().eq(SysRoleMenu::getSysRoleId, roleMenuUpdate.getRoleId()));
        return ReturnResult.ok(list.stream().map(SysRoleMenu::getSysMenuId).collect(Collectors.toUnmodifiableSet()));
    }
}
