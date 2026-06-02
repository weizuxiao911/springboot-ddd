package top.archaiharness.framework.infrastructure.config.aop;

import top.archaiharness.framework.common.annotation.Lock;
import top.archaiharness.framework.infrastructure.config.support.SpelUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.UUID;

/**
 * 分布式锁 AOP 切面
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class LockAspect {

    private final StringRedisTemplate redisTemplate;

    private static final String LOCK_PREFIX = "lock:";

    // Lua 脚本：原子性获取锁
    private static final String LOCK_SCRIPT = 
            "if redis.call('setnx', KEYS[1], ARGV[1]) == 1 then " +
            "return redis.call('expire', KEYS[1], ARGV[2]) " +
            "else return 0 end";

    // Lua 脚本：原子性释放锁（验证 value 防止误删）
    private static final String UNLOCK_SCRIPT = 
            "if redis.call('get', KEYS[1]) == ARGV[1] then " +
            "return redis.call('del', KEYS[1]) " +
            "else return 0 end";

    @SuppressWarnings("null")
    @Around("@annotation(lock)")
    public Object around(ProceedingJoinPoint point, Lock lock) throws Throwable {
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();

        String lockKey = LOCK_PREFIX + SpelUtils.parse(lock.key(), method, point.getArgs());
        String lockValue = UUID.randomUUID().toString();
        
        boolean acquired = Boolean.TRUE.equals(
                redisTemplate.execute(new DefaultRedisScript<>(LOCK_SCRIPT, Boolean.class),
                        Collections.singletonList(lockKey),
                        lockValue, String.valueOf(lock.leaseTime()))
        );

        if (!acquired) {
            log.warn("Failed to acquire lock: {}", lockKey);
            throw new RuntimeException("System busy, please try again later");
        }

        try {
            return point.proceed();
        } finally {
            redisTemplate.execute(new DefaultRedisScript<>(UNLOCK_SCRIPT, Long.class),
                    Collections.singletonList(lockKey),
                    lockValue);
        }
    }
}
