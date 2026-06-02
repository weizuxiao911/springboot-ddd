package top.archaiharness.framework.application.dto.response;

import top.archaiharness.framework.domain.user.entity.User;
import lombok.Data;

/**
 * 用户响应
 */
@Data
public class UserResponse {
    private String userId;
    private String username;
    private String email;
    private String status;

    /**
     * 从领域实体转换为响应 DTO
     */
    public static UserResponse from(User user) {
        UserResponse response = new UserResponse();
        response.setUserId(user.getId().value());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setStatus(user.getStatus().getValue());
        return response;
    }
}