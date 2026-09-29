package com.chua.starter.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.utils.ObjectUtils;
import com.chua.common.support.utils.StringUtils;
import com.chua.starter.common.support.logger.SysLoggerInfo;
import com.chua.starter.common.support.utils.RequestUtils;
import com.chua.starter.oauth.client.support.execute.AuthSessionUtils;
import jakarta.servlet.http.HttpServletRequest;
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
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SysLogServiceImpl.class);

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

    /**
     * CSV导出时间格式化
     */
    private static final DateTimeFormatter EXPORT_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    /**
     * 建表中定长 255 的列宽
     */
    private static final int COLUMN_WIDTH_255 = 255;
    /**
     * 建表中定长 512 的列宽（原始浏览器 User-Agent）
     */
    private static final int COLUMN_WIDTH_512 = 512;
    /**
     * 建表中定长 64 的列宽（浏览器指纹）
     */
    private static final int COLUMN_WIDTH_64 = 64;
    /**
     * 建表中定长 2000 的列宽（请求描述）
     */
    private static final int COLUMN_WIDTH_2000 = 2000;
    /**
     * 定长列写入前的占位值
     */
    private static final String UNKNOWN = "UNKNOWN";

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
        log.info("[SYSLOG] event received: name={}, module={}, status={}", event.getCreateName(), event.getLogModule(), event.getLogStatus());
        SysLog sysLog = new SysLog();
        sysLog.setSysLogUsername(fitColumn(resolveUsername(event), COLUMN_WIDTH_255));
        sysLog.setSysLogFrom(fitColumn(event.getLogModule(), COLUMN_WIDTH_255));
        sysLog.setSysLogCost(event.getLogCost());
        sysLog.setSysLogAddress(fitColumn(event.getClientIp(), COLUMN_WIDTH_255));
        sysLog.setSysLogParam(fitColumn(event.getLogParam(), COLUMN_WIDTH_255));
        sysLog.setSysLogStatus(event.getLogStatus());
        sysLog.setSysLogUrl(fitColumn(resolvePath(event), COLUMN_WIDTH_255));
        sysLog.setSysLogBrowser(fitColumn(StringUtils.isBlank(event.getBrowser()) ? UNKNOWN : event.getBrowser(), COLUMN_WIDTH_255));
        sysLog.setSysLogSystem(fitColumn(StringUtils.isBlank(event.getSystem()) ? UNKNOWN : event.getSystem(), COLUMN_WIDTH_255));
        sysLog.setSysLogUa(fitColumn(event.getUa(), COLUMN_WIDTH_512));
        sysLog.setSysLogFingerprint(fitColumn(event.getFingerprint(), COLUMN_WIDTH_64));
        sysLog.setSysLogContent(fitColumn(StringUtils.isBlank(event.getLogContent()) ? event.getLogName() : event.getLogContent(),
                COLUMN_WIDTH_2000));

        try {
            baseMapper.insert(sysLog);
            log.info("[SYSLOG] insert OK id={}", sysLog.getSysLogId());
        } catch (Exception e) {
            log.error("[SYSLOG] insert failed", e);
        }
    }

    /**
     * 按建表声明的列宽裁剪写入值
     *
     * <p>{@code sys_log} 的定长列（255/512/64/2000）一旦超长，H2 与 MySQL 都会让整条 INSERT 失败，
     * 导致操作日志整体丢失。请求体等字段的长度不受调用方控制（例如登录报文常超过 255），
     * 因此写库前统一按列宽截断，宁可丢尾部也不丢整条审计记录。</p>
     *
     * @param value     原始值
     * @param maxLength 建表声明的列宽
     * @return 裁剪后的值；{@code value} 为 null 或未超长时原样返回
     */
    private String fitColumn(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }

    /**
     * 解析操作人：依次回退 事件createName -> 请求头/属性 -> 会话 -> Servlet principal
     *
     * @param event 日志事件
     * @return 操作人用户名，可能为 null
     */
    private String resolveUsername(SysLoggerInfo event) {
        String username = event.getCreateName();
        if (StringUtils.isNotBlank(username)) {
            return username;
        }
        username = RequestUtils.getUsername();
        if (StringUtils.isNotBlank(username)) {
            return username;
        }
        username = AuthSessionUtils.getUsername();
        if (StringUtils.isNotBlank(username)) {
            return username;
        }
        try {
            HttpServletRequest req = RequestUtils.getRequest();
            if (req != null) {
                if (StringUtils.isNotBlank(req.getRemoteUser())) {
                    return req.getRemoteUser();
                }
                if (req.getUserPrincipal() != null && StringUtils.isNotBlank(req.getUserPrincipal().getName())) {
                    return req.getUserPrincipal().getName();
                }
            }
        } catch (Exception ignored) {
        }
        return username;
    }

    /**
     * 解析请求地址：仅保留接口 path（去除协议/主机/端口及 context-path）
     *
     * @param event 日志事件
     * @return 接口路径
     */
    private String resolvePath(SysLoggerInfo event) {
        try {
            HttpServletRequest req = RequestUtils.getRequest();
            if (req != null) {
                String uri = req.getRequestURI();
                String ctx = req.getContextPath();
                if (uri != null && ctx != null && !ctx.isEmpty() && uri.startsWith(ctx)) {
                    uri = uri.substring(ctx.length());
                }
                if (uri != null && !uri.isEmpty()) {
                    return uri;
                }
            }
        } catch (Exception ignored) {
        }
        String mapping = event.getLogMapping();
        if (mapping != null && mapping.startsWith("http")) {
            try {
                return java.net.URI.create(mapping).getRawPath();
            } catch (Exception ignored) {
            }
        }
        return mapping;
    }
}
