package com.chua.starter.monitor.service.data;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.entity.SystemDataFieldComment;

/**
 * 字段注释 Service
 *
 * 定义字段注释查询/保存接口。
 *
 * @author CH
 * @since 2023-04-05
 */
public interface SystemDataFieldCommentService extends IService<SystemDataFieldComment> {

    /**
     * 新增或更新注释
     *
     * @param settingId    设置ID，用于标识特定的配置项，例如：1
     * @param nodePath     节点路径，表示数据节点在系统中的位置，例如："root.database.table"
     * @param comment      注释内容，要添加或更新的字段注释，例如："用户姓名"
     * @param databaseName 数据库名称，指定操作的数据库，例如："user_db"
     * @param tableName    表名，指定操作的数据表，例如："user_info"
     * @param columnName   列名，指定操作的字段列，例如："user_name"
     * @return ReturnResult<Boolean> 操作结果，成功返回true，失败返回false
     */
    ReturnResult<Boolean> saveOrUpdateComment(Integer settingId, String nodePath, String comment,
                                              String databaseName, String tableName, String columnName);
}


