package com.chua.starter.monitor.starter.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.session.Session;
import com.chua.common.support.spi.ServiceProvider;
import com.chua.common.support.utils.FileUtils;
import com.chua.starter.monitor.starter.entity.MonitorSysGen;
import com.chua.starter.monitor.starter.mapper.MonitorSysGenMapper;
import com.chua.starter.monitor.starter.properties.ReportGenProperties;
import com.chua.starter.monitor.starter.service.MonitorGenBackupService;
import com.chua.starter.monitor.starter.service.MonitorSysGenService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

import static com.chua.starter.common.support.constant.CacheConstant.REDIS_CACHE_ALWAYS;

@Service
@RequiredArgsConstructor
public class MonitorSysGenServiceImpl extends ServiceImpl<MonitorSysGenMapper, MonitorSysGen> implements MonitorSysGenService {
    final MonitorGenBackupService monitorGenBackupService;
    final ReportGenProperties genProperties;
    @Override
    @CacheEvict(cacheManager = REDIS_CACHE_ALWAYS, cacheNames = REDIS_CACHE_ALWAYS, keyGenerator = "customTenantedKeyGenerator",
            key = "'sys:gen:' + #newSysGen.genId")
    public void updateFor(MonitorSysGen newSysGen, MonitorSysGen oldSysGen) {
        baseMapper.updateById(newSysGen);
        //更新备份
        Integer genBackupStatus = newSysGen.getGenBackupStatus();
        //备份已开启
        if(newSysGen.userHasChange(oldSysGen)) {
            if(null != genBackupStatus && genBackupStatus == 1) {
                monitorGenBackupService.stop(oldSysGen);
                monitorGenBackupService.start(newSysGen);
            }
        }
        monitorGenBackupService.upgrade(newSysGen);
    }

    @Override
    @CacheEvict(cacheManager = REDIS_CACHE_ALWAYS, cacheNames = REDIS_CACHE_ALWAYS, keyGenerator = "customTenantedKeyGenerator", key = "'sys:gen:' + #id")
    public Boolean deleteFor(String id, MonitorSysGen oldSysGen) {
        monitorGenBackupService.stop(oldSysGen);
        File mkdir = FileUtils.mkdir(new File(genProperties.getTempPath(), id));
        try {
            FileUtils.forceDelete(mkdir);
        } catch (IOException ignored) {
        }
        ServiceProvider.of(Session.class).closeKeepExtension(oldSysGen.getGenId() + "");
        baseMapper.deleteById(oldSysGen.getGenId());
        return true;
    }

    @Override
    @Cacheable(cacheManager = REDIS_CACHE_ALWAYS, cacheNames = REDIS_CACHE_ALWAYS, keyGenerator = "customTenantedKeyGenerator", key = "'sys:gen:' + #monitorSysGenId")
    public MonitorSysGen getByIdCache(Integer monitorSysGenId) {
        return this.getById(monitorSysGenId);
    }
}
