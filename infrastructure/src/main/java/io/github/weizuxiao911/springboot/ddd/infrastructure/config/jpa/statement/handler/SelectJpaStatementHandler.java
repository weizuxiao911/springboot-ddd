package io.github.weizuxiao911.springboot.ddd.infrastructure.config.jpa.statement.handler;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import io.github.weizuxiao911.springboot.ddd.infrastructure.config.jpa.statement.AbstractJpaStatementHandler;

import lombok.extern.slf4j.Slf4j;

/**
 * 查询 JPA 语句处理器
 * 自动为 SELECT 语句添加 deleted=0 条件，过滤已软删除的数据
 * 跳过包含 ignore_deleted 或已有 deleted=1 条件的 SQL
 */
@Slf4j
@Order(90)
@Component
public class SelectJpaStatementHandler extends AbstractJpaStatementHandler {

    private static final String WHERE_CLAUSE_PREFIX = "where ";

    private static final Pattern SELECT_MAIN_PATTERN = Pattern.compile(
            "^select\\s+(.+?)\\s+from\\s+(.+?)(?:\\s+((?:where|group\\s+by|having|order\\s+by|limit)\\b.*))?$",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    private static final Pattern FROM_TABLE_PATTERN = Pattern.compile(
            "^\\s*((?:\\(|[`\"\\w]+\\.)?[`\"\\w]+)(?:\\s+(?:as\\s+)?)(?!where\\b)([`\"\\w]+)?\\s*",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    private static final Pattern SUBQUERY_ALIAS_PATTERN = Pattern.compile(
            "\\)\\s*(?:as\\s+)?(?!where\\b)([`\"\\w]+)\\s*$",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    private static final Pattern SKIP_PATTERN = Pattern.compile("ignore_deleted|deleted\\s*=\\s*1",
            Pattern.CASE_INSENSITIVE);

    private static final Pattern WHERE_START_PATTERN = Pattern.compile("^\\s*where\\b", Pattern.CASE_INSENSITIVE);

    @Override
    public boolean filter(String sql) {
        if (!StringUtils.hasText(sql)) {
            return false;
        }
        String trimmedSql = sql.trim();
        if (!StringUtils.startsWithIgnoreCase(trimmedSql, "select")) {
            return false;
        }
        return !SKIP_PATTERN.matcher(trimmedSql).find();
    }

    @Override
    public String handle(String sql) {
        Matcher mainMatcher = SELECT_MAIN_PATTERN.matcher(sql.trim());
        if (!mainMatcher.find()) {
            log.warn("SELECT 语句格式不匹配，跳过处理: [{}]", sql);
            return sql;
        }

        String selectColumns = mainMatcher.group(1).trim();
        String fromPart = mainMatcher.group(2).trim();
        String afterFrom = mainMatcher.group(3) != null ? mainMatcher.group(3).trim() : "";

        String mainTableRef = parseTableReference(fromPart);
        String deletedCondition = mainTableRef == null ? "deleted=0" : mainTableRef + ".deleted=0";
        String processedAfterFrom = processAfterFrom(afterFrom, deletedCondition);

        StringBuilder finalSql = new StringBuilder();
        finalSql.append("select ").append(selectColumns)
                .append(" from ").append(fromPart)
                .append(" ").append(processedAfterFrom);

        return finalSql.toString().replaceAll("\\s+", " ").trim();
    }

    private String parseTableReference(String fromPart) {
        if (fromPart.startsWith("(") && fromPart.contains(")")) {
            Matcher subMatcher = SUBQUERY_ALIAS_PATTERN.matcher(fromPart);
            if (subMatcher.find()) {
                return subMatcher.group(1).trim();
            }
            String subSql = fromPart.substring(1, fromPart.lastIndexOf(")")).trim();
            Matcher subMainMatcher = SELECT_MAIN_PATTERN.matcher(subSql);
            if (subMainMatcher.find()) {
                return parseTableReference(subMainMatcher.group(2).trim());
            }
            return null;
        }

        Matcher tableMatcher = FROM_TABLE_PATTERN.matcher(fromPart);
        if (tableMatcher.find()) {
            String tableName = tableMatcher.group(1).trim();
            String alias = tableMatcher.group(2);

            if (StringUtils.hasText(alias)) {
                return alias.trim();
            } else {
                return tableName.contains(".") ? tableName.substring(tableName.lastIndexOf(".") + 1).trim() : tableName;
            }
        }

        return null;
    }

    private String processAfterFrom(String afterFrom, String deletedCondition) {
        if (!StringUtils.hasText(afterFrom)) {
            return WHERE_CLAUSE_PREFIX + deletedCondition;
        }

        Matcher whereMatcher = WHERE_START_PATTERN.matcher(afterFrom);
        if (whereMatcher.find()) {
            int whereEndIndex = whereMatcher.end();
            String originalWhereConditions = afterFrom.substring(whereEndIndex).trim();

            return WHERE_CLAUSE_PREFIX + deletedCondition
                    + (StringUtils.hasText(originalWhereConditions) ? " and " + originalWhereConditions : "");
        } else {
            return WHERE_CLAUSE_PREFIX + deletedCondition + " " + afterFrom;
        }
    }
}
