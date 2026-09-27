package com.chua.starter.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.entity.system.SysMessageRecipient;
import org.apache.ibatis.annotations.Mapper;

/**
 * 消息接收人送达记录 Mapper
 *
 * @author CH
 * @since 4.0.0.52
 */
@Mapper
public interface SysMessageRecipientMapper extends BaseMapper<SysMessageRecipient> {
}
