package com.chua.starter.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.lang.code.ErrorItem;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.core.utils.SimpleCase;
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
 * @author CH
 * @since 2025/9/10 16:26
 */
@Service
public class SysSettingGroupServiceImpl extends ServiceImpl<SysSettingGroupMapper, SysSettingGroup> implements SysSettingGroupService {

    @Override
    public ReturnResult<Boolean> saveOrUpdateForGroup(@NonNull SysSettingGroup sysSettingGroup, @NonNull Set<String> roles) {
        Errors errors = JakartaValidationUtils.validate(sysSettingGroup, it -> it.getSysSettingGroupId() != null, UpdateGroup.class, AddGroup.class);
        if (errors.hasErrors()) {
            return JakartaValidationUtils.toReturnResult(errors);
        }
        return ReturnResult.of(SimpleCase.<SysSettingGroup, ErrorItem>create()
                .when(it -> sysSettingGroup.getSysSettingGroupId() != null).then(it -> {
                    if (null != sysSettingGroup.getSysSettingGroupCode()) {
                        if (exists(Wrappers.lambdaQuery(SysSettingGroup.class)
                                .eq(SysSettingGroup::getSysSettingGroupCode, sysSettingGroup.getSysSettingGroupCode())
                                .ne(SysSettingGroup::getSysSettingGroupId, sysSettingGroup.getSysSettingGroupId())
                        )) {
                            return ErrorItem.of("分组编码已存在");
                        }
                    }
                    return ErrorItem.of(updateById(sysSettingGroup));
                })
                .otherwise(() -> {
                    if (exists(Wrappers.lambdaQuery(SysSettingGroup.class)
                            .eq(SysSettingGroup::getSysSettingGroupCode, sysSettingGroup.getSysSettingGroupCode()))) {
                        return ErrorItem.of("分组编码已存在");
                    }
                    return ErrorItem.of(save(sysSettingGroup));
                })
                .invoke(sysSettingGroup));

    }

    @Override
    public ReturnResult<Boolean> deleteForGroup(@NonNull Long sysSettingGroupMapperId) {
        return ReturnResult.of(removeById(sysSettingGroupMapperId));
    }

    @Override
    public List<SysSettingGroup> listForSettingGroup(@NonNull SysSettingGroup sysSettingGroup, @NonNull Set<String> roles) {
        return this.list(Wrappers.lambdaQuery(SysSettingGroup.class)
                .orderByAsc(SysSettingGroup::getSysSettingGroupSort)
        );
    }

    @Override
    public ReturnResult<Boolean> batchUpdate(List<SysSettingGroup> sysSettingGroups, Set<String> roles) {
        return ReturnResult.of(
                this.saveOrUpdateBatch(sysSettingGroups)
        );
    }
}
