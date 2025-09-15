package com.chua.starter.monitor.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.chua.starter.monitor.entity.SystemSoftImage;

import java.util.List;

/**
 * 系统软件镜像管理服务接口
 *
 * @author CH
 * @version 1.0
 * @since 2025/01/15
 */
public interface SystemSoftImageService extends IService<SystemSoftImage> {

    /**
     * 分页查询镜像列表
     *
     * @param current  当前页
     * @param size     每页大小
     * @param serverId 服务器ID（可选）
     * @param status   镜像状态（可选）
     * @param keyword  关键词（可选）
     * @return 分页结果
     */
    IPage<SystemSoftImage> getImagePage(Long current, Long size, Integer serverId, String status, String keyword);

    /**
     * 根据软件ID查询镜像列表
     *
     * @param softId 软件ID
     * @return 镜像列表
     */
    List<SystemSoftImage> getImagesBySoftId(Integer softId);

    /**
     * 根据服务器ID查询镜像列表
     *
     * @param serverId 服务器ID
     * @return 镜像列表
     */
    List<SystemSoftImage> getImagesByServerId(Integer serverId);

    /**
     * 同步单个镜像状态
     * 从远程Docker服务器获取最新镜像状态并同步到数据库
     *
     * @param imageId 镜像ID
     * @return 同步结果
     */
    boolean syncImageStatus(Integer imageId);

    /**
     * 同步所有镜像状态
     * 从远程Docker服务器获取最新镜像状态并同步到数据库
     *
     * @param serverId 服务器ID（可选，为null时同步所有服务器）
     * @return 同步的镜像数量
     */
    int syncAllImageStatus(Integer serverId);

    /**
     * 同步指定服务器的所有镜像状态
     *
     * @param serverId 服务器ID
     * @return 同步结果
     */
    boolean syncAllImagesByServerId(Integer serverId);

    /**
     * 根据镜像名称和标签查询镜像
     *
     * @param imageName 镜像名称
     * @param imageTag  镜像标签
     * @param serverId  服务器ID
     * @return 镜像信息
     */
    SystemSoftImage getImageByNameAndTag(String imageName, String imageTag, Integer serverId);

    /**
     * 检查镜像是否存在
     *
     * @param imageName 镜像名称
     * @param imageTag  镜像标签
     * @param serverId  服务器ID
     * @return 是否存在
     */
    boolean checkImageExists(String imageName, String imageTag, Integer serverId);

    /**
     * 获取可用的镜像列表
     *
     * @return 可用镜像列表
     */
    List<SystemSoftImage> getAvailableImages();

    /**
     * 获取异常状态的镜像列表
     *
     * @return 异常镜像列表
     */
    List<SystemSoftImage> getAbnormalImages();

    /**
     * 根据关键字搜索镜像
     *
     * @param keyword 关键字
     * @return 镜像列表
     */
    List<SystemSoftImage> searchImagesByKeyword(String keyword);

    /**
     * 更新镜像状态
     *
     * @param imageId 镜像ID
     * @param status  新状态
     * @return 更新结果
     */
    boolean updateImageStatus(Integer imageId, String status);

    /**
     * 批量更新镜像状态
     *
     * @param imageIds 镜像ID列表
     * @param status   新状态
     * @return 更新结果
     */
    boolean batchUpdateImageStatus(List<Integer> imageIds, String status);

    /**
     * 删除镜像记录
     *
     * @param imageId 镜像ID
     * @return 删除结果
     */
    boolean deleteImage(Integer imageId);

    /**
     * 根据软件ID删除所有镜像记录
     *
     * @param softId 软件ID
     * @return 删除结果
     */
    boolean deleteImagesBySoftId(Integer softId);

    /**
     * 根据服务器ID删除所有镜像记录
     *
     * @param serverId 服务器ID
     * @return 删除结果
     */
    boolean deleteImagesByServerId(Integer serverId);

    /**
     * 统计镜像数量
     *
     * @param serverId 服务器ID（可选）
     * @param status   状态（可选）
     * @return 镜像数量
     */
    Long countImages(Integer serverId, String status);

    /**
     * 获取最新拉取的镜像列表
     *
     * @param limit 限制数量
     * @return 最新镜像列表
     */
    List<SystemSoftImage> getLatestImages(Integer limit);
}