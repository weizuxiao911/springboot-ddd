package top.archaiharness.framework.infrastructure.config.redis;

import com.alibaba.fastjson.JSON;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.lang.Nullable;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Fastjson Redis 序列化器.
 *
 * <p>注意:已移除 SerializerFeature.WriteClassName,因为该特性会写入 @type 字段
 * 在反序列化时存在 RCE 风险(fastjson 1.x 已知 CVE-2022-25845 等).
 * 如需类型信息,请改用 Jackson + DefaultTyping 或显式维护类型映射.</p>
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
        return JSON.toJSONString(t).getBytes(DEFAULT_CHARSET);
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
