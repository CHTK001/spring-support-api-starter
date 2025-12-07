package com.chua.starter.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.entity.service.SysDictItemProperty;
import com.chua.starter.service.pojo.dict.DictItemPropertyV1Request;

/**
 * 字典配置项
 *
 * @author CH
 */
public interface SysDictItemPropertyService extends IService<SysDictItemProperty> {


    /**
     * 保存或更新
     *
     * @param request 请求
     * @return 结果
     */
    ReturnResult<Boolean> saveOrUpdate(DictItemPropertyV1Request request);
}
