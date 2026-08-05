package io.github.weizuxiao911.springboot.ddd.infrastructure.config.jpa.statement;

/**
 * 抽象 JPA 语句处理器
 */
public abstract class AbstractJpaStatementHandler {

    /**
     * 过滤 SQL 语句，决定是否处理
     */
    public abstract boolean filter(String sql);

    /**
     * 处理 SQL 语句
     */
    public abstract String handle(String sql);
}
