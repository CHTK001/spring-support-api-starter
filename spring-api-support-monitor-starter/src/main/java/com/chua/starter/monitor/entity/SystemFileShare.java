package com.chua.starter.monitor.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 文件分享记录实体
 * 用于存储分享链接和闪图信息
 *
 * @author CH
 * @since 2025-12-08
 */
@Data
@TableName("monitor_sys_file_share")
public class SystemFileShare {

    /**
     * 分享ID（UUID）
     */
    @TableId(type = IdType.ASSIGN_UUID)
    private String shareId;

    /**
     * 服务器ID
     */
    private Long serverId;

    /**
     * 存储桶
     */
    private String bucket;

    /**
     * 文件路径
     */
    private String filePath;

    /**
     * 文件名
     */
    private String fileName;

    /**
     * 分享类型：SHARE-普通分享，FLASH-闪图（阅后即焚）
     */
    private String shareType;

    /**
     * 过期时间，NULL 表示永不过期
     */
    private Date expireTime;

    /**
     * 查看次数
     */
    private Integer viewCount;

    /**
     * 最大查看次数，-1 表示无限制，1 表示闪图
     */
    private Integer maxViewCount;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 是否已删除：0-否，1-是
     */
    private Integer isDeleted;
}
