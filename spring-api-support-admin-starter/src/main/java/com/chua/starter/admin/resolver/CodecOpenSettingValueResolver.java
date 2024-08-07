package com.chua.starter.admin.resolver;

import com.chua.common.support.annotations.Spi;
import com.chua.common.support.objects.annotation.AutoInject;
import com.chua.starter.admin.entity.SysSetting;
import com.chua.starter.common.support.codec.CodecFactory;

import java.util.List;

/**
 * 值解析器
 * @author Administrator
 * @date 2024/01/18
 */
@Spi("response_codec_open")
public class CodecOpenSettingValueResolver implements SettingValueResolver{


    @AutoInject
    private CodecFactory codecFactory;

    @Override
    public void resolve(List<SysSetting> result) {
        if(result.size() != 1) {
            return;
        }

        SysSetting sysSetting1 = result.get(0);
        try {
            codecFactory.setEnable(Boolean.parseBoolean(sysSetting1.getSettingValue()));
        } catch (Exception ignored) {
        }
    }
}
