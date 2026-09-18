package com.chua.starter.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.entity.system.SysMessageTemplate;
import com.chua.starter.mybatis.entity.Query;

import java.util.List;
import java.util.Map;

/**
 * 消息模板服务接口
 *
 * @since 4.0.0.50
 * @author CH
 */
public interface SysMessageTemplateService extends IService<SysMessageTemplate> {

    /**
     * 分页查询模板
     *
     * @param query     分页参数
     * @param template  查询条件
     * @return 模板分页
     */
    IPage<SysMessageTemplate> pageForTemplate(Query<SysMessageTemplate> query, SysMessageTemplate template);

    /**
     * 新增模板
     *
     * @param template 模板
     * @return 新增后的模板
     */
    ReturnResult<SysMessageTemplate> saveTemplate(SysMessageTemplate template);

    /**
     * 修改模板
     *
     * @param template 模板
     * @return 修改结果
     */
    ReturnResult<Boolean> updateTemplate(SysMessageTemplate template);

    /**
     * 批量删除模板
     *
     * @param ids 模板ID集合
     * @return 删除结果
     */
    ReturnResult<Boolean> deleteTemplate(List<Long> ids);

    /**
     * 根据模板编码渲染标题与内容(替换变量)
     *
     * @param templateCode 模板编码
     * @param variables    变量键值对
     * @return [渲染后标题, 渲染后内容], 模板不存在返回空
     */
    String[] renderByCode(String templateCode, Map<String, String> variables);
}
