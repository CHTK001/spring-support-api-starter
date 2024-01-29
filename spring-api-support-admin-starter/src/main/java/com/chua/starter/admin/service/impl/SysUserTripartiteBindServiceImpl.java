package com.chua.starter.admin.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.starter.admin.entity.SysUserTripartiteBind;
import com.chua.starter.admin.mapper.SysUserTripartiteBindMapper;
import com.chua.starter.admin.service.SysUserTripartiteBindService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SysUserTripartiteBindServiceImpl extends ServiceImpl<SysUserTripartiteBindMapper, SysUserTripartiteBind> implements SysUserTripartiteBindService{

    @Override
    public List<SysUserTripartiteBind> listBind(Long userId) {
        return baseMapper.selectList(Wrappers.<SysUserTripartiteBind>lambdaQuery().eq(SysUserTripartiteBind::getUserId, userId));
    }
}
