package com.chua.starter.admin.service.impl;

import com.chua.starter.admin.service.SysMessagePushService;
import com.chua.starter.admin.service.SysUserSettingService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * sys消息推送服务impl
 *
 * @author CH
 */
@Service
public class SysMessagePushServiceImpl implements SysMessagePushService {

    @Resource
    private SysUserSettingService sysUserSettingService;


}
