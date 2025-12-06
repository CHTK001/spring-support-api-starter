package com.chua.starter.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.service.entity.SysMessageHistory;
import org.apache.ibatis.annotations.Mapper;

/**
 * 系统消息历史记录Mapper接口
 *
 * @author CH
 * @since 2024/12/04
 */
@Mapper
public interface SysMessageHistoryMapper extends BaseMapper<SysMessageHistory> {
}
