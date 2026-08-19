package com.chua.starter.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.entity.service.SysSetting;
import org.apache.ibatis.annotations.Mapper;

/**
 * 系统设置映射器接口
 * 
 * @since 2024/8/9
 * @author CH
 */
@Mapper
public interface SysSettingMapper extends BaseMapper<SysSetting> {
}