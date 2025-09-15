package com.chua.starter.monitor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.monitor.entity.SystemSoftVersion;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 系统软件版本信息Mapper接口
 *
 * @author CH
 * @since 2025/01/15
 */
@Mapper
public interface SystemSoftVersionMapper extends BaseMapper<SystemSoftVersion> {

    /**
     * 根据软件ID查询版本列表
     *
     * @param softId 软件ID
     * @return 版本列表
     */
    List<SystemSoftVersion> selectBySoftId(@Param("softId") Integer softId);

    /**
     * 根据软件ID和版本名称查询版本信息
     *
     * @param softId      软件ID
     * @param versionName 版本名称
     * @return 版本信息
     */
    SystemSoftVersion selectBySoftIdAndVersionName(@Param("softId") Integer softId,
                                                   @Param("versionName") String versionName);

    /**
     * 根据软件ID和镜像标签查询版本信息
     *
     * @param softId   软件ID
     * @param imageTag 镜像标签
     * @return 版本信息
     */
    SystemSoftVersion selectBySoftIdAndImageTag(@Param("softId") Integer softId,
                                                @Param("imageTag") String imageTag);

    /**
     * 根据软件ID查询最新版本
     *
     * @param softId 软件ID
     * @return 最新版本信息
     */
    SystemSoftVersion selectLatestBySoftId(@Param("softId") Integer softId);

    /**
     * 根据软件ID查询稳定版本列表
     *
     * @param softId 软件ID
     * @return 稳定版本列表
     */
    List<SystemSoftVersion> selectStableBySoftId(@Param("softId") Integer softId);

    /**
     * 根据软件ID查询启用的版本列表
     *
     * @param softId 软件ID
     * @return 启用的版本列表
     */
    List<SystemSoftVersion> selectEnabledBySoftId(@Param("softId") Integer softId);

    /**
     * 根据版本号查询版本信息
     *
     * @param versionNumber 版本号
     * @return 版本信息列表
     */
    List<SystemSoftVersion> selectByVersionNumber(@Param("versionNumber") String versionNumber);

    /**
     * 根据镜像标签查询版本信息
     *
     * @param imageTag 镜像标签
     * @return 版本信息列表
     */
    List<SystemSoftVersion> selectByImageTag(@Param("imageTag") String imageTag);

    /**
     * 根据软件ID统计版本数量
     *
     * @param softId 软件ID
     * @return 版本数量
     */
    Long countBySoftId(@Param("softId") Integer softId);

    /**
     * 根据软件ID和状态统计版本数量
     *
     * @param softId 软件ID
     * @param status 版本状态
     * @return 版本数量
     */
    Long countBySoftIdAndStatus(@Param("softId") Integer softId,
                                @Param("status") Integer status);

    /**
     * 批量更新版本状态
     *
     * @param versionIds 版本ID列表
     * @param status     新状态
     * @return 更新的记录数
     */
    Integer batchUpdateStatus(@Param("versionIds") List<Integer> versionIds,
                              @Param("status") Integer status);

    /**
     * 根据软件ID批量更新最新版本标识
     *
     * @param softId          软件ID
     * @param latestVersionId 最新版本ID
     * @return 更新的记录数
     */
    Integer updateLatestFlag(@Param("softId") Integer softId,
                             @Param("latestVersionId") Integer latestVersionId);

    /**
     * 根据软件ID删除版本
     *
     * @param softId 软件ID
     * @return 删除的记录数
     */
    Integer deleteBySoftId(@Param("softId") Integer softId);

    /**
     * 检查版本名称是否存在
     *
     * @param softId      软件ID
     * @param versionName 版本名称
     * @param excludeId   排除的ID
     * @return 存在的数量
     */
    Long checkVersionNameExists(@Param("softId") Integer softId,
                                @Param("versionName") String versionName,
                                @Param("excludeId") Integer excludeId);

    /**
     * 根据关键字搜索版本
     *
     * @param keyword 关键字
     * @return 版本信息列表
     */
    List<SystemSoftVersion> searchByKeyword(@Param("keyword") String keyword);

    /**
     * 获取最新发布的版本列表
     *
     * @param limit 限制数量
     * @return 最新版本列表
     */
    List<SystemSoftVersion> selectLatestReleased(@Param("limit") Integer limit);

    /**
     * 根据大小范围查询版本列表
     *
     * @param minSize 最小大小
     * @param maxSize 最大大小
     * @return 版本信息列表
     */
    List<SystemSoftVersion> selectBySizeRange(@Param("minSize") Long minSize,
                                              @Param("maxSize") Long maxSize);
}