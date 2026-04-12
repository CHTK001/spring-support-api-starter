package com.chua.starter.soft.support.mapper;

import com.chua.starter.soft.support.entity.SoftRepository;

public interface SoftRepositoryMapper {

    SoftRepository selectOne(Object queryWrapper);
}
