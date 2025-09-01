package com.chua.starter.monitor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.monitor.entity.SystemDataSetting;
import com.chua.starter.monitor.pojo.ServerStatic;
import org.apache.ibatis.annotations.Mapper;

/**
 * 系统数据配置映射接口
 *
 * @author CH
 * @since 2025/9/1 13:31
 */
@Mapper
public interface SystemDataSettingMapper extends BaseMapper<SystemDataSetting> {
    /**
     * 获取服务器静态信息
     *
     * @return ServerStatic 服务器静态信息对象，包含数据源类型等信息
     * @example ServerStatic staticInfo = systemDataSettingMapper.getServerStatic();
     */
    ServerStatic getServerStatic();
}


