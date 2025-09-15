package com.chua.starter.monitor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.monitor.entity.SystemSoftRegistry;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 系统软件镜像仓库信息Mapper接口
 *
 * @author CH
 * @since 2025/01/15
 */
@Mapper
public interface SystemSoftRegistryMapper extends BaseMapper<SystemSoftRegistry> {

    /**
     * 根据仓库名称查询仓库信息
     *
     * @param registryName 仓库名称
     * @return 仓库信息
     */
    SystemSoftRegistry selectByRegistryName(@Param("registryName") String registryName);

    /**
     * 根据仓库类型查询仓库列表
     *
     * @param registryType 仓库类型
     * @return 仓库列表
     */
    List<SystemSoftRegistry> selectByRegistryType(@Param("registryType") String registryType);

    /**
     * 根据仓库地址查询仓库信息
     *
     * @param registryUrl 仓库地址
     * @return 仓库信息
     */
    SystemSoftRegistry selectByRegistryUrl(@Param("registryUrl") String registryUrl);

    /**
     * 查询默认仓库
     *
     * @return 默认仓库信息
     */
    SystemSoftRegistry selectDefaultRegistry();

    /**
     * 查询所有启用的仓库
     *
     * @return 启用的仓库列表
     */
    List<SystemSoftRegistry> selectEnabledRegistries();

    /**
     * 根据连接状态查询仓库列表
     *
     * @param connectStatus 连接状态
     * @return 仓库列表
     */
    List<SystemSoftRegistry> selectByConnectStatus(@Param("connectStatus") Integer connectStatus);

    /**
     * 根据仓库类型统计仓库数量
     *
     * @param registryType 仓库类型
     * @return 仓库数量
     */
    Long countByRegistryType(@Param("registryType") String registryType);

    /**
     * 根据状态统计仓库数量
     *
     * @param status 仓库状态
     * @return 仓库数量
     */
    Long countByStatus(@Param("status") Integer status);

    /**
     * 检查仓库名称是否存在
     *
     * @param registryName 仓库名称
     * @param excludeId    排除的ID
     * @return 存在的数量
     */
    Long checkRegistryNameExists(@Param("registryName") String registryName,
                                 @Param("excludeId") Integer excludeId);

    /**
     * 检查仓库地址是否存在
     *
     * @param registryUrl 仓库地址
     * @param excludeId   排除的ID
     * @return 存在的数量
     */
    Long checkRegistryUrlExists(@Param("registryUrl") String registryUrl,
                                @Param("excludeId") Integer excludeId);

    /**
     * 批量更新仓库状态
     *
     * @param registryIds 仓库ID列表
     * @param status      新状态
     * @return 更新的记录数
     */
    Integer batchUpdateStatus(@Param("registryIds") List<Integer> registryIds,
                              @Param("status") Integer status);

    /**
     * 更新默认仓库标识
     *
     * @param registryId 仓库ID
     * @return 更新的记录数
     */
    Integer updateDefaultFlag(@Param("registryId") Integer registryId);

    /**
     * 清除所有默认仓库标识
     *
     * @return 更新的记录数
     */
    Integer clearAllDefaultFlags();

    /**
     * 更新仓库连接状态
     *
     * @param registryId      仓库ID
     * @param connectStatus   连接状态
     * @param errorMessage    错误信息
     * @param lastConnectTime 最后连接时间
     * @return 更新的记录数
     */
    Integer updateConnectStatus(@Param("registryId") Integer registryId,
                                @Param("connectStatus") Integer connectStatus,
                                @Param("errorMessage") String errorMessage,
                                @Param("lastConnectTime") String lastConnectTime);

    /**
     * 根据关键字搜索仓库
     *
     * @param keyword 关键字
     * @return 仓库信息列表
     */
    List<SystemSoftRegistry> searchByKeyword(@Param("keyword") String keyword);

    /**
     * 获取连接正常的仓库列表
     *
     * @return 连接正常的仓库列表
     */
    List<SystemSoftRegistry> selectConnectedRegistries();

    /**
     * 获取连接异常的仓库列表
     *
     * @return 连接异常的仓库列表
     */
    List<SystemSoftRegistry> selectDisconnectedRegistries();

    /**
     * 根据排序号查询仓库列表
     *
     * @return 按排序号排序的仓库列表
     */
    List<SystemSoftRegistry> selectOrderBySort();

    /**
     * 获取最近添加的仓库列表
     *
     * @param limit 限制数量
     * @return 最近添加的仓库列表
     */
    List<SystemSoftRegistry> selectLatestRegistries(@Param("limit") Integer limit);
}