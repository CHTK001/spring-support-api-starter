package com.chua.starter.monitor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.monitor.entity.MonitorSysGenScriptCategory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 脚本分类Mapper
 * 
 * @author CH
 * @since 2024/12/19
 */
@Mapper
public interface MonitorSysGenScriptCategoryMapper extends BaseMapper<MonitorSysGenScriptCategory> {

        /**
         * 根据分类名称查找分类
         *
         * @param categoryName 分类名称
         * @return 脚本分类
         */
        MonitorSysGenScriptCategory findByCategoryName(@Param("categoryName") String categoryName);

        /**
         * 查找所有启用的分类
         *
         * @return 启用的分类列表
         */
        List<MonitorSysGenScriptCategory> findEnabledCategories();

        /**
         * 查找根分类
         *
         * @return 根分类列表
         */
        List<MonitorSysGenScriptCategory> findRootCategories();

        /**
         * 根据父分类ID查找子分类
         *
         * @param parentId 父分类ID
         * @return 子分类列表
         */
        List<MonitorSysGenScriptCategory> findByParentId(@Param("parentId") Integer parentId);

        /**
         * 获取分类树结构
         *
         * @return 分类树列表
         */
        List<MonitorSysGenScriptCategory> findCategoryTree();

        /**
         * 检查分类名称是否存在
         *
         * @param categoryName 分类名称
         * @param excludeId    排除的ID
         * @return 存在的数量
         */
        int countByCategoryName(@Param("categoryName") String categoryName, @Param("excludeId") Integer excludeId);

        /**
         * 获取分类下的脚本数量
         *
         * @param categoryName 分类名称
         * @return 脚本数量
         */
        int countScriptsByCategory(@Param("categoryName") String categoryName);
}
