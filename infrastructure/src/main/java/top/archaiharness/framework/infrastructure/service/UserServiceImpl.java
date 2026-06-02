package top.archaiharness.framework.infrastructure.service;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import top.archaiharness.framework.common.dto.R;
import top.archaiharness.framework.common.exception.DomainException;
import top.archaiharness.framework.domain.user.service.UserService;
import top.archaiharness.framework.domain.user.vo.UserId;
import top.archaiharness.framework.domain.user.vo.UserInfo;
import top.archaiharness.framework.infrastructure.feign.client.UserFeignClient;
import top.archaiharness.framework.infrastructure.feign.dto.response.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 用户外部服务接口实现
 * 实现 Domain 层定义的 UserService 接口。
 * 内部调用 Feign 客户端，完成 DTO → VO 转换。
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserFeignClient feignClient;

    @Override
    public UserInfo getUserInfo(UserId userId) {
        // 直接调 Feign，异常由 Feign 框架处理（重试、超时、降级）
        R<UserResponse> result = feignClient.getUserById(userId.value());
        UserResponse dto = result.getData();
        if (dto == null) {
            throw DomainException.notFound("User", userId.value());
        }
        // DTO → VO 转换
        return new UserInfo(UserId.of(dto.getUserId()), dto.getUsername(), dto.getEmail(), dto.getTenantId());
    }

    @Override
    public Map<UserId, UserInfo> batchGetUserInfo(Set<UserId> userIds) {
        if (userIds.isEmpty()) {
            return Map.of();
        }
        // 批量调用 Feign
        Set<String> idStrings = userIds.stream().map(UserId::value).collect(Collectors.toSet());
        R<Map<String, UserResponse>> result = feignClient.batchGetUserByIds(idStrings);
        Map<String, UserResponse> dtos = result.getData();
        if (dtos == null) {
            return Map.of();
        }
        // DTO → VO 转换
        return userIds.stream()
                .collect(Collectors.toMap(
                        id -> id,
                        id -> {
                            UserResponse dto = dtos.get(id.value());
                            return dto != null 
                                    ? new UserInfo(UserId.of(dto.getUserId()), dto.getUsername(), dto.getEmail(), dto.getTenantId())
                                    : null;
                        }
                ));
    }
}
