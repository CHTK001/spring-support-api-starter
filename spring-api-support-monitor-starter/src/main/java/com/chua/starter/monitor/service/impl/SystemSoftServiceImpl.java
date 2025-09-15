package com.chua.starter.monitor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.utils.ArrayUtils;
import com.chua.docker.support.client.DockerProtocolClient;
import com.chua.socketio.support.session.SocketSessionTemplate;
import com.chua.starter.monitor.callback.WebSocketDockerPullCallback;
import com.chua.starter.monitor.constant.SystemSoftRecordStatus;
import com.chua.starter.monitor.entity.SystemSoft;
import com.chua.starter.monitor.entity.SystemSoftImage;
import com.chua.starter.monitor.entity.SystemSoftRecord;
import com.chua.starter.monitor.factory.DockerProtocolClientFactory;
import com.chua.starter.monitor.mapper.SystemSoftImageMapper;
import com.chua.starter.monitor.mapper.SystemSoftMapper;
import com.chua.starter.monitor.mapper.SystemSoftRecordMapper;
import com.chua.starter.monitor.service.SystemSoftService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * 系统软件服务实现类
 *
 * @author CH
 * @version 1.0.0
 * @since 2024/01/01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SystemSoftServiceImpl extends ServiceImpl<SystemSoftMapper, SystemSoft> implements SystemSoftService {

    private final SystemSoftImageMapper systemSoftImageMapper;
    private final SystemSoftRecordMapper systemSoftRecordMapper;

    private final DockerProtocolClientFactory dockerProtocolClientFactory;
    private final SocketSessionTemplate socketSessionTemplate;


    @Override
    public IPage<SystemSoft> selectSoftPage(Page<SystemSoft> page, String softCode, String softName,
                                            String softCategory, Integer softStatus) {
        log.debug("分页查询软件列表 - softCode: {}, softName: {}, softCategory: {}, softStatus: {}",
                softCode, softName, softCategory, softStatus);

        LambdaQueryWrapper<SystemSoft> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(softCode), SystemSoft::getSystemSoftCode, softCode)
                .like(StringUtils.hasText(softName), SystemSoft::getSystemSoftName, softName)
                .eq(StringUtils.hasText(softCategory), SystemSoft::getSystemSoftCategory, softCategory)
                .eq(Objects.nonNull(softStatus), SystemSoft::getSystemSoftStatus, softStatus)
                .orderByDesc(SystemSoft::getCreateTime);

        IPage<SystemSoft> result = this.page(page, wrapper);
        log.debug("分页查询软件列表完成 - 总数: {}, 当前页数据: {}", result.getTotal(), result.getRecords().size());
        return result;
    }

    @Override
    public SystemSoft selectBySoftCode(String softCode) {
        log.debug("根据软件代码查询软件信息 - softCode: {}", softCode);

        if (!StringUtils.hasText(softCode)) {
            log.warn("软件代码为空");
            return null;
        }

        LambdaQueryWrapper<SystemSoft> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SystemSoft::getSystemSoftCode, softCode);

        SystemSoft result = this.getOne(wrapper);
        log.debug("根据软件代码查询软件信息完成 - 结果: {}", Objects.nonNull(result) ? "找到" : "未找到");
        return result;
    }

    @Override
    public List<SystemSoft> selectByCategory(String category) {
        log.debug("根据分类查询软件列表 - category: {}", category);

        LambdaQueryWrapper<SystemSoft> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StringUtils.hasText(category), SystemSoft::getSystemSoftCategory, category)
                .eq(SystemSoft::getSystemSoftStatus, 1)
                .orderByDesc(SystemSoft::getCreateTime);

        List<SystemSoft> result = this.list(wrapper);
        log.debug("根据分类查询软件列表完成 - 数量: {}", result.size());
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean syncSoftImage(Long softId, Long serverId) {
        log.info("开始同步软件镜像 - softId: {}, serverId: {}", softId, serverId);

        try {
            // 获取软件信息
            SystemSoft systemSoft = this.getById(softId);
            if (Objects.isNull(systemSoft)) {
                log.error("软件不存在 - softId: {}", softId);
                return false;
            }

            // 记录同步操作
            SystemSoftRecord record = new SystemSoftRecord();
            record.setSystemSoftId(Math.toIntExact(softId));
            record.setSystemServerId(Math.toIntExact(serverId));
            record.setSystemSoftRecordOperationType("SYNC");
            record.setSystemSoftRecordStatus(SystemSoftRecordStatus.IN_PROGRESS);
            record.setSystemSoftRecordStartTime(LocalDateTime.now());
            record.setSystemSoftRecordUser("system");
            record.setSystemSoftRecordRemark("同步软件镜像: " + systemSoft.getSystemSoftName());
            systemSoftRecordMapper.insert(record);

            // 调用Docker API进行镜像同步
            DockerProtocolClient dockerClient = dockerProtocolClientFactory.createClient(Math.toIntExact(serverId));
            if (dockerClient != null) {
                try {
                    String imageName = systemSoft.getSystemSoftDockerImage();
                    if (!StringUtils.hasText(imageName)) {
                        log.error("软件Docker镜像名称为空 - softId: {}", softId);
                        record.setSystemSoftRecordStatus(SystemSoftRecordStatus.FAILED);
                        record.setSystemSoftRecordEndTime(LocalDateTime.now());
                        record.setSystemSoftRecordRemark(record.getSystemSoftRecordRemark() + " - 失败原因: Docker镜像名称为空");
                        systemSoftRecordMapper.updateById(record);
                        return false;
                    }

                    log.info("开始同步Docker镜像: {}", imageName);

                    // 获取当前镜像列表
                    List<com.github.dockerjava.api.model.Image> existingImages = dockerClient.listImages();
                    log.debug("当前服务器镜像数量: {}", existingImages.size());

                    // 拉取最新镜像（默认使用latest标签）
                    String defaultTag = "latest";
                    String fullImageName = imageName + ":" + defaultTag;

                    // 创建WebSocket拉取回调
                    WebSocketDockerPullCallback syncCallback = new WebSocketDockerPullCallback(
                            socketSessionTemplate, "sync", fullImageName);

                    // 使用Docker客户端拉取镜像（带回调）
                    dockerClient.pullImage(fullImageName, syncCallback);

                    // 验证镜像是否拉取成功
                    List<com.github.dockerjava.api.model.Image> updatedImages = dockerClient.listImages();
                    com.github.dockerjava.api.model.Image syncedImage = updatedImages.stream()
                            .filter(img -> img.getRepoTags() != null &&
                                    ArrayUtils.contains(img.getRepoTags(), fullImageName))
                            .findFirst()
                            .orElse(null);

                    if (syncedImage != null) {
                        log.info("镜像同步成功: {}, 镜像ID: {}", fullImageName, syncedImage.getId());

                        // 检查是否已存在该镜像记录
                        LambdaQueryWrapper<SystemSoftImage> imageWrapper = new LambdaQueryWrapper<>();
                        imageWrapper.eq(SystemSoftImage::getSystemSoftId, Math.toIntExact(softId))
                                .eq(SystemSoftImage::getSystemSoftImageServerId, Math.toIntExact(serverId))
                                .eq(SystemSoftImage::getSystemSoftImageName, imageName)
                                .eq(SystemSoftImage::getSystemSoftImageTag, defaultTag);

                        SystemSoftImage existingImageRecord = systemSoftImageMapper.selectOne(imageWrapper);
                        if (existingImageRecord == null) {
                            // 创建新的镜像记录
                            SystemSoftImage newImageRecord = new SystemSoftImage();
                            newImageRecord.setSystemSoftId(Math.toIntExact(softId));
                            newImageRecord.setSystemSoftImageServerId(Math.toIntExact(serverId));
                            newImageRecord.setSystemSoftImageName(imageName);
                            newImageRecord.setSystemSoftImageTag(defaultTag);
                            newImageRecord.setSystemSoftImageStatus("AVAILABLE");
                            newImageRecord.setCreateTime(LocalDateTime.now());
                            systemSoftImageMapper.insert(newImageRecord);
                            log.info("创建新镜像记录: {}", newImageRecord.getSystemSoftImageId());
                        } else {
                            // 更新现有镜像记录
                            existingImageRecord.setSystemSoftImageStatus("AVAILABLE");
                            existingImageRecord.setUpdateTime(LocalDateTime.now());
                            systemSoftImageMapper.updateById(existingImageRecord);
                            log.info("更新镜像记录: {}", existingImageRecord.getSystemSoftImageId());
                        }

                        // 更新操作记录备注
                        record.setSystemSoftRecordRemark(record.getSystemSoftRecordRemark() + " - 镜像ID: " + syncedImage.getId());
                    } else {
                        log.warn("镜像同步完成但未找到镜像信息: {}", fullImageName);
                        record.setSystemSoftRecordRemark(record.getSystemSoftRecordRemark() + " - 警告: 未找到同步后的镜像信息");
                    }

                } catch (Exception e) {
                    log.error("Docker镜像同步失败: {}", e.getMessage(), e);
                    // 更新记录状态为失败
                    record.setSystemSoftRecordStatus(SystemSoftRecordStatus.FAILED);
                    record.setSystemSoftRecordEndTime(LocalDateTime.now());
                    record.setSystemSoftRecordRemark(record.getSystemSoftRecordRemark() + " - 失败原因: " + e.getMessage());
                    systemSoftRecordMapper.updateById(record);
                    return false;
                }
            } else {
                log.warn("Docker客户端未配置，跳过实际镜像同步操作");
                record.setSystemSoftRecordRemark(record.getSystemSoftRecordRemark() + " - 警告: Docker客户端未配置");
            }

            // 更新记录状态
            record.setSystemSoftRecordStatus(SystemSoftRecordStatus.SUCCESS);
            record.setSystemSoftRecordEndTime(LocalDateTime.now());
            systemSoftRecordMapper.updateById(record);

            log.info("同步软件镜像完成 - softId: {}, serverId: {}", softId, serverId);
            return true;

        } catch (Exception e) {
            log.error("同步软件镜像失败 - softId: {}, serverId: {}, error: {}", softId, serverId, e.getMessage(), e);
            return false;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean pullSoftImage(Long softId, Long serverId, String imageTag) {
        log.info("开始拉取软件镜像 - softId: {}, serverId: {}, imageTag: {}", softId, serverId, imageTag);

        try {
            // 获取软件信息
            SystemSoft systemSoft = this.getById(softId);
            if (Objects.isNull(systemSoft)) {
                log.error("软件不存在 - softId: {}", softId);
                return false;
            }

            // 记录拉取操作
            SystemSoftRecord record = new SystemSoftRecord();
            record.setSystemSoftId(Math.toIntExact(softId));
            record.setSystemServerId(Math.toIntExact(serverId));
            record.setSystemSoftRecordOperationType("PULL");
            record.setSystemSoftRecordStatus(SystemSoftRecordStatus.IN_PROGRESS);
            record.setSystemSoftRecordStartTime(LocalDateTime.now());
            record.setSystemSoftRecordUser("system");
            record.setSystemSoftRecordRemark("拉取软件镜像: " + systemSoft.getSystemSoftName() + ":" + imageTag);
            systemSoftRecordMapper.insert(record);

            // 调用Docker API拉取镜像
            DockerProtocolClient dockerClient = dockerProtocolClientFactory.createClient(Math.toIntExact(serverId));
            if (dockerClient != null) {
                try {
                    String imageName = systemSoft.getSystemSoftDockerImage();
                    String fullImageName = imageName + ":" + imageTag;

                    log.info("开始拉取Docker镜像: {}", fullImageName);

                    // 创建WebSocket拉取回调
                    WebSocketDockerPullCallback pullCallback = new WebSocketDockerPullCallback(
                            socketSessionTemplate, "pull", fullImageName);

                    // 使用Docker客户端拉取镜像（带回调）
                    dockerClient.pullImage(fullImageName, pullCallback);

                    // 获取镜像信息
                    List<com.github.dockerjava.api.model.Image> images = dockerClient.listImages();
                    com.github.dockerjava.api.model.Image pulledImage = images.stream()
                            .filter(img -> img.getRepoTags() != null &&
                                    ArrayUtils.contains(img.getRepoTags(), fullImageName))
                            .findFirst()
                            .orElse(null);

                    if (pulledImage != null) {
                        log.info("镜像拉取成功: {}, 镜像ID: {}", fullImageName, pulledImage.getId());
                    } else {
                        log.warn("镜像拉取完成但未找到镜像信息: {}", fullImageName);
                    }

                } catch (Exception e) {
                    log.error("Docker镜像拉取失败: {}", e.getMessage(), e);
                    // 更新记录状态为失败
                    record.setSystemSoftRecordStatus(SystemSoftRecordStatus.FAILED);
                    record.setSystemSoftRecordEndTime(LocalDateTime.now());
                    record.setSystemSoftRecordRemark(record.getSystemSoftRecordRemark() + " - 失败原因: " + e.getMessage());
                    systemSoftRecordMapper.updateById(record);
                    return false;
                }
            } else {
                log.warn("Docker客户端未配置，跳过实际镜像拉取操作");
            }

            // 创建镜像记录
            SystemSoftImage image = new SystemSoftImage();
            image.setSystemSoftId(Math.toIntExact(softId));
            image.setSystemSoftImageServerId(Math.toIntExact(serverId));
            image.setSystemSoftImageName(systemSoft.getSystemSoftDockerImage());
            image.setSystemSoftImageTag(imageTag);
            image.setSystemSoftImageStatus("AVAILABLE");
            image.setCreateTime(LocalDateTime.now());
            systemSoftImageMapper.insert(image);

            // 更新记录状态
            record.setSystemSoftRecordStatus(SystemSoftRecordStatus.SUCCESS);
            record.setSystemSoftRecordEndTime(LocalDateTime.now());
            record.setSystemSoftRecordRemark(record.getSystemSoftRecordRemark() + " - 镜像ID: " + image.getSystemSoftImageId());
            systemSoftRecordMapper.updateById(record);

            log.info("拉取软件镜像完成 - softId: {}, serverId: {}, imageTag: {}", softId, serverId, imageTag);
            return true;

        } catch (Exception e) {
            log.error("拉取软件镜像失败 - softId: {}, serverId: {}, imageTag: {}, error: {}",
                    softId, serverId, imageTag, e.getMessage(), e);
            return false;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteSoftImage(Long softId, Long serverId, String imageId) {
        log.info("开始删除软件镜像 - softId: {}, serverId: {}, imageId: {}", softId, serverId, imageId);

        try {
            // 获取镜像信息
            SystemSoftImage image = systemSoftImageMapper.selectById(imageId);
            if (Objects.isNull(image)) {
                log.error("镜像不存在 - imageId: {}", imageId);
                return false;
            }

            // 记录删除操作
            SystemSoftRecord record = new SystemSoftRecord();
            record.setSystemSoftId(Math.toIntExact(softId));
            record.setSystemServerId(Math.toIntExact(serverId));
            record.setSystemSoftRecordRemark("删除软件镜像: " + image.getSystemSoftImageName() + ":" + image.getSystemSoftImageTag());
            record.setSystemSoftRecordOperationType("DELETE");
            record.setSystemSoftRecordStatus(SystemSoftRecordStatus.IN_PROGRESS);
            record.setSystemSoftRecordStartTime(LocalDateTime.now());
            record.setSystemSoftRecordUser("system");
            systemSoftRecordMapper.insert(record);

            // 调用Docker API进行镜像删除
            DockerProtocolClient dockerClient = dockerProtocolClientFactory.createClient(Math.toIntExact(serverId));
            if (dockerClient != null) {
                try {
                    String fullImageName = image.getSystemSoftImageName() + ":" + image.getSystemSoftImageTag();

                    log.info("开始删除Docker镜像: {}", fullImageName);

                    // 删除镜像
                    dockerClient.removeImage(fullImageName, true);

                    log.info("镜像删除成功: {}", fullImageName);

                } catch (Exception e) {
                    log.error("Docker镜像删除失败: {}", e.getMessage(), e);
                    // 更新记录状态为失败
                    record.setSystemSoftRecordStatus(SystemSoftRecordStatus.FAILED);
                    record.setSystemSoftRecordEndTime(LocalDateTime.now());
                    record.setSystemSoftRecordRemark(record.getSystemSoftRecordRemark() + " - 失败原因: " + e.getMessage());
                    systemSoftRecordMapper.updateById(record);
                    return false;
                }
            } else {
                log.warn("Docker客户端未配置，跳过实际镜像删除操作");
            }

            // 删除镜像记录
            systemSoftImageMapper.deleteById(imageId);

            // 更新记录状态
            record.setSystemSoftRecordStatus(SystemSoftRecordStatus.SUCCESS);
            record.setSystemSoftRecordEndTime(LocalDateTime.now());
            systemSoftRecordMapper.updateById(record);

            log.info("删除软件镜像完成 - softId: {}, serverId: {}, imageId: {}", softId, serverId, imageId);
            return true;

        } catch (Exception e) {
            log.error("删除软件镜像失败 - softId: {}, serverId: {}, imageId: {}, error: {}",
                    softId, serverId, imageId, e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean validateSoftConfig(SystemSoft systemSoft) {
        log.debug("开始验证软件配置 - softCode: {}, softName: {}",
                systemSoft.getSystemSoftCode(), systemSoft.getSystemSoftName());

        try {
            // 基本字段验证
            if (!StringUtils.hasText(systemSoft.getSystemSoftCode())) {
                log.warn("软件代码不能为空");
                return false;
            }

            if (!StringUtils.hasText(systemSoft.getSystemSoftName())) {
                log.warn("软件名称不能为空");
                return false;
            }

            // 检查软件代码是否重复（新增时）
            if (Objects.isNull(systemSoft.getSystemSoftId())) {
                SystemSoft existingSoft = this.selectBySoftCode(systemSoft.getSystemSoftCode());
                if (Objects.nonNull(existingSoft)) {
                    log.warn("软件代码已存在 - softCode: {}", systemSoft.getSystemSoftCode());
                    return false;
                }
            } else {
                // 更新时检查软件代码是否与其他软件重复
                LambdaQueryWrapper<SystemSoft> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(SystemSoft::getSystemSoftCode, systemSoft.getSystemSoftCode())
                        .ne(SystemSoft::getSystemSoftId, systemSoft.getSystemSoftId());
                SystemSoft existingSoft = this.getOne(wrapper);
                if (Objects.nonNull(existingSoft)) {
                    log.warn("软件代码已被其他软件使用 - softCode: {}, existingId: {}",
                            systemSoft.getSystemSoftCode(), existingSoft.getSystemSoftId());
                    return false;
                }
            }

            // Docker镜像配置验证
            if (StringUtils.hasText(systemSoft.getSystemSoftDockerImage())) {
                String dockerImage = systemSoft.getSystemSoftDockerImage().trim();

                // 验证Docker镜像名称格式
                if (!isValidDockerImageName(dockerImage)) {
                    log.warn("Docker镜像名称格式不正确 - dockerImage: {}", dockerImage);
                    return false;
                }
            }

            // 安装方式验证
            if (StringUtils.hasText(systemSoft.getSystemSoftDefaultInstallMethod())) {
                String installMethod = systemSoft.getSystemSoftDefaultInstallMethod().trim();
                if (!isValidInstallMethod(installMethod)) {
                    log.warn("安装方式不支持 - installMethod: {}", installMethod);
                    return false;
                }
            }

            // 软件状态验证
            if (Objects.nonNull(systemSoft.getSystemSoftStatus())) {
                if (systemSoft.getSystemSoftStatus() < 0 || systemSoft.getSystemSoftStatus() > 1) {
                    log.warn("软件状态值无效 - status: {}", systemSoft.getSystemSoftStatus());
                    return false;
                }
            }

            log.debug("软件配置验证通过 - softCode: {}", systemSoft.getSystemSoftCode());
            return true;

        } catch (Exception e) {
            log.error("验证软件配置失败 - softCode: {}, error: {}",
                    systemSoft.getSystemSoftCode(), e.getMessage(), e);
            return false;
        }
    }

    /**
     * 验证Docker镜像名称格式
     *
     * @param dockerImage Docker镜像名称
     * @return 验证结果
     */
    private boolean isValidDockerImageName(String dockerImage) {
        if (!StringUtils.hasText(dockerImage)) {
            return false;
        }

        // Docker镜像名称基本格式验证
        // 支持格式: image, image:tag, registry/image, registry/image:tag, registry:port/image:tag
        String imagePattern = "^[a-zA-Z0-9][a-zA-Z0-9._/-]*[a-zA-Z0-9]?(:[a-zA-Z0-9][a-zA-Z0-9._-]*)?$";
        return dockerImage.matches(imagePattern) && dockerImage.length() <= 255;
    }

    /**
     * 验证安装方式
     *
     * @param installMethod 安装方式
     * @return 验证结果
     */
    private boolean isValidInstallMethod(String installMethod) {
        if (!StringUtils.hasText(installMethod)) {
            return true; // 允许为空
        }

        // 支持的安装方式
        Set<String> validMethods = Set.of("docker", "binary", "package", "source", "script");
        return validMethods.contains(installMethod.toLowerCase());
    }

}