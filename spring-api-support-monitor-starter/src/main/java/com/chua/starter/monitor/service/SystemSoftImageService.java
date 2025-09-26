package com.chua.starter.monitor.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.entity.SystemSoftImage;
import com.chua.starter.monitor.entity.SystemSoftImageStatus;
import com.chua.docker.support.client.DockerPullImage;
import com.chua.docker.support.client.DockerCreateContainer;

import java.util.List;

/**
 * 系统软件镜像管理服务接口
 *
 * @author CH
 * @since 2025/01/15
 */
public interface SystemSoftImageService extends IService<SystemSoftImage> {

    /**
     * 根据条件查询镜像列表
     *
     * @param serverId  服务器ID
     * @param softId    软件ID
     * @param status    镜像状态
     * @param imageName 镜像名称
     * @return 镜像列表
     */
    ReturnResult<List<SystemSoftImage>> selectImageList(Integer serverId, Integer softId, SystemSoftImageStatus status, String imageName);

    /**
     * 根据条件查询镜像列表(ForImage)
     *
     * @param serverId  服务器ID
     * @param softId    软件ID
     * @param status    镜像状态
     * @param imageName 镜像名称
     * @return 镜像列表
     */
    default ReturnResult<List<SystemSoftImage>> selectImageListForImage(Integer serverId, Integer softId, SystemSoftImageStatus status, String imageName) {
        return selectImageList(serverId, softId, status, imageName);
    }

    /**
     * 从软件拉取镜像到服务器
     *
     * @param softId    软件ID
     * @param serverId  服务器ID
     * @param imageTag  镜像标签
     * @param config    拉取配置
     * @return 拉取结果
     */
    ReturnResult<SystemSoftImage> pullImage(Integer softId, Integer serverId, String imageTag, DockerPullImage config);

    /**
     * 从软件拉取镜像到服务器(ForImage)
     *
     * @param softId    软件ID
     * @param serverId  服务器ID
     * @param imageTag  镜像标签
     * @param config    拉取配置
     * @return 拉取结果
     */
    default ReturnResult<SystemSoftImage> pullImageForImage(Integer softId, Integer serverId, String imageTag, DockerPullImage config) {
        return pullImage(softId, serverId, imageTag, config);
    }

    /**
     * 创建容器
     *
     * @param imageId 镜像ID
     * @param config  容器配置
     * @return 创建结果
     */
    ReturnResult<Boolean> createContainer(Integer imageId, DockerCreateContainer config);

    /**
     * 删除镜像（从服务器中删除）
     *
     * @param imageId 镜像ID
     * @param force   是否强制删除
     * @return 删除结果
     */
    ReturnResult<Boolean> removeImage(Integer imageId, Boolean force);

    /**
     * 获取镜像详情
     *
     * @param imageId 镜像ID
     * @return 镜像详情
     */
    ReturnResult<SystemSoftImage> getImageDetails(Integer imageId);

}