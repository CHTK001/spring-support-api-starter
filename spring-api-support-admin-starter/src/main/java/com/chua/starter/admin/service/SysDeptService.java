package com.chua.starter.admin.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.chua.starter.admin.entity.SysDept;

/**
 * 部门业务接口
 *
 * @author CH
 * @since 2021/8/22
 */
public interface SysDeptService extends IService<SysDept> {

    /**
     * 分页关键字
     *
     * @param objectPage 对象分页
     * @param keyword    关键字
     * @return {@link IPage}<{@link SysDept}>
     */
    IPage<SysDept> pageKeyword(Page<SysDept> objectPage, String keyword);
}
