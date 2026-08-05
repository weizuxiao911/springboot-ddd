package io.github.weizuxiao911.springboot.ddd.infrastructure.config.jpa.statement;

import org.hibernate.resource.jdbc.spi.StatementInspector;
import org.springframework.stereotype.Component;

import io.github.weizuxiao911.springboot.ddd.infrastructure.config.context.SpringContext;

import lombok.extern.slf4j.Slf4j;

/**
 * JPA 语句拦截器
 * 需配置 spring.jpa.properties.hibernate.session_factory.statement_inspector 指向当前类名
 */
@Slf4j
@Component
public class JpaStatementInspector implements StatementInspector {

    @Override
    public String inspect(String sql) {
        if (sql == null || sql.isBlank()) {
            return sql;
        }
        AbstractJpaStatementHandler handler = SpringContext.getBean(AbstractJpaStatementHandler.class);
        return handler != null ? handler.handle(sql) : sql;
    }
}
