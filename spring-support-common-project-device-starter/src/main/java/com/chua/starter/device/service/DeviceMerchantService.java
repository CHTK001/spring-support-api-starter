package com.chua.starter.device.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.chua.cloud.support.security.SecurityClient;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.device.support.DeviceSecurityClientFactory;
import com.chua.starter.entity.project.SysProject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;

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

    private final DeviceMerchantStoreService deviceMerchantStoreService;
    private final DeviceSecurityClientFactory deviceSecurityClientFactory;

    public IPage<SysProject> page(long page, long pageSize, SysProject request) {
        return deviceMerchantStoreService.page(page, pageSize, normalizeQuery(request));
    }

    public List<SysProject> list(SysProject request) {
        return deviceMerchantStoreService.list(LIST_PAGE_SIZE, normalizeQuery(request));
    }

    public ReturnResult<SysProject> save(SysProject sysProject) {
        if (sysProject == null) {
            return ReturnResult.illegal("请求参数不能为空");
        }
        sysProject.setSysProjectFunction(deviceSecurityClientFactory.appendDeviceFunction(sysProject.getSysProjectFunction()));
        return deviceMerchantStoreService.save(sysProject);
    }

    public ReturnResult<Boolean> update(SysProject sysProject) {
        if (sysProject == null || sysProject.getSysProjectId() == null) {
            return ReturnResult.illegal("商户ID不能为空");
        }
        SysProject existed = deviceMerchantStoreService.getOne(sysProject.getSysProjectId());
        if (existed == null) {
            return ReturnResult.illegal("设备商户不存在");
        }
        sysProject.setSysProjectFunction(deviceSecurityClientFactory.appendDeviceFunction(
                StringUtils.defaultIfBlank(sysProject.getSysProjectFunction(), existed.getSysProjectFunction())
        ));
        return deviceMerchantStoreService.update(sysProject);
    }

    public ReturnResult<Boolean> delete(Integer sysProjectId) {
        return deviceMerchantStoreService.delete(sysProjectId);
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
        query.setSysProjectFunction(deviceSecurityClientFactory.getDeviceFunctionCode());
        return query;
    }
}
