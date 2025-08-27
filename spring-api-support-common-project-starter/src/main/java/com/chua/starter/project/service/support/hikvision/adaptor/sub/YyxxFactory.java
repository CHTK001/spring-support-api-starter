package com.chua.starter.project.service.support.hikvision.adaptor.sub;


import com.chua.starter.project.service.support.hikvision.HikvisionClient;
import com.chua.starter.project.service.support.hikvision.adaptor.sub.level3.RyjzpFactory;
import com.chua.starter.project.service.support.hikvision.adaptor.sub.level3.ZzxxFactory;

/**
 * 人员信息
 *
 * @author CH
 */
public class YyxxFactory {

    protected final HikvisionClient hikvisionClient;

    public YyxxFactory(HikvisionClient hikvisionClient) {
        this.hikvisionClient = hikvisionClient;
    }

    /**
     * 组织信息接口
     *
     * @return 组织信息接口
     */
    public ZzxxFactory getOrgInfoFactory() {
        return new ZzxxFactory(hikvisionClient);
    }

    /**
     * 人员及照片接口
     *
     * @return 人员及照片接口
     */
    public RyjzpFactory getPeoplePictureFactory() {
        return new RyjzpFactory(hikvisionClient);
    }

}
