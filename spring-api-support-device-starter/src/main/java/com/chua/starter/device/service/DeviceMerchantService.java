package com.chua.starter.device.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chua.cloud.support.security.SecurityClient;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.device.support.DeviceSecurityClientFactory;
import com.chua.starter.entity.project.SysProject;
import com.chua.starter.project.mapper.SysProjectMapper;
import com.chua.starter.project.service.SysProjectService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

/**
 * 设备商户服务。
 *
 * @author CH
 * @since 2026/3/26
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceMerchantService {

    private static final long LIST_PAGE_SIZE = 1000L;

    private final SysProjectMapper sysProjectMapper;
    private final SysProjectService sysProjectService;
    private final DeviceSecurityClientFactory deviceSecurityClientFactory;

    public IPage<SysProject> page(long page, long pageSize, SysProject request) {
        return sysProjectMapper.selectPageFor(new Page<>(page, pageSize), normalizeQuery(request));
    }

    public List<SysProject> list(SysProject request) {
        return sysProjectMapper.selectPageFor(new Page<>(1, LIST_PAGE_SIZE), normalizeQuery(request)).getRecords();
    }

    public ReturnResult<SysProject> save(SysProject sysProject) {
        if (sysProject == null) {
            return ReturnResult.illegal("请求参数不能为空");
        }
        sysProject.setSysProjectDelete(0);
        if (sysProject.getSysProjectStatus() == null) {
            sysProject.setSysProjectStatus(1);
        }
        sysProject.setSysProjectFunction(deviceSecurityClientFactory.appendDeviceFunction(sysProject.getSysProjectFunction()));
        return sysProjectService.save(sysProject, Set.of());
    }

    public ReturnResult<Boolean> update(SysProject sysProject) {
        if (sysProject == null || sysProject.getSysProjectId() == null) {
            return ReturnResult.illegal("商户ID不能为空");
        }
        SysProject existed = sysProjectService.getOne(sysProject.getSysProjectId());
        if (existed == null) {
            return ReturnResult.illegal("设备商户不存在");
        }
        sysProject.setSysProjectFunction(deviceSecurityClientFactory.appendDeviceFunction(
                StringUtils.defaultIfBlank(sysProject.getSysProjectFunction(), existed.getSysProjectFunction())
        ));
        return sysProjectService.update(Set.of(), sysProject);
    }

    public ReturnResult<Boolean> delete(Integer sysProjectId) {
        if (sysProjectId == null) {
            return ReturnResult.illegal("商户ID不能为空");
        }
        return sysProjectService.delete(sysProjectId);
    }

    public ReturnResult<Boolean> testConnection(Integer sysProjectId) {
        if (sysProjectId == null) {
            return ReturnResult.illegal("商户ID不能为空");
        }
        try (SecurityClient client = deviceSecurityClientFactory.create(sysProjectId)) {
            return ReturnResult.of(client.testConnection());
        } catch (Exception e) {
            log.warn("测试设备商户连接失败, sysProjectId={}", sysProjectId, e);
            return ReturnResult.illegal(StringUtils.defaultIfBlank(e.getMessage(), "连接失败"));
        }
    }

    private SysProject normalizeQuery(SysProject request) {
        SysProject query = request == null ? new SysProject() : request;
        if (query.getSysProjectDelete() == null) {
            query.setSysProjectDelete(0);
        }
        query.setSysProjectFunction(String.valueOf(deviceSecurityClientFactory.getDeviceFunctionId()));
        return query;
    }
}
