package com.chua.starter.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.utils.CollectionUtils;
import com.chua.starter.service.SysDictItemPropertyService;
import com.chua.starter.service.SysDictItemService;
import com.chua.starter.service.mapper.SysDictItemPropertyMapper;
import com.chua.starter.system.entity.SysDictItem;
import com.chua.starter.system.entity.SysDictItemProperty;
import com.chua.starter.system.pojo.dict.DictItemPropertyV1Request;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 字典配置项服务实现类
 *
 * @author CH
 * @since 2024-08-07
 */
@Service
@RequiredArgsConstructor
public class SysDictItemPropertyServiceImpl extends ServiceImpl<SysDictItemPropertyMapper, SysDictItemProperty> implements SysDictItemPropertyService {

    /**
     * 字典项服务接口，用于获取和操作字典项数据
     */
    final SysDictItemService sysDictItemService;

    /**
     * 保存或更新字典子项的属性配置
     *
     * @param request 字典子项属性请求对象，包含要保存或更新的属性列表和关联的字典子项ID
     *                示例: {
     *                "sysDictItemId": 1,
     *                "property": [
     *                {
     *                "sysDictItemPropertyName": "color",
     *                "sysDictItemPropertyValue": "red"
     *                }
     *                ]
     *                }
     * @return 操作结果，成功返回true，失败返回错误信息
     */
    @Override
    public ReturnResult<Boolean> saveOrUpdate(DictItemPropertyV1Request request) {
        // 获取请求中的字典子项ID
        Integer sysDictItemId = request.getSysDictItemId();
        // 检查字典子项ID是否为空
        if (null == sysDictItemId) {
            return ReturnResult.error("字典子项ID不能为空");
        }
        // 根据ID获取字典子项信息
        SysDictItem sysDictItem = sysDictItemService.getById(sysDictItemId);
        // 检查字典子项是否存在
        if (null == sysDictItem) {
            return ReturnResult.error("字典子项不存在");
        }
        // 清除与该字典子项相关的各种缓存
        sysDictItemService.deleteCacheId(sysDictItem);
        sysDictItemService.deleteCacheCode(sysDictItem);
        sysDictItemService.deleteCachePid(sysDictItem);
        sysDictItemService.deleteCachePcode(sysDictItem);
        // 获取请求中的属性列表
        List<SysDictItemProperty> property = request.getProperty();
        // 删除该字典子项下所有已存在的属性配置
        this.remove(Wrappers.<SysDictItemProperty>lambdaQuery()
                .eq(SysDictItemProperty::getSysDictItemId, sysDictItemId));

        // 如果属性列表为空，则直接返回成功
        if (CollectionUtils.isEmpty(property)) {
            return ReturnResult.of(true);
        }

        // 为每个属性设置字典子项ID
        for (SysDictItemProperty sysDictItemProperty : property) {
            sysDictItemProperty.setSysDictItemId(sysDictItemId);
        }
        // 批量保存新的属性配置
        return ReturnResult.of(saveBatch(property));
    }
}
