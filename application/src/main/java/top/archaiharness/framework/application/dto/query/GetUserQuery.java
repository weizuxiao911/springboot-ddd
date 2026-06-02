package top.archaiharness.framework.application.dto.query;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 获取用户查询
 */
@Data
public class GetUserQuery {

    @NotBlank(message = "用户 ID 不能为空")
    private String userId;
}
