package com.chua.starter.monitor.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.lang.code.ReturnPageResult;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.entity.SoftServiceLog;
import com.chua.starter.monitor.mapper.SoftServiceLogMapper;
import com.chua.starter.monitor.service.SoftServiceLogService;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.mybatis.utils.ReturnPageResultUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 软件服务日志服务实现类
 * 只处理文件系统中的日志，不使用数据库表
 *
 * @author CH
 * @since 2025/6/5
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SoftServiceLogServiceImpl extends ServiceImpl<SoftServiceLogMapper, SoftServiceLog> implements SoftServiceLogService {

    private static final String LOG_DIR_PATH = "/soft/install/";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    public ReturnPageResult<SoftServiceLog> pageForLogs(Query<SoftServiceLog> query, SoftServiceLog softServiceLog) {
        try {
            // 由于我们不再使用数据库，这里将返回文件系统中的日志信息
            List<SoftServiceLog> logs = new ArrayList<>();

            // 如果指定了installId，则只获取该installId的日志
            if (softServiceLog != null && softServiceLog.getSoftServiceLogInstallId() != null) {
                Integer installId = softServiceLog.getSoftServiceLogInstallId();
                String installLogDir = LOG_DIR_PATH + installId + "/";
                File logDir = new File(installLogDir);

                if (logDir.exists() && logDir.isDirectory()) {
                    File[] logFiles = logDir.listFiles(file -> file.isFile() && file.getName().endsWith(".log"));
                    if (logFiles != null) {
                        for (File logFile : logFiles) {
                            SoftServiceLog log = createLogFromFile(installId, logFile);

                            // 根据查询条件过滤
                            if (matchesFilter(log, softServiceLog)) {
                                logs.add(log);
                            }
                        }
                    }
                }
            } else {
                // 遍历所有installId目录
                File baseDir = new File(LOG_DIR_PATH);
                if (baseDir.exists() && baseDir.isDirectory()) {
                    File[] installDirs = baseDir.listFiles(File::isDirectory);
                    if (installDirs != null) {
                        for (File installDir : installDirs) {
                            try {
                                Integer installId = Integer.parseInt(installDir.getName());
                                File[] logFiles = installDir.listFiles(file -> file.isFile() && file.getName().endsWith(".log"));
                                if (logFiles != null) {
                                    for (File logFile : logFiles) {
                                        SoftServiceLog log = createLogFromFile(installId, logFile);

                                        // 根据查询条件过滤
                                        if (matchesFilter(log, softServiceLog)) {
                                            logs.add(log);
                                        }
                                    }
                                }
                            } catch (NumberFormatException e) {
                                // 忽略非数字目录名
                                log.warn("忽略非法安装ID目录: {}", installDir.getName());
                            }
                        }
                    }
                }
            }

            // 创建一个虚拟的分页结果
            int total = logs.size();
            int pageSize = query.getSize();
            int pageNum = query.getPage();
            int startIndex = (pageNum - 1) * pageSize;
            int endIndex = Math.min(startIndex + pageSize, total);

            // 分页处理
            List<SoftServiceLog> pagedLogs;
            if (startIndex < total) {
                pagedLogs = logs.subList(startIndex, endIndex);
            } else {
                pagedLogs = new ArrayList<>();
            }

            // 创建分页结果
            IPage<SoftServiceLog> page = query.createFullPage();
            page.setRecords(pagedLogs);
            page.setTotal(total);

            return ReturnPageResultUtils.ok(page);
        } catch (Exception e) {
            log.error("分页查询日志列表失败: {}", e.getMessage(), e);
            return ReturnPageResult.error("分页查询日志列表失败: " + e.getMessage());
        }
    }

    /**
     * 从日志文件创建日志对象
     *
     * @param installId 安装ID
     * @param logFile   日志文件
     * @return 日志对象
     */
    private SoftServiceLog createLogFromFile(Integer installId, File logFile) {
        SoftServiceLog log = new SoftServiceLog();
        log.setSoftServiceLogInstallId(installId);
        log.setSoftServiceLogFilePath(logFile.getAbsolutePath());

        // 从文件名解析操作类型 (如 install.log, start.log 等)
        String fileName = logFile.getName();
        String operationType = fileName.substring(0, fileName.lastIndexOf("."));
        log.setSoftServiceLogOperationType(operationType);

        // 设置日志时间为文件最后修改时间
        log.setSoftServiceLogTime(logFile.lastModified());

        // 设置日志文件大小
        log.setSoftServiceLogFileSize(logFile.length());

        // 尝试解析文件内容以确定状态
        try {
            String content = new String(Files.readAllBytes(logFile.toPath()));
            // 简单判断状态
            if (content.contains("成功") || content.contains("SUCCESS")) {
                log.setSoftServiceLogStatus(1); // 成功
            } else if (content.contains("失败") || content.contains("ERROR") || content.contains("FAILURE")) {
                log.setSoftServiceLogStatus(2); // 失败
            } else {
                log.setSoftServiceLogStatus(0); // 进行中
            }

            // 设置日志内容预览
            if (content.length() > 200) {
                log.setSoftServiceLogContent(content.substring(0, 200) + "...");
            } else {
                log.setSoftServiceLogContent(content);
            }
        } catch (IOException e) {
            log.error("读取日志文件内容失败: {}", e.getMessage());
            log.setSoftServiceLogStatus(0); // 默认进行中
            log.setSoftServiceLogContent("无法读取日志内容");
        }

        return log;
    }

    /**
     * 检查日志是否符合过滤条件
     *
     * @param log    日志对象
     * @param filter 过滤条件
     * @return 是否符合过滤条件
     */
    private boolean matchesFilter(SoftServiceLog log, SoftServiceLog filter) {
        if (filter == null) {
            return true;
        }

        if (filter.getSoftServiceLogInstallId() != null &&
                !filter.getSoftServiceLogInstallId().equals(log.getSoftServiceLogInstallId())) {
            return false;
        }

        if (filter.getSoftServiceLogServiceId() != null &&
                !filter.getSoftServiceLogServiceId().equals(log.getSoftServiceLogServiceId())) {
            return false;
        }

        if (StringUtils.hasText(filter.getSoftServiceLogSshId()) &&
                !filter.getSoftServiceLogSshId().equals(log.getSoftServiceLogSshId())) {
            return false;
        }

        if (filter.getSoftServiceLogType() != null &&
                !filter.getSoftServiceLogType().equals(log.getSoftServiceLogType())) {
            return false;
        }

        if (filter.getSoftServiceLogStatus() != null &&
                !filter.getSoftServiceLogStatus().equals(log.getSoftServiceLogStatus())) {
            return false;
        }

        return !StringUtils.hasText(filter.getSoftServiceLogOperationType()) ||
                filter.getSoftServiceLogOperationType().equals(log.getSoftServiceLogOperationType());
    }

    @Override
    public ReturnResult<List<SoftServiceLog>> getLogsByInstallId(Integer installId) {
        if (installId == null) {
            return ReturnResult.illegal("安装ID不能为空");
        }

        try {
            List<SoftServiceLog> logs = new ArrayList<>();
            String installLogDir = LOG_DIR_PATH + installId + "/";
            File logDir = new File(installLogDir);

            if (logDir.exists() && logDir.isDirectory()) {
                File[] logFiles = logDir.listFiles(file -> file.isFile() && file.getName().endsWith(".log"));
                if (logFiles != null) {
                    for (File logFile : logFiles) {
                        SoftServiceLog log = createLogFromFile(installId, logFile);
                        logs.add(log);
                    }
                }
            }

            return ReturnResult.success(logs);
        } catch (Exception e) {
            log.error("查询安装ID为{}的日志列表失败: {}", installId, e.getMessage(), e);
            return ReturnResult.error("查询日志列表失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<String> getLogContent(Integer installId, String operationType) {
        if (installId == null) {
            return ReturnResult.illegal("安装ID不能为空");
        }

        if (!StringUtils.hasText(operationType)) {
            return ReturnResult.illegal("操作类型不能为空");
        }

        try {
            String logFilePath = getLogFilePath(installId, operationType);
            File logFile = new File(logFilePath);

            if (!logFile.exists() || !logFile.isFile()) {
                return ReturnResult.illegal("日志文件不存在: " + logFilePath);
            }

            String logContent = new String(Files.readAllBytes(Paths.get(logFilePath)));
            return ReturnResult.success(logContent);
        } catch (Exception e) {
            log.error("获取日志内容失败: {}", e.getMessage(), e);
            return ReturnResult.error("获取日志内容失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<List<String>> getLogFilesByInstallId(Integer installId) {
        if (installId == null) {
            return ReturnResult.illegal("安装ID不能为空");
        }

        try {
            String installLogDir = LOG_DIR_PATH + installId + "/";
            File logDir = new File(installLogDir);

            if (!logDir.exists() || !logDir.isDirectory()) {
                return ReturnResult.success(new ArrayList<>());
            }

            File[] logFiles = logDir.listFiles(file -> file.isFile() && file.getName().endsWith(".log"));
            if (logFiles == null || logFiles.length == 0) {
                return ReturnResult.success(new ArrayList<>());
            }

            List<String> logFileList = new ArrayList<>();
            for (File logFile : logFiles) {
                logFileList.add(logFile.getName());
            }

            return ReturnResult.success(logFileList);
        } catch (Exception e) {
            log.error("获取安装ID为{}的日志文件列表失败: {}", installId, e.getMessage(), e);
            return ReturnResult.error("获取日志文件列表失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<byte[]> downloadLogFile(Integer installId, String operationType) {
        if (installId == null) {
            return ReturnResult.illegal("安装ID不能为空");
        }

        if (!StringUtils.hasText(operationType)) {
            return ReturnResult.illegal("操作类型不能为空");
        }

        try {
            String logFilePath = getLogFilePath(installId, operationType);
            File logFile = new File(logFilePath);

            if (!logFile.exists() || !logFile.isFile()) {
                return ReturnResult.illegal("日志文件不存在: " + logFilePath);
            }

            byte[] logFileContent = Files.readAllBytes(Paths.get(logFilePath));
            return ReturnResult.success(logFileContent);
        } catch (Exception e) {
            log.error("下载日志文件失败: {}", e.getMessage(), e);
            return ReturnResult.error("下载日志文件失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Boolean> saveLog(SoftServiceLog softServiceLog) {
        if (softServiceLog == null) {
            return ReturnResult.illegal("日志信息不能为空");
        }

        if (softServiceLog.getSoftServiceLogInstallId() == null) {
            return ReturnResult.illegal("安装ID不能为空");
        }

        if (!StringUtils.hasText(softServiceLog.getSoftServiceLogOperationType())) {
            return ReturnResult.illegal("操作类型不能为空");
        }

        try {
            // 将日志保存到文件系统
            Integer installId = softServiceLog.getSoftServiceLogInstallId();
            String operationType = softServiceLog.getSoftServiceLogOperationType();
            String content = softServiceLog.getSoftServiceLogContent();

            // 保存日志内容到文件
            saveLogToFile(installId, operationType, content);

            return ReturnResult.success(true);
        } catch (Exception e) {
            log.error("保存日志失败: {}", e.getMessage(), e);
            return ReturnResult.error("保存日志失败: " + e.getMessage());
        }
    }

    /**
     * 保存日志到文件
     *
     * @param installId     安装ID
     * @param operationType 操作类型
     * @param content       日志内容
     * @throws IOException IO异常
     */
    public void saveLogToFile(Integer installId, String operationType, String content) throws IOException {
        // 创建日志目录
        String installLogDir = LOG_DIR_PATH + installId + "/";
        File logDir = new File(installLogDir);
        if (!logDir.exists()) {
            boolean created = logDir.mkdirs();
            if (!created) {
                throw new IOException("无法创建日志目录: " + installLogDir);
            }
        }

        // 获取日志文件路径
        String logFilePath = getLogFilePath(installId, operationType);
        File logFile = new File(logFilePath);

        // 写入日志内容
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(logFile, true))) {
            // 添加时间戳
            String timestamp = DATE_FORMAT.format(new Date());
            writer.write("[" + timestamp + "] " + content);
            writer.newLine();
            writer.flush();
        }
    }

    /**
     * 追加日志内容到现有日志文件
     *
     * @param installId     安装ID
     * @param operationType 操作类型
     * @param content       日志内容
     * @return 操作结果
     */
    public ReturnResult<Boolean> appendLogContent(Integer installId, String operationType, String content) {
        if (installId == null) {
            return ReturnResult.illegal("安装ID不能为空");
        }

        if (!StringUtils.hasText(operationType)) {
            return ReturnResult.illegal("操作类型不能为空");
        }

        try {
            // 保存日志内容到文件
            saveLogToFile(installId, operationType, content);
            return ReturnResult.success(true);
        } catch (Exception e) {
            log.error("追加日志内容失败: {}", e.getMessage(), e);
            return ReturnResult.error("追加日志内容失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Boolean> updateLog(SoftServiceLog softServiceLog) {
        // 由于我们只使用文件系统，这个方法实际上会追加日志内容
        if (softServiceLog == null) {
            return ReturnResult.illegal("日志信息不能为空");
        }

        if (softServiceLog.getSoftServiceLogInstallId() == null) {
            return ReturnResult.illegal("安装ID不能为空");
        }

        if (!StringUtils.hasText(softServiceLog.getSoftServiceLogOperationType())) {
            return ReturnResult.illegal("操作类型不能为空");
        }

        try {
            // 将日志追加到文件系统
            Integer installId = softServiceLog.getSoftServiceLogInstallId();
            String operationType = softServiceLog.getSoftServiceLogOperationType();
            String content = softServiceLog.getSoftServiceLogContent();

            // 追加日志内容到文件
            return appendLogContent(installId, operationType, content);
        } catch (Exception e) {
            log.error("更新日志失败: {}", e.getMessage(), e);
            return ReturnResult.error("更新日志失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Boolean> removeLogById(Integer logId) {
        // 我们不再使用数据库ID，所以这个方法无法直接实现
        return ReturnResult.illegal("不支持通过ID删除日志，请使用cleanLogsByInstallId方法清理指定安装ID的所有日志");
    }

    @Override
    public ReturnResult<Boolean> cleanLogsByInstallId(Integer installId) {
        if (installId == null) {
            return ReturnResult.illegal("安装ID不能为空");
        }

        try {
            // 删除日志文件
            String installLogDir = LOG_DIR_PATH + installId + "/";
            File logDir = new File(installLogDir);

            if (logDir.exists() && logDir.isDirectory()) {
                File[] logFiles = logDir.listFiles();
                if (logFiles != null) {
                    for (File logFile : logFiles) {
                        if (!logFile.delete()) {
                            log.warn("删除日志文件失败: {}", logFile.getAbsolutePath());
                        }
                    }
                }

                if (!logDir.delete()) {
                    log.warn("删除日志目录失败: {}", logDir.getAbsolutePath());
                }
            }

            return ReturnResult.success(true);
        } catch (Exception e) {
            log.error("清理安装ID为{}的日志失败: {}", installId, e.getMessage(), e);
            return ReturnResult.error("清理日志失败: " + e.getMessage());
        }
    }

    /**
     * 获取日志文件路径
     *
     * @param installId     安装ID
     * @param operationType 操作类型
     * @return 日志文件路径
     */
    private String getLogFilePath(Integer installId, String operationType) {
        return LOG_DIR_PATH + installId + "/" + operationType + ".log";
    }
}
