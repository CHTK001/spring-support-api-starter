package com.chua.starter.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.entity.system.SysErrorLog;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.system.pojo.error.ErrorReportV1Request;

import java.util.List;
import java.util.Map;

/**
* 前端异常上报日志服务
*
* @since 2026/8/8
* @author CH
 */
public interface SysErrorLogService extends IService<SysErrorLog> {

    /**
    * 批量保存前端上报的异常记录
    *
    * @param request 前端异常上报请求
    * @return 是否保存成功
     */
    ReturnResult<Boolean> saveReport(ErrorReportV1Request request);

    /**
    * 查询异常日志列表
    *
    * @param source 异常来源(可空)
    * @param limit  最大条数
    * @return 异常日志列表
     */
    List<SysErrorLog> listRecent(String source, int limit);

    /**
    * 分页查询异常日志
    *
    * @param query       分页参数
    * @param sysErrorLog 查询条件
    * @return 异常日志分页结果
     */
    IPage<SysErrorLog> pageForLog(Query<SysErrorLog> query, SysErrorLog sysErrorLog);

    /**
    * 异常日志统计
    *
    * <p>返回总数、今日新增、按来源分组统计、按错误Key分组的Top5异常。</p>
    *
    * @return 统计结果Map
     */
    Map<String, Object> statistic();

    /**
    * 批量删除异常日志
    *
    * @param sysErrorLogIds 异常日志ID集合
    * @return 删除结果
     */
    ReturnResult<Boolean> deleteByIds(List<Long> sysErrorLogIds);

    /**
    * 查询单条错误日志详情(含完整堆栈)
    *
    * @param sysErrorLogId 日志ID
    * @return 错误日志
     */
    SysErrorLog getDetail(Long sysErrorLogId);

    /**
    * 按异常类型统计数量
    *
    * @param startTime 起始时间(毫秒, 可空)
    * @param endTime   结束时间(毫秒, 可空)
    * @return 统计列表: [{type, count}]
     */
    List<Map<String, Object>> statisticBySource(Long startTime, Long endTime);

    /**
    * 清理指定天数前的错误日志
    *
    * @param days 保留最近 days 天
    * @return 删除数量
     */
    int cleanBefore(int days);

    /**
    * 更新异常日志处理状态
    *
    * @param sysErrorLogId 异常日志ID
    * @param status       状态: 0-未处理 1-已处理 2-已忽略
    * @return 更新结果
     */
    ReturnResult<Boolean> updateStatus(Long sysErrorLogId, Integer status);
}
