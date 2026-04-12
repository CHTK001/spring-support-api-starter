package com.chua.starter.soft.support.service;

import com.chua.starter.soft.support.entity.SoftRepository;
import com.chua.starter.soft.support.entity.SoftTarget;

public interface SoftManagementService {

    SoftTarget saveTarget(SoftTarget target);

    SoftRepository saveRepository(SoftRepository repository);
}
