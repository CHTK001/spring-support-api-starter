package com.chua.starter.monitor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.monitor.entity.SoftServiceInstall;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 软件服务安装记录Mapper
 *
 * @author CH
 * @since 2024/7/19
 */
@Mapper
public interface SoftServiceInstallMapper extends BaseMapper<SoftServiceInstall> {

    /**
     * 根据SSH ID查询安装记录
     *
     * @param sshId SSH ID
     * @return 安装记录列表
     */
    List<SoftServiceInstall> selectBySshId(@Param("sshId") String sshId);
}