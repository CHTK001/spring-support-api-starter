package com.chua.spider.support.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.spider.support.entity.SpiderDefinition;
import org.apache.ibatis.annotations.Mapper;

/**
 * 爬虫定义 Mapper。
 *
 * @author CH
 * @since 4.0.0.42
 */
@Mapper
public interface SpiderDefinitionMapper extends BaseMapper<SpiderDefinition> {
}
