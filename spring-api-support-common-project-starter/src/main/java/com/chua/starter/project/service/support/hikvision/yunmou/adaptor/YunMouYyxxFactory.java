package com.chua.starter.project.service.support.hikvision.yunmou.adaptor;

import com.chua.starter.project.service.support.hikvision.HikvisionClient;
import com.chua.starter.project.service.support.hikvision.adaptor.sub.YyxxFactory;
import com.chua.starter.project.service.support.hikvision.adaptor.sub.level3.RyjzpFactory;
import com.chua.starter.project.service.support.hikvision.adaptor.sub.level3.ZzxxFactory;
import com.chua.starter.entity.project.SysProject;

/**
 * 云眸
 *
 * @author CH
 */
public class YunMouYyxxFactory extends YyxxFactory {
    private final SysProject sysProject;

    public YunMouYyxxFactory(HikvisionClient client, SysProject sysProject) {
        super(client);
        this.sysProject = sysProject;
    }

    @Override
    public ZzxxFactory getOrgInfoFactory() {
        return new YunMouZzxxFactory(hikvisionClient, sysProject);
    }

    @Override
    public RyjzpFactory getPeoplePictureFactory() {
        return new YunMouRyjzpFactory(hikvisionClient, sysProject);
    }
}
