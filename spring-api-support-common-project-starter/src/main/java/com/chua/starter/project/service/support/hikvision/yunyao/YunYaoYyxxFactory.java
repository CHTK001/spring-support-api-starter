package com.chua.starter.project.service.support.hikvision.yunyao;

import com.chua.starter.project.service.support.hikvision.HikvisionClient;
import com.chua.starter.project.service.support.hikvision.adaptor.sub.YyxxFactory;
import com.chua.starter.project.service.support.hikvision.adaptor.sub.level3.RyjzpFactory;
import com.chua.starter.system.entity.SysProject;
import lombok.extern.slf4j.Slf4j;

/**
 * 海康云曜
 *
 * @author CH
 */
@Slf4j
public class YunYaoYyxxFactory extends YyxxFactory {
    private final SysProject factory;
    private final String token;

    public YunYaoYyxxFactory(HikvisionClient hikvisionClient, SysProject factory, String token) {
        super(hikvisionClient);
        this.factory = factory;
        this.token = token;
    }

    @Override
    public RyjzpFactory getPeoplePictureFactory() {
        return new YunYaoRyjzpFactory(hikvisionClient, factory, token);
    }
}
