package com.chua.starter.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.starter.service.SysServiceItemService;
import com.chua.starter.service.mapper.SysServiceItemMapper;
import com.chua.starter.system.entity.SysServiceModuleItem;
import org.springframework.stereotype.Service;

/**
 * 服务子项
 *
 * @author chunyang.leng
 * @since 2022-03-26
 */
@Service
public class SysServiceItemServiceImpl extends ServiceImpl<SysServiceItemMapper, SysServiceModuleItem> implements SysServiceItemService {


}
