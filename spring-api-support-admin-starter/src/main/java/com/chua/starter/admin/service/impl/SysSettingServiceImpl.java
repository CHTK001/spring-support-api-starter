package com.chua.starter.admin.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.json.Json;
import com.chua.common.support.lang.code.ErrorResult;
import com.chua.common.support.matcher.PathMatcher;
import com.chua.common.support.spi.ServiceProvider;
import com.chua.common.support.utils.CollectionUtils;
import com.chua.common.support.utils.StringUtils;
import com.chua.common.support.utils.ThreadUtils;
import com.chua.starter.admin.entity.SysSetting;
import com.chua.starter.admin.mapper.SysSettingMapper;
import com.chua.starter.admin.resolver.SettingValueResolver;
import com.chua.starter.admin.service.SysSettingService;
import com.chua.starter.common.support.properties.CodecProperties;
import com.chua.starter.common.support.properties.VersionProperties;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
public class SysSettingServiceImpl extends ServiceImpl<SysSettingMapper, SysSetting> implements SysSettingService, ApplicationContextAware, InitializingBean {

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    private VersionProperties versionProperties;

    @Resource
    private CodecProperties codecProperties;

    @Resource
    private SysSettingService sysSettingService;

    @Override
    public List<SysSetting> getSettings(String name) {
        Set<Object> keys = stringRedisTemplate.opsForHash().keys("sys:setting");
        if(CollectionUtils.isEmpty(keys)) {
            List<SysSetting> sysSettings = baseMapper.selectList(Wrappers.<SysSetting>lambdaQuery()
                    .like(SysSetting::getSettingName, name)
                    .eq(StringUtils.isNotBlank(versionProperties.getNo()), SysSetting::getSettingVersion, versionProperties.getNo())
            );
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

            ServiceProvider.of(SettingValueResolver.class).getExtension(sysSetting1.getSettingName())
                            .resolve(sysSetting);
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
        if(null == settingId && !Objects.equals(sysSetting.getSettingVersion(), versionProperties.getNo())) {
            return false;
        }

        stringRedisTemplate.opsForHash().delete("sys:setting", sysSetting.getSettingName());
        return false;
    }

    @Override
    public boolean saveSetting(SysSetting sysSetting) {
        sysSetting.setSettingVersion(versionProperties.getNo());
        SysSetting sysSetting1 = baseMapper.selectOne(Wrappers.<SysSetting>lambdaUpdate().eq(SysSetting::getSettingName, sysSetting.getSettingName())
                .eq(StringUtils.isNotBlank(versionProperties.getNo()), SysSetting::getSettingVersion, versionProperties.getNo()));
        int result = 0;
        if(null == sysSetting1) {
            result = baseMapper.insert(sysSetting);
        } else {
            result = baseMapper.update(sysSetting,
                    Wrappers.<SysSetting>lambdaUpdate().eq(SysSetting::getSettingName, sysSetting.getSettingName())
                            .eq(StringUtils.isNotBlank(versionProperties.getNo()), SysSetting::getSettingVersion, versionProperties.getNo())
            );
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
            SysSetting item1 = new SysSetting();
            item1.setSettingName("codec_open");
            item1.setSettingValue(codecProperties.isEnable() + "");
            item1.setSettingVersion(versionProperties.getNo());

            sysSettingService.saveOrUpdate(item1, Wrappers.<SysSetting>lambdaQuery()
                    .eq(StringUtils.isNotBlank(versionProperties.getNo()), SysSetting::getSettingVersion, versionProperties.getNo())
                    .eq(SysSetting::getSettingName, item1.getSettingName())
            );

            SysSetting item2 = new SysSetting();
            item2.setSettingName("codec_key");
            item2.setSettingValue(codecProperties.getPublicKey());
            item2.setSettingVersion(versionProperties.getNo());

            sysSettingService.saveOrUpdate(item2, Wrappers.<SysSetting>lambdaQuery()
                    .eq(StringUtils.isNotBlank(versionProperties.getNo()), SysSetting::getSettingVersion, versionProperties.getNo())
                    .eq(SysSetting::getSettingName, item2.getSettingName())
            );
            List<SysSetting> list = list(Wrappers.<SysSetting>lambdaQuery()
                    .eq(StringUtils.isNotBlank(versionProperties.getNo()), SysSetting::getSettingVersion, versionProperties.getNo()));
            for (SysSetting sysSetting : list) {
                stringRedisTemplate.opsForHash().put("sys:setting", sysSetting.getSettingName(), Json.toJson(sysSetting));
            }
        });



    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        versionProperties = Binder.get(applicationContext.getEnvironment())
                .bindOrCreate(VersionProperties.PRE, VersionProperties.class);
    }
}
