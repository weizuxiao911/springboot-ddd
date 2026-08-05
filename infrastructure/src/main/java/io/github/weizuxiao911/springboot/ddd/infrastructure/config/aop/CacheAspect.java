package io.github.weizuxiao911.springboot.ddd.infrastructure.config.aop;

import com.alibaba.fastjson.JSON;
import io.github.weizuxiao911.springboot.ddd.common.annotation.Cache;
import io.github.weizuxiao911.springboot.ddd.infrastructure.config.support.SpelUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

/**
 * 缓存 AOP 切面
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class CacheAspect {

    private final StringRedisTemplate redisTemplate;

    private static final String CACHE_PREFIX = "cache:";

    @SuppressWarnings("null")
    @Around("@annotation(cache)")
    public Object around(ProceedingJoinPoint point, Cache cache) throws Throwable {
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();

        String cacheKey = CACHE_PREFIX + SpelUtils.parse(cache.key(), method, point.getArgs());

        // 1. 尝试从缓存获取
        String cachedValue = redisTemplate.opsForValue().get(cacheKey);
        if (cachedValue != null) {
            log.debug("Cache hit: {}", cacheKey);
            return JSON.parseObject(cachedValue, method.getReturnType());
        }

        // 2. 缓存未命中，执行方法
        Object result = point.proceed();

        // 3. 写入缓存
        if (result != null) {
            String jsonValue = JSON.toJSONString(result);
            redisTemplate.opsForValue().set(cacheKey, jsonValue, cache.expire(), TimeUnit.SECONDS);
            log.debug("Cache set: {}", cacheKey);
        }

        return result;
    }
}
