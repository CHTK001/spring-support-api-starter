package com.chua.starter.device.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chua.common.support.utils.IdUtils;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.device.mapper.DeviceMerchantMapper;
import com.chua.starter.entity.project.SysProject;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DeviceMerchantStoreService {

    private final DeviceMerchantMapper deviceMerchantMapper;

    public IPage<SysProject> page(long page, long pageSize, SysProject request) {
        return deviceMerchantMapper.selectPageFor(new Page<>(page, pageSize), normalizeQuery(request));
    }

    public List<SysProject> list(long pageSize, SysProject request) {
        return deviceMerchantMapper.selectPageFor(new Page<>(1, pageSize), normalizeQuery(request)).getRecords();
    }

    public SysProject getOne(Integer sysProjectId) {
        if (sysProjectId == null) {
            return null;
        }
        return deviceMerchantMapper.getOne(sysProjectId);
    }

    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<SysProject> save(SysProject sysProject) {
        if (sysProject == null) {
            return ReturnResult.illegal("请求参数不能为空");
        }
        sysProject.setSysProjectDelete(0);
        if (sysProject.getSysProjectStatus() == null) {
            sysProject.setSysProjectStatus(1);
        }
        if (sysProject.getSysProjectCode() == null) {
            sysProject.setSysProjectCode(IdUtils.fastUuid());
        }
        deviceMerchantMapper.insert(sysProject);
        return ReturnResult.ok(getOne(sysProject.getSysProjectId()));
    }

    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<Boolean> update(SysProject sysProject) {
        if (sysProject == null || sysProject.getSysProjectId() == null) {
            return ReturnResult.illegal("商户ID不能为空");
        }
        SysProject existed = getOne(sysProject.getSysProjectId());
        if (existed == null) {
            return ReturnResult.illegal("设备商户不存在");
        }
        sysProject.setSysProjectCode(null);
        sysProject.setSysProjectDelete(null);
        return ReturnResult.of(deviceMerchantMapper.updateById(sysProject) > 0);
    }

    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<Boolean> delete(Integer sysProjectId) {
        if (sysProjectId == null) {
            return ReturnResult.illegal("商户ID不能为空");
        }
        if (getOne(sysProjectId) == null) {
            return ReturnResult.illegal("设备商户不存在");
        }
        return ReturnResult.of(deviceMerchantMapper.deleteById(sysProjectId) > 0);
    }

    private SysProject normalizeQuery(SysProject request) {
        SysProject query = request == null ? new SysProject() : request;
        if (query.getSysProjectDelete() == null) {
            query.setSysProjectDelete(0);
        }
        return query;
    }
}
