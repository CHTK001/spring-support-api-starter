package com.chua.starter.monitor.service;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.entity.SystemSoftImage;

import java.util.List;
import java.util.Map;

/**
 * 系统软件镜像管理服务接口
 *
 * @author CH
 * @since 2025/01/15
 */
public interface SystemSoftImageService {

    /**
     * 根据条件查询镜像列表
     *
     * @param serverId  服务器ID
     * @param softId    软件ID
     * @param status    镜像状态
     * @param imageName 镜像名称
     * @return 镜像列表
     */
    ReturnResult<List<SystemSoftImage>> selectImageList(Integer serverId, Integer softId, String status, String imageName);

    /**
     * 保存或更新镜像信息
     *
     * @param systemSoftImage 镜像信息
     * @return 保存结果
     */
    ReturnResult<SystemSoftImage> saveOrUpdate(SystemSoftImage systemSoftImage);

    /**
     * 根据ID删除镜像
     *
     * @param imageId 镜像ID
     * @return 删除结果
     */
    ReturnResult<Boolean> removeById(Integer imageId);

    /**
     * 批量删除镜像
     *
     * @param imageIds 镜像ID列表
     * @return 删除结果
     */
    ReturnResult<Boolean> removeByIds(List<Integer> imageIds);

    /**
     * 从软件拉取镜像到服务器
     *
     * @param softId    软件ID
     * @param serverId  服务器ID
     * @param imageTag  镜像标签
     * @param params    拉取参数
     * @return 拉取结果
     */
    ReturnResult<SystemSoftImage> pullImage(Integer softId, Integer serverId, String imageTag, Map<String, Object> params);

    /**
     * 启动镜像（创建容器）
     *
     * @param imageId 镜像ID
     * @param params  启动参数（端口映射、环境变量、数据卷等）
     * @return 启动结果
     */
    ReturnResult<Boolean> startImage(Integer imageId, Map<String, Object> params);

    /**
     * 停止镜像（停止容器）
     *
     * @param imageId 镜像ID
     * @return 停止结果
     */
    ReturnResult<Boolean> stopImage(Integer imageId);

    /**
     * 重启镜像（重启容器）
     *
     * @param imageId 镜像ID
     * @return 重启结果
     */
    ReturnResult<Boolean> restartImage(Integer imageId);

    /**
     * 删除镜像（从服务器中删除）
     *
     * @param imageId 镜像ID
     * @param force   是否强制删除
     * @return 删除结果
     */
    ReturnResult<Boolean> deleteImage(Integer imageId, Boolean force);

    /**
     * 获取镜像详情
     *
     * @param imageId 镜像ID
     * @return 镜像详情
     */
    ReturnResult<SystemSoftImage> getImageDetails(Integer imageId);

    /**
     * 获取镜像日志
     *
     * @param imageId 镜像ID
     * @return 镜像日志
     */
    ReturnResult<String> getImageLogs(Integer imageId);

    /**
     * 同步镜像状态
     *
     * @param serverId 服务器ID
     * @return 同步结果
     */
    ReturnResult<Integer> syncImageStatus(Integer serverId);

    /**
     * 检查镜像是否存在
     *
     * @param imageName 镜像名称
     * @param imageTag  镜像标签
     * @param serverId  服务器ID
     * @return 是否存在
     */
    ReturnResult<Boolean> checkImageExists(String imageName, String imageTag, Integer serverId);

    /**
     * 批量更新镜像状态
     *
     * @param imageIds 镜像ID列表
     * @param status   新状态
     * @return 更新结果
     */
    ReturnResult<Boolean> batchUpdateStatus(List<Integer> imageIds, String status);

    /**
     * 根据软件ID获取镜像列表
     *
     * @param softId 软件ID
     * @return 镜像列表
     */
    ReturnResult<List<SystemSoftImage>> getImagesBySoftId(Integer softId);

    /**
     * 根据服务器ID获取镜像列表
     *
     * @param serverId 服务器ID
     * @return 镜像列表
     */
    ReturnResult<List<SystemSoftImage>> getImagesByServerId(Integer serverId);

    /**
     * 获取镜像统计信息
     *
     * @param serverId 服务器ID（可选）
     * @return 统计信息
     */
    ReturnResult<Map<String, Object>> getImageStatistics(Integer serverId);
}