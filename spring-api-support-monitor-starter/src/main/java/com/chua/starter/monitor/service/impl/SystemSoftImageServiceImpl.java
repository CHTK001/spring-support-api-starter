package com.chua.starter.monitor.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.utils.StringUtils;
import com.chua.docker.support.client.DockerCreateContainer;
import com.chua.docker.support.client.DockerProtocolClient;
import com.chua.docker.support.client.DockerPullImage;
import com.chua.starter.monitor.callback.WebSocketContainerCreateCallback;
import com.chua.starter.monitor.enums.OperationType;
import com.chua.starter.monitor.entity.SystemSoft;
import com.chua.starter.monitor.entity.SystemSoftImage;
import com.chua.starter.monitor.entity.SystemSoftImageStatus;
import com.chua.starter.monitor.mapper.SystemSoftImageMapper;
import com.chua.starter.monitor.mapper.SystemSoftMapper;
import com.chua.starter.monitor.service.DockerCallbackService;
import com.chua.starter.monitor.service.SystemSoftImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;

/**
 * 系统软件镜像管理服务实现类
 *
 * @author CH
 * @since 2025/01/15
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SystemSoftImageServiceImpl extends ServiceImpl<SystemSoftImageMapper, SystemSoftImage> implements SystemSoftImageService {

    private final SystemSoftImageMapper systemSoftImageMapper;
    private final com.chua.starter.monitor.factory.DockerProtocolClientFactory dockerProtocolClientFactory;
    private final DockerCallbackService dockerCallbackService;
    private final SystemSoftMapper systemSoftMapper;

    private final TransactionTemplate transactionTemplate;

    /**
     * 根据条件查询镜像列表
     *
     * @param serverId  服务器ID
     * @param softId    软件ID
     * @param status    镜像状态
     * @param imageName 镜像名称
     * @return 镜像列表
     */
    @Override
    public ReturnResult<Object> selectImageList(Integer serverId, Integer softId, SystemSoftImageStatus status, String imageName) {
        log.info("根据条件查询镜像列表 - 服务器ID: {}, 软件ID: {}, 状态: {}, 镜像名称: {}", serverId, softId, status, imageName);

        try {
            // 查询数据库中的镜像列表
            // 这里应该根据实际的查询条件构造查询逻辑
            List<SystemSoftImage> imageList = systemSoftImageMapper.selectList(null);
            log.info("根据条件查询镜像列表完成");

            return ReturnResult.ok(imageList);
        } catch (Exception e) {
            log.error("根据条件查询镜像列表失败 - 错误信息: {}", e.getMessage(), e);
            return ReturnResult.error("查询失败: " + e.getMessage());
        }
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
    @Override
    public ReturnResult<SystemSoftImage> pullImage(Integer softId, Integer serverId, String imageTag, DockerPullImage config) {
        log.info("拉取镜像 - 软件ID: {}, 服务器ID: {}, 镜像标签: {}", softId, serverId, imageTag);

        try {
            // 获取软件信息
            SystemSoft systemSoft = systemSoftMapper.selectById(softId);
            if (null == systemSoft) {
                log.error("无法获取镜像信息 - 软件ID: {}", softId);
                return ReturnResult.error("无法获取镜像信息");
            }

            String imageName = systemSoft.getSystemSoftDockerImage();
            // 构造完整的镜像名称
            String fullImageName = imageName;
            if (StringUtils.hasText(imageTag)) {
                fullImageName = imageName + ":" + imageTag;
            }

            // 更新镜像状态为拉取中
            SystemSoftImage image = systemSoftImageMapper.selectById(softId);
            if (image == null) {
                image = new SystemSoftImage();
                image.setSystemSoftId(softId);
                image.setSystemSoftImageServerId(serverId);
                image.setSystemSoftImageName(imageName);
            }

            // 更新镜像的基本信息
            image.setSystemSoftImageStatus(SystemSoftImageStatus.PULLING);
            image.setSystemSoftImageTag(imageTag);
            image.setSystemSoftImageFullName(fullImageName);
            image.setSystemSoftImageRepository(systemSoft.getSystemSoftRegistryId() != null ? systemSoft.getSystemSoftRegistryId().toString() : "");

            // 增加拉取次数
            Long pullCount = image.getSystemSoftImagePullCount();
            if (pullCount == null) {
                pullCount = 0L;
            }
            image.setSystemSoftImagePullCount(pullCount + 1);

            // 更新最后拉取时间
            image.setSystemSoftImageLastPulled(java.time.LocalDateTime.now());

            this.saveOrUpdate(image);

            // 如果config为null，则创建默认的DockerPullImage配置对象
            if (config == null) {
                config = DockerPullImage.builder()
                        .imageName(imageName)
                        .tag(imageTag)
                        .build();
            }

            // 获取Docker客户端
            DockerProtocolClient dockerClient = dockerProtocolClientFactory.createClient(serverId);
            if (dockerClient == null) {
                log.error("无法创建Docker客户端 - 服务器ID: {}", serverId);
                // 更新镜像状态为拉取失败
                image.setSystemSoftImageStatus(SystemSoftImageStatus.PULL_FAILED);
                this.saveOrUpdate(image);
                return ReturnResult.error("无法创建Docker客户端");
            }

            // 创建状态更新WebSocket回调处理器
            var callback = dockerCallbackService.createStatusUpdatingWebSocketPullCallback(OperationType.PULL, fullImageName, image.getSystemSoftImageId());

            // 执行拉取操作（带回调）
            log.info("开始拉取镜像(带回调): {}:{}", imageName, imageTag);
            dockerCallbackService.pullImageWithCallback(dockerClient, config, callback);

            log.info("拉取镜像请求已发送 - 软件ID: {}, 服务器ID: {}, 镜像标签: {}", softId, serverId, imageTag);
            return ReturnResult.ok(image);
        } catch (Exception e) {
            // 更新镜像状态为拉取失败
            try {
                SystemSoftImage image = systemSoftImageMapper.selectById(softId);
                if (image != null) {
                    image.setSystemSoftImageStatus(SystemSoftImageStatus.PULL_FAILED);
                    this.saveOrUpdate(image);
                }
            } catch (Exception updateEx) {
                log.warn("更新镜像状态为拉取失败时出错: {}", updateEx.getMessage());
            }

            log.error("拉取镜像失败 - 错误信息: {}", e.getMessage(), e);
            return ReturnResult.error("拉取镜像失败: " + e.getMessage());
        }
    }

    /**
     * 创建容器
     *
     * @param imageId 镜像ID
     * @param config  容器配置
     * @return 创建结果
     */
    @Override
    public ReturnResult<Boolean> createContainer(Integer imageId, DockerCreateContainer config) {
        log.info("创建容器 - 镜像ID: {}", imageId);

        try {
            // 获取镜像信息
            SystemSoftImage image = systemSoftImageMapper.selectById(imageId);
            if (image == null) {
                log.error("镜像不存在 - 镜像ID: {}", imageId);
                return ReturnResult.error("镜像不存在");
            }

            // 更新镜像状态为启动中
            image.setSystemSoftImageStatus(SystemSoftImageStatus.STARTING);
            this.saveOrUpdate(image);

            // 获取Docker客户端
            Integer serverId = image.getSystemSoftImageServerId();
            DockerProtocolClient dockerClient = dockerProtocolClientFactory.createClient(serverId);
            if (dockerClient == null) {
                log.error("无法创建Docker客户端 - 服务器ID: {}", serverId);
                // 更新镜像状态为启动失败
                image.setSystemSoftImageStatus(SystemSoftImageStatus.START_FAILED);
                this.saveOrUpdate(image);
                return ReturnResult.error("无法创建Docker客户端");
            }

            // 获取容器名称（如果配置中没有提供，则生成一个默认名称）
            String containerName = config.getContainerName();
            if (!StringUtils.hasText(containerName)) {
                containerName = "container-" + image.getSystemSoftImageName().replace("/", "-") + "-" + System.currentTimeMillis();
                config.setContainerName(containerName);
            }

            // 构造完整的镜像名称（如果配置中没有提供）
            if (!StringUtils.hasText(config.getImageName())) {
                String fullImageName = image.getSystemSoftImageName();
                if (StringUtils.hasText(image.getSystemSoftImageTag())) {
                    fullImageName = image.getSystemSoftImageName() + ":" + image.getSystemSoftImageTag();
                }
                config.setImageName(fullImageName);
            }

            // 创建WebSocket回调处理器
            var callback = new WebSocketContainerCreateCallback(
                    dockerCallbackService.getSocketSessionTemplate(), OperationType.START, containerName);

            // 执行启动操作（创建容器带回调）
            log.info("开始创建容器(带回调): {} 使用镜像: {}", containerName, config.getImageName());
            dockerCallbackService.createContainerWithCallback(dockerClient, config, callback);

            log.info("创建容器请求已发送 - 镜像ID: {}", imageId);
            return ReturnResult.ok(true);
        } catch (Exception e) {
            // 更新镜像状态为启动失败
            try {
                SystemSoftImage image = systemSoftImageMapper.selectById(imageId);
                if (image != null) {
                    image.setSystemSoftImageStatus(SystemSoftImageStatus.START_FAILED);
                    this.saveOrUpdate(image);
                }
            } catch (Exception updateEx) {
                log.warn("更新镜像状态为启动失败时出错: {}", updateEx.getMessage());
            }

            log.error("创建容器失败 - 错误信息: {}", e.getMessage(), e);
            return ReturnResult.error("创建容器失败: " + e.getMessage());
        }
    }

    /**
     * 删除镜像（从服务器中删除）
     *
     * @param imageId 镜像ID
     * @param force   是否强制删除
     * @return 删除结果
     */
    @Override
    public ReturnResult<Boolean> removeImage(Integer imageId, Boolean force) {
        log.info("删除镜像（从服务器中删除） - 镜像ID: {}, 强制删除: {}", imageId, force);

        try {
            // 获取镜像信息
            SystemSoftImage image = systemSoftImageMapper.selectById(imageId);
            if (image == null) {
                log.error("镜像不存在 - 镜像ID: {}", imageId);
                return ReturnResult.error("镜像不存在");
            }

            // 更新镜像状态为删除中
            image.setSystemSoftImageStatus(SystemSoftImageStatus.DELETING);
            this.saveOrUpdate(image);

            // 获取Docker客户端
            Integer serverId = image.getSystemSoftImageServerId();
            DockerProtocolClient dockerClient = dockerProtocolClientFactory.createClient(serverId);
            if (dockerClient == null) {
                log.error("无法创建Docker客户端 - 服务器ID: {}", serverId);
                // 更新镜像状态为删除失败
                image.setSystemSoftImageStatus(SystemSoftImageStatus.DELETE_FAILED);
                this.saveOrUpdate(image);
                return ReturnResult.error("无法创建Docker客户端");
            }

            // 使用事务模板执行删除操作
            return transactionTemplate.execute(status -> {
                try {
                    // 执行删除操作
                    // 根据文档，DockerProtocolClient有removeImage方法
                    dockerClient.removeImage(image.getSystemSoftImageName(), force != null ? force : false);

                    // 从数据库删除记录
                    super.removeById(imageId);

                    log.info("删除镜像成功 - 镜像ID: {}, 强制删除: {}", imageId, force);
                    return ReturnResult.ok(true);
                } catch (Exception e) {
                    // 更新镜像状态为删除失败
                    try {
                        SystemSoftImage img = systemSoftImageMapper.selectById(imageId);
                        if (img != null) {
                            img.setSystemSoftImageStatus(SystemSoftImageStatus.DELETE_FAILED);
                            this.saveOrUpdate(img);
                        }
                    } catch (Exception updateEx) {
                        log.warn("更新镜像状态为删除失败时出错: {}", updateEx.getMessage());
                    }

                    log.error("删除镜像失败 - 错误信息: {}", e.getMessage(), e);
                    return ReturnResult.error("删除镜像失败: " + e.getMessage());
                }
            });
        } catch (Exception e) {
            // 更新镜像状态为删除失败
            try {
                SystemSoftImage image = systemSoftImageMapper.selectById(imageId);
                if (image != null) {
                    image.setSystemSoftImageStatus(SystemSoftImageStatus.DELETE_FAILED);
                    this.saveOrUpdate(image);
                }
            } catch (Exception updateEx) {
                log.warn("更新镜像状态为删除失败时出错: {}", updateEx.getMessage());
            }

            log.error("删除镜像失败 - 错误信息: {}", e.getMessage(), e);
            return ReturnResult.error("删除镜像失败: " + e.getMessage());
        }
    }

    /**
     * 获取镜像详情
     *
     * @param imageId 镜像ID
     * @return 镜像详情
     */
    @Override
    public ReturnResult<SystemSoftImage> getImageDetails(Integer imageId) {
        log.info("获取镜像详情 - 镜像ID: {}", imageId);

        try {
            SystemSoftImage image = systemSoftImageMapper.selectById(imageId);
            if (image == null) {
                log.error("镜像不存在 - 镜像ID: {}", imageId);
                return ReturnResult.error("镜像不存在");
            }

            log.info("获取镜像详情成功 - 镜像ID: {}", imageId);
            return ReturnResult.ok(image);
        } catch (Exception e) {
            log.error("获取镜像详情失败 - 错误信息: {}", e.getMessage(), e);
            return ReturnResult.error("获取镜像详情失败: " + e.getMessage());
        }
    }

}