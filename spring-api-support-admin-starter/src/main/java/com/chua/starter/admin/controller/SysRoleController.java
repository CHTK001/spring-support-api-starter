package com.chua.starter.admin.controller;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chua.common.support.function.Joiner;
import com.chua.common.support.function.Splitter;
import com.chua.common.support.lang.code.ReturnPageResult;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.admin.entity.SysRole;
import com.chua.starter.admin.entity.SysRoleMenu;
import com.chua.starter.admin.query.SysRoleUpdateForMenu;
import com.chua.starter.admin.result.SysRoleForMenuResult;
import com.chua.starter.admin.service.SysRoleMenuService;
import com.chua.starter.admin.service.SysRoleService;
import com.chua.starter.admin.service.SysUserRoleService;
import com.chua.starter.admin.utils.ServicePageResultUtils;
import com.chua.starter.common.support.annotations.OperateLog;
import com.chua.starter.common.support.annotations.UserLogger;
import com.chua.starter.common.support.constant.Constant;
import com.chua.starter.common.support.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 机构
 * @author CH
 */
@Tag(name = "角色接口")
@RestController
@RequestMapping("/v2/role")
@RequiredArgsConstructor
public class SysRoleController implements Constant {


    private final SysRoleService sysRoleService;

    private final SysUserRoleService sysUserRoleService;

    private final SysRoleMenuService sysRoleMenuService;
    /**
     * 角色列表
     * @return 角色列表
     */
    @Operation(summary = "角色列表")
    @GetMapping("list")
    @Cacheable(cacheManager = SYSTEM, cacheNames = SYSTEM, key = "'role'")
    public ReturnResult<List<SysRole>> list() {
        return Result.ok(sysRoleService.list(Wrappers.<SysRole>lambdaQuery().ne(SysRole::getRoleCode, ADMIN)
                .eq(SysRole::getRoleStatus, 1)
                .orderByAsc(SysRole::getRoleSort)
        ));
    }
    /**
     * 角色分页
     * @return 角色分页
     */
    @Operation(summary = "角色分页")
    @GetMapping("page")
    public ReturnPageResult<SysRole> page(
            String keyword,
            @Parameter(description = "页码") @RequestParam(value = "page", defaultValue = "1") Integer page,
            @Parameter(description = "数量") @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize
    ) {
        return
                ServicePageResultUtils.ok(
                    sysRoleService.page(new Page<>(page, pageSize),
                            Wrappers.<SysRole>lambdaQuery()
                                    .like(StringUtils.isNotBlank(keyword), SysRole::getRoleName, keyword)
                                    .or().like(StringUtils.isNotBlank(keyword), SysRole::getRoleCode, keyword)
                                    .ne(SysRole::getRoleCode, ADMIN)
                                    .orderByAsc(SysRole::getRoleSort)
                ));
    }
    /**
     * 修改角色
     * @return 修改角色
     */
    @Operation(summary = "修改角色数据")
    @PostMapping("updateRole")
    @OperateLog
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(cacheManager = SYSTEM, cacheNames = SYSTEM, key = "'role'")
    public ReturnResult<Boolean> updateRole(@RequestBody SysRoleUpdateForMenu forMenu) {
        Long roleId = forMenu.getRoleId();
        if(null == roleId) {
            return Result.illegal("唯一标识不能为空");
        }

        SysRole sysRole = sysRoleService.getById(roleId);
        if(null == sysRole) {
            return Result.illegal("角色不存在, 请重新刷新或者登录");
        }
        sysRole.setRoleGrid(Joiner.on(',').join(Splitter.on(',').trimResults().omitEmptyStrings().splitToSet(forMenu.getRoleGrid())));
        sysRole.setRoleDataScope(forMenu.getRoleDataScope());
        sysRole.setRoleDataRule(forMenu.getRoleDataRule());
        sysRole.setRoleDashboard(forMenu.getRoleDashboard());
        sysRoleService.updateById(sysRole);

        Set<String> strings = Splitter.on(',').trimResults().omitEmptyStrings().splitToSet(forMenu.getMenuIds());
        List<SysRoleMenu> menus = new ArrayList<>(strings.size());
        for (String string : strings) {
            try {
                menus.add(new SysRoleMenu(roleId, Long.parseLong(string)));
            } catch (NumberFormatException ignored) {
            }
        }

        sysRoleMenuService.remove(Wrappers.<SysRoleMenu>lambdaQuery().eq(SysRoleMenu::getRoleId, roleId));
        sysRoleMenuService.saveBatch(menus);

        return Result.success();
    }
    /**
     * 获取角色信息
     * @return 获取角色信息
     */
    @Operation(summary = "修改角色数据")
    @GetMapping("getRole")
    @OperateLog
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<SysRoleForMenuResult> updateRole(Long roleId) {
        if(null == roleId) {
            return Result.illegal("唯一标识不能为空");
        }

        SysRole sysRole = sysRoleService.getById(roleId);
        if(null == sysRole) {
            return Result.illegal("角色不存在, 请重新刷新或者登录");
        }

        SysRoleForMenuResult rs = new SysRoleForMenuResult();
        rs.setRoleGrid(Splitter.on(",").omitEmptyStrings().trimResults().split(sysRole.getRoleGrid()));
        rs.setRoleDataScope(sysRole.getRoleDataScope());
        rs.setRoleDataRule(sysRole.getRoleDataRule());
        rs.setRoleDashboard(sysRole.getRoleDashboard());
        List<SysRoleMenu> list = sysRoleMenuService.list(Wrappers.<SysRoleMenu>lambdaQuery().eq(SysRoleMenu::getRoleId, roleId));
        rs.setMenuIds(list.stream().map(SysRoleMenu::getMenuId).map(String::valueOf).toArray(String[]::new));
        return Result.success(rs);
    }
    /**
     * 修改角色
     * @return 修改角色
     */
    @Operation(summary = "修改角色")
    @PutMapping("update")
    @OperateLog
    @CacheEvict(cacheManager = SYSTEM, cacheNames = SYSTEM, key = "'role'")
    public ReturnResult<Boolean> update(@RequestBody SysRole sysRole) {
        if(null == sysRole.getRoleId()) {
            return Result.illegal("唯一标识不能为空");
        }

        if(com.chua.common.support.utils.StringUtils.isNotEmpty(sysRole.getRoleCode())
                &&  sysRoleService.count(
                        Wrappers.<SysRole>lambdaQuery()
                                .eq(SysRole::getRoleCode, sysRole.getRoleCode())
                                .ne(SysRole::getRoleId, sysRole.getRoleId())
        ) > 0L) {
            return Result.illegal("角色编码已存在");
        }
        SysRole sysRole1 = sysRoleService.getById(sysRole.getRoleId());
        if(null != sysRole1.getRoleSys() && sysRole1.getRoleSys() == 1) {
            return Result.illegal("系统角色不允许修改");
        }


        sysRole.setRoleSys(0);

        return Result.ok(sysRoleService.updateById(sysRole));
    }
    /**
     * 新增角色
     * @return 新增角色
     */
    @Operation(summary = "新增角色")
    @PostMapping("save")
    @OperateLog
    @javax.transaction.Transactional(rollbackOn = Exception.class)
    @CacheEvict(cacheManager = SYSTEM, cacheNames = SYSTEM, key = "'role'")
    public ReturnResult<SysRole> save(@RequestBody SysRole sysRole, @Valid BindingResult bindingResult) {
        if(bindingResult.hasErrors()) {
            return Result.illegal(bindingResult.getAllErrors().get(0).getDefaultMessage());
        }


        if(sysRoleService.count(Wrappers.<SysRole>lambdaQuery().eq(SysRole::getRoleCode, sysRole.getRoleCode())) > 0L) {
            return Result.illegal("角色编码已存在");
        }

        sysRole.setRoleSys(0);
        sysRoleService.save(sysRole);
        return Result.ok(sysRole);
    }
    /**
     * 删除角色
     * @return 删除角色
     */
    @Operation(summary = "删除角色")
    @DeleteMapping("delete")
    @UserLogger
    @OperateLog
    @javax.transaction.Transactional(rollbackOn = Exception.class)
    @CacheEvict(cacheManager = SYSTEM, cacheNames = SYSTEM, key = "'role'")
    public ReturnResult<Boolean> delete(Long roleId) {
        if(null == roleId) {
            return Result.illegal("角色不存在");
        }

        SysRole sysRole = sysRoleService.getById(roleId);
        if(null == sysRole) {
            return Result.illegal("角色不存在");
        }

        if(ADMIN.equalsIgnoreCase(sysRole.getRoleCode())) {
            return Result.illegal("系统管理员不允许删除");
        }

        if(null != sysRole.getRoleSys() && sysRole.getRoleSys() == 1) {
            return Result.illegal("系统角色不允许删除");
        }


        return Result.ok(sysRoleService.removeById(roleId));
    }
    /**
     * 批量删除角色
     * @return 批量删除角色
     */
    @UserLogger
    @Operation(summary = "批量删除角色")
    @DeleteMapping("batchDelete")
    @javax.transaction.Transactional(rollbackOn = Exception.class)
    @CacheEvict(cacheManager = SYSTEM, cacheNames = SYSTEM, key = "'role'")
    public ReturnResult<Boolean> batchDelete(String roleIds) {
        if(StringUtils.isBlank(roleIds)) {
            return Result.illegal("角色不存在");
        }

        Set<String> strings = Splitter.on(',').omitEmptyStrings().trimResults().splitToSet(roleIds);
        long count = sysRoleService.count(Wrappers.<SysRole>lambdaQuery().in(SysRole::getRoleId, strings).eq(SysRole::getRoleSys, 1));
        if(count > 0L) {
            return Result.illegal("删除的角色中包含系统角色不允许删除");
        }

        return Result.ok(sysRoleService.removeBatchByIds(strings));
    }
}
