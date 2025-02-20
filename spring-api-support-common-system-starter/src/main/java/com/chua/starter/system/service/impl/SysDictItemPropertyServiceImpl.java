package com.chua.starter.system.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.utils.CollectionUtils;
import com.chua.starter.system.entity.SysDictItem;
import com.chua.starter.system.entity.SysDictItemProperty;
import com.chua.starter.system.mapper.SysDictItemPropertyMapper;
import com.chua.starter.system.pojo.dict.DictItemPropertyV1Request;
import com.chua.starter.system.service.SysDictItemPropertyService;
import com.chua.starter.system.service.SysDictItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SysDictItemPropertyServiceImpl extends ServiceImpl<SysDictItemPropertyMapper, SysDictItemProperty> implements SysDictItemPropertyService {

    final SysDictItemService sysDictItemService;

    @Override
    public ReturnResult<Boolean> saveOrUpdate(DictItemPropertyV1Request request) {
        Integer sysDictItemId = request.getSysDictItemId();
        if (null == sysDictItemId) {
            return ReturnResult.error("字典子项ID不能为空");
        }
        SysDictItem sysDictItem = sysDictItemService.getById(sysDictItemId);
        if (null == sysDictItem) {
            return ReturnResult.error("字典子项不存在");
        }
        sysDictItemService.deleteCacheId(sysDictItem);
        sysDictItemService.deleteCacheCode(sysDictItem);
        sysDictItemService.deleteCachePid(sysDictItem);
        sysDictItemService.deleteCachePcode(sysDictItem);
        List<SysDictItemProperty> property = request.getProperty();
        this.remove(Wrappers.<SysDictItemProperty>lambdaQuery()
                .eq(SysDictItemProperty::getSysDictItemId, sysDictItemId));

        if (CollectionUtils.isEmpty(property)) {
            return ReturnResult.of(true);
        }

        for (SysDictItemProperty sysDictItemProperty : property) {
            sysDictItemProperty.setSysDictItemId(sysDictItemId);
        }
        return ReturnResult.of(saveBatch(property));
    }
}
