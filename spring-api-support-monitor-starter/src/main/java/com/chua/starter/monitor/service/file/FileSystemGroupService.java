package com.chua.starter.monitor.service.file;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chua.starter.monitor.entity.FileSystemGroup;

import java.util.List;

/**
 * 文件系统分组服务接口
 *
 * @author CH
 * @since 2024/12/30
 */
public interface FileSystemGroupService extends IService<FileSystemGroup> {

    /**
     * 获取分组树形结构
     *
     * @return 分组树
     */
    List<FileSystemGroup> getGroupTree();

    /**
     * 创建分组
     *
     * @param group 分组信息
     * @return 创建的分组
     */
    FileSystemGroup createGroup(FileSystemGroup group);

    /**
     * 更新分组
     *
     * @param group 分组信息
     * @return 更新的分组
     */
    FileSystemGroup updateGroup(FileSystemGroup group);

    /**
     * 删除分组
     *
     * @param groupId 分组ID
     * @return 删除结果
     */
    boolean deleteGroup(Integer groupId);

    /**
     * 移动分组
     *
     * @param groupId 分组ID
     * @param newParentId 新父分组ID
     * @return 移动结果
     */
    boolean moveGroup(Integer groupId, Integer newParentId);

    /**
     * 获取或创建默认分组
     *
     * @return 默认分组
     */
    FileSystemGroup getOrCreateDefaultGroup();

    /**
     * 获取分组及其所有子分组的ID列表
     *
     * @param groupId 分组ID
     * @return ID列表
     */
    List<Integer> getGroupAndChildrenIds(Integer groupId);

    /**
     * 迁移文件到指定分组
     *
     * @param fileIds 文件ID列表
     * @param targetGroupId 目标分组ID
     * @return 迁移的文件数量
     */
    int migrateFilesToGroup(List<Integer> fileIds, Integer targetGroupId);

    /**
     * 初始化默认分组
     */
    void initDefaultGroups();

    /**
     * 根据文件类型获取分组ID
     *
     * @param fileExtension 文件扩展名
     * @return 分组ID
     */
    Integer getGroupByFileType(String fileExtension);

    /**
     * 更新分组树路径
     *
     * @param groupId 分组ID
     */
    void updateTreePath(Integer groupId);

    /**
     * 更新子分组的树路径
     *
     * @param parentId 父分组ID
     */
    void updateChildrenTreePath(Integer parentId);
}
