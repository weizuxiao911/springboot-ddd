package io.github.weizuxiao911.springboot.ddd.interfaces.controller;

import io.github.weizuxiao911.springboot.ddd.application.dto.command.CreateUserCommand;
import io.github.weizuxiao911.springboot.ddd.application.dto.query.GetUserQuery;
import io.github.weizuxiao911.springboot.ddd.application.dto.response.UserResponse;
import io.github.weizuxiao911.springboot.ddd.application.service.UserAppService;
import io.github.weizuxiao911.springboot.ddd.common.annotation.RequirePermission;
import io.github.weizuxiao911.springboot.ddd.common.annotation.RequirePolicy;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 用户控制器
 * 负责接收 HTTP 请求，调用应用服务，返回响应。
 * 注：返回值由 ResponseBodyWrapper 统一包装为 R<T> 格式
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "用户管理", description = "用户 CRUD 操作")
public class UserController {

    private final UserAppService userAppService;

    /**
     * 创建用户
     */
    @PostMapping
    @Operation(summary = "创建用户", description = "创建一个新用户")
    @RequirePolicy(permissionCode = "USER:CREATE")
    public UserResponse createUser(@Valid @RequestBody CreateUserCommand command) {
        return userAppService.createUser(command);
    }

    /**
     * 获取用户
     */
    @GetMapping("/{userId}")
    @Operation(summary = "获取用户", description = "根据用户 ID 获取用户信息")
    @RequirePermission(resource = "USER", resourceValue = "#userId", action = "READ")
    public UserResponse getUser(@Parameter(description = "用户 ID") @PathVariable String userId) {
        GetUserQuery query = new GetUserQuery();
        query.setUserId(userId);
        return userAppService.getUser(query);
    }

    /**
     * 更新用户邮箱
     */
    @PutMapping("/{userId}/email")
    @Operation(summary = "更新用户邮箱", description = "更新指定用户的邮箱地址")
    @RequirePolicy(permissionCode = "USER:UPDATE")
    public UserResponse updateEmail(
            @Parameter(description = "用户 ID") @PathVariable String userId,
            @Parameter(description = "新邮箱地址") @RequestParam String email) {
        return userAppService.updateEmail(userId, email);
    }

    /**
     * 停用用户
     */
    @DeleteMapping("/{userId}")
    @Operation(summary = "停用用户", description = "停用指定用户")
    @RequirePolicy(permissionCode = "USER:DELETE")
    public void deactivateUser(@Parameter(description = "用户 ID") @PathVariable String userId) {
        userAppService.deactivate(userId);
    }
}