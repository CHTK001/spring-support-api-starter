package com.chua.starter.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.entity.system.SysAnnouncementRead;
import org.apache.ibatis.annotations.Mapper;

/**
* 公告已读记录 映射器接口
*
* @since 4.0.0.50
* @author CH
 */
@Mapper
public interface SysAnnouncementReadMapper extends BaseMapper<SysAnnouncementRead> {
}
