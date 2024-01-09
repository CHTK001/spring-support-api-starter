package com.chua.starter.admin.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
    @Override
    public IPage<SysDept> pageKeyword(Page<SysDept> objectPage, String keyword) {
        return baseMapper.selectPage(objectPage,  Wrappers.<SysDept>lambdaQuery()
                .like(StringUtils.isNotBlank(keyword), SysDept::getDeptName, keyword)
                .or().like(StringUtils.isNotBlank(keyword), SysDept::getDeptMarker, keyword)
        );
    }
}

