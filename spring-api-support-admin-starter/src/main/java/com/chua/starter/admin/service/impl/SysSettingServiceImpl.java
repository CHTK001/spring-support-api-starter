package com.chua.starter.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.function.Splitter;
import com.chua.common.support.json.Json;
import com.chua.common.support.lang.code.ErrorResult;
import com.chua.common.support.spi.ServiceProvider;
import com.chua.common.support.utils.*;
import com.chua.starter.admin.entity.SysSetting;
import com.chua.starter.admin.mapper.SysSettingMapper;
import com.chua.starter.admin.resolver.SettingValueResolver;
import com.chua.starter.admin.service.SysSettingService;
import com.chua.starter.common.support.codec.CodecFactory;
import com.chua.starter.common.support.constant.OperatorType;
import com.chua.starter.common.support.properties.ControlProperties;
import com.chua.starter.common.support.utils.StreamUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
@RequiredArgsConstructor
public class SysSettingServiceImpl extends ServiceImpl<SysSettingMapper, SysSetting> implements SysSettingService, ApplicationContextAware, InitializingBean {

    private ControlProperties controlProperties;

    final CodecFactory codecFactory;

    final SysSettingService sysSettingService;

    final StringRedisTemplate stringRedisTemplate;

    @Override
    public List<SysSetting> getSettings(String name) {
        List<SysSetting> result = new LinkedList<>();
        Set<String> strings = Splitter.on(",").trimResults().omitEmptyStrings().splitToSet(name);

        strings = findCache(result, strings);
        if(CollectionUtils.isEmpty(strings)) {
            return result;
        }


        List<SysSetting> sysSettings = baseMapper.selectList(Wrappers.<SysSetting>lambdaQuery()
                .in(SysSetting::getSettingName, strings)
                .eq(StringUtils.isNotBlank(controlProperties.getNo()), SysSetting::getSettingVersion, controlProperties.getNo())
        );
        if(CollectionUtils.isEmpty(sysSettings)) {
            return cacheSetting(strings, Collections.emptyList());
        }
        //混淆数据
        SysSetting sysSetting1 = new SysSetting();
        sysSetting1.setSettingDesc("codec-key");
        sysSetting1.setSettingValue(Hex.toHex(IdUtils.createTid().getBytes(StandardCharsets.UTF_8)));
        sysSetting1.setSettingName("codec-key");
        result.add(sysSetting1);
        result.addAll(sysSettings);

        return cacheSetting(strings, result);
    }

    private Set<String> findCache(List<SysSetting> result, Set<String> strings) {
        Set<String> less = new HashSet<>(strings.size());
        for (String string : strings) {
            String string1 = stringRedisTemplate.opsForValue().get("sys:setting:" + string);
            if(null != string1) {
                result.add(Json.fromJson(string1, SysSetting.class));
                continue;
            }

            less.add(string);
        }

        return less;
    }

    private List<SysSetting> cacheSetting(Set<String> name, List<SysSetting> objects) {
        if(objects.isEmpty()) {
            for (String string : name) {
                stringRedisTemplate.opsForValue().set("sys:setting:" + string, "{}");
            }
            return objects;
        }
        for (SysSetting item : objects) {
            stringRedisTemplate.opsForValue().set("sys:setting:" + item.getSettingName(), Json.toJson(item));
        }

        return objects;
    }

    @Override
    public boolean updateSetting(SysSetting sysSetting) {
        return saveSetting(sysSetting);
    }

    @Override
    public boolean deleteSetting(Integer settingId) {
        SysSetting sysSetting = getById(settingId);
        stringRedisTemplate.delete("sys:setting:" + sysSetting.getSettingName());
        return baseMapper.deleteById(settingId) > 0;
    }

    @Override
    public boolean saveSetting(SysSetting sysSetting) {
        sysSetting.setSettingVersion(controlProperties.getNo());
        SysSetting sysSetting1 = baseMapper.selectOne(Wrappers.<SysSetting>lambdaUpdate().eq(SysSetting::getSettingName, sysSetting.getSettingName())
                .eq(StringUtils.isNotBlank(controlProperties.getNo()), SysSetting::getSettingVersion, controlProperties.getNo()));
        int result = 0;
        if(null == sysSetting1) {
            result = baseMapper.insert(sysSetting);
            sysSetting1 = sysSetting;
        } else {
            result = baseMapper.update(sysSetting,
                    Wrappers.<SysSetting>lambdaUpdate().eq(SysSetting::getSettingName, sysSetting.getSettingName())
                            .eq(StringUtils.isNotBlank(controlProperties.getNo()), SysSetting::getSettingVersion, controlProperties.getNo())
            );
        }

        if(result > 0) {
            stringRedisTemplate.delete("sys:setting:" + sysSetting1.getSettingName());
            ServiceProvider.of(SettingValueResolver.class).getExtension(sysSetting1.getSettingName())
                    .resolve(Collections.singletonList(sysSetting));
            String settingValue = sysSetting.getSettingValue();
            return null != settingValue;
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
                    .eq(SysSetting::getSettingName, "response_codec_open")
                    .eq(StringUtils.isNotBlank(controlProperties.getNo()), SysSetting::getSettingVersion, controlProperties.getNo()));
            doCodec(list);
        });

    }

    private void doCodec(List<SysSetting> codecSetting) {
        if(codecSetting.size() != 2) {
            doRegister();
            return;
        }

        SysSetting sysSetting = StreamUtils.getFirst(codecSetting, SysSetting::getSettingName, OperatorType.EQ, "response_codec_open");
        if(null != sysSetting) {
            codecFactory.setEnable(Boolean.parseBoolean(sysSetting.getSettingValue()));
        }
    }

    private void doRegister() {
        SysSetting item1 = new SysSetting();
        item1.setSettingName("response_codec_open");
        item1.setSettingValue(codecFactory.isEnable() + "");
        item1.setSettingVersion(controlProperties.getNo());

        LambdaUpdateWrapper<SysSetting> eq = Wrappers.<SysSetting>lambdaUpdate()
                .eq(StringUtils.isNotBlank(controlProperties.getNo()), SysSetting::getSettingVersion, controlProperties.getNo())
                .eq(SysSetting::getSettingName, item1.getSettingName());

        if(sysSettingService.count(eq) == 0) {
            sysSettingService.save(item1);
            return;
        }
        sysSettingService.update(item1, eq);

    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        controlProperties = Binder.get(applicationContext.getEnvironment())
                .bindOrCreate(ControlProperties.PRE, ControlProperties.class);
    }
}
