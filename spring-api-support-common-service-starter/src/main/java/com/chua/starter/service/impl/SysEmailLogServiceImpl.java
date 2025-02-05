package com.chua.starter.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.starter.service.SysEmailLogService;
import com.chua.starter.service.mapper.SysEmailLogMapper;
import com.chua.starter.system.entity.SysEmailLog;
import org.springframework.stereotype.Service;

@Service
public class SysEmailLogServiceImpl extends ServiceImpl<SysEmailLogMapper, SysEmailLog> implements SysEmailLogService {

}
