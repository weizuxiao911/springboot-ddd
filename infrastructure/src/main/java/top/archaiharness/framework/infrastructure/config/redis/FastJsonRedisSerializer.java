package top.archaiharness.framework.infrastructure.config.redis;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.lang.Nullable;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Fastjson Redis 序列化器
 */
public class FastJsonRedisSerializer implements RedisSerializer<Object> {

    private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    private final Class<?> clazz;

    public FastJsonRedisSerializer(Class<?> clazz) {
        this.clazz = clazz;
    }

    @Override
    public byte[] serialize(@Nullable Object t) throws SerializationException {
        if (t == null) {
            return new byte[0];
        }
        return JSON.toJSONString(t, SerializerFeature.WriteClassName).getBytes(DEFAULT_CHARSET);
    }

    @SuppressWarnings("null")
    @Override
    public Object deserialize(byte[] bytes) throws SerializationException {
        if (bytes == null || bytes.length <= 0) {
            return null;
        }
        String str = new String(bytes, DEFAULT_CHARSET);
        return JSON.parseObject(str, clazz);
    }
}
