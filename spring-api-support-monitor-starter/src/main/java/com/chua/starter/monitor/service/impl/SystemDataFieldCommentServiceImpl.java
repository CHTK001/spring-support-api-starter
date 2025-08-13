package com.chua.starter.monitor.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.entity.SystemDataFieldComment;
import com.chua.starter.monitor.mapper.SystemDataFieldCommentMapper;
import com.chua.starter.monitor.service.SystemDataFieldCommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 字段注释 Service 实现
 *
 * 提供基于 settingId + nodePath 的注释查询与新增/更新能力。
 */
@Service
@RequiredArgsConstructor
public class SystemDataFieldCommentServiceImpl
        extends ServiceImpl<SystemDataFieldCommentMapper, SystemDataFieldComment>
        implements SystemDataFieldCommentService {

    @Override
    public ReturnResult<SystemDataFieldComment> getBySettingAndPath(Integer settingId, String nodePath) {
        SystemDataFieldComment one = getOne(Wrappers.<SystemDataFieldComment>lambdaQuery()
                .eq(SystemDataFieldComment::getSystemDataFieldCommentSettingId, settingId)
                .eq(SystemDataFieldComment::getSystemDataFieldCommentNodePath, nodePath)
                .last("limit 1"));
        return ReturnResult.ok(one);
    }

    @Override
    public ReturnResult<Boolean> saveOrUpdateComment(Integer settingId, String nodePath, String comment,
                                                     String databaseName, String tableName, String columnName) {
        SystemDataFieldComment one = getOne(Wrappers.<SystemDataFieldComment>lambdaQuery()
                .eq(SystemDataFieldComment::getSystemDataFieldCommentSettingId, settingId)
                .eq(SystemDataFieldComment::getSystemDataFieldCommentNodePath, nodePath)
                .last("limit 1"));
        if (one == null) {
            one = new SystemDataFieldComment();
            one.setSystemDataFieldCommentSettingId(settingId);
            one.setSystemDataFieldCommentNodePath(nodePath);
        }
        one.setSystemDataFieldCommentComment(comment);
        one.setSystemDataFieldCommentDatabaseName(databaseName);
        one.setSystemDataFieldCommentTableName(tableName);
        one.setSystemDataFieldCommentColumnName(columnName);
        saveOrUpdate(one);
        return ReturnResult.ok(true);
    }
}


