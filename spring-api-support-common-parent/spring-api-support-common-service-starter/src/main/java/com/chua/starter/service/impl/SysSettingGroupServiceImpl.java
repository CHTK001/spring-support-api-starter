package com.chua.starter.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.chua.common.support.lang.code.ErrorItem;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.base.validator.group.AddGroup;
import com.chua.common.support.base.validator.group.UpdateGroup;
import com.chua.starter.common.support.utils.JakartaValidationUtils;
import com.chua.starter.service.SysSettingGroupService;
import com.chua.starter.service.mapper.SysSettingGroupMapper;
import com.chua.starter.entity.service.SysSettingGroup;
import lombok.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;

import java.util.List;
import java.util.Set;

/**
 * @since 2025/9/10 16:26
 * @author CH
 */
@Service
public class SysSettingGroupServiceImpl extends ServiceImpl<SysSettingGroupMapper, SysSettingGroup> implements SysSettingGroupService {

    @Override
    /**
     * 保存或更新For分组
    */
    public ReturnResult<Boolean> saveOrUpdateForGroup(@NonNull SysSettingGroup sysSettingGroup, @NonNull Set<String> roles) {
        Errors errors = JakartaValidationUtils.validate(sysSettingGroup, it -> it.getSysSettingGroupId() != null, UpdateGroup.class,
            AddGroup.class);
        if (errors.hasErrors()) {
            return JakartaValidationUtils.toReturnResult(errors);
        }
        if (sysSettingGroup.getSysSettingGroupId() != null) {
            // 更新
            if (null != sysSettingGroup.getSysSettingGroupCode()) {
                if (exists(Wrappers.lambdaQuery(SysSettingGroup.class)
                        .eq(SysSettingGroup::getSysSettingGroupCode, sysSettingGroup.getSysSettingGroupCode())
                        .ne(SysSettingGroup::getSysSettingGroupId, sysSettingGroup.getSysSettingGroupId())
                )) {
                    return ReturnResult.error("分组编码已存在");
                }
            }
            return ReturnResult.of(updateById(sysSettingGroup));
        } else {
            // 新增
            if (exists(Wrappers.lambdaQuery(SysSettingGroup.class)
                    .eq(SysSettingGroup::getSysSettingGroupCode, sysSettingGroup.getSysSettingGroupCode()))) {
                return ReturnResult.error("分组编码已存在");
            }
            return ReturnResult.of(save(sysSettingGroup));
        }
    }

    @Override
    /**
     * 删除For分组
    */
    public ReturnResult<Boolean> deleteForGroup(@NonNull Long sysSettingGroupId) {
        return ReturnResult.of(removeById(sysSettingGroupId));
    }

    @Override
    /**
     * 列表forsetting分组
    */
    public List<SysSettingGroup> listForSettingGroup(@NonNull SysSettingGroup sysSettingGroup, @NonNull Set<String> roles) {
        return this.list(Wrappers.lambdaQuery(SysSettingGroup.class)
                .orderByAsc(SysSettingGroup::getSysSettingGroupSort)
        );
    }

    @Override
    /**
     * 批量更新
    */
    public ReturnResult<Boolean> batchUpdate(List<SysSettingGroup> sysSettingGroups, Set<String> roles) {
        return ReturnResult.of(
                this.saveOrUpdateBatch(sysSettingGroups)
        );
    }
}
