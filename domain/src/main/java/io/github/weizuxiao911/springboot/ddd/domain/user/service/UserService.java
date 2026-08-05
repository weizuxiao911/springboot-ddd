package io.github.weizuxiao911.springboot.ddd.domain.user.service;

import io.github.weizuxiao911.springboot.ddd.domain.user.vo.UserId;
import io.github.weizuxiao911.springboot.ddd.domain.user.vo.UserInfo;

import java.util.Map;
import java.util.Set;

/**
 * 用户外部服务接口（防腐层）
 * 定义在 Domain 层，是业务概念，不是 RPC 接口。
 * 由 infrastructure 层实现，内部调用 Feign/RPC。
 */
public interface UserService {

    /**
     * 获取用户信息
     *
     * @param userId 用户ID
     * @return 用户信息值对象
     */
    UserInfo getUserInfo(UserId userId);

    /**
     * 批量获取用户信息
     *
     * @param userIds 用户ID集合
     * @return 用户信息映射
     */
    Map<UserId, UserInfo> batchGetUserInfo(Set<UserId> userIds);
}
