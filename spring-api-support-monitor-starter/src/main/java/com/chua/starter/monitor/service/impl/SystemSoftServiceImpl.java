package com.chua.starter.monitor.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.entity.SystemSoft;
import com.chua.starter.monitor.mapper.SystemSoftMapper;
import com.chua.starter.monitor.service.SystemSoftService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 软件信息服务实现
 */
@Slf4j
@Service
public class SystemSoftServiceImpl extends ServiceImpl<SystemSoftMapper, SystemSoft> implements SystemSoftService {

    @Override
    public ReturnResult<Boolean> syncFromDocker() {
        // TODO: 调用 Docker API 同步（占位）
        log.info("同步 SystemSoft（占位）");
        return ReturnResult.ok(true);
    }
}

