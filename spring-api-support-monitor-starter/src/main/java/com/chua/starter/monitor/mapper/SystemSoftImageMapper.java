package com.chua.starter.monitor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.monitor.entity.SystemSoftImage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 系统软件镜像信息Mapper接口
 *
 * @author CH
 * @since 2025/01/15
 */
@Mapper
public interface SystemSoftImageMapper extends BaseMapper<SystemSoftImage> {

    /**
     * 根据软件ID查询镜像列表
     *
     * @param softId 软件ID
     * @return 镜像列表
     */
    List<SystemSoftImage> selectBySoftId(@Param("softId") Integer softId);

    /**
     * 根据服务器ID查询镜像列表
     *
     * @param serverId 服务器ID
     * @return 镜像列表
     */
    List<SystemSoftImage> selectByServerId(@Param("serverId") Integer serverId);

    /**
     * 根据Docker镜像ID查询镜像信息
     *
     * @param dockerImageId Docker镜像ID
     * @return 镜像信息
     */
    SystemSoftImage selectByDockerImageId(@Param("dockerImageId") String dockerImageId);

    /**
     * 根据镜像名称和标签查询镜像信息
     *
     * @param imageName 镜像名称
     * @param imageTag  镜像标签
     * @return 镜像信息
     */
    SystemSoftImage selectByImageNameAndTag(@Param("imageName") String imageName,
                                            @Param("imageTag") String imageTag);

    /**
     * 根据软件ID和服务器ID查询镜像列表
     *
     * @param softId   软件ID
     * @param serverId 服务器ID
     * @return 镜像列表
     */
    List<SystemSoftImage> selectBySoftIdAndServerId(@Param("softId") Integer softId,
                                                    @Param("serverId") Integer serverId);

    /**
     * 根据镜像状态查询镜像列表
     *
     * @param imageStatus 镜像状态
     * @return 镜像列表
     */
    List<SystemSoftImage> selectByImageStatus(@Param("imageStatus") String imageStatus);

    /**
     * 根据服务器ID和状态查询镜像列表
     *
     * @param serverId    服务器ID
     * @param imageStatus 镜像状态
     * @return 镜像列表
     */
    List<SystemSoftImage> selectByServerIdAndStatus(@Param("serverId") Integer serverId,
                                                    @Param("imageStatus") String imageStatus);

    /**
     * 根据镜像名称查询镜像列表
     *
     * @param imageName 镜像名称
     * @return 镜像列表
     */
    List<SystemSoftImage> selectByImageName(@Param("imageName") String imageName);

    /**
     * 根据镜像标签查询镜像列表
     *
     * @param imageTag 镜像标签
     * @return 镜像列表
     */
    List<SystemSoftImage> selectByImageTag(@Param("imageTag") String imageTag);

    /**
     * 根据软件ID统计镜像数量
     *
     * @param softId 软件ID
     * @return 镜像数量
     */
    Long countBySoftId(@Param("softId") Integer softId);

    /**
     * 根据服务器ID统计镜像数量
     *
     * @param serverId 服务器ID
     * @return 镜像数量
     */
    Long countByServerId(@Param("serverId") Integer serverId);

    /**
     * 根据镜像状态统计镜像数量
     *
     * @param imageStatus 镜像状态
     * @return 镜像数量
     */
    Long countByImageStatus(@Param("imageStatus") String imageStatus);

    /**
     * 根据服务器ID和状态统计镜像数量
     *
     * @param serverId    服务器ID
     * @param imageStatus 镜像状态
     * @return 镜像数量
     */
    Long countByServerIdAndStatus(@Param("serverId") Integer serverId,
                                  @Param("imageStatus") String imageStatus);

    /**
     * 批量更新镜像状态
     *
     * @param imageIds    镜像ID列表
     * @param imageStatus 新状态
     * @return 更新的记录数
     */
    Integer batchUpdateImageStatus(@Param("imageIds") List<Integer> imageIds,
                                   @Param("imageStatus") String imageStatus);

    /**
     * 根据软件ID删除镜像
     *
     * @param softId 软件ID
     * @return 删除的记录数
     */
    Integer deleteBySoftId(@Param("softId") Integer softId);

    /**
     * 根据服务器ID删除镜像
     *
     * @param serverId 服务器ID
     * @return 删除的记录数
     */
    Integer deleteByServerId(@Param("serverId") Integer serverId);

    /**
     * 检查镜像是否存在
     *
     * @param imageName 镜像名称
     * @param imageTag  镜像标签
     * @param serverId  服务器ID
     * @param excludeId 排除的ID
     * @return 存在的数量
     */
    Long checkImageExists(@Param("imageName") String imageName,
                          @Param("imageTag") String imageTag,
                          @Param("serverId") Integer serverId,
                          @Param("excludeId") Integer excludeId);

    /**
     * 根据关键字搜索镜像
     *
     * @param keyword 关键字
     * @return 镜像信息列表
     */
    List<SystemSoftImage> searchByKeyword(@Param("keyword") String keyword);

    /**
     * 获取可用的镜像列表
     *
     * @return 可用的镜像列表
     */
    List<SystemSoftImage> selectAvailableImages();

    /**
     * 获取异常状态的镜像列表
     *
     * @return 异常状态的镜像列表
     */
    List<SystemSoftImage> selectAbnormalImages();

    /**
     * 根据大小范围查询镜像列表
     *
     * @param minSize 最小大小
     * @param maxSize 最大大小
     * @return 镜像信息列表
     */
    List<SystemSoftImage> selectBySizeRange(@Param("minSize") Long minSize,
                                            @Param("maxSize") Long maxSize);

    /**
     * 根据创建时间范围查询镜像列表
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 镜像信息列表
     */
    List<SystemSoftImage> selectByCreateTimeRange(@Param("startTime") String startTime,
                                                  @Param("endTime") String endTime);

    /**
     * 获取最新拉取的镜像列表
     *
     * @param limit 限制数量
     * @return 最新镜像列表
     */
    List<SystemSoftImage> selectLatestImages(@Param("limit") Integer limit);

    /**
     * 根据软件ID和镜像标签查询镜像信息
     *
     * @param softId   软件ID
     * @param imageTag 镜像标签
     * @return 镜像信息
     */
    SystemSoftImage selectBySoftIdAndImageTag(@Param("softId") Integer softId,
                                              @Param("imageTag") String imageTag);
}