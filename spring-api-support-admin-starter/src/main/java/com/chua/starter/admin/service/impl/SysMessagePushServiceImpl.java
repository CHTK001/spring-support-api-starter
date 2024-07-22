package com.chua.starter.admin.service.impl;

import com.chua.common.support.json.JsonObject;
import com.chua.common.support.lang.code.ErrorResult;
import com.chua.starter.admin.service.SysMessagePushService;
import com.chua.starter.admin.service.SysUserSettingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * sys消息推送服务impl
 *
 * @author CH
 */
@Service
@RequiredArgsConstructor
public class SysMessagePushServiceImpl implements SysMessagePushService {

    final SysUserSettingService sysUserSettingService;


    @Override
    public ErrorResult publish(String templateId, String[] userId, JsonObject param) {
        return null;
    }
}
