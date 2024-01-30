package com.chua.starter.admin.service;

import com.chua.starter.admin.entity.SysMessageTemplate;
import com.baomidou.mybatisplus.extension.service.IService;
public interface SysMessageTemplateService extends IService<SysMessageTemplate>{


    /**
     * 保存模板
     *
     * @param template 样板
     * @return {@link SysMessageTemplate}
     */
    SysMessageTemplate saveTemplate(SysMessageTemplate template);

    /**
     * 更新模板
     *
     * @param template 样板
     * @return {@link SysMessageTemplate}
     */
    SysMessageTemplate updateTemplate(SysMessageTemplate template);

    /**
     * 删除模板
     *
     * @param messageId 消息id
     * @return boolean
     */
    Boolean deleteTemplate(String messageId);
}
