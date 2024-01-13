package com.chua.starter.admin.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.utils.StringUtils;
import com.chua.common.support.utils.ThreadUtils;
import com.chua.starter.admin.entity.SysFileStorage;
import com.chua.starter.admin.mapper.SysFileStorageMapper;
import com.chua.starter.admin.service.SysFileStorageService;
import com.chua.starter.admin.utils.FileStorageUtils;
import com.chua.starter.common.support.filestorage.FileStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
* @author Administrator
* @description 针对表【sys_file_storage(OSS)】的数据库操作Service实现
* @createDate 2024-01-10 09:14:00
*/
@Slf4j
@Service
public class SysFileStorageServiceImpl extends ServiceImpl<SysFileStorageMapper, SysFileStorage>
    implements SysFileStorageService, InitializingBean {
    @Resource
    public FileStorageService fileStorageService;

    @Override
    public void afterPropertiesSet() {
        ThreadUtils.newStaticThreadPool()
                .execute(() -> {
                    log.info("初始化bucket");
                    List<SysFileStorage> sysFileStorages = baseMapper.selectList(Wrappers.<SysFileStorage>lambdaQuery().eq(SysFileStorage::getFsStatus, 1));
                    sysFileStorages.forEach(it -> {
                        if(StringUtils.isBlank(it.getFsBucket())) {
                            return;
                        }
                        FileStorageUtils.openFileStorage(fileStorageService, it);
                    });
                    log.info("本次共初始化bucket: {}", sysFileStorages.size());
                });
    }
}




