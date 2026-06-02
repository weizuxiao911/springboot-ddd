package top.archaiharness.framework.infrastructure.feign.client;

import java.util.Map;
import java.util.Set;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import top.archaiharness.framework.common.dto.R;
import top.archaiharness.framework.infrastructure.feign.dto.request.UserQueryRequest;
import top.archaiharness.framework.infrastructure.feign.dto.response.UserResponse;
import top.archaiharness.framework.infrastructure.feign.factory.UserFeignClientFallbackFactory;

/**
 * 用户服务 Feign 客户端示例
 * 配置了降级工厂、超时和重试策略
 */
@FeignClient(
        name = "user-service",
        url = "${service.user.url:http://user}",
        fallbackFactory = UserFeignClientFallbackFactory.class
)
public interface UserFeignClient {

    /**
     * 根据用户 ID 查询用户
     */
    @GetMapping("/users/{userId}")
    R<UserResponse> getUserById(@PathVariable("userId") String userId);

    /**
     * 条件查询用户
     */
    @PostMapping("/users/query")
    R<UserResponse> queryUser(@RequestBody UserQueryRequest request);

    /**
     * 批量获取用户信息（必须提供批量接口，禁止 N+1 调用）
     */
    @PostMapping("/users/batch")
    R<Map<String, UserResponse>> batchGetUserByIds(@RequestBody Set<String> userIds);
}
