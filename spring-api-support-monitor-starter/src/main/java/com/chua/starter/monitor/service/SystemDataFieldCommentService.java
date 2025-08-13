package com.chua.starter.monitor.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.entity.SystemDataFieldComment;

/**
 * 字段注释 Service
 *
 * 定义字段注释查询/保存接口。
 */
public interface SystemDataFieldCommentService extends IService<SystemDataFieldComment> {

    /**
     * 根据 settingId + nodePath 查询注释
     */
    ReturnResult<SystemDataFieldComment> getBySettingAndPath(Integer settingId, String nodePath);

    /**
     * 新增或更新注释
     */
    ReturnResult<Boolean> saveOrUpdateComment(Integer settingId, String nodePath, String comment,
                                              String databaseName, String tableName, String columnName);
}


