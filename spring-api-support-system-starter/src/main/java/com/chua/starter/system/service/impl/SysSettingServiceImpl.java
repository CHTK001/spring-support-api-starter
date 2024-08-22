package com.chua.starter.system.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.function.Splitter;
import com.chua.common.support.lang.code.ReturnResult;
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
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

import static com.chua.starter.system.constant.SystemConstant.CACHE_WITH_REDIS;

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
    @Cacheable(cacheManager = CACHE_WITH_REDIS, cacheNames = CACHE_WITH_REDIS, key = "'cache:sys:setting:id:' + #sysSettingUpdate.sysSettingGroup")
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
        boolean b = baseMapper.updateById(sysSetting) > 0;
        if(sysSetting.getSysSettingStatus() == 1) {
            GlobalSettingFactory.getInstance().set(sysSetting.getSysSettingGroup(), sysSetting.getSysSettingName(), sysSetting.getSysSettingValue());
        }
        sysSettingService.clearCache(sysSetting);
        return ReturnResult.of(b);
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

    /**
     * 清理缓存
     * @param sysSetting 配置
     * @return 是否成功
     */
    @Override
    public SysSetting clearCache(SysSetting sysSetting) {
        return sysSetting;
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        GlobalSettingFactory globalSettingFactory = GlobalSettingFactory.getInstance();
        globalSettingFactory.register("default", new GuestDefaultSetting());
        globalSettingFactory.register("codec", new CodecSetting());
        upgrade();
    }

    private void upgrade() {
        GlobalSettingFactory globalSettingFactory = GlobalSettingFactory.getInstance();
        ThreadUtils.newStaticThreadPool().execute(() -> {
            List<SysSetting> list = list(Wrappers.<SysSetting>lambdaQuery()
                    .eq(SysSetting::getSysSettingStatus, 1)
            );
            for (SysSetting sysSetting : list) {
                globalSettingFactory.set(sysSetting.getSysSettingGroup(), sysSetting.getSysSettingName(), sysSetting.getSysSettingValue());
            }
        });
    }
}
