package com.chua.starter.monitor.service.spider;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chua.common.support.lang.code.ReturnPageResult;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.entity.MonitorSpiderTask;

import java.util.List;
import java.util.Map;

/**
 * 爬虫任务服务接口
 *
 * @author CH
 * @since 2024/12/25
 */
public interface MonitorSpiderTaskService extends IService<MonitorSpiderTask> {

    /**
     * 预览网页内容
     *
     * @param url 目标URL
     * @return 网页内容
     */
    ReturnResult<Map<String, Object>> previewUrl(String url);

    /**
     * 解析HTML结构
     *
     * @param url 目标URL
     * @return HTML结构树
     */
    ReturnResult<Map<String, Object>> parseHtml(String url);

    /**
     * 测试选择器
     *
     * @param url      目标URL
     * @param selector 选择器表达式
     * @param type     选择器类型
     * @return 提取结果
     */
    ReturnResult<List<String>> testSelector(String url, String selector, String type);

    /**
     * 生成XPath
     *
     * @param url         目标URL
     * @param elementPath 元素路径
     * @return XPath表达式
     */
    ReturnResult<String> generateXpath(String url, String elementPath);

    /**
     * 立即运行爬虫任务
     *
     * @param taskId 任务ID
     * @return 运行结果
     */
    ReturnResult<Boolean> runSpiderTask(Integer taskId);

    /**
     * 停止爬虫任务
     *
     * @param taskId 任务ID
     * @return 停止结果
     */
    ReturnResult<Boolean> stopSpiderTask(Integer taskId);

    /**
     * 获取爬虫任务运行状态
     *
     * @param taskId 任务ID
     * @return 运行状态
     */
    ReturnResult<Map<String, Object>> getSpiderTaskStatus(Integer taskId);

    /**
     * 获取爬虫任务日志
     *
     * @param taskId 任务ID
     * @param page   页码
     * @param size   每页大小
     * @return 日志列表
     */
    ReturnPageResult<Map<String, Object>> getSpiderTaskLogs(Integer taskId, Integer page, Integer size);

    /**
     * 获取爬取的数据
     *
     * @param taskId 任务ID
     * @param page   页码
     * @param size   每页大小
     * @return 爬取的数据
     */
    ReturnPageResult<Map<String, Object>> getSpiderTaskData(Integer taskId, Integer page, Integer size);

    /**
     * 导出爬取的数据
     *
     * @param taskId 任务ID
     * @param format 导出格式
     * @return 导出文件路径
     */
    ReturnResult<String> exportSpiderTaskData(Integer taskId, String format);

    /**
     * 清空爬取的数据
     *
     * @param taskId 任务ID
     * @return 清空结果
     */
    ReturnResult<Boolean> clearSpiderTaskData(Integer taskId);

    /**
     * 获取爬虫任务统计信息
     *
     * @return 统计信息
     */
    ReturnResult<Map<String, Object>> getSpiderTaskStatistics();

    /**
     * 删除单条爬取数据
     *
     * @param dataId 数据ID
     * @return 删除结果
     */
    ReturnResult<Boolean> deleteSpiderData(Long dataId);

    /**
     * 切换任务状态（启用/停用）
     *
     * @param taskId 任务ID
     * @return 切换结果
     */
    ReturnResult<Boolean> toggleTaskStatus(Integer taskId);
}
