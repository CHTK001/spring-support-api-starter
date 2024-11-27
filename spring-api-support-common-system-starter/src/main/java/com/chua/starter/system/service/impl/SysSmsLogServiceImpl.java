package com.chua.starter.system.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.starter.system.mapper.SysSmsLogMapper;
import com.chua.starter.system.entity.SysSmsLog;
import com.chua.starter.system.service.SysSmsLogService;
/**
 *
 * @since 2024/11/27
 * @author CH    
 */
@Service
public class SysSmsLogServiceImpl extends ServiceImpl<SysSmsLogMapper, SysSmsLog> implements SysSmsLogService{

}
