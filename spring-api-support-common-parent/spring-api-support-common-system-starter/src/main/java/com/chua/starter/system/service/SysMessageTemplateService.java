package com.chua.starter.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.entity.system.SysMessageTemplate;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.system.pojo.message.TemplatePreviewRequest;
import com.chua.starter.system.pojo.message.TemplatePreviewResponse;

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
     * 列出启用中的模板(下拉用)
     *
     * @return 模板列表
     */
    List<SysMessageTemplate> listForTemplate();

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
     * 用给定变量渲染模板标题与内容
     *
     * @param request 预览请求(模板ID + 变量)
     * @return 渲染结果，含未填充占位符
     */
    ReturnResult<TemplatePreviewResponse> previewTemplate(TemplatePreviewRequest request);

    /**
     * 按模板编码渲染标题与内容(替换变量)
     *
     * @param templateCode 模板编码
     * @param variables    变量键值对
     * @return [渲染后标题, 渲染后内容], 模板不存在返回空
     */
    String[] renderByCode(String templateCode, Map<String, String> variables);

    /**
     * 渲染 {key} 占位符
     *
     * @param text   原始文本，可为空
     * @param params 变量键值对，值按字符串替换
     * @return 渲染后的文本
     */
    String render(String text, Map<String, ?> params);
}
