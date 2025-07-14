package com.chua.starter.monitor.starter.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.monitor.starter.entity.FileSystemGroup;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 文件系统分组Mapper接口
 *
 * @author CH
 * @since 2024/12/30
 */
@Mapper
public interface FileSystemGroupMapper extends BaseMapper<FileSystemGroup> {

    /**
     * 查询分组树（包含文件数量统计）
     *
     * @return 分组列表
     */
    List<FileSystemGroup> selectGroupTreeWithFileCount();

    /**
     * 查询默认分组
     *
     * @return 默认分组
     */
    FileSystemGroup selectDefaultGroup();

    /**
     * 统计分组下的文件数量
     *
     * @param groupId 分组ID
     * @return 文件数量
     */
    Long countFilesByGroupId(@Param("groupId") Integer groupId);

    /**
     * 查询分组及其所有子分组的ID
     *
     * @param groupId 分组ID
     * @return ID列表
     */
    List<Integer> selectGroupAndChildrenIds(@Param("groupId") Integer groupId);

    /**
     * 根据父分组ID查询子分组
     *
     * @param parentId 父分组ID
     * @return 子分组列表
     */
    List<FileSystemGroup> selectByParentId(@Param("parentId") Integer parentId);
}
