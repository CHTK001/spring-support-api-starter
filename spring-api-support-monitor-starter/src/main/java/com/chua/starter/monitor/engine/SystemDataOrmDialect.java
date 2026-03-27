package com.chua.starter.monitor.engine;

/**
 * Engine ORM 查询方言。
 */
public enum SystemDataOrmDialect {

    SQL("sql"),
    LUCENE("lucene"),
    CYPHER("cypher");

    private final String commandType;

    SystemDataOrmDialect(String commandType) {
        this.commandType = commandType;
    }

    public String getCommandType() {
        return commandType;
    }
}
