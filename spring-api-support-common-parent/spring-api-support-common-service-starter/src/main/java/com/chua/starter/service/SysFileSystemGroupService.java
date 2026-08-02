package com.chua.starter.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.entity.service.SysFileSystemGroup;

import java.util.List;

/**
 * 文件系统分组服务接口
 *
 * @author CH
 * @since 2024/12/10
 * @version 1.0.0
 */
public interface SysFileSystemGroupService extends IService<SysFileSystemGroup> {

    /**
     * 获取分组树
     *
     * @return 分组树列表
     */
    List<SysFileSystemGroup> getTreeForFileSystem();

    /**
     * 获取默认分组
     *
     * @return 默认分组
     */
    SysFileSystemGroup getDefaultGroupForFileSystem();

    /**
     * 初始化默认分组
     *
     * @return 操作结果
     */
    ReturnResult<Boolean> initDefaultGroupsForFileSystem();
}
