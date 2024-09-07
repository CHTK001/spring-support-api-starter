package com.chua.starter.system.setting;

import com.chua.common.support.function.Upgrade;
import lombok.Data;

/**
 * 过期时间
 * @author CH
 * @since 2024/9/7
 */
@Data
public class ExpireSetting implements Upgrade<ExpireSetting> {

    /**
     * 日志
     */
    private Long log;

    /**
     * sql
     */
    private Long sql;

    /**
     * cpu
     */
    private Long cpu;

    /**
     * 设备
     */
    private Long device;
    /**
     * 内存
     */
    private Long mem;

    @Override
    public void upgrade(ExpireSetting expireSetting) {

        if(null != expireSetting.getLog()) {
            log = expireSetting.getLog();
        }
        if(null != expireSetting.getSql()) {
            sql = expireSetting.getSql();
        }
        if(null != expireSetting.getCpu()) {
            cpu = expireSetting.getCpu();
        }
        if(null != expireSetting.getDevice()) {
            device = expireSetting.getDevice();
        }
        if(null != expireSetting.getMem()) {
            mem = expireSetting.getMem();
        }
    }
}
