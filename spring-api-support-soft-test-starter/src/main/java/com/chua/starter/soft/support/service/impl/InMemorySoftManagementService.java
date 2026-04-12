package com.chua.starter.soft.support.service.impl;

import com.chua.starter.soft.support.entity.SoftRepository;
import com.chua.starter.soft.support.entity.SoftTarget;
import com.chua.starter.soft.support.service.SoftManagementService;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class InMemorySoftManagementService implements SoftManagementService {

    private final AtomicLong targetSequence = new AtomicLong(1L);
    private final AtomicLong repositorySequence = new AtomicLong(1L);

    @Override
    public SoftTarget saveTarget(SoftTarget target) {
        if (target == null) {
            return null;
        }
        if (target.getSoftTargetId() == null) {
            target.setSoftTargetId(targetSequence.getAndIncrement());
        }
        if (!StringUtils.hasText(target.getTargetCode())) {
            target.setTargetCode("target-" + target.getSoftTargetId());
        }
        return target;
    }

    @Override
    public SoftRepository saveRepository(SoftRepository repository) {
        if (repository == null) {
            return null;
        }
        if (repository.getSoftRepositoryId() == null) {
            repository.setSoftRepositoryId(repositorySequence.getAndIncrement());
        }
        return repository;
    }
}
