package com.chua.starter.monitor.service;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.entity.SystemSoft;
import com.chua.starter.monitor.entity.SystemSoftImage;

import java.util.List;
import java.util.Map;

/**
 * 系统软件管理服务接口
 *
 * @author CH
 * @since 2025/01/15
 */
public interface SystemSoftService {

    /**
     * 根据软件代码查询软件信息
     *
     * @param softCode 软件代码
     * @return 软件信息
     */
    ReturnResult<SystemSoft> getBySoftCode(String softCode);

    /**
     * 根据条件查询软件列表
     *
     * @param softCode 软件代码
     * @param softName 软件名称
     * @param category 软件分类
     * @param status   软件状态
     * @return 软件列表
     */
    ReturnResult<List<SystemSoft>> selectSoftList(String softCode, String softName, String category, Integer status);

    /**
     * 保存或更新软件信息
     *
     * @param systemSoft 软件信息
     * @return 保存结果
     */
    ReturnResult<SystemSoft> saveOrUpdate(SystemSoft systemSoft);

    /**
     * 根据ID删除软件
     *
     * @param softId 软件ID
     * @return 删除结果
     */
    ReturnResult<Boolean> removeById(Integer softId);

    /**
     * 批量删除软件
     *
     * @param softIds 软件ID列表
     * @return 删除结果
     */
    ReturnResult<Boolean> removeByIds(List<Integer> softIds);

    /**
     * 从仓库同步软件信息
     *
     * @param registryId 仓库ID
     * @return 同步的软件数量
     */
    ReturnResult<Integer> syncFromRegistry(Integer registryId);

    /**
     * 同步单个软件信息
     *
     * @param registryId   仓库ID
     * @param softwareName 软件名称
     * @return 同步的软件信息
     */
    ReturnResult<SystemSoft> syncSingleSoftware(Integer registryId, String softwareName);

    /**
     * 安装软件到指定服务器
     *
     * @param softId    软件ID
     * @param serverIds 服务器ID列表
     * @param imageTag  镜像标签
     * @return 安装结果
     */
    ReturnResult<Boolean> installSoftware(Integer softId, List<Integer> serverIds, String imageTag);

    /**
     * 获取软件的可用标签列表
     *
     * @param softId 软件ID
     * @return 标签列表
     */
    ReturnResult<List<String>> getAvailableTags(Integer softId);

    /**
     * 检查软件代码是否已存在
     *
     * @param softCode  软件代码
     * @param excludeId 排除的ID
     * @return 是否存在
     */
    ReturnResult<Boolean> checkSoftCodeExists(String softCode, Integer excludeId);

    /**
     * 批量更新软件状态
     *
     * @param softIds 软件ID列表
     * @param status  新状态
     * @return 更新结果
     */
    ReturnResult<Boolean> batchUpdateStatus(List<Integer> softIds, Integer status);

    /**
     * 根据关键字搜索软件
     *
     * @param keyword 关键字
     * @return 软件列表
     */
    ReturnResult<List<SystemSoft>> searchByKeyword(String keyword);

    /**
     * 根据分类查询软件列表
     *
     * @param category 分类
     * @return 软件列表
     */
    ReturnResult<List<SystemSoft>> getByCategory(String category);

    /**
     * 获取热门软件列表
     *
     * @param limit 限制数量
     * @return 热门软件列表
     */
    ReturnResult<List<SystemSoft>> getPopularSofts(Integer limit);

    /**
     * 获取最新软件列表
     *
     * @param limit 限制数量
     * @return 最新软件列表
     */
    ReturnResult<List<SystemSoft>> getLatestSofts(Integer limit);

    /**
     * 拉取Docker镜像（支持异步回调）
     *
     * @param softId   软件ID
     * @param serverId 服务器ID
     * @param imageTag 镜像标签
     * @param params   拉取参数
     * @return 拉取结果
     */
    ReturnResult<SystemSoftImage> pullDockerImageWithCallback(Integer softId, Integer serverId, String imageTag, Map<String, Object> params);
}