package io.github.weizuxiao911.springboot.ddd.infrastructure.feign.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户查询请求 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserQueryRequest {

    /** 用户名 */
    private String username;

    /** 邮箱 */
    private String email;

    /** 租户 ID */
    private String tenantId;
}
