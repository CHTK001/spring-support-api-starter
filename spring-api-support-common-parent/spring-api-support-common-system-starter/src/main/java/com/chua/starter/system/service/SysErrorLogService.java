package com.chua.starter.system.service;

import com.baomidou.mybatisplus.spring.service.IService;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.entity.system.SysErrorLog;
import com.chua.starter.system.pojo.error.ErrorReportV1Request;

import java.util.List;

/**
 * 前端异常上报日志服务
 *
 * @author CH
 * @since 2026/8/8
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
}
