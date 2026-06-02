package top.archaiharness.framework.infrastructure.config.feign;

import java.util.concurrent.TimeUnit;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import feign.Client;
import feign.Feign;
import okhttp3.ConnectionPool;

/**
 * Feign 配置
 * 使用 OkHttp 作为底层客户端，配置连接池和超时时间
 */
@Configuration
@ConditionalOnClass(Feign.class)
public class FeignConfig {

    @Bean
    @ConditionalOnClass(okhttp3.OkHttpClient.class)
    public okhttp3.OkHttpClient okHttpClient() {
        return new okhttp3.OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .connectionPool(new ConnectionPool(10, 5, TimeUnit.MINUTES))
                .retryOnConnectionFailure(false)
                .build();
    }

    @Bean
    public Client feignClient(okhttp3.OkHttpClient client) {
        return new feign.okhttp.OkHttpClient(client);
    }
}
