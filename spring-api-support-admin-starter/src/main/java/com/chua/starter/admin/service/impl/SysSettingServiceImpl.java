package com.chua.starter.admin.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.json.Json;
import com.chua.common.support.utils.CollectionUtils;
import com.chua.starter.admin.entity.SysSetting;
import com.chua.starter.admin.mapper.SysSettingMapper;
import com.chua.starter.admin.service.SysSettingService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class SysSettingServiceImpl extends ServiceImpl<SysSettingMapper, SysSetting> implements SysSettingService{

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public SysSetting getSetting(String name) {
        Object o = stringRedisTemplate.opsForHash().get("sys:setting", name);
        if(null != o) {
            return Json.fromJson(o.toString(), SysSetting.class);
        }

        List<SysSetting> sysSettings = baseMapper.selectList(Wrappers.<SysSetting>lambdaQuery().eq(SysSetting::getSettingName, name));
        if(CollectionUtils.isEmpty(sysSettings)) {
            return null;
        }
        SysSetting sysSetting = sysSettings.get(0);
        updateSetting(sysSetting);
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
        int result = baseMapper.updateById(sysSetting);
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
}
