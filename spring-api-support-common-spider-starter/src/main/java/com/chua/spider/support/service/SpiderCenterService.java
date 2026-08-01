package com.chua.spider.support.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chua.spider.support.dto.SpiderDefinitionSaveRequest;
import com.chua.spider.support.vo.SpiderDefinitionVo;

/**
 * 爬虫中心服务接口。
 *
 * <p>定义爬虫定义、采集数据、执行记录的分页查询与增删改能力。</p>
 *
 * @author CH
 * @since 4.0.0.42
 */
public interface SpiderCenterService {

    /**
     * 分页查询爬虫定义。
     *
     * @param pageNo    页码
     * @param pageSize  每页大小
     * @param keyword   关键词（匹配编码或名称）
     * @return 分页结果
     */
    Page pageDefinitions(int pageNo, int pageSize, String keyword);

    /**
     * 查询爬虫定义详情。
     *
     * @param spiderCode 爬虫编码
     * @return 爬虫定义视图
     */
    SpiderDefinitionVo getDefinition(String spiderCode);

    /**
     * 保存爬虫定义（新增或更新）。
     *
     * @param req 保存请求
     * @return 保存后的爬虫定义视图
     */
    SpiderDefinitionVo saveDefinition(SpiderDefinitionSaveRequest req);

    /**
     * 删除爬虫定义。
     *
     * @param spiderCode 爬虫编码
     * @return 是否删除成功
     */
    boolean deleteDefinition(String spiderCode);

    /**
     * 切换启停状态。
     *
     * @param spiderCode  爬虫编码
     * @param newStatus   新状态 0/1
     * @return 是否切换成功
     */
    boolean toggleStatus(String spiderCode, Integer newStatus);

    /**
     * 分页查询采集数据。
     *
     * @param pageNo               页码
     * @param pageSize             每页大小
     * @param spiderDefinitionCode 爬虫编码
     * @param spiderExecutionNo    执行批次号（可为空）
     * @param depth                深度过滤（可为空）
     * @return 分页结果
     */
    Page pageDataRecords(int pageNo, int pageSize, String spiderDefinitionCode, String spiderExecutionNo, Integer depth);

    /**
     * 分页查询执行记录。
     *
     * @param pageNo               页码
     * @param pageSize             每页大小
     * @param spiderDefinitionCode 爬虫编码
     * @return 分页结果
     */
    Page pageExecutions(int pageNo, int pageSize, String spiderDefinitionCode);
}
