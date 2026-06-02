package top.archaiharness.framework.infrastructure.feign.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户信息响应 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    /** 用户 ID */
    private String userId;

    /** 用户名 */
    private String username;

    /** 邮箱 */
    private String email;

    /** 手机号 */
    private String phone;

    /** 状态 */
    private String status;

    /** 租户 ID */
    private String tenantId;
}
