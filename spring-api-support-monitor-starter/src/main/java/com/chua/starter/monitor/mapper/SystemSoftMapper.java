package com.chua.starter.monitor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.monitor.entity.SystemSoft;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 系统软件信息Mapper接口
 *
 * @author CH
 * @since 2025/01/15
 */
@Mapper
public interface SystemSoftMapper extends BaseMapper<SystemSoft> {

    /**
     * 根据软件代码查询软件信息
     *
     * @param softCode 软件代码
     * @return 软件信息
     */
    SystemSoft selectBySoftCode(@Param("softCode") String softCode);

    /**
     * 根据软件名称查询软件信息
     *
     * @param softName 软件名称
     * @return 软件信息列表
     */
    List<SystemSoft> selectBySoftName(@Param("softName") String softName);

    /**
     * 根据软件分类查询软件列表
     *
     * @param category 软件分类
     * @return 软件信息列表
     */
    List<SystemSoft> selectByCategory(@Param("category") String category);

    /**
     * 查询所有启用的软件
     *
     * @return 启用的软件列表
     */
    List<SystemSoft> selectEnabledSofts();

    /**
     * 查询官方软件列表
     *
     * @return 官方软件列表
     */
    List<SystemSoft> selectOfficialSofts();

    /**
     * 根据Docker镜像名称查询软件
     *
     * @param dockerImage Docker镜像名称
     * @return 软件信息
     */
    SystemSoft selectByDockerImage(@Param("dockerImage") String dockerImage);

    /**
     * 根据镜像仓库信息查询软件列表
     *
     * @param registryType 仓库类型
     * @param registryUrl  仓库地址
     * @return 软件信息列表
     */
    List<SystemSoft> selectByRegistry(@Param("registryType") String registryType,
                                      @Param("registryUrl") String registryUrl);

    /**
     * 根据标签查询软件列表
     *
     * @param tag 标签
     * @return 软件信息列表
     */
    List<SystemSoft> selectByTag(@Param("tag") String tag);

    /**
     * 统计软件总数
     *
     * @return 软件总数
     */
    Long countTotalSofts();

    /**
     * 根据分类统计软件数量
     *
     * @param category 软件分类
     * @return 软件数量
     */
    Long countByCategory(@Param("category") String category);

    /**
     * 根据状态统计软件数量
     *
     * @param status 软件状态
     * @return 软件数量
     */
    Long countByStatus(@Param("status") Integer status);

    /**
     * 检查软件代码是否存在
     *
     * @param softCode  软件代码
     * @param excludeId 排除的ID
     * @return 存在的数量
     */
    Long checkSoftCodeExists(@Param("softCode") String softCode,
                             @Param("excludeId") Integer excludeId);

    /**
     * 批量更新软件状态
     *
     * @param softIds 软件ID列表
     * @param status  新状态
     * @return 更新的记录数
     */
    Integer batchUpdateStatus(@Param("softIds") List<Integer> softIds,
                              @Param("status") Integer status);

    /**
     * 根据关键字搜索软件
     *
     * @param keyword 关键字
     * @return 软件信息列表
     */
    List<SystemSoft> searchByKeyword(@Param("keyword") String keyword);

    /**
     * 获取热门软件列表
     *
     * @param limit 限制数量
     * @return 热门软件列表
     */
    List<SystemSoft> selectPopularSofts(@Param("limit") Integer limit);

    /**
     * 获取最新添加的软件列表
     *
     * @param limit 限制数量
     * @return 最新软件列表
     */
    List<SystemSoft> selectLatestSofts(@Param("limit") Integer limit);
}