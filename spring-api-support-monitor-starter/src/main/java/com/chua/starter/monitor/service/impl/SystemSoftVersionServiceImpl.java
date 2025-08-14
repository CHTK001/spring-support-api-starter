package com.chua.starter.monitor.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.starter.monitor.entity.SystemSoftVersion;
import com.chua.starter.monitor.mapper.SystemSoftVersionMapper;
import com.chua.starter.monitor.service.SystemSoftVersionService;
import org.springframework.stereotype.Service;

/**
 * 软件版本服务实现
 */
@Service
public class SystemSoftVersionServiceImpl extends ServiceImpl<SystemSoftVersionMapper, SystemSoftVersion> implements SystemSoftVersionService {
}

