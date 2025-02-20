package com.chua.starter.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.starter.system.entity.SysEmailLog;
import com.chua.starter.system.mapper.SysEmailLogMapper;
import com.chua.starter.system.service.SysEmailLogService;
import org.springframework.stereotype.Service;

@Service
public class SysEmailLogServiceImpl extends ServiceImpl<SysEmailLogMapper, SysEmailLog> implements SysEmailLogService {

}
