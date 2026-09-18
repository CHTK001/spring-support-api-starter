package com.chua.starter.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.utils.ObjectUtils;
import com.chua.common.support.utils.StringUtils;
import com.chua.starter.common.support.logger.SysLoggerInfo;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.service.SysLogService;
import com.chua.starter.service.mapper.SysLogMapper;
import com.chua.starter.entity.service.SysLog;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;

/**
 * 系统日志服务实现类，提供系统日志的分页查询、删除和监听日志事件功能
 *
 * @since 2024/8/7
 * @author CH
 */
@Service
public class SysLogServiceImpl extends ServiceImpl<SysLogMapper, SysLog> implements SysLogService, ApplicationListener<SysLoggerInfo> {

    /**
     * 分页查询系统日志信息
     *
     * @param query  分页查询对象，用于指定分页参数和查询条件
     * 例如：新 查询<SysLog>().设置page(1).设置page大小(10)
     * @param sysLog 日志实体对象，用于设置查询条件
     * 例如：新 sys日志().设置sys日志用户名("admin").设置sys日志状态(1)
     * @param roles  用户角色集合，用于权限控制或查询特定角色的日志
     * 例如：设置.的("角色_ADMIN", "角色_用户")
     * @return 返回分页查询结果，包含日志列表和分页信息
     */
    @Override
    public IPage<SysLog> pageForLog(Query<SysLog> query, SysLog sysLog, Set<String> roles) {
        return baseMapper.selectPage(query.createPage(), Wrappers.<SysLog>lambdaQuery()
                .like(StringUtils.isNotBlank(sysLog.getSysLogUsername()), SysLog::getSysLogUsername, sysLog.getSysLogUsername())
                .like(StringUtils.isNotBlank(sysLog.getSysLogFrom()), SysLog::getSysLogFrom, sysLog.getSysLogFrom())
                .like(StringUtils.isNotBlank(sysLog.getSysLogAddress()), SysLog::getSysLogAddress, sysLog.getSysLogAddress())
                .eq(ObjectUtils.isNotEmpty(sysLog.getSysLogStatus()), SysLog::getSysLogStatus, sysLog.getSysLogStatus())
                .orderByDesc(SysLog::getCreateTime)
        );
    }

    /**
     * 删除指定的日志记录
     *
     * @param logId 日志记录的唯一标识
     *              例如：1L
     * @return 返回删除操作的结果，包含成功与否的标志和可能的错误信息
     */
    @Override
    public ReturnResult<Boolean> delete(Long logId) {
        if (baseMapper.deleteById(logId) > 0) {
            return ReturnResult.<Boolean>ok(Boolean.TRUE);
        }
        return ReturnResult.<Boolean>ok(Boolean.FALSE);
    }

    /** CSV 导出时间格式化 */
    private static final DateTimeFormatter EXPORT_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public byte[] exportLogs(SysLog sysLog, String startDate, String endDate) {
        List<SysLog> records = baseMapper.selectList(Wrappers.<SysLog>lambdaQuery()
                .like(StringUtils.isNotBlank(sysLog.getSysLogUsername()), SysLog::getSysLogUsername, sysLog.getSysLogUsername())
                .like(StringUtils.isNotBlank(sysLog.getSysLogFrom()), SysLog::getSysLogFrom, sysLog.getSysLogFrom())
                .like(StringUtils.isNotBlank(sysLog.getSysLogAddress()), SysLog::getSysLogAddress, sysLog.getSysLogAddress())
                .eq(ObjectUtils.isNotEmpty(sysLog.getSysLogStatus()), SysLog::getSysLogStatus, sysLog.getSysLogStatus())
                .ge(StringUtils.isNotBlank(startDate), SysLog::getCreateTime, startDate)
                .le(StringUtils.isNotBlank(endDate), SysLog::getCreateTime, endDate)
                .orderByDesc(SysLog::getCreateTime)
        );

        StringBuilder sb = new StringBuilder();
        sb.append("日志ID,账号,请求模块,请求IP,请求URL,浏览器,请求描述,请求参数,耗时(毫秒),状态,操作时间\r\n");
        for (SysLog log : records) {
            sb.append(csv(log.getSysLogId())).append(',')
                    .append(csv(log.getSysLogUsername())).append(',')
                    .append(csv(log.getSysLogFrom())).append(',')
                    .append(csv(log.getSysLogAddress())).append(',')
                    .append(csv(log.getSysLogUrl())).append(',')
                    .append(csv(log.getSysLogBrowser())).append(',')
                    .append(csv(log.getSysLogContent())).append(',')
                    .append(csv(log.getSysLogParam())).append(',')
                    .append(null == log.getSysLogCost() ? "" : log.getSysLogCost()).append(',')
                    .append(log.getSysLogStatus() == null ? "" : (log.getSysLogStatus() == 1 ? "成功" : "失败")).append(',')
                    .append(null == log.getCreateTime() ? "" : log.getCreateTime().format(EXPORT_TIME_FORMATTER)).append("\r\n");
        }
        return withUtf8Bom(sb.toString());
    }

    /**
     * CSV字段转义：含逗号/引号/换行时用双引号包裹并转义内部双引号
     *
     * @param value 原始值
     * @return 转义后的CSV字段
     */
    private String csv(Object value) {
        if (null == value) {
            return "";
        }
        String text = String.valueOf(value);
        if (text.contains(",") || text.contains("\"") || text.contains("\n") || text.contains("\r")) {
            return "\"" + text.replace("\"", "\"\"") + "\"";
        }
        return text;
    }

    /**
     * 给CSV内容前置UTF-8 BOM，便于Excel正确识别中文
     *
     * @param content CSV文本
     * @return 带BOM的字节数组
     */
    private byte[] withUtf8Bom(String content) {
        byte[] body = content.getBytes(StandardCharsets.UTF_8);
        byte[] bom = new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
        byte[] result = new byte[bom.length + body.length];
        System.arraycopy(bom, 0, result, 0, bom.length);
        System.arraycopy(body, 0, result, bom.length, body.length);
        return result;
    }

    /**
     * 监听系统日志事件，并将日志信息保存到数据库中
     *
     * @param event 系统日志事件对象，包含日志的相关信息
     * 例如：sys日志记录器信息.构建器().创建名称("admin").日志module("用户管理").日志cost(100L).构建()
     */
    @Override
    public void onApplicationEvent(SysLoggerInfo event) {
        SysLog sysLog = new SysLog();
        sysLog.setSysLogUsername(event.getCreateName());
        sysLog.setSysLogFrom(event.getLogModule());
        sysLog.setSysLogCost(event.getLogCost());
        sysLog.setSysLogAddress(event.getClientIp());
        sysLog.setSysLogParam(event.getLogParam());
        sysLog.setSysLogStatus(event.getLogStatus());
        sysLog.setSysLogUrl(event.getLogMapping());
        sysLog.setSysLogBrowser(event.getBrowser());
        sysLog.setSysLogContent(event.getLogContent());

        try {
            baseMapper.insert(sysLog);
        } catch (Exception ignored) {
        }
    }
}
