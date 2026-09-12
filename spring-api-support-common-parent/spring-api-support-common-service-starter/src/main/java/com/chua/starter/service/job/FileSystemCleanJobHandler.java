package com.chua.starter.service.job;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Component;

/**
* 文件系统清理任务处理器（接口桩）
*
* @since 2024/12/20
* @author CH
 */
@Component("fileSystemCleanJobHandler")
@ConditionalOnClass(name = "com.chua.starter.job.support.handler.JobHandler")
public class FileSystemCleanJobHandler {

    /** 日志 */
    private static final Logger log = LoggerFactory.getLogger(FileSystemCleanJobHandler.class);

    /** 执行 */
    public void execute() {
        log.info("文件系统清理任务桩实现，外部依赖未配置");
    }
}