package com.chua.starter.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.entity.system.SysMessageLog;
import com.chua.starter.mybatis.entity.Query;

import java.time.LocalDateTime;

/**
 * 消息推送日志服务接口
 *
 * @since 4.0.0.50
 * @author CH
 */
public interface SysMessageLogService {

    /**
     * 分页查询推送日志
     *
     * @param query 分页参数
     * @param log   查询条件(接收人/返回码/提供商/已读状态)
     * @return 日志分页
     */
    IPage<SysMessageLog> pageForLog(Query<SysMessageLog> query, SysMessageLog log);

    /**
     * 清理指定时间之前的推送日志
     *
     * @param beforeTime 截止时间，不可为空
     * @return 删除条数
     */
    ReturnResult<Integer> cleanLog(LocalDateTime beforeTime);
}
