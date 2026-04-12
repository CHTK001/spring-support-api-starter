package com.chua.starter.soft.support.mapper.impl;

import com.chua.starter.soft.support.entity.SoftRepository;
import com.chua.starter.soft.support.mapper.SoftRepositoryMapper;
import org.springframework.stereotype.Repository;

@Repository
public class SoftRepositoryMapperStub implements SoftRepositoryMapper {

    @Override
    public SoftRepository selectOne(Object queryWrapper) {
        return null;
    }
}
