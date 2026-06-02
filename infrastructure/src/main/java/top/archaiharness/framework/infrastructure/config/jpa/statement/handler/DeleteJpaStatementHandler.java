package top.archaiharness.framework.infrastructure.config.jpa.statement.handler;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import top.archaiharness.framework.infrastructure.config.jpa.statement.AbstractJpaStatementHandler;

import lombok.extern.slf4j.Slf4j;

/**
 * 删除 JPA 语句处理器
 * 将 DELETE 语句转为软删除 UPDATE 语句（设置 deleted=1）
 * 跳过包含 force_delete 或 physical_delete 关键字的 SQL
 */
@Slf4j
@Order(99)
@Component
public class DeleteJpaStatementHandler extends AbstractJpaStatementHandler {

    private static final Pattern DELETE_PATTERN = Pattern.compile(
            "^delete\\s+(?:([`\"\\w]+)\\s+)?from\\s+([`\"\\w\\.]+)(?:\\s+(?:as\\s+)?(?!where\\b)([`\"\\w]+))?\\s*(where\\s+.*)?$",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    private static final Pattern SKIP_PATTERN = Pattern.compile("force_delete|physical_delete",
            Pattern.CASE_INSENSITIVE);

    @Override
    public boolean filter(String sql) {
        if (!StringUtils.hasText(sql)) {
            return false;
        }
        String trimmedSql = sql.trim();
        if (!StringUtils.startsWithIgnoreCase(trimmedSql, "delete")) {
            return false;
        }
        return !SKIP_PATTERN.matcher(trimmedSql).find();
    }

    @Override
    public String handle(String sql) {
        Matcher matcher = DELETE_PATTERN.matcher(sql.trim());
        if (!matcher.find()) {
            log.warn("DELETE 语句格式不匹配，跳过处理: [{}]", sql);
            return sql;
        }
        return rebuildStatement(sql, matcher);
    }

    private String rebuildStatement(String originalSql, Matcher matcher) {
        String deleteAlias = matcher.group(1);
        String tableName = matcher.group(2);
        String tableAlias = matcher.group(3);
        String whereClause = matcher.group(4);

        String aliasToUse = StringUtils.hasText(tableAlias) ? tableAlias : deleteAlias;

        StringBuilder updateSql = new StringBuilder();
        updateSql.append("update ").append(tableName);

        if (StringUtils.hasText(aliasToUse)) {
            updateSql.append(" ").append(aliasToUse);
        }

        String deletedColumn = StringUtils.hasText(aliasToUse) ? aliasToUse + ".deleted=1" : "deleted=1";
        updateSql.append(" set ").append(deletedColumn);

        if (StringUtils.hasText(whereClause)) {
            updateSql.append(" ").append(whereClause);
        }

        return updateSql.toString().replaceAll("\\s+", " ").trim();
    }
}
