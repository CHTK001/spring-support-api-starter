package com.chua.starter.monitor.service;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.pojo.response.FileSystemResponse;

import java.util.List;

/**
 * 文件系统
 *
 * @author CH
 */
public interface FileSystemService {


    /**
     * 获取文件系统
     *
     * @param parentPath 父路径
     * @param name 名称
     * @return 文件系统
     */
    ReturnResult<List<FileSystemResponse>> list(String parentPath, String name);
}
