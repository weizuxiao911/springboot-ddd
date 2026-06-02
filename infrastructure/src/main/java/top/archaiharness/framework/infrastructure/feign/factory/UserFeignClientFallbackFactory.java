package top.archaiharness.framework.infrastructure.feign.factory;

import java.util.Map;
import java.util.Set;

import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import top.archaiharness.framework.common.dto.R;
import top.archaiharness.framework.infrastructure.feign.client.UserFeignClient;
import top.archaiharness.framework.infrastructure.feign.dto.request.UserQueryRequest;
import top.archaiharness.framework.infrastructure.feign.dto.response.UserResponse;

import lombok.extern.slf4j.Slf4j;

/**
 * UserFeignClient 降级工厂
 * 当远程服务不可用时触发降级逻辑
 */
@Slf4j
@Component
public class UserFeignClientFallbackFactory implements FallbackFactory<UserFeignClient> {

    @Override
    public UserFeignClient create(Throwable cause) {
        log.error("UserFeignClient fallback triggered", cause);
        return new UserFeignClient() {
            @Override
            public R<UserResponse> getUserById(String userId) {
                return R.fail("用户服务暂时不可用，请稍后重试");
            }

            @Override
            public R<UserResponse> queryUser(UserQueryRequest request) {
                return R.fail("用户服务暂时不可用，请稍后重试");
            }

            @Override
            public R<Map<String, UserResponse>> batchGetUserByIds(Set<String> userIds) {
                return R.fail("用户服务暂时不可用，请稍后重试");
            }
        };
    }
}
