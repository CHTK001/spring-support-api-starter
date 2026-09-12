package com.chua.starter.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.entity.service.SysMessage;
import org.apache.ibatis.annotations.Mapper;

/**
* 系统消息 映射器
*
* @since 4.0.0.42
* @author CH
 */
@Mapper
public interface SysMessageMapper extends BaseMapper<SysMessage> {
}
