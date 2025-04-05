package com.chua.starter.monitor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.monitor.entity.MaintenanceFile;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 维护文件Mapper接口
 *
 * @author CH
 */
@Mapper
public interface MaintenanceFileMapper extends BaseMapper<MaintenanceFile> {

    /**
     * 根据维护组ID获取文件列表
     *
     * @param groupId 维护组ID
     * @param status  状态
     * @return 文件列表
     */
    List<MaintenanceFile> selectFilesByGroupId(@Param("groupId") Long groupId, @Param("status") Integer status);

    /**
     * 根据ID获取文件
     *
     * @param maintenanceFileId 文件ID
     * @return 文件
     */
    MaintenanceFile selectFileById(@Param("maintenanceFileId") Long maintenanceFileId);
}