package com.chua.starter.system.service.support.hikvision.yunmou.adaptor;

import com.chua.starter.system.entity.SysProject;
import com.chua.starter.system.service.support.hikvision.HikvisionClient;
import com.chua.starter.system.service.support.hikvision.adaptor.sub.level3.ZzxxFactory;

/**
 * 云眸
 *
 * @author CH
 */
public class YunMouZzxxFactory extends ZzxxFactory {
    private final SysProject sysProject;

    public YunMouZzxxFactory(HikvisionClient hikvisionClient, SysProject sysProject) {
        super(hikvisionClient);
        this.sysProject = sysProject;
    }


}
