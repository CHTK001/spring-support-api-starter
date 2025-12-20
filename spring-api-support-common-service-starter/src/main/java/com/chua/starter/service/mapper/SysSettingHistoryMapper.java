package com.chua.starter.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.entity.service.SysSettingHistory;
import org.apache.ibatis.annotations.Mapper;

/**
 * 系统配置变更历史Mapper
 *
 * @author CH
 * @since 2024/12/20
 */
@Mapper
public interface SysSettingHistoryMapper extends BaseMapper<SysSettingHistory> {
}
