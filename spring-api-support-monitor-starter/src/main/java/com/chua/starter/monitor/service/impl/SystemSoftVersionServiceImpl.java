package com.chua.starter.monitor.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.starter.monitor.mapper.SystemSoftVersionMapper;
import com.chua.starter.monitor.entity.SystemSoftVersion;
import com.chua.starter.monitor.service.SystemSoftVersionService;
/**
 *
 * @author CH
 * @since 2025/10/21 13:38
 */
@Service
public class SystemSoftVersionServiceImpl extends ServiceImpl<SystemSoftVersionMapper, SystemSoftVersion> implements SystemSoftVersionService{

}
