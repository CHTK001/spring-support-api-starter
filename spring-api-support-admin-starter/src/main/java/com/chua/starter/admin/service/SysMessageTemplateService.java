package com.chua.starter.admin.service;

import com.chua.starter.admin.entity.SysMessageTemplate;
import com.baomidou.mybatisplus.extension.service.IService;
public interface SysMessageTemplateService extends IService<SysMessageTemplate>{


    /**
     * 删除数据
     *
     * @param messageId 消息id
     * @return boolean
     */
    boolean delete(String messageId);
}
