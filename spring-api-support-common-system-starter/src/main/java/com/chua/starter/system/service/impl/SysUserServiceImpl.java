package com.chua.starter.system.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.base.bean.BeanUtils;
import com.chua.common.support.geo.GeoCity;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.core.utils.CollectionUtils;
import com.chua.common.support.core.utils.DigestUtils;
import com.chua.common.support.core.utils.RandomUtils;
import com.chua.common.support.core.utils.StringUtils;
import com.chua.starter.common.support.application.GlobalSettingFactory;
import com.chua.starter.common.support.service.IptablesService;
import com.chua.starter.common.support.utils.RequestUtils;
import com.chua.starter.oauth.client.support.contants.AuthConstant;
import com.chua.starter.oauth.client.support.enums.UpgradeType;
import com.chua.starter.oauth.client.support.execute.AuthClientExecute;
import com.chua.starter.oauth.client.support.properties.AuthClientProperties;
import com.chua.starter.system.mapper.SysUserMapper;
import com.chua.starter.system.model.user.SysUserQuery;
import com.chua.starter.entity.system.*;
import com.chua.starter.system.mapper.SysRoleMapper;
import com.chua.starter.system.mapper.SysUserLayoutMapper;
import com.chua.starter.system.pojo.menu.SysMenuVO;
import com.chua.starter.system.pojo.user.SysUserPasswordUpdate;
import com.chua.starter.system.pojo.user.SysUserUpdate;
import com.chua.starter.system.pojo.user.SysUserVO;
import com.chua.starter.system.pojo.user.UserLayout;
import com.chua.starter.system.service.SysMenuService;
import com.chua.starter.system.service.SysUserRoleService;
import com.chua.starter.system.service.SysUserService;
import com.chua.starter.system.constant.SystemConstant;
import com.chua.starter.system.properties.SystemSecurityProperties;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author CH
 * @since 2024/8/7
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {
    final AuthClientProperties authProperties;
    final TransactionTemplate transactionTemplate;
    final SysRoleMapper sysRoleMapper;
    final SysUserRoleService sysUserRoleService;
    final RestTemplate restTemplate;
    final RedisTemplate redisTemplate;
    final SysMenuService sysMenuService;
    final IptablesService iptablesService;
    final SysUserLayoutMapper sysUserLayoutMapper;
    final SystemSecurityProperties securityProperties;
    private static final GlobalSettingFactory GLOBAL_SETTING_FACTORY = GlobalSettingFactory.getInstance();

    @Override
    public IPage<SysUserVO> pageForUser(SysUserQuery sysUserQuery, Set<String> roles) {
        IPage<SysUser> sysUserPage = baseMapper.selectPageUser(sysUserQuery.createPage(), sysUserQuery, AuthConstant.hasAdmin(roles));

        List<SysUser> records1 = sysUserPage.getRecords();
        Page<SysUserVO> result = new Page<>();
        result.setCurrent(sysUserPage.getCurrent())
                .setSize(sysUserPage.getSize())
                .setTotal(sysUserPage.getTotal())
                .setRecords(records1.stream().map(it -> BeanUtils.copyProperties(it, SysUserVO.class)).collect(Collectors.toList()));
        List<SysUserVO> records = result.getRecords();
        if (CollectionUtils.isEmpty(records)) {
            return result;
        }
        Set<Integer> userIds = records.stream().map(SysUserVO::getSysUserId).collect(Collectors.toUnmodifiableSet());
        List<SysRole> sysRoles = sysRoleMapper.listByUserId(userIds);
        Map<Integer, List<SysRole>> collect = sysRoles.stream().collect(Collectors.groupingBy(SysRole::getSysUserId));
        for (SysUserVO sysUser : records) {
            sysUser.setUserRoles(collect.getOrDefault(sysUser.getSysUserId(), Collections.emptyList()));
        }
        return result;
    }

    @Override
    public ReturnResult<Boolean> delete(Long userId) {
        SysUser sysUser1 = getById(userId);
        if (null == sysUser1) {
            return ReturnResult.illegal("用户信息不存在");
        }

        if (SystemConstant.IN_SYSTEM_YES == sysUser1.getSysUserInSystem()) {
            return ReturnResult.illegal("系统账号不能删除");
        }

        return ReturnResult.of(removeById(userId));
    }

    @Override
    public ReturnResult<SysUser> resetOwner(Long userId) {
        SysUser sysUser1 = getById(userId);
        if (null == sysUser1) {
            return ReturnResult.illegal("用户信息不存在");
        }

        String newPassword = RandomUtils.randomString(8);
        sysUser1.setSysUserSalt(RandomUtils.randomString(securityProperties.getSaltLength()));
        sysUser1.setSysUserPassword(DigestUtils.md5Hex(newPassword + sysUser1.getSysUserSalt()));

        updateById(sysUser1);
        return ReturnResult.ok();
    }

    @Override
    public ReturnResult<String> reset(Long userId) {
        SysUser sysUser1 = getById(userId);
        if (null == sysUser1) {
            return ReturnResult.illegal("用户信息不存在");
        }

        String password = RandomUtils.randomString(8);
        sysUser1.setSysUserSalt(RandomUtils.randomString(securityProperties.getSaltLength()));
        sysUser1.setSysUserPassword(DigestUtils.md5Hex(password + sysUser1.getSysUserSalt()));

        updateById(sysUser1);
        return ReturnResult.ok(password);
    }

    @Override
    public ReturnResult<SysUser> updateUser(Set<String> roles, Long userId, SysUserUpdate sysUser) {
        Integer sysUserId = sysUser.getSysUserId();
        SysUser sysUser1;
        if (null == sysUserId) {
            //更新自己账号
            sysUser1 = getById(userId);
        } else {
            //更新其他账号
            if (!AuthConstant.hasAdmin(roles)) {
                return ReturnResult.illegal("没有权限操作");
            }
            sysUser1 = getById(sysUserId);
        }

        BeanUtils.copyProperties(sysUser, sysUser1);

        if (StringUtils.isNotBlank(sysUser.getSysUserPassword())) {
            sysUser1.setSysUserSalt(RandomUtils.randomString(securityProperties.getSaltLength()));
            sysUser1.setSysUserPassword(DigestUtils.md5Hex(sysUser.getSysUserPassword() + sysUser1.getSysUserSalt()));
        }
        sysUser1.setSysUserUsername(null);
        sysUser1.setSysUserInSystem(null);
//        sysUser1.setUpdateTime(LocalDateTime.now());
        try {
            return ReturnResult.ok(transactionTemplate.execute(status -> {
                updateById(sysUser1);
                if(sysUser.isUpdateRole()) {
                    sysUserRoleService.remove(Wrappers.<SysUserRole>lambdaQuery().eq(SysUserRole::getSysUserId, sysUser1.getSysUserId()));
                }
                if (CollectionUtils.isNotEmpty(sysUser.getRoleIds())) {
                    sysUserRoleService.saveBatch(sysUser.getRoleIds().stream().map(role -> {
                        try {
                            SysUserRole rolsysUserRole = new SysUserRole();
                            rolsysUserRole.setSysUserId(sysUser1.getSysUserId());
                            rolsysUserRole.setSysRoleId(Integer.valueOf(role));
                            return rolsysUserRole;
                        } catch (NumberFormatException e) {
                            return null;
                        }
                    }).filter(Objects::nonNull).collect(Collectors.toList()));
                }
                return sysUser1;
            }));
        } finally {
            AuthClientExecute.getInstance().upgrade(null, UpgradeType.TIMESTAMP);
        }
    }

    @Override
    public ReturnResult<SysUser> saveUser(SysUserUpdate sysUserUpdate, HttpServletRequest request) {
        if(baseMapper.selectCount(Wrappers.<SysUser>lambdaQuery().eq(SysUser::getSysUserUsername, sysUserUpdate.getSysUserUsername())
        ) > 0L) {
            return ReturnResult.illegal("账号已存在");
        }
        sysUserUpdate.setSysUserSalt(RandomUtils.randomString(securityProperties.getSaltLength()));
        sysUserUpdate.setSysUserPassword(DigestUtils.md5Hex(sysUserUpdate.getSysUserPassword() + sysUserUpdate.getSysUserSalt()));
        sysUserUpdate.setSysUserInSystem(SystemConstant.IN_SYSTEM_NO);
        sysUserUpdate.setSysUserDelete(SystemConstant.NOT_DELETED);
        sysUserUpdate.setSysUserStatus(SystemConstant.USER_STATUS_ENABLED);
        sysUserUpdate.setSysUserRegisterIp(RequestUtils.getIpAddress(request));
        sysUserUpdate.setSysUserLastIp(RequestUtils.getIpAddress(request));
        if (org.apache.commons.lang3.StringUtils.isNotBlank(sysUserUpdate.getSysUserLastIp())) {
            ReturnResult<GeoCity> returnResult = iptablesService.transferAddress(sysUserUpdate.getSysUserLastIp());
            if (returnResult.isOk()) {
                GeoCity geoCity = returnResult.getData();
                sysUserUpdate.setSysUserLastAddress(geoCity.getProvince() + " " + geoCity.getCity());
            }
        }
        if (org.apache.commons.lang3.StringUtils.isNotBlank(sysUserUpdate.getSysUserRegisterIp())) {
            ReturnResult<GeoCity> returnResult = iptablesService.transferAddress(sysUserUpdate.getSysUserRegisterIp());
            if (returnResult.isOk()) {
                GeoCity geoCity = returnResult.getData();
                sysUserUpdate.setSysUserRegisterAddress(geoCity.getProvince() + " " + geoCity.getCity());
            }
        }
        return ReturnResult.ok(transactionTemplate.execute(status -> {
            save(sysUserUpdate);
            if (CollectionUtils.isNotEmpty(sysUserUpdate.getRoleIds())) {
                sysUserRoleService.saveBatch(sysUserUpdate.getRoleIds().stream().map(role -> {
                    try {
                        SysUserRole rolsysUserRole = new SysUserRole();
                        rolsysUserRole.setSysUserId(sysUserUpdate.getSysUserId());
                        rolsysUserRole.setSysRoleId(Integer.valueOf(role));
                        return rolsysUserRole;
                    } catch (NumberFormatException e) {
                        return null;
                    }
                }).filter(Objects::nonNull).collect(Collectors.toList()));
            }
            return sysUserUpdate;
        }));
    }

    @Override
    public ReturnResult<List<SysMenuVO>> getUserMenu(Set<String> roles) {
        return sysMenuService.list(new SysMenu(), roles, 3);
    }



    @Override
    public ReturnResult<SysUser> updateOwner(Set<String> roles, Long userId, SysUserPasswordUpdate sysUserPasswordUpdate) {
        SysUser sysUser = baseMapper.selectById(userId);
        if(null == sysUser) {
            return ReturnResult.illegal("当前账号信息异常，请重新登录或者刷新后重试！");
        }

        if(null != sysUser.getSysUserStatus() && sysUser.getSysUserStatus() == SystemConstant.USER_STATUS_DISABLED) {
            return ReturnResult.illegal("当前账号已禁用，请联系管理员！");
        }

        String oldPassword = sysUserPasswordUpdate.getOldPassword();
        if (StringUtils.isNotBlank(oldPassword)) {
            if (!DigestUtils.md5Hex(oldPassword + sysUser.getSysUserSalt()).equals(sysUser.getSysUserPassword())) {
                return ReturnResult.illegal("当前密码错误！");
            }
        } else {
            return ReturnResult.illegal("新密码不能为空！");
        }

        if (StringUtils.isNotBlank(sysUserPasswordUpdate.getNewPassword())) {
            sysUser.setSysUserSalt(RandomUtils.randomString(securityProperties.getSaltLength()));
            sysUser.setSysUserPassword(DigestUtils.md5Hex(sysUserPasswordUpdate.getNewPassword() + sysUser.getSysUserSalt()));
        } else {
            return ReturnResult.illegal("新密码不能为空！");
        }

        try {
            updateById(sysUser);
            return ReturnResult.ok(sysUser);
        } finally {
            // 刷新OAuth用户会话信息
            AuthClientExecute.getInstance().upgrade(null, UpgradeType.TIMESTAMP);
        }
    }

    @Override
    public UserLayout getUserLayout(Long userId, Set<String> roles) {
        SysUserLayout sysUserLayout = sysUserLayoutMapper.selectOne(Wrappers.<SysUserLayout>lambdaQuery().eq(SysUserLayout::getSysLayoutUserId, userId));
        UserLayout userLayout = new UserLayout();
        if(null != sysUserLayout) {
            userLayout.setLayout(sysUserLayout.getSysLayoutLayout());
            userLayout.setGrid(sysUserLayout.getSysLayoutGrid());
            userLayout.setComponent(sysUserLayout.getSysLayoutComponent());
            return userLayout;
        }
        return null;
    }

    @Override
    public UserLayout saveUserLayout(Long userId, UserLayout userLayout) {
        SysUserLayout sysUserLayout = sysUserLayoutMapper.selectOne(Wrappers.<SysUserLayout>lambdaQuery().eq(SysUserLayout::getSysLayoutUserId, userId));
        if(null == sysUserLayout) {
            sysUserLayout = new SysUserLayout();
        }
        sysUserLayout.setSysLayoutComponent(userLayout.getComponent());
        sysUserLayout.setSysLayoutGrid(userLayout.getGrid());
        sysUserLayout.setSysLayoutLayout(userLayout.getLayout());
        sysUserLayout.setSysLayoutUserId(Math.toIntExact(userId));

        if(null == sysUserLayout.getSysLayoutId()) {
            sysUserLayoutMapper.insert(sysUserLayout);
        } else {
            sysUserLayoutMapper.updateById(sysUserLayout);
        }

        return userLayout;
    }

    @Override
    public void exportUsers(SysUserQuery query, Set<String> roles, jakarta.servlet.http.HttpServletResponse response) {
        try {
            response.setContentType("text/csv;charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment;filename=users_" + System.currentTimeMillis() + ".csv");
            response.setCharacterEncoding("UTF-8");
            // BOM for Excel UTF-8
            response.getOutputStream().write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});
            
            java.io.PrintWriter writer = response.getWriter();
            // CSV头
            writer.println("用户名,昵称,邮箱,手机号,性别,部门,角色,状态,备注");
            
            // 查询数据
            query.setPageSize(securityProperties.getMaxExportCount()); // 限制最大导出数量
            IPage<SysUserVO> page = pageForUser(query, roles);
            
            for (SysUserVO user : page.getRecords()) {
                StringBuilder line = new StringBuilder();
                line.append(escapeCsv(user.getSysUserUsername())).append(",");
                line.append(escapeCsv(user.getSysUserNickname())).append(",");
                line.append(escapeCsv(user.getSysUserEmail())).append(",");
                line.append(escapeCsv(user.getSysUserPhone())).append(",");
                line.append(user.getSysUserSex() == null ? "" : (user.getSysUserSex() == 0 ? "女" : "男")).append(",");
                line.append("").append(",");
                String roleNames = user.getUserRoles() != null ? 
                        user.getUserRoles().stream().map(SysRole::getSysRoleName).collect(Collectors.joining(";")) : "";
                line.append(escapeCsv(roleNames)).append(",");
                line.append(user.getSysUserStatus() == null ? "" : (user.getSysUserStatus() == SystemConstant.USER_STATUS_ENABLED ? "启用" : "禁用")).append(",");
                line.append(escapeCsv(user.getSysUserRemark()));
                writer.println(line.toString());
            }
            writer.flush();
        } catch (Exception e) {
            log.error("导出用户失败", e);
            throw new RuntimeException("导出失败: " + e.getMessage());
        }
    }

    @Override
    public void downloadTemplate(jakarta.servlet.http.HttpServletResponse response) {
        try {
            response.setContentType("text/csv;charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment;filename=user_import_template.csv");
            response.setCharacterEncoding("UTF-8");
            // BOM for Excel UTF-8
            response.getOutputStream().write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});
            
            java.io.PrintWriter writer = response.getWriter();
            // CSV头
            writer.println("用户名(*),昵称(*),邮箱,手机号,性别(0男/1女),部门ID,角色编码(多个用;分隔),状态(0禁用/1启用),备注");
            // 示例数据
            writer.println("zhangsan,张三,zhangsan@example.com,13800138000,0,1,admin;user,1,测试用户");
            writer.flush();
        } catch (Exception e) {
            log.error("下载模板失败", e);
            throw new RuntimeException("下载模板失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<String> importUsers(org.springframework.web.multipart.MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return ReturnResult.illegal("请上传文件");
        }
        
        int successCount = 0;
        int failCount = 0;
        StringBuilder errors = new StringBuilder();
        
        try (java.io.BufferedReader reader = new java.io.BufferedReader(
                new java.io.InputStreamReader(file.getInputStream(), java.nio.charset.StandardCharsets.UTF_8))) {
            String line;
            int lineNum = 0;
            
            while ((line = reader.readLine()) != null) {
                lineNum++;
                // 跳过头行
                if (lineNum == 1) {
                    continue;
                }
                
                // 跳过空行
                if (line.trim().isEmpty()) {
                    continue;
                }
                
                try {
                    String[] fields = parseCsvLine(line);
                    if (fields.length < 2) {
                        errors.append("第").append(lineNum).append("行: 数据格式错误\n");
                        failCount++;
                        continue;
                    }
                    
                    String username = fields[0].trim();
                    String nickname = fields[1].trim();
                    
                    if (username.isEmpty() || nickname.isEmpty()) {
                        errors.append("第").append(lineNum).append("行: 用户名或昵称不能为空\n");
                        failCount++;
                        continue;
                    }
                    
                    // 检查用户名是否已存在
                    if (baseMapper.selectCount(Wrappers.<SysUser>lambdaQuery()
                            .eq(SysUser::getSysUserUsername, username)) > 0) {
                        errors.append("第").append(lineNum).append("行: 用户名").append(username).append("已存在\n");
                        failCount++;
                        continue;
                    }
                    
                    // 创建用户
                    SysUser sysUser = new SysUser();
                    sysUser.setSysUserUsername(username);
                    sysUser.setSysUserNickname(nickname);
                    sysUser.setSysUserEmail(fields.length > 2 ? fields[2].trim() : null);
                    sysUser.setSysUserPhone(fields.length > 3 ? fields[3].trim() : null);
                    sysUser.setSysUserSex(fields.length > 4 && !fields[4].trim().isEmpty() ? 
                            Integer.parseInt(fields[4].trim()) : 0);
                    sysUser.setSysDeptId(fields.length > 5 && !fields[5].trim().isEmpty() ? 
                            Integer.parseInt(fields[5].trim()) : null);
                    sysUser.setSysUserStatus(fields.length > 7 && !fields[7].trim().isEmpty() ? 
                            Integer.parseInt(fields[7].trim()) : SystemConstant.USER_STATUS_ENABLED);
                    sysUser.setSysUserRemark(fields.length > 8 ? fields[8].trim() : null);
                    
                    // 设置默认密码
                    sysUser.setSysUserSalt(RandomUtils.randomString(securityProperties.getSaltLength()));
                    sysUser.setSysUserPassword(DigestUtils.md5Hex(securityProperties.getDefaultImportPassword() + sysUser.getSysUserSalt()));
                    sysUser.setSysUserInSystem(SystemConstant.IN_SYSTEM_NO);
                    sysUser.setSysUserDelete(SystemConstant.NOT_DELETED);
                    
                    save(sysUser);
                    
                    // 处理角色
                    if (fields.length > 6 && !fields[6].trim().isEmpty()) {
                        String[] roleNames = fields[6].split(";");
                        for (String roleName : roleNames) {
                            SysRole role = sysRoleMapper.selectOne(Wrappers.<SysRole>lambdaQuery()
                                    .eq(SysRole::getSysRoleCode, roleName.trim()));
                            if (role != null) {
                                SysUserRole userRole = new SysUserRole();
                                userRole.setSysUserId(sysUser.getSysUserId());
                                userRole.setSysRoleId(role.getSysRoleId());
                                sysUserRoleService.save(userRole);
                            }
                        }
                    }
                    
                    successCount++;
                } catch (Exception e) {
                    errors.append("第").append(lineNum).append("行: ").append(e.getMessage()).append("\n");
                    failCount++;
                }
            }
        } catch (Exception e) {
            log.error("导入用户失败", e);
            return ReturnResult.error("导入失败: " + e.getMessage());
        }
        
        String result = "导入完成！成功: " + successCount + " 条，失败: " + failCount + " 条";
        if (errors.length() > 0) {
            result += "\n错误详情:\n" + errors.toString();
        }
        return ReturnResult.ok(result);
    }

    /**
     * CSV字段转义
     */
    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    /**
     * 解析CSV行
     */
    private String[] parseCsvLine(String line) {
        java.util.List<String> result = new java.util.ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                result.add(current.toString());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }
        result.add(current.toString());
        
        return result.toArray(new String[0]);
    }
}
