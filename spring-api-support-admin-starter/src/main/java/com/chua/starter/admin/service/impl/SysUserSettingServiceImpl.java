package com.chua.starter.admin.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.function.Splitter;
import com.chua.common.support.json.Json;
import com.chua.common.support.json.JsonArray;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.utils.CollectionUtils;
import com.chua.common.support.utils.StringUtils;
import com.chua.starter.admin.entity.SysUserSetting;
import com.chua.starter.admin.mapper.SysUserGridMapper;
import com.chua.starter.admin.service.SysUserSettingService;
import com.chua.starter.common.support.constant.Constant;
import com.chua.starter.common.support.result.Result;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 *    
 * @author CH
 */     
@Service
public class SysUserSettingServiceImpl extends ServiceImpl<SysUserGridMapper, SysUserSetting> implements SysUserSettingService, Constant {

    @Override
    @CacheEvict(cacheManager = REDIS_CACHE, cacheNames = REDIS_CACHE, key = "'grid' + #sysUserGrid.userId")
    public boolean saveGrid(SysUserSetting sysUserGrid) {
        List<SysUserSetting> sysUserGrids = baseMapper.selectList(Wrappers.<SysUserSetting>lambdaQuery().eq(SysUserSetting::getUserId, sysUserGrid.getUserId()));
        if(sysUserGrids.isEmpty()) {
            return baseMapper.insert(sysUserGrid) == 1;
        }
        for (SysUserSetting userGrid : sysUserGrids) {
            BeanUtils.copyProperties(sysUserGrid, userGrid, "grid");
            baseMapper.updateById(userGrid);
        }
        return true;
    }

    @Override
    @Cacheable(cacheManager = REDIS_CACHE, cacheNames = REDIS_CACHE, key = "'grid' + #userId")
    public SysUserSetting getGridByUserId(String userId) {
        return baseMapper.selectOne(Wrappers.<SysUserSetting>lambdaQuery().eq(SysUserSetting::getUserId, userId));
    }

    @Override
    public ReturnResult<Boolean> saveUserSetting(SysUserSetting sysUserSetting, String userDashboardGrid, Long userId, String username, Set<String> roles) {
        sysUserSetting.setUserId(userId);
        if (!ADMIN.equalsIgnoreCase(username) && !roles.contains(ADMIN)) {
            if (StringUtils.isEmpty(userDashboardGrid)) {
                return Result.illegal("当前账号没有权限配置");
            }
            List<String> strings = Splitter.on(",").trimResults().omitEmptyStrings().splitToList(userDashboardGrid);
            String copmsList = sysUserSetting.getCopmsList();
            JsonArray jsonArray = Json.getJsonArray(copmsList);
            for (int i = 0; i < jsonArray.size(); i++) {
                if (CollectionUtils.notContains(strings, (Collection<String>) jsonArray.get(i))) {
                    return Result.illegal("当前账号没有权限添加其它组件");
                }
            }

        }
        return Result.success(saveGrid(sysUserSetting));
    }
}
