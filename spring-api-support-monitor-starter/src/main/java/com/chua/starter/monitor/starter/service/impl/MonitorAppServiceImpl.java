package com.chua.starter.monitor.starter.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.starter.monitor.starter.entity.MonitorApplication;
import com.chua.starter.monitor.starter.mapper.MonitorAppMapper;
import com.chua.starter.monitor.starter.service.MonitorAppService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 监视器应用
 * @author Administrator
 */
@Service
@RequiredArgsConstructor
public class MonitorAppServiceImpl extends ServiceImpl<MonitorAppMapper, MonitorApplication> implements MonitorAppService {

}
