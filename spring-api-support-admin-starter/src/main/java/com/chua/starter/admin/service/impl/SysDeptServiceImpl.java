package com.chua.starter.admin.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.starter.admin.entity.SysDept;
import com.chua.starter.admin.mapper.SysDeptMapper;
import com.chua.starter.admin.service.SysDeptService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 部门业务实现类
 *
 * @author haoxr
 * @since 2021-08-22
 */
@Service
@RequiredArgsConstructor
public class SysDeptServiceImpl extends ServiceImpl<SysDeptMapper, SysDept> implements SysDeptService {
}

