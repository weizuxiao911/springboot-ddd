package io.github.weizuxiao911.springboot.ddd.domain.user.vo;

/**
 * 用户信息值对象（外部服务返回）
 * 用于跨服务交互场景，由 infrastructure 层实现后返回。
 */
public record UserInfo(
    UserId userId,
    String username,
    String email,
    String tenantId
) {}
