package com.chua.starter.project.service.support.hikvision.yunyao;

import com.chua.starter.project.service.support.hikvision.HikvisionClient;
import com.chua.starter.project.service.support.hikvision.adaptor.ResourceFactory;
import com.chua.starter.project.service.support.hikvision.adaptor.sub.YyxxFactory;
import com.chua.starter.project.service.support.hikvision.adaptor.sub.ZyxxFactory;
import com.chua.starter.project.entity.SysProject;
import lombok.extern.slf4j.Slf4j;

/**
 * 海康云曜资源
 *
 * @author CH
 */
@Slf4j
public class YunYaoResourceFactory extends ResourceFactory {
    private final SysProject factory;
    private final String token;

    public YunYaoResourceFactory(HikvisionClient hikvisionClient, SysProject factory, String token) {
        super(hikvisionClient);
        this.factory = factory;
        this.token = token;
    }

    @Override
    public ZyxxFactory getZyxxFactory() {
        return new YunYaoZyxxFactory(hikvisionClient, factory, token);
    }


    @Override
    public YyxxFactory getPeopleFactory() {
        return new YunYaoYyxxFactory(hikvisionClient, factory, token);
    }
}
