package com.chua.starter.system.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.function.Splitter;
import com.chua.common.support.json.Json;
import com.chua.common.support.json.JsonObject;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.utils.StringUtils;
import com.chua.common.support.utils.ThreadUtils;
import com.chua.starter.common.support.application.GlobalSettingFactory;
import com.chua.starter.common.support.codec.CodecSetting;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.oauth.client.support.contants.AuthConstant;
import com.chua.starter.system.entity.SysSetting;
import com.chua.starter.system.mapper.SysSettingMapper;
import com.chua.starter.system.pojo.setting.SysSettingUpdate;
import com.chua.starter.system.service.SysSettingService;
import com.chua.starter.system.setting.GuestDefaultSetting;
import com.chua.starter.system.setting.SmtpSetting;
import com.chua.starter.system.setting.VersionSetting;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static com.chua.starter.common.support.constant.CacheConstant.REDIS_CACHE_ALWAYS;

/**
 *
 * @since 2024/8/7
 * @author CH    
 */
@Service
public class SysSettingServiceImpl extends ServiceImpl<SysSettingMapper, SysSetting> implements SysSettingService, InitializingBean {

    @Autowired
    private SysSettingService sysSettingService;

    @Override
    @Cacheable(cacheManager = REDIS_CACHE_ALWAYS, cacheNames = REDIS_CACHE_ALWAYS, key = "'cache:sys:setting:id:' + #sysSettingUpdate.sysSettingGroup")
    public List<SysSetting> list(SysSettingUpdate sysSettingUpdate, Set<String> roles) {
        return baseMapper.selectList(Wrappers.<SysSetting>lambdaQuery()
                .in(StringUtils.isNotBlank(sysSettingUpdate.getSysSettingGroup()), SysSetting::getSysSettingGroup, Splitter.on(",").omitEmptyStrings().trimResults().splitToSet(sysSettingUpdate.getSysSettingGroup()))
        );
    }

    @Override
    public ReturnResult<Boolean> delete(Long settingId) {
        if(null == settingId) {
            return ReturnResult.fail("配置ID不能为空");
        }

        SysSetting sysSetting = baseMapper.selectById(settingId);
        if(null == sysSetting) {
            return ReturnResult.fail("配置不存在, 请刷新后重试!");
        }

        Integer sysRoleInSystem = sysSetting.getSysSettingInSystem();

        if(null != sysRoleInSystem && sysRoleInSystem == 1) {
            return ReturnResult.fail("内置配置不允许删除");
        }

        int b = baseMapper.deleteById(settingId);
        sysSettingService.clearCache(sysSetting);
        return ReturnResult.of(b);
    }

    @Override
    public ReturnResult<Boolean> update(Set<String> roles, SysSetting sysSetting) {
        SysSetting sysSetting1 = baseMapper.selectById(sysSetting.getSysSettingId());
        Integer sysRoleInSystem = sysSetting1.getSysSettingInSystem();
        if(!AuthConstant.isSuperAdmin(roles)) {
            if(null != sysRoleInSystem && sysRoleInSystem == 1) {
                return ReturnResult.fail("内置配置不允许修改");
            }
            sysSetting.setSysSettingAppInner(null);
        }

        String sysSettingName = sysSetting.getSysSettingName();
        sysSetting.setSysSettingInSystem(null);
        if(StringUtils.isNotBlank(sysSettingName)) {
            SysSetting sysSetting2 = baseMapper.selectOne(Wrappers.<SysSetting>lambdaQuery()
                    .eq(SysSetting::getSysSettingName, sysSettingName)
                    .eq(SysSetting::getSysSettingGroup, sysSetting.getSysSettingGroup())
                    .ne(SysSetting::getSysSettingId, sysSetting.getSysSettingId()))
                    ;
            if(null != sysSetting2) {
                return ReturnResult.fail("配置编码已存在");
            }
        }
        SysSetting version = baseMapper.selectOne(Wrappers.<SysSetting>lambdaQuery().eq(SysSetting::getSysSettingName, "version"));
        version.setSysSettingConfig(UUID.randomUUID().toString());
        List<SysSetting> sysSettings = new LinkedList<>();
        sysSettings.add(sysSetting);
        sysSettings.add(version);
        sysSettingService.updateBatchById(sysSettings);

        if(null != sysSetting.getSysSettingStatus() && sysSetting.getSysSettingStatus() == 1) {
            GlobalSettingFactory.getInstance().set(sysSetting.getSysSettingGroup(), sysSetting.getSysSettingName(), sysSetting.getSysSettingValue());
            upgradeVersion(version);
        }
        sysSettingService.clearCache(sysSetting);
        return ReturnResult.of(sysSetting);
    }

    @Override
    public ReturnResult<SysSetting> saveForEntity(SysSetting sysSetting, Set<String> roles) {
        sysSetting.setSysSettingInSystem(0);
        if(!AuthConstant.isSuperAdmin(roles)) {
            sysSetting.setSysSettingAppInner(0);
        }

        if(null == sysSetting.getSysSettingStatus()) {
            sysSetting.setSysSettingStatus(1);
        }
        SysSetting setting = baseMapper.selectOne(Wrappers.<SysSetting>lambdaQuery()
                .eq(SysSetting::getSysSettingName, sysSetting.getSysSettingName())
                .eq(SysSetting::getSysSettingGroup, sysSetting.getSysSettingGroup()));
        if(null != setting) {
            return ReturnResult.fail("配置编码已存在");
        }

        if(baseMapper.insert(sysSetting) > 0) {
            if(sysSetting.getSysSettingStatus() == 1) {
                GlobalSettingFactory.getInstance().set(sysSetting.getSysSettingGroup(), sysSetting.getSysSettingName(), sysSetting.getSysSettingValue());
            }
            return ReturnResult.of(sysSettingService.clearCache(sysSetting));
        }
        return ReturnResult.illegal("新增失败");
    }

    @Override
    public IPage<SysSetting> pageForSetting(Query<SysSetting> query, SysSetting sysSetting, Set<String> roles) {
        return baseMapper.selectPage(query.createPage(), Wrappers.<SysSetting>lambdaQuery()
                .like(StringUtils.isNotBlank(sysSetting.getSysSettingGroup()), SysSetting::getSysSettingGroup, sysSetting.getSysSettingGroup())
                .like(StringUtils.isNotBlank(sysSetting.getSysSettingName()), SysSetting::getSysSettingName, sysSetting.getSysSettingName())
                .like(StringUtils.isNotBlank(sysSetting.getSysSettingRemark()), SysSetting::getSysSettingRemark, sysSetting.getSysSettingRemark())
                .orderByDesc(SysSetting::getCreateTime)
        );
    }

    @Override
    public ReturnResult<Boolean> updateBatch(Set<String> roles, List<SysSetting> sysSetting) {
        SysSetting version = baseMapper.selectOne(Wrappers.<SysSetting>lambdaQuery().eq(SysSetting::getSysSettingName, "version"));
        version.setSysSettingConfig(UUID.randomUUID().toString());
        sysSetting.add(version);
        sysSettingService.updateBatchById(sysSetting);
        for (SysSetting setting : sysSetting) {
            sysSettingService.clearCache(setting);
        }
        register(GlobalSettingFactory.getInstance(), sysSetting);
        return ReturnResult.of(true);
    }

    private void upgradeVersion(SysSetting version) {
        GlobalSettingFactory.getInstance().set("config", "version", version.getSysSettingVersion());
    }

    /**
     * 清理缓存
     * @param sysSetting 配置
     * @return 是否成功
     */
    @Override
    @CacheEvict(cacheManager = REDIS_CACHE_ALWAYS, cacheNames = REDIS_CACHE_ALWAYS, key = "'cache:sys:setting:id:' + #sysSetting.sysSettingGroup")
    public SysSetting clearCache(SysSetting sysSetting) {
        return sysSetting;
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        GlobalSettingFactory globalSettingFactory = GlobalSettingFactory.getInstance();
        globalSettingFactory.register("config", new GuestDefaultSetting());
        globalSettingFactory.register("config", new CodecSetting());
        globalSettingFactory.register("config", new VersionSetting());
        globalSettingFactory.register("smtp", new SmtpSetting());
        upgrade();
    }

    private void upgrade() {
        GlobalSettingFactory globalSettingFactory = GlobalSettingFactory.getInstance();
        ThreadUtils.newStaticThreadPool().execute(() -> {
            List<SysSetting> list = list(Wrappers.<SysSetting>lambdaQuery()
                    .eq(SysSetting::getSysSettingStatus, 1)
            );
            register(globalSettingFactory, list);
        });
    }

    /**
     * 注册配置
     * @param globalSettingFactory 配置工厂
     * @param list 配置列表
     */
    private void register(GlobalSettingFactory globalSettingFactory, List<SysSetting> list) {
        Map<String, List<SysSetting>> collect = list.stream().collect(Collectors.groupingBy(SysSetting::getSysSettingGroup));
        for (Map.Entry<String, List<SysSetting>> entry : collect.entrySet()) {
            register(globalSettingFactory, entry.getKey(), entry.getValue());
        }
    }

    /**
     * 注册配置
     * @param globalSettingFactory 配置工厂
     * @param key 配置组
     * @param value 配置
     */
    private void register(GlobalSettingFactory globalSettingFactory, String key, List<SysSetting> value) {
        Map<String, Object> map = value.stream().collect(Collectors.toMap(SysSetting::getSysSettingName, SysSetting::getSysSettingValue));
        globalSettingFactory.register(key, map);
    }

    /**
     * 注册配置
     * @param globalSettingFactory 配置工厂
     * @param sysSetting 配置
     */
    private void register(GlobalSettingFactory globalSettingFactory, SysSetting sysSetting) {
        if(sysSetting.getSysSettingCollect() == 0) {
            globalSettingFactory.set(sysSetting.getSysSettingGroup(), sysSetting.getSysSettingName(), sysSetting.getSysSettingValue());
            return;
        }

        String sysSettingValue = sysSetting.getSysSettingValue();
        if(StringUtils.isEmpty(sysSettingValue)) {
            return;
        }
        JsonObject jsonObject = Json.getJsonObject(sysSettingValue);
        globalSettingFactory.set(sysSetting.getSysSettingGroup(), jsonObject);

    }
}
