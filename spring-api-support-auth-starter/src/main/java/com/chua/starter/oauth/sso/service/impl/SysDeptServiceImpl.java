package com.chua.starter.oauth.sso.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.starter.oauth.sso.mapper.SysDeptMapper;
import com.chua.starter.entity.system.SysDept;
import com.chua.starter.oauth.sso.service.SysDeptService;
@Service
public class SysDeptServiceImpl extends ServiceImpl<SysDeptMapper, SysDept> implements SysDeptService{

}
