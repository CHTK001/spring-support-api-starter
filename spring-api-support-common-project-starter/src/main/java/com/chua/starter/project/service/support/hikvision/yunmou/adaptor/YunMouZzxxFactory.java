package com.chua.starter.project.service.support.hikvision.yunmou.adaptor;

import com.chua.starter.project.service.support.hikvision.HikvisionClient;
import com.chua.starter.project.service.support.hikvision.adaptor.sub.level3.ZzxxFactory;
import com.chua.starter.project.entity.SysProject;

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
