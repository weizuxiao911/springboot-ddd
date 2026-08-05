package io.github.weizuxiao911.springboot.ddd.application.dto.query;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 获取用户信息查询（跨服务）
 */
@Data
public class GetUserInfoFromRemoteQuery {

    @NotBlank(message = "用户 ID 不能为空")
    private String userId;
}