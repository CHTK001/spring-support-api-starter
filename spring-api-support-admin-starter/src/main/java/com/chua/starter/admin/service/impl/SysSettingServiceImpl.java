package com.chua.starter.admin.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.json.Json;
import com.chua.common.support.lang.code.ErrorResult;
import com.chua.common.support.matcher.PathMatcher;
import com.chua.common.support.utils.CollectionUtils;
import com.chua.common.support.utils.ThreadUtils;
import com.chua.starter.admin.entity.SysSetting;
import com.chua.starter.admin.mapper.SysSettingMapper;
import com.chua.starter.admin.service.SysSettingService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

@Service
public class SysSettingServiceImpl extends ServiceImpl<SysSettingMapper, SysSetting> implements SysSettingService, InitializingBean {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public List<SysSetting> getSettings(String name) {
        Set<Object> keys = stringRedisTemplate.opsForHash().keys("sys:setting");
        if(CollectionUtils.isEmpty(keys)) {
            List<SysSetting> sysSettings = baseMapper.selectList(Wrappers.<SysSetting>lambdaQuery().like(SysSetting::getSettingName, name));
            if(CollectionUtils.isEmpty(sysSettings)) {
                return null;
            }
            SysSetting sysSetting = sysSettings.get(0);
            updateSetting(sysSetting);
            return sysSettings;
        }

        List<SysSetting> sysSetting = new LinkedList<>();
        for (Object key : keys) {
            if(!name.equals(key) && !PathMatcher.INSTANCE.match(name, key.toString())) {
                continue;
            }
            Object o = stringRedisTemplate.opsForHash().get("sys:setting", key);
            SysSetting sysSetting1 = Json.fromJson(o.toString(), SysSetting.class);
            if(null == sysSetting1) {
                continue;
            }

            sysSetting.add(sysSetting1);
        }


        return sysSetting;
    }

    @Override
    public boolean updateSetting(SysSetting sysSetting) {
        return saveSetting(sysSetting);
    }

    @Override
    public boolean deleteSetting(Integer settingId) {
        SysSetting sysSetting = baseMapper.selectById(settingId);
        if(null == settingId) {
            return false;
        }

        stringRedisTemplate.opsForHash().delete("sys:setting", sysSetting.getSettingName());
        return false;
    }

    @Override
    public boolean saveSetting(SysSetting sysSetting) {
        SysSetting sysSetting1 = baseMapper.selectOne(Wrappers.<SysSetting>lambdaUpdate().eq(SysSetting::getSettingName, sysSetting.getSettingName()));
        int result = 0;
        if(null == sysSetting1) {
            result = baseMapper.insert(sysSetting);
        } else {
            result = baseMapper.update(sysSetting, Wrappers.<SysSetting>lambdaUpdate().eq(SysSetting::getSettingName, sysSetting.getSettingName()));
        }
        if(result > 0) {
            String settingValue = sysSetting.getSettingValue();
            if(null == settingValue) {
                return false;
            }
            stringRedisTemplate.opsForHash().put("sys:setting", sysSetting.getSettingName(), Json.toJson(sysSetting));
            return true;
        }
        return false;
    }

    @Override
    public ErrorResult batchSetting(List<SysSetting> sysSettings) {
        ErrorResult result = new ErrorResult();
        for (SysSetting sysSetting : sysSettings) {
            boolean saveSetting = saveSetting(sysSetting);
            if(!saveSetting) {
                result.addError("{}保存失败", sysSetting.getSettingName());
            }
            continue;
        }
        return result;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        ThreadUtils.newStaticThreadPool().execute(() -> {
            List<SysSetting> list = list();
            for (SysSetting sysSetting : list) {
                stringRedisTemplate.opsForHash().put("sys:setting", sysSetting.getSettingName(), Json.toJson(sysSetting));
            }
        });
    }
}
