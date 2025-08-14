package com.chua.starter.monitor.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.entity.SystemSoft;

/**
 * 软件信息服务
 *
 * 提供软件信息的CRUD与同步能力。
 *
 * 作者: CH 时间: 2025/08/14
 */
public interface SystemSoftService extends IService<SystemSoft> {

    /**
     * 从仓库同步软件与版本 说明: 根据 SystemSoft 的仓库配置与镜像库信息拉取可用版本。 示例: POST /v1/soft/sync
     */
    ReturnResult<Boolean> syncFromDocker();
}
