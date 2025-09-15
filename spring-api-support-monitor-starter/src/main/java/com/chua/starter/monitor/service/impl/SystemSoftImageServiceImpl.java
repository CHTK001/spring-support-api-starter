package com.chua.starter.monitor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.starter.monitor.entity.SystemSoftImage;
import com.chua.starter.monitor.mapper.SystemSoftImageMapper;
import com.chua.starter.monitor.service.SystemSoftImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 系统软件镜像管理服务实现类
 *
 * @author CH
 * @version 1.0
 * @since 2025/01/15
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SystemSoftImageServiceImpl extends ServiceImpl<SystemSoftImageMapper, SystemSoftImage> implements SystemSoftImageService {

    private final SystemSoftImageMapper systemSoftImageMapper;

    /**
     * 分页查询镜像列表
     *
     * @param current  当前页码，从1开始计数，例如：1
     * @param size     每页显示的记录数，例如：10
     * @param serverId 服务器ID（可选），用于筛选特定服务器上的镜像，例如：1
     * @param status   镜像状态（可选），用于筛选特定状态的镜像，例如："AVAILABLE"
     * @param keyword  搜索关键词（可选），用于模糊搜索镜像名称、标签或服务器名称，例如："nginx"
     * @return 分页结果，包含当前页的镜像列表和总记录数
     */
    @Override
    public IPage<SystemSoftImage> getImagePage(Long current, Long size, Integer serverId, String status, String keyword) {
        log.info("分页查询镜像列表 - 当前页: {}, 每页大小: {}, 服务器ID: {}, 状态: {}, 关键词: {}",
                current, size, serverId, status, keyword);

        Page<SystemSoftImage> page = new Page<>(current, size);
        LambdaQueryWrapper<SystemSoftImage> wrapper = new LambdaQueryWrapper<>();

        // 添加查询条件
        if (serverId != null) {
            wrapper.eq(SystemSoftImage::getSystemSoftImageServerId, serverId);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(SystemSoftImage::getSystemSoftImageStatus, status);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(SystemSoftImage::getSystemSoftImageName, keyword)
                    .or().like(SystemSoftImage::getSystemSoftImageTag, keyword)
                    .or().like(SystemSoftImage::getSystemSoftImageServerName, keyword));
        }

        // 按创建时间倒序排列
        wrapper.orderByDesc(SystemSoftImage::getCreateTime);

        IPage<SystemSoftImage> result = systemSoftImageMapper.selectPage(page, wrapper);
        log.info("分页查询镜像列表完成 - 总记录数: {}, 当前页数据量: {}", result.getTotal(), result.getRecords().size());

        return result;
    }

    /**
     * 根据软件ID查询镜像列表
     *
     * @param softId 软件ID，用于筛选特定软件的镜像，例如：1
     * @return 镜像列表，按创建时间倒序排列
     */
    @Override
    public List<SystemSoftImage> getImagesBySoftId(Integer softId) {
        log.info("根据软件ID查询镜像列表 - 软件ID: {}", softId);

        LambdaQueryWrapper<SystemSoftImage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SystemSoftImage::getSystemSoftId, softId)
                .orderByDesc(SystemSoftImage::getCreateTime);

        List<SystemSoftImage> result = systemSoftImageMapper.selectList(wrapper);
        log.info("根据软件ID查询镜像列表完成 - 软件ID: {}, 记录数: {}", softId, result.size());

        return result;
    }

    /**
     * 根据服务器ID查询镜像列表
     *
     * @param serverId 服务器ID，用于筛选特定服务器上的镜像，例如：1
     * @return 镜像列表，按创建时间倒序排列
     */
    @Override
    public List<SystemSoftImage> getImagesByServerId(Integer serverId) {
        log.info("根据服务器ID查询镜像列表 - 服务器ID: {}", serverId);

        LambdaQueryWrapper<SystemSoftImage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SystemSoftImage::getSystemSoftImageServerId, serverId)
                .orderByDesc(SystemSoftImage::getCreateTime);

        List<SystemSoftImage> result = systemSoftImageMapper.selectList(wrapper);
        log.info("根据服务器ID查询镜像列表完成 - 服务器ID: {}, 记录数: {}", serverId, result.size());

        return result;
    }

    /**
     * 同步单个镜像状态
     * 从远程Docker服务器获取最新镜像状态并同步到数据库
     *
     * @param imageId 镜像ID，用于标识需要同步的镜像，例如：1
     * @return 同步结果，成功返回true，失败返回false
     */
    @Override
    public boolean syncImageStatus(Integer imageId) {
        log.info("同步单个镜像状态 - 镜像ID: {}", imageId);

        try {
            SystemSoftImage image = systemSoftImageMapper.selectById(imageId);
            if (image == null) {
                log.warn("镜像未找到 - 镜像ID: {}", imageId);
                return false;
            }

            // TODO: 实现与Docker服务器的实际同步逻辑
            // 这里暂时模拟同步过程，实际应该调用Docker API获取镜像状态
            log.info("开始同步镜像状态 - 镜像名称: {}, 镜像标签: {}",
                    image.getSystemSoftImageName(), image.getSystemSoftImageTag());

            // 更新镜像状态和同步时间
            image.setSystemSoftImageStatus("AVAILABLE");
            image.setUpdateTime(LocalDateTime.now());

            int updateCount = systemSoftImageMapper.updateById(image);
            boolean success = updateCount > 0;

            log.info("同步单个镜像状态完成 - 镜像ID: {}, 结果: {}", imageId, success);
            return success;

        } catch (Exception e) {
            log.error("同步单个镜像状态失败 - 镜像ID: {}, 错误信息: {}", imageId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 同步所有镜像状态
     * 从远程Docker服务器获取最新镜像状态并同步到数据库
     *
     * @param serverId 服务器ID（可选），为null时同步所有服务器的镜像，例如：1
     * @return 同步的镜像数量
     */
    @Override
    public int syncAllImageStatus(Integer serverId) {
        log.info("同步所有镜像状态 - 服务器ID: {}", serverId);

        try {
            LambdaQueryWrapper<SystemSoftImage> wrapper = new LambdaQueryWrapper<>();
            if (serverId != null) {
                wrapper.eq(SystemSoftImage::getSystemSoftImageServerId, serverId);
            }

            List<SystemSoftImage> images = systemSoftImageMapper.selectList(wrapper);
            log.info("找到需要同步的镜像数量: {}", images.size());

            int syncCount = 0;
            for (SystemSoftImage image : images) {
                try {
                    // TODO: 实现与Docker服务器的实际同步逻辑
                    // 这里暂时模拟同步过程，实际应该调用Docker API获取镜像状态
                    log.debug("同步镜像 - 镜像ID: {}, 镜像名称: {}, 镜像标签: {}",
                            image.getSystemSoftImageId(), image.getSystemSoftImageName(), image.getSystemSoftImageTag());

                    // 更新镜像状态和同步时间
                    image.setSystemSoftImageStatus("AVAILABLE");
                    image.setUpdateTime(LocalDateTime.now());

                    int updateCount = systemSoftImageMapper.updateById(image);
                    if (updateCount > 0) {
                        syncCount++;
                    }

                } catch (Exception e) {
                    log.error("同步镜像失败 - 镜像ID: {}, 错误信息: {}",
                            image.getSystemSoftImageId(), e.getMessage(), e);
                }
            }

            log.info("同步所有镜像状态完成 - 服务器ID: {}, 总数: {}, 成功数: {}",
                    serverId, images.size(), syncCount);
            return syncCount;

        } catch (Exception e) {
            log.error("同步所有镜像状态失败 - 服务器ID: {}, 错误信息: {}", serverId, e.getMessage(), e);
            return 0;
        }
    }

    /**
     * 同步指定服务器的所有镜像状态
     *
     * @param serverId 服务器ID，用于标识需要同步的服务器，例如：1
     * @return 同步结果，成功返回true，失败返回false
     */
    @Override
    public boolean syncAllImagesByServerId(Integer serverId) {
        log.info("同步指定服务器的所有镜像状态 - 服务器ID: {}", serverId);

        try {
            int syncCount = syncAllImageStatus(serverId);
            boolean success = syncCount > 0;

            log.info("同步指定服务器的所有镜像状态完成 - 服务器ID: {}, 同步数量: {}, 结果: {}",
                    serverId, syncCount, success);
            return success;

        } catch (Exception e) {
            log.error("同步指定服务器的所有镜像状态失败 - 服务器ID: {}, 错误信息: {}", serverId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 根据镜像名称和标签查询镜像
     *
     * @param imageName 镜像名称，用于精确匹配镜像名称，例如："nginx"
     * @param imageTag  镜像标签，用于精确匹配镜像标签，例如："latest"
     * @param serverId  服务器ID，用于筛选特定服务器上的镜像，例如：1
     * @return 镜像信息，如果找到则返回镜像对象，否则返回null
     */
    @Override
    public SystemSoftImage getImageByNameAndTag(String imageName, String imageTag, Integer serverId) {
        log.info("根据镜像名称和标签查询镜像 - 镜像名称: {}, 镜像标签: {}, 服务器ID: {}",
                imageName, imageTag, serverId);

        LambdaQueryWrapper<SystemSoftImage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SystemSoftImage::getSystemSoftImageName, imageName)
                .eq(SystemSoftImage::getSystemSoftImageTag, imageTag)
                .eq(SystemSoftImage::getSystemSoftImageServerId, serverId);

        SystemSoftImage result = systemSoftImageMapper.selectOne(wrapper);
        log.info("根据镜像名称和标签查询镜像完成 - 镜像名称: {}, 镜像标签: {}, 服务器ID: {}, 结果: {}",
                imageName, imageTag, serverId, result != null ? "找到" : "未找到");

        return result;
    }

    /**
     * 检查镜像是否存在
     *
     * @param imageName 镜像名称，用于精确匹配镜像名称，例如："nginx"
     * @param imageTag  镜像标签，用于精确匹配镜像标签，例如："latest"
     * @param serverId  服务器ID，用于筛选特定服务器上的镜像，例如：1
     * @return 是否存在，存在返回true，不存在返回false
     */
    @Override
    public boolean checkImageExists(String imageName, String imageTag, Integer serverId) {
        log.info("检查镜像是否存在 - 镜像名称: {}, 镜像标签: {}, 服务器ID: {}",
                imageName, imageTag, serverId);

        SystemSoftImage image = getImageByNameAndTag(imageName, imageTag, serverId);
        boolean exists = image != null;

        log.info("检查镜像是否存在完成 - 镜像名称: {}, 镜像标签: {}, 服务器ID: {}, 是否存在: {}",
                imageName, imageTag, serverId, exists);

        return exists;
    }

    /**
     * 获取可用的镜像列表
     *
     * @return 可用镜像列表，按创建时间倒序排列
     */
    @Override
    public List<SystemSoftImage> getAvailableImages() {
        log.info("获取可用的镜像列表");

        LambdaQueryWrapper<SystemSoftImage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SystemSoftImage::getSystemSoftImageStatus, "AVAILABLE")
                .orderByDesc(SystemSoftImage::getCreateTime);

        List<SystemSoftImage> result = systemSoftImageMapper.selectList(wrapper);
        log.info("获取可用的镜像列表完成 - 记录数: {}", result.size());

        return result;
    }

    /**
     * 获取异常状态的镜像列表
     *
     * @return 异常镜像列表，按创建时间倒序排列
     */
    @Override
    public List<SystemSoftImage> getAbnormalImages() {
        log.info("获取异常状态的镜像列表");

        LambdaQueryWrapper<SystemSoftImage> wrapper = new LambdaQueryWrapper<>();
        wrapper.ne(SystemSoftImage::getSystemSoftImageStatus, "AVAILABLE")
                .orderByDesc(SystemSoftImage::getCreateTime);

        List<SystemSoftImage> result = systemSoftImageMapper.selectList(wrapper);
        log.info("获取异常状态的镜像列表完成 - 记录数: {}", result.size());

        return result;
    }

    /**
     * 根据关键字搜索镜像
     *
     * @param keyword 关键字，用于模糊搜索镜像名称、标签或服务器名称，例如："nginx"
     * @return 镜像列表，按创建时间倒序排列
     */
    @Override
    public List<SystemSoftImage> searchImagesByKeyword(String keyword) {
        log.info("根据关键字搜索镜像 - 关键字: {}", keyword);

        LambdaQueryWrapper<SystemSoftImage> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(SystemSoftImage::getSystemSoftImageName, keyword)
                    .or().like(SystemSoftImage::getSystemSoftImageTag, keyword)
                    .or().like(SystemSoftImage::getSystemSoftImageServerName, keyword));
        }
        wrapper.orderByDesc(SystemSoftImage::getCreateTime);

        List<SystemSoftImage> result = systemSoftImageMapper.selectList(wrapper);
        log.info("根据关键字搜索镜像完成 - 关键字: {}, 记录数: {}", keyword, result.size());

        return result;
    }

    /**
     * 更新镜像状态
     *
     * @param imageId 镜像ID，用于标识需要更新的镜像，例如：1
     * @param status  新状态，用于更新镜像的状态，例如："AVAILABLE"
     * @return 更新结果，成功返回true，失败返回false
     */
    @Override
    public boolean updateImageStatus(Integer imageId, String status) {
        log.info("更新镜像状态 - 镜像ID: {}, 状态: {}", imageId, status);

        try {
            SystemSoftImage image = systemSoftImageMapper.selectById(imageId);
            if (image == null) {
                log.warn("镜像未找到 - 镜像ID: {}", imageId);
                return false;
            }

            image.setSystemSoftImageStatus(status);
            image.setUpdateTime(LocalDateTime.now());

            int updateCount = systemSoftImageMapper.updateById(image);
            boolean success = updateCount > 0;

            log.info("更新镜像状态完成 - 镜像ID: {}, 状态: {}, 结果: {}", imageId, status, success);
            return success;

        } catch (Exception e) {
            log.error("更新镜像状态失败 - 镜像ID: {}, 状态: {}, 错误信息: {}", imageId, status, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 批量更新镜像状态
     *
     * @param imageIds 镜像ID列表，用于标识需要更新的镜像，例如：[1, 2, 3]
     * @param status   新状态，用于更新镜像的状态，例如："AVAILABLE"
     * @return 更新结果，成功返回true，失败返回false
     */
    @Override
    public boolean batchUpdateImageStatus(List<Integer> imageIds, String status) {
        log.info("批量更新镜像状态 - 镜像ID列表: {}, 状态: {}", imageIds, status);

        if (imageIds == null || imageIds.isEmpty()) {
            log.warn("镜像ID列表为空");
            return false;
        }

        try {
            int successCount = 0;
            for (Integer imageId : imageIds) {
                if (updateImageStatus(imageId, status)) {
                    successCount++;
                }
            }

            boolean success = successCount == imageIds.size();
            log.info("批量更新镜像状态完成 - 总数: {}, 成功数: {}, 结果: {}",
                    imageIds.size(), successCount, success);

            return success;

        } catch (Exception e) {
            log.error("批量更新镜像状态失败 - 镜像ID列表: {}, 状态: {}, 错误信息: {}",
                    imageIds, status, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 删除镜像记录
     *
     * @param imageId 镜像ID，用于标识需要删除的镜像，例如：1
     * @return 删除结果，成功返回true，失败返回false
     */
    @Override
    public boolean deleteImage(Integer imageId) {
        log.info("删除镜像记录 - 镜像ID: {}", imageId);

        try {
            int deleteCount = systemSoftImageMapper.deleteById(imageId);
            boolean success = deleteCount > 0;

            log.info("删除镜像记录完成 - 镜像ID: {}, 结果: {}", imageId, success);
            return success;

        } catch (Exception e) {
            log.error("删除镜像记录失败 - 镜像ID: {}, 错误信息: {}", imageId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 根据软件ID删除所有镜像记录
     *
     * @param softId 软件ID，用于标识需要删除的软件的所有镜像，例如：1
     * @return 删除结果，成功返回true，失败返回false
     */
    @Override
    public boolean deleteImagesBySoftId(Integer softId) {
        log.info("根据软件ID删除所有镜像记录 - 软件ID: {}", softId);

        try {
            LambdaQueryWrapper<SystemSoftImage> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SystemSoftImage::getSystemSoftId, softId);

            int deleteCount = systemSoftImageMapper.delete(wrapper);
            log.info("根据软件ID删除所有镜像记录完成 - 软件ID: {}, 删除数量: {}", softId, deleteCount);

            return true;

        } catch (Exception e) {
            log.error("根据软件ID删除所有镜像记录失败 - 软件ID: {}, 错误信息: {}", softId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 根据服务器ID删除所有镜像记录
     *
     * @param serverId 服务器ID，用于标识需要删除的服务器的所有镜像，例如：1
     * @return 删除结果，成功返回true，失败返回false
     */
    @Override
    public boolean deleteImagesByServerId(Integer serverId) {
        log.info("根据服务器ID删除所有镜像记录 - 服务器ID: {}", serverId);

        try {
            LambdaQueryWrapper<SystemSoftImage> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SystemSoftImage::getSystemSoftImageServerId, serverId);

            int deleteCount = systemSoftImageMapper.delete(wrapper);
            log.info("根据服务器ID删除所有镜像记录完成 - 服务器ID: {}, 删除数量: {}", serverId, deleteCount);

            return true;

        } catch (Exception e) {
            log.error("根据服务器ID删除所有镜像记录失败 - 服务器ID: {}, 错误信息: {}", serverId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 统计镜像数量
     *
     * @param serverId 服务器ID（可选），用于筛选特定服务器上的镜像，例如：1
     * @param status   状态（可选），用于筛选特定状态的镜像，例如："AVAILABLE"
     * @return 镜像数量
     */
    @Override
    public Long countImages(Integer serverId, String status) {
        log.info("统计镜像数量 - 服务器ID: {}, 状态: {}", serverId, status);

        LambdaQueryWrapper<SystemSoftImage> wrapper = new LambdaQueryWrapper<>();
        if (serverId != null) {
            wrapper.eq(SystemSoftImage::getSystemSoftImageServerId, serverId);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(SystemSoftImage::getSystemSoftImageStatus, status);
        }

        Long count = systemSoftImageMapper.selectCount(wrapper);
        log.info("统计镜像数量完成 - 服务器ID: {}, 状态: {}, 数量: {}", serverId, status, count);

        return count;
    }

    /**
     * 获取最新拉取的镜像列表
     *
     * @param limit 限制数量，用于限制返回的镜像数量，例如：10
     * @return 最新镜像列表，按创建时间倒序排列
     */
    @Override
    public List<SystemSoftImage> getLatestImages(Integer limit) {
        log.info("获取最新拉取的镜像列表 - 限制数量: {}", limit);

        LambdaQueryWrapper<SystemSoftImage> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(SystemSoftImage::getCreateTime)
                .last("LIMIT " + limit);

        List<SystemSoftImage> result = systemSoftImageMapper.selectList(wrapper);
        log.info("获取最新拉取的镜像列表完成 - 限制数量: {}, 记录数: {}", limit, result.size());

        return result;
    }
}