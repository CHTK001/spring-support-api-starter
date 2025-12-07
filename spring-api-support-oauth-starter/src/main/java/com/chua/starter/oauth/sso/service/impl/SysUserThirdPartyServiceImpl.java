package com.chua.starter.oauth.sso.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.starter.service.model.SysUserThirdParty;
import com.chua.starter.oauth.sso.mapper.SysUserThirdPartyMapper;
import com.chua.starter.oauth.sso.service.SysUserThirdPartyService;
@Service
public class SysUserThirdPartyServiceImpl extends ServiceImpl<SysUserThirdPartyMapper, SysUserThirdParty> implements SysUserThirdPartyService{

}
