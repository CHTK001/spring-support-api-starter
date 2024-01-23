package com.chua.starter.admin.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.lang.code.ErrorResult;
import com.chua.common.support.spi.ServiceProvider;
import com.chua.common.support.utils.*;
import com.chua.starter.admin.entity.SysSetting;
import com.chua.starter.admin.mapper.SysSettingMapper;
import com.chua.starter.admin.resolver.SettingValueResolver;
import com.chua.starter.admin.service.SysSettingService;
import com.chua.starter.common.support.constant.OperatorType;
import com.chua.starter.common.support.properties.VersionProperties;
import com.chua.starter.common.support.provider.CodecProvider;
import com.chua.starter.common.support.utils.StreamUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

@Service
public class SysSettingServiceImpl extends ServiceImpl<SysSettingMapper, SysSetting> implements SysSettingService, ApplicationContextAware, InitializingBean {

    private VersionProperties versionProperties;

    @Resource
    private CodecProvider codecProvider;

    @Resource
    private SysSettingService sysSettingService;

    @Override
    public List<SysSetting> getSettings(String name) {
        List<SysSetting> sysSettings = baseMapper.selectList(Wrappers.<SysSetting>lambdaQuery()
                .like(SysSetting::getSettingName, name)
                .eq(StringUtils.isNotBlank(versionProperties.getNo()), SysSetting::getSettingVersion, versionProperties.getNo())
        );
        if(CollectionUtils.isEmpty(sysSettings)) {
            return Collections.emptyList();
        }
        //混淆数据
        SysSetting sysSetting1 = new SysSetting();
        sysSetting1.setSettingDesc("codec-key");
        sysSetting1.setSettingValue(Hex.toHex(IdUtils.createTid().getBytes(StandardCharsets.UTF_8)));
        sysSetting1.setSettingName("codec-key");
        sysSettings.add(sysSetting1);
        return sysSettings;
    }

    @Override
    public boolean updateSetting(SysSetting sysSetting) {
        return saveSetting(sysSetting);
    }

    @Override
    public boolean deleteSetting(Integer settingId) {
        return baseMapper.deleteById(settingId) > 0;
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
            ServiceProvider.of(SettingValueResolver.class).getExtension(sysSetting1.getSettingName())
                    .resolve(Collections.singletonList(sysSetting));
            String settingValue = sysSetting.getSettingValue();
            if(null == settingValue) {
                return false;
            }
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
            List<SysSetting> list = list(Wrappers.<SysSetting>lambdaQuery()
                    .eq(SysSetting::getSettingName, "codec_open")
                    .eq(StringUtils.isNotBlank(versionProperties.getNo()), SysSetting::getSettingVersion, versionProperties.getNo()));
            doCodec(list);
        });

    }

    private void doCodec(List<SysSetting> codecSetting) {
        if(codecSetting.size() != 2) {
            doRegister();
            return;
        }

        SysSetting sysSetting = StreamUtils.getFirst(codecSetting, SysSetting::getSettingName, OperatorType.EQ, "codec_open");
        if(null != sysSetting) {
            codecProvider.setEnable(Boolean.parseBoolean(sysSetting.getSettingValue()));
        }
    }

    private void doRegister() {
        SysSetting item1 = new SysSetting();
        item1.setSettingName("codec_open");
        item1.setSettingValue(codecProvider.isEnable() + "");
        item1.setSettingVersion(versionProperties.getNo());

        sysSettingService.saveOrUpdate(item1, Wrappers.<SysSetting>lambdaQuery()
                .eq(StringUtils.isNotBlank(versionProperties.getNo()), SysSetting::getSettingVersion, versionProperties.getNo())
                .eq(SysSetting::getSettingName, item1.getSettingName())
        );

    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        versionProperties = Binder.get(applicationContext.getEnvironment())
                .bindOrCreate(VersionProperties.PRE, VersionProperties.class);
    }
}
