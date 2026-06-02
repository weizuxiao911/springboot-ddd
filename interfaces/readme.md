# 接口层（Interfaces Layer）

## 定位与职责

**核心职责**：
- 暴露系统能力：HTTP/REST、GraphQL、RPC、CLI 等对外适配
- 协议与传输：请求路由、参数绑定、序列化/反序列化、返回格式
- 横切控制：鉴权、限流、幂等令牌、审计、错误映射

**构成要素**：
- 控制器/接口适配器：接受请求并调用应用层用例
- 输出模型：直接复用 application 层 Response，由 `ResponseBodyWrapper` 统一封装为 `R<T>`
- 过滤器/拦截器：鉴权、幂等、审计、跨域、异常处理

**不该做的事**：
- 不编码核心业务规则（领域层职责）
- 不直接操作仓库或外部客户端（应用层编排）
- 不暴露领域内部结构和技术细节

## 包结构

```
interfaces/
├── controller/          # HTTP 控制器
│   └── UserController.java
├── graphql/            # GraphQL（可选）
├── rpc/                # RPC 适配器（可选）
├── config/             # 技术配置（保护，非必要勿修改）
│   ├── OpenApiConfig.java
│   └── WebMvcConfig.java
├── advice/             # 全局异常处理
│   └── GlobalExceptionAdvice.java
└── security/           # 认证鉴权
    ├── AuthInterceptor.java
    └── PermissionAspect.java
```

**包职责**：

| 包 | 职责 | 命名规则 |
|---|---|---|
| controller/ | HTTP 控制器 | `<Aggregate>Controller` |
| config/ | 技术配置 | 保护，非必要勿修改 |
| advice/ | 全局异常处理 | `GlobalExceptionAdvice` |
| security/ | 认证鉴权 | 拦截器、切面 |

## 设计原则

- **单一职责**：接口层仅负责交互协议与入出站转换
- **稳定契约**：版本化 API，明确错误码与返回结构
- **安全优先**：鉴权/授权、输入校验、避免信息泄露
- **可观测性**：统一日志、Trace ID、指标与告警

## 控制器规范

**定义方式**：

```java
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "用户管理", description = "用户 CRUD 操作")
public class UserController {

    private final UserAppService userAppService;

    @PostMapping
    @Operation(summary = "创建用户", description = "创建一个新用户")
    public UserResponse createUser(@Valid @RequestBody CreateUserCommand command) {
        return userAppService.createUser(command);
    }
}
```

**命名规范**：

| 元素 | 命名格式 | 示例 |
|------|----------|------|
| 控制器类 | `<Aggregate>Controller` | `UserController` |
| 请求路径 | `/api/<复数聚合名>` | `/api/users` |
| 方法名 | 动词开头 | `createUser`, `getUser` |

## 交互流程

```
HTTP 请求
    ↓
Controller 接收
    ↓
参数校验 (@Valid)
    ↓
调用 Application Service
    ↓
返回 Response DTO
    ↓
ResponseBodyWrapper 包装为 R<T>
    ↓
HTTP 响应
```

## 错误处理

**全局异常处理**（`GlobalExceptionAdvice`）：

- 输入校验错误 → 400 + 字段提示
- 业务错误（DomainException） → 400 + 业务错误码
- 权限错误（AccessDeniedException） → 403
- 系统错误 → 500 + TraceId

**示例**：

```java
@ExceptionHandler(DomainException.class)
@ResponseStatus(HttpStatus.BAD_REQUEST)
public R<Void> handleDomainException(DomainException e) {
    log.warn("Domain exception: {} - {}", e.getCode(), e.getMessage());
    return R.fail(e.getCode(), e.getMessage());
}
```

## 安全与合规

- **鉴权与授权**：Token/JWT/OIDC
- **幂等控制**：幂等键/令牌，防重复提交
- **速率限制**：防止滥用与攻击
- **审计与合规**：关键操作记录、隐私保护与脱敏

## 与领域事件的关系

- 接口层**不直接**产生或消费领域事件
- 接口层仅负责接收请求 → 调用应用层用例 → 返回结果
- 领域事件由应用层在事务提交后发布，由应用层消费者处理
- 如需实时推送事件结果给前端，可通过 WebSocket/SSE 等机制，由应用层消费者触发

#### 交互流程

- 接收请求 → 绑定并校验 application 层 Command/Query → 调用应用层用例
- 应用层返回 Response → `ResponseBodyWrapper` 统一封装为 `R<T>`
- 输出：HTTP 状态码 + 业务错误码 + 数据负载

#### 错误处理与返回

- 输入校验错误：返回 4xx 与明确错误码/字段信息
- 业务错误：由应用层抛出，接口层映射为统一错误响应
- 系统错误：返回 5xx，隐藏内部细节并记录 Trace

#### 安全与合规

- 鉴权与授权：Token/JWT/OIDC 等
- 幂等控制：幂等键/令牌，防重复提交
- 速率限制：防止滥用与攻击
- 审计与合规：关键操作记录、隐私保护与脱敏

#### 与领域事件的关系

- 接口层**不直接**产生或消费领域事件
- 接口层仅负责接收请求 → 调用应用层用例 → 返回结果
- 领域事件由应用层在事务提交后发布，由应用层消费者处理
- 如需实时推送事件结果给前端，可通过 WebSocket/SSE 等机制，由应用层消费者触发

#### 异常处理规范

- **直接捕获** `DomainException`，不需要定义 `ApplicationException`
- **不做**异常转换，不需要 AOP 切面
- 一个 `@ExceptionHandler(DomainException.class)` 统一处理

```java
@ExceptionHandler(DomainException.class)
@ResponseStatus(HttpStatus.BAD_REQUEST)
public R<Void> handleDomainException(DomainException e) {
    log.warn("Domain exception: {} - {}", e.getCode(), e.getMessage());
    return R.fail(e.getCode(), e.getMessage());
}
```

- API 层 import `DomainException` 是允许的，异常处理是横切关注点，不是业务依赖
