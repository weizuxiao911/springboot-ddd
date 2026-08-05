package io.github.weizuxiao911.springboot.ddd.infrastructure.config.jpa.statement.handler;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import io.github.weizuxiao911.springboot.ddd.infrastructure.config.jpa.statement.AbstractJpaStatementHandler;

import lombok.extern.slf4j.Slf4j;

/**
 * 主 JPA 语句处理器
 * 收集所有 AbstractJpaStatementHandler 实现，按 @Order 排序后链式处理
 */
@Slf4j
@Primary
@Component
public class PrimaryJpaStatementHandler extends AbstractJpaStatementHandler {

    private final List<AbstractJpaStatementHandler> handlers;

    public PrimaryJpaStatementHandler(List<AbstractJpaStatementHandler> handlers) {
        this.handlers = handlers.stream()
                .filter(handler -> !(handler instanceof PrimaryJpaStatementHandler))
                .sorted(Comparator.comparingInt(handler -> {
                    Order order = handler.getClass().getAnnotation(Order.class);
                    return order != null ? order.value() : Integer.MAX_VALUE;
                }))
                .collect(Collectors.toList());
    }

    @Override
    public boolean filter(String sql) {
        return true;
    }

    @Override
    public String handle(String sql) {
        return handlers.stream().reduce(sql, (processedSql, handler) -> {
            if (handler.filter(processedSql)) {
                log.debug("Before Translated => {}", processedSql);
                return handler.handle(processedSql);
            }
            return processedSql;
        }, (a, b) -> b);
    }
}
