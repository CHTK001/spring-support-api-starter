package com.chua.starter.scheduler.support.ddl;

/**
 * 表结构变更动作类型。
 *
 * @author CH
 */
public enum ActionType {

    /**
     * 创建表
     */
    CREATE,

    /**
     * 删除并重建表
     */
    DROP_CREATE,

    /**
     * 更新表结构
     */
    UPDATE
}
