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
 *
 * 作者: CH 时间: 2025/08/14
 */
@Slf4j
@Service
public class SystemSoftServiceImpl extends ServiceImpl<SystemSoftMapper, SystemSoft> implements SystemSoftService {

    /**
     * 从仓库同步软件与版本 说明: 根据 SystemSoft 的仓库配置与镜像库信息拉取可用版本。 示例: POST /v1/soft/sync
     * 
     * @return 操作结果 示例:true
     */
    @Override
    public ReturnResult<Boolean> syncFromDocker() {
        // TODO: 调用 Docker API 同步（占位）
        log.info("同步 SystemSoft（占位）");
        return ReturnResult.ok(true);
    }
}
