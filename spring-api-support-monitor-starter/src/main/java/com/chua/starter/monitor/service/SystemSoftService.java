package com.chua.starter.monitor.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.chua.starter.monitor.entity.SystemSoft;
import com.chua.starter.monitor.entity.SystemSoftImage;
import com.chua.starter.monitor.entity.SystemSoftRecord;

import java.util.List;
import java.util.Map;

/**
 * 系统软件服务接口
 *
 * @author CH
 * @version 1.0.0
 * @since 2024/01/01
 */
public interface SystemSoftService extends IService<SystemSoft> {

    /**
     * 分页查询软件列表
     *
     * @param page 分页参数
     * @param softCode 软件代码
     * @param softName 软件名称
     * @param softCategory 软件分类
     * @param softStatus 软件状态
     * @return 分页结果
     */
    IPage<SystemSoft> selectSoftPage(Page<SystemSoft> page, String softCode, String softName,
                                     String softCategory, Integer softStatus);

    /**
     * 根据软件代码查询软件信息
     *
     * @param softCode 软件代码
     * @return 软件信息
     */
    SystemSoft selectBySoftCode(String softCode);

    /**
     * 根据分类查询软件列表
     *
     * @param category 分类
     * @return 软件列表
     */
    List<SystemSoft> selectByCategory(String category);

    /**
     * 同步软件镜像
     *
     * @param softId   软件ID
     * @param serverId 服务器ID
     * @return 同步结果
     */
    boolean syncSoftImage(Long softId, Long serverId);

    /**
     * 拉取软件镜像
     *
     * @param softId   软件ID
     * @param serverId 服务器ID
     * @param imageTag 镜像标签
     * @return 拉取结果
     */
    boolean pullSoftImage(Long softId, Long serverId, String imageTag);

    /**
     * 删除软件镜像
     *
     * @param softId   软件ID
     * @param serverId 服务器ID
     * @param imageId  镜像ID
     * @return 删除结果
     */
    boolean deleteSoftImage(Long softId, Long serverId, String imageId);

    /**
     * 验证软件配置
     *
     * @param systemSoft 软件信息
     * @return 验证结果
     */
    boolean validateSoftConfig(SystemSoft systemSoft);

}