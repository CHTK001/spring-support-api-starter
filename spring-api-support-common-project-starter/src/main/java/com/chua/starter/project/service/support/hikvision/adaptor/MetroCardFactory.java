package com.chua.starter.project.service.support.hikvision.adaptor;


import com.chua.starter.project.service.support.hikvision.HikvisionClient;
import com.chua.starter.project.service.support.hikvision.adaptor.sub.EntranceFactory;
import com.chua.starter.project.service.support.hikvision.adaptor.sub.YktQxFactory;

/**
 * 一卡通
 *
 * @author CH
 */
public class MetroCardFactory {

    private final HikvisionClient hikvisionClient;

    public MetroCardFactory(HikvisionClient hikvisionClient) {
        this.hikvisionClient = hikvisionClient;
    }

    /**
     * 门禁权限
     *
     * @return 门禁
     */
    public YktQxFactory getYktQxFactory() {
        return new YktQxFactory(hikvisionClient);
    }

    /**
     * 门禁
     *
     * @return 门禁
     */
    public EntranceFactory getEntranceFactory() {
        return new EntranceFactory(hikvisionClient);
    }

}
