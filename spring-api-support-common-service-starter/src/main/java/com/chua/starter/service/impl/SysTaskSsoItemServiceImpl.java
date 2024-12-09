package com.chua.starter.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qiren.starter.service.SysTaskSsoItemService;
import com.qiren.starter.service.mapper.SysTaskSsoItemMapper;
import com.qiren.starter.system.entity.SysTaskSsoItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 *
 * 分片子表
 * @since 2024/12/4
 * @author CH    
 */
@Service
@RequiredArgsConstructor
public class SysTaskSsoItemServiceImpl extends ServiceImpl<SysTaskSsoItemMapper, SysTaskSsoItem> implements SysTaskSsoItemService {

}
