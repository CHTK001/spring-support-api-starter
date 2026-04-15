package com.chua.starter.project.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.lang.code.ReturnPageResult;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.mybatis.utils.ReturnPageResultUtils;
import com.chua.starter.entity.project.SysAiGroupMessage;
import com.chua.starter.project.mapper.SysAiGroupMessageMapper;
import com.chua.starter.project.service.SysAiGroupMessageService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author CH
 * @since 2025/9/4 22:47
 */
@Service
public class SysAiGroupMessageServiceImpl extends ServiceImpl<SysAiGroupMessageMapper, SysAiGroupMessage> implements SysAiGroupMessageService {

    @Override
    public ReturnResult<Boolean> saveGroupMessage(List<SysAiGroupMessage> sysAiGroupMessages) {
        return ReturnResult.of(this.saveBatch(sysAiGroupMessages));
    }

    @Override
    public ReturnPageResult<SysAiGroupMessage> list(Integer sysAiGroupId, long current, long size) {
        return ReturnPageResultUtils.ok(
                this.page(
                        new Page<>(current, size),
                        Wrappers.lambdaQuery(SysAiGroupMessage.class)
                                .eq(SysAiGroupMessage::getSysAiGroupId, sysAiGroupId)
                                .orderByAsc(SysAiGroupMessage::getCreateTime)
                )
        );
    }
}
