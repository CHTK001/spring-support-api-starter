package com.chua.starter.monitor.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chua.common.support.lang.code.ReturnPageResult;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.entity.SoftServiceLog;
import com.chua.starter.mybatis.entity.Query;

import java.util.List;

/**
 * 软件服务日志服务接口
 *
 * @author CH
 * @since 2025/6/5
 */
public interface SoftServiceLogService extends IService<SoftServiceLog> {

    /**
     * 分页查询日志列表
     *
     * @param query          查询参数
     * @param softServiceLog 查询条件
     * @return 分页日志列表
     */
    ReturnPageResult<SoftServiceLog> pageForLogs(Query<SoftServiceLog> query, SoftServiceLog softServiceLog);

    /**
     * 根据安装ID查询日志列表
     *
     * @param installId 安装ID
     * @return 日志列表
     */
    ReturnResult<List<SoftServiceLog>> getLogsByInstallId(Integer installId);

    /**
     * 根据安装ID和操作类型查询日志
     *
     * @param installId     安装ID
     * @param operationType 操作类型
     * @return 日志内容
     */
    ReturnResult<String> getLogContent(Integer installId, String operationType);

    /**
     * 根据安装ID查询日志文件列表
     *
     * @param installId 安装ID
     * @return 日志文件列表
     */
    ReturnResult<List<String>> getLogFilesByInstallId(Integer installId);

    /**
     * 下载日志文件
     *
     * @param installId     安装ID
     * @param operationType 操作类型
     * @return 日志文件字节数组
     */
    ReturnResult<byte[]> downloadLogFile(Integer installId, String operationType);

    /**
     * 保存日志
     *
     * @param softServiceLog 日志信息
     * @return 操作结果
     */
    ReturnResult<Boolean> saveLog(SoftServiceLog softServiceLog);

    /**
     * 更新日志
     *
     * @param softServiceLog 日志信息
     * @return 操作结果
     */
    ReturnResult<Boolean> updateLog(SoftServiceLog softServiceLog);

    /**
     * 删除日志
     *
     * @param logId 日志ID
     * @return 操作结果
     */
    ReturnResult<Boolean> removeLogById(Integer logId);

    /**
     * 清理指定安装ID的日志
     *
     * @param installId 安装ID
     * @return 操作结果
     */
    ReturnResult<Boolean> cleanLogsByInstallId(Integer installId);

    /**
     * 追加日志内容到现有日志文件
     *
     * @param installId     安装ID
     * @param operationType 操作类型
     * @param content       日志内容
     * @return 操作结果
     */
    ReturnResult<Boolean> appendLogContent(Integer installId, String operationType, String content);
}
