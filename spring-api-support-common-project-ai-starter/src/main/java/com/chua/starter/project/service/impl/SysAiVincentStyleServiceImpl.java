package com.chua.starter.project.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.project.mapper.SysAiVincentStyleMapper;
import com.chua.starter.project.service.SysAiVincentStyleService;
import com.chua.starter.entity.project.SysAiVincentStyle;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author CH
 * @since 2023-10-10
 */
@Service
public class SysAiVincentStyleServiceImpl extends ServiceImpl<SysAiVincentStyleMapper, SysAiVincentStyle> implements SysAiVincentStyleService {

    /**
     * 根据模块样式ID删除SysAiVincentStyle实体
     *
     * @param sysAiVincentStyleId SysAiVincentStyle实体的ID，例如：1
     * @return 返回删除操作的结果，包括是否成功
     */
    @Override
    public ReturnResult<Boolean> deleteForModuleStyle(Integer sysAiVincentStyleId) {
        return ReturnResult.ok(baseMapper.deleteById(sysAiVincentStyleId) > 0);
    }

    /**
     * 保存SysAiVincentStyle实体到数据库
     *
     * @param sysAiVincentStyle 要保存的SysAiVincentStyle实体，例如：{"sysAiModuleId": 1, "sysAiVincentStyleName": "风景画", "sysAiVincentStyleCode": "landscape", "sysAiVincentStyleImage": "http://example.com/image.jpg"}
     * @return 返回保存操作的结果，包括实体信息
     */
    @Override
    public ReturnResult<SysAiVincentStyle> saveForModuleStyle(SysAiVincentStyle sysAiVincentStyle) {
        save(sysAiVincentStyle);
        return ReturnResult.ok(sysAiVincentStyle);
    }

    /**
     * 更新数据库中的SysAiVincentStyle实体
     *
     * @param sysAiVincentStyle 要更新的SysAiVincentStyle实体，例如：{"sysAiVincentStyleId": 1, "sysAiModuleId": 1, "sysAiVincentStyleName": "人物画", "sysAiVincentStyleCode": "portrait", "sysAiVincentStyleImage": "http://example.com/image.jpg"}
     * @return 返回更新操作的结果，包括是否成功
     */
    @Override
    public ReturnResult<Boolean> updateForModuleStyle(SysAiVincentStyle sysAiVincentStyle) {
        return ReturnResult.ok(updateById(sysAiVincentStyle));
    }

    /**
     * 根据SysAiVincentStyle实体的条件列出所有匹配的实体列表
     *
     * @param sysAiVincentStyle 用于查询条件的SysAiVincentStyle实体，例如：{"sysAiModuleId": 1}
     * @return 返回查询结果的列表，包括所有匹配的SysAiVincentStyle实体
     */
    @Override
    public ReturnResult<List<SysAiVincentStyle>> listForModuleStyle(SysAiVincentStyle sysAiVincentStyle) {
        return ReturnResult.ok(baseMapper.selectList(Wrappers.lambdaQuery(SysAiVincentStyle.class)
                .eq(SysAiVincentStyle::getSysAiModuleId, sysAiVincentStyle.getSysAiModuleId())
        ));
    }
}
