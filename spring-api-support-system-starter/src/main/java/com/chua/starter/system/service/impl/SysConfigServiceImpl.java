package com.chua.starter.system.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.starter.system.mapper.SysConfigMapper;
import com.chua.starter.system.entity.SysConfig;
import com.chua.starter.system.service.SysConfigService;
/**
 *
 * @since 2024/8/28
 * @author CH    
 */
@Service
public class SysConfigServiceImpl extends ServiceImpl<SysConfigMapper, SysConfig> implements SysConfigService{

}
