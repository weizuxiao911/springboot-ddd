package top.archaiharness.framework.infrastructure.config.support;

import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.lang.NonNull;

import java.lang.reflect.Method;

/**
 * SpEL 表达式解析工具
 */
public class SpelUtils {

    private static final ExpressionParser PARSER = new SpelExpressionParser();
    private static final ParameterNameDiscoverer DISCOVERER = new DefaultParameterNameDiscoverer();

    private SpelUtils() {
    }

    /**
     * 解析 SpEL 表达式
     *
     * @param expression SpEL 表达式 (如 "#userId")
     * @param method     当前方法
     * @param args       方法参数
     * @return 解析后的值
     */
    public static String parse(@NonNull String expression, @NonNull Method method, Object[] args) {
        String[] params = DISCOVERER.getParameterNames(method);
        StandardEvaluationContext context = new StandardEvaluationContext();
        if (params != null) {
            for (int i = 0; i < params.length; i++) {
                context.setVariable(params[i], args[i]);
            }
        }
        return PARSER.parseExpression(expression).getValue(context, String.class);
    }
}
