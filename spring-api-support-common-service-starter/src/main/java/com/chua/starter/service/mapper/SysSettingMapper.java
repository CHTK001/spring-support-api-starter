package com.chua.starter.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.system.entity.SysSetting;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author CH
 * @since 2024/8/9
 */
@Mapper
public interface SysSettingMapper extends BaseMapper<SysSetting> {
}