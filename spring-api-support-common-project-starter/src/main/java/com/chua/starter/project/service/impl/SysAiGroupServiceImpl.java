package com.chua.starter.project.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.lang.code.ErrorItem;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.utils.SimpleCase;
import com.chua.common.support.validator.group.AddGroup;
import com.chua.common.support.validator.group.UpdateGroup;
import com.chua.starter.common.support.oauth.AuthService;
import com.chua.starter.common.support.oauth.CurrentUser;
import com.chua.starter.common.support.utils.JakartaValidationUtils;
import com.chua.starter.entity.project.SysAiGroup;
import com.chua.starter.entity.project.SysAiGroupMessage;
import com.chua.starter.project.mapper.SysAiGroupMapper;
import com.chua.starter.project.service.SysAiGroupMessageService;
import com.chua.starter.project.service.SysAiGroupService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.validation.Errors;

import java.util.List;

/**
 * ai模型组
 *
 * @author CH
 * @since 2025/9/4 13:07
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysAiGroupServiceImpl extends ServiceImpl<SysAiGroupMapper, SysAiGroup> implements SysAiGroupService {

    final AuthService authService;
    final TransactionTemplate transactionTemplate;
    final SysAiGroupMessageService sysAiGroupMessageService;

    @Override
    public ReturnResult<List<SysAiGroup>> group(@NonNull Integer sysProjectId) {
        CurrentUser currentUser = authService.getCurrentUser();
        String sysUserId = currentUser.getUserId();
        return ReturnResult.of(baseMapper.group(sysProjectId, sysUserId));
    }

    @Override
    public ReturnResult<Boolean> saveOrUpdateForGroup(@NonNull SysAiGroup sysAiGroup) {
        Errors validated = JakartaValidationUtils.validate(sysAiGroup, it -> null == it.getSysAiGroupId(), AddGroup.class, UpdateGroup.class);
        if (validated.hasErrors()) {
            return JakartaValidationUtils.toReturnResult(validated);
        }
        CurrentUser currentUser = authService.getCurrentUser();
        String sysUserId = currentUser.getUserId();
        return ReturnResult.of(SimpleCase.<SysAiGroup, ErrorItem>create()
                .when(it -> it.getSysAiGroupId() != null).then(it -> ErrorItem.of(baseMapper.updateById(it) > 0))
                .when(it -> it.getSysAiGroupId() == null).then(it -> {
                    if (this.count(
                            Wrappers.<SysAiGroup>lambdaQuery()
                                    .eq(null != sysAiGroup.getSysProjectId(), SysAiGroup::getSysProjectId, sysAiGroup.getSysProjectId())
                                    .eq(SysAiGroup::getSysUserId, sysUserId))
                            > 10) {
                        return ErrorItem.of("分组数量不能超过10个");
                    }
                    it.setSysUserId(sysUserId);
                    return ErrorItem.of(baseMapper.insert(it) > 0);
                })
                .invoke(sysAiGroup));
    }

    @Override
    public ReturnResult<Boolean> deleteForGroup(@NonNull Integer sysAiGroupId) {
        CurrentUser currentUser = authService.getCurrentUser();
        String sysUserId = currentUser.getUserId();
        return transactionTemplate.execute(status -> {
            sysAiGroupMessageService.remove(Wrappers.<SysAiGroupMessage>lambdaQuery()
                    .eq(SysAiGroupMessage::getSysAiGroupId, sysAiGroupId)
            );
            return ReturnResult.of(baseMapper.delete(
                    Wrappers.<SysAiGroup>lambdaQuery()
                            .eq(SysAiGroup::getSysAiGroupId, sysAiGroupId)
                            .eq(SysAiGroup::getSysUserId, sysUserId)
            ));
        });
    }
}
