package com.chua.starter.monitor.starter.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chua.starter.monitor.starter.entity.MonitorSysGen;
import com.chua.starter.monitor.starter.entity.MonitorSysGenTable;
import com.chua.starter.monitor.starter.pojo.GenTable;
import com.chua.starter.monitor.starter.pojo.TemplateResult;
import com.chua.starter.monitor.starter.query.Download;

import java.util.List;

/**
 * @author CH
 */
public interface MonitorSysGenTableService extends IService<MonitorSysGenTable> {


    /**
     * 下载代码
     *
     * @param download 下载
     * @return {@link byte[]}
     */
    byte[] downloadCode(Download download);

    /**
     * 样板
     *
     * @param tabId 选项卡id
     * @return {@link List}<{@link String}>
     */
    List<TemplateResult> template(Integer tabId);

    /**
     * 更新表
     *
     * @param sysGen 系统信息
     * @param table  表
     * @return {@link Boolean}
     */
    Boolean updateTable(MonitorSysGen sysGen, GenTable table);
}
