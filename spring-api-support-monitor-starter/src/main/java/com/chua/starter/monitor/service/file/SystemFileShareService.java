package com.chua.starter.monitor.service.file;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chua.starter.monitor.entity.SystemFileShare;

/**
 * 文件分享服务接口
 *
 * @author CH
 * @since 2025-12-08
 */
public interface SystemFileShareService extends IService<SystemFileShare> {

    /**
     * 根据分享ID获取分享记录
     *
     * @param shareId 分享ID
     * @return 分享记录
     */
    SystemFileShare getByShareId(String shareId);

    /**
     * 创建分享链接
     *
     * @param serverId      服务器ID
     * @param bucket        存储桶
     * @param filePath      文件路径
     * @param fileName      文件名
     * @param expireMinutes 过期时间（分钟），0 表示永不过期
     * @return 分享记录
     */
    SystemFileShare createShareLink(Long serverId, String bucket, String filePath, String fileName, Integer expireMinutes);

    /**
     * 创建闪图（阅后即焚）
     *
     * @param serverId 服务器ID
     * @param bucket   存储桶
     * @param filePath 文件路径
     * @param fileName 文件名
     * @return 分享记录
     */
    SystemFileShare createFlashImage(Long serverId, String bucket, String filePath, String fileName);

    /**
     * 增加查看次数
     *
     * @param shareId 分享ID
     */
    void incrementViewCount(String shareId);

    /**
     * 标记分享为已删除
     *
     * @param shareId 分享ID
     */
    void markDeleted(String shareId);

    /**
     * 清理过期的分享记录
     *
     * @return 清理的记录数
     */
    int cleanExpiredShares();

    /**
     * 生成短链接 URL
     *
     * @param baseUrl 服务器基础 URL
     * @param shareId 分享ID
     * @return 短链接
     */
    String generateShortUrl(String baseUrl, String shareId);
}
