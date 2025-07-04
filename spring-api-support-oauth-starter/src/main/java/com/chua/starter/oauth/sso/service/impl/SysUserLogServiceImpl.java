package com.chua.starter.oauth.sso.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.starter.oauth.sso.mapper.SysUserLogMapper;
import com.chua.starter.system.entity.SysUserLog;
import com.chua.starter.oauth.sso.service.SysUserLogService;
@Service
public class SysUserLogServiceImpl extends ServiceImpl<SysUserLogMapper, SysUserLog> implements SysUserLogService{

}
