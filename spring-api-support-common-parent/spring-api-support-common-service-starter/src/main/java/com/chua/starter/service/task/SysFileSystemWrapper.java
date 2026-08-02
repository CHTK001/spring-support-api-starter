package com.chua.starter.service.task;

import com.chua.starter.entity.service.SysFileSystem;
import com.chua.starter.service.SysFileSystemService;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 文件系统包装类
 * 用于任务处理时传递文件系统对象和服务
 *
 * @author CH
 * @since 2024/12/10
 * @version 1.0.0
 */
@Data
@AllArgsConstructor
public class SysFileSystemWrapper {

    /**
     * 文件系统对象
     */
    private SysFileSystem sysFileSystem;

    /**
     * 文件系统服务
     */
    private SysFileSystemService sysFileSystemService;
}
