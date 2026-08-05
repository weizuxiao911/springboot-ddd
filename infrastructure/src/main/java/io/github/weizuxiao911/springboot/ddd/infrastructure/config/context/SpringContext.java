package io.github.weizuxiao911.springboot.ddd.infrastructure.config.context;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

/**
 * Spring上下文工具类
 */
@Component
public class SpringContext implements ApplicationContextAware {

    private SpringContext() {
    }

    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        SpringContext.applicationContext = applicationContext;
    }

    /**
     * 获取Spring上下文
     */
    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    /**
     * 获取Bean
     */
    public static Object getBean(@NonNull String beanName) {
        return applicationContext.getBean(beanName);
    }

    /**
     * 获取Bean
     */
    public static <T> T getBean(@NonNull Class<T> requiredType) {
        return applicationContext.getBean(requiredType);
    }

    /**
     * 获取Bean
     */
    public static <T> T getBean(@NonNull String beanName, @NonNull Class<T> requiredType) {
        return applicationContext.getBean(beanName, requiredType);
    }
}
