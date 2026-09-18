package com.chua.starter.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.entity.system.SysMessageTemplate;
import org.apache.ibatis.annotations.Mapper;

/**
 * 消息模板 映射器接口
 *
 * @since 4.0.0.50
 * @author CH
 */
@Mapper
public interface SysMessageTemplateMapper extends BaseMapper<SysMessageTemplate> {
}
