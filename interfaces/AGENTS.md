# Interfaces 层 AI 审查规则

> **优先级顺序**：P0 > P1 > P2
> - P0：核心架构违规，**必须立即修复**，禁止合并
> - P1：代码质量违规，**必须修复**，禁止合并
> - P2：最佳实践违规，建议修复，可合并

> 本文档定义接口层（Interfaces Layer）的开发规范、约束和最佳实践。
> 所有新增或修改的接口层代码必须遵守本规范。

## 快速索引

- [架构约束](#一架构约束)
- [控制器规范](#二控制器规范)
- [DTO 规范](#三dto-规范)
- [全局异常处理](#四全局异常处理)
- [拦截器规范](#五拦截器规范)
- [响应包装](#六响应包装)
- [上下文管理](#七上下文管理)
- [API 文档](#八api-文档)
- [单元测试](#九单元测试)
- [代码审查](#十代码审查)
- [问题分级](#十一问题分级)
- [禁止事项](#十二禁止事项清单)
- [编译验证](#十三编译验证)

## 目录

- [一、架构约束](#一架构约束)
  - [1.1 分层架构](#11-分层架构)
  - [1.2 包结构](#12-包结构)
  - [1.3 依赖方向](#13-依赖方向)
- [二、控制器规范](#二控制器规范)
  - [2.1 定义方式](#21-定义方式)
  - [2.2 命名规范](#22-命名规范)
  - [2.3 接口设计](#23-接口设计)
- [三、DTO 规范](#三dto-规范)
  - [3.1 统一响应体 R](#31-统一响应体-r)
  - [3.2 请求 DTO](#32-请求-dto)
  - [3.3 响应 DTO](#33-响应-dto)
- [四、全局异常处理](#四全局异常处理)
- [五、拦截器规范](#五拦截器规范)
  - [5.1 认证拦截器](#51-认证拦截器)
  - [5.2 请求拦截器](#52-请求拦截器)
- [六、响应包装](#六响应包装)
- [七、上下文管理](#七上下文管理)
  - [7.1 AppContext](#71-appcontext)
  - [7.2 清理规则](#72-清理规则)
- [八、API 文档](#八api-文档)
  - [8.1 文档配置](#81-文档配置)
  - [8.2 控制器注解](#82-控制器注解)
  - [8.3 访问地址](#83-访问地址)
- [九、单元测试](#九单元测试)
  - [9.1 测试结构（可选）](#91-测试结构可选)
- [十、代码审查](#十代码审查)
  - [10.1 审查范围](#101-审查范围)
  - [10.2 审查顺序](#102-审查顺序)
  - [10.3 报告要求](#103-报告要求)
- [十一、问题分级](#十一问题分级)
- [十二、禁止事项清单](#十二禁止事项清单)
- [十三、编译验证](#十三编译验证)

## 一、架构约束

### 1.1 分层架构

**接口层职责**

- 接收 HTTP 请求，解析参数
- 调用应用服务执行用例
- 封装响应体（`R<T>`）返回客户端
- 全局异常处理、跨域、认证、限流等横切关注点

**禁止做的事**

- 不写业务规则（应由领域层承担）
- 不做用例编排（应由应用层承担）
- 不直接调用 Repository 或基础设施组件
- 不定义业务异常

**依赖方向**

```
interfaces → application → domain → common
interfaces → common (直接依赖 DomainException、AppContext)
```

**强制规则**

- `interfaces` **只能依赖** `application` 和 `common`
- **禁止依赖** `infrastructure` 模块
- `AppContext` 定义在 `common/context`，两层共享

**技术封装保护**

- `interfaces/config/` 是统一的技术封装（如 OpenAPI、WebMvcConfig 等）
- **非必要禁止修改**，保持开箱即用

## 二、控制器规范

### 2.1 定义方式

- 使用 `@RestController` + `@RequestMapping`
- 通过构造函数注入应用服务
- 一个聚合一个控制器

```java
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserAppService userAppService;

    @PostMapping
    public UserResponse createUser(@Valid @RequestBody CreateUserCommand command) {
        return userAppService.createUser(command);
    }
}
```

> 注：返回值由 `ResponseBodyWrapper` 统一包装为 `R<T>` 格式，Controller 无需手动包装。

### 2.2 命名规范

| 元素 | 命名格式 | 示例 |
|------|----------|------|
| 控制器类 | `<Aggregate>Controller` | `UserController` |
| 请求路径 | `/api/<复数聚合名>` | `/api/users` |
| 方法名 | 动词开头 | `createUser`, `getUser` |

### 2.3 接口设计

- 使用 RESTful 风格
- 路径使用复数名词：`/api/users`
- 操作使用 HTTP 方法区分：`GET/POST/PUT/DELETE`
- 所有接口返回 `R<T>` 统一响应格式

## 三、DTO 规范

### 3.1 统一响应体 R

- `R<T>` 定义在 `common` 层，供全栈共用。
- Controller 方法返回值**不需要**手动包装为 `R<T>`。
- 由 `ResponseBodyWrapper` 自动包装返回值。

```java
@PostMapping
public UserResponse createUser(@Valid @RequestBody CreateUserCommand command) {
    // 直接返回业务 DTO，框架自动包装为 R<UserResponse>
    return userAppService.createUser(command);
}
```

### 3.2 请求 DTO

- **直接复用 application 层的 Command/Query**
- Controller 方法参数直接使用 `@RequestBody CreateUserCommand`
- 校验注解由 application 层定义，interfaces 层通过 `@Valid` 触发

### 3.3 响应 DTO

- **直接复用 application 层的 Response**
- Controller 方法返回值直接使用 `UserResponse`
- 由 `ResponseBodyWrapper` 统一包装为 `R<T>` 格式

### DTO 转换规范

- **禁止定义 interfaces 层专属业务 DTO**（Request/VO）。
- Controller 直接接收 Command/Query，返回 Response。
- 统一响应体 `R` 定义在 `common` 层，由 `ResponseBodyWrapper` 自动包装返回值。

## 四、全局异常处理

- 由 `GlobalExceptionAdvice` 统一处理所有异常
- **禁止**在 Controller 中 try-catch 业务异常
- `DomainException` 直接映射为 400 响应
- 系统未知异常映射为 500，隐藏内部细节

## 五、拦截器规范

### 5.1 认证拦截器

- `AuthInterceptor` 负责 JWT 令牌校验
- 令牌验证通过后，将 `userId` 注入 `AppContext`
- 无效令牌直接返回 401，不进入业务逻辑

### 5.2 上下文清理

- `AppContextFilter` 在请求结束时自动调用 `AppContext.clear()`
- `AuthInterceptor.afterCompletion` 再次确保清理，防止内存泄漏

## 六、响应包装

- `ResponseBodyWrapper` 自动将所有 Controller 返回值包装为 `R<T>`
- 若返回值已是 `R<T>` 类型，则跳过包装（防重复）
- Controller 方法只需返回业务 DTO（如 `UserResponse`）

## 七、上下文管理

### 7.1 AppContext

- 请求级 ThreadLocal 存储，用于透传 Header、TraceId、UserId
- 通过 `AppContextFilter` 自动从 HTTP Header 提取并注入
- 异步线程通过 `ContextTaskDecorator` 自动透传

### 7.2 清理规则

- 请求结束时由 Filter/Interceptor 自动清理
- **禁止**在业务代码中手动调用 `AppContext.clear()`

## 八、API 文档

### 8.1 文档配置

使用 SpringDoc OpenAPI 3 + Knife4j 提供 API 文档。

**配置类**：

```java
@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Framework API")
                        .description("基于 DDD 架构的企业级脚手架 API 文档")
                        .version("1.0.0"))
                .schemaRequirement("Bearer", new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT"));
    }
}
```

### 8.2 控制器注解

| 注解 | 位置 | 说明 |
|------|------|------|
| `@Tag` | 类 | API 分组名称 |
| `@Operation` | 方法 | API 概要说明 |
| `@Parameter` | 参数 | 参数描述 |

**示例**：

```java
@RestController
@RequestMapping("/api/users")
@Tag(name = "用户管理", description = "用户 CRUD 操作")
public class UserController {

    @PostMapping
    @Operation(summary = "创建用户", description = "创建一个新用户")
    public UserResponse createUser(@Valid @RequestBody CreateUserCommand command) {
        // ...
    }
}
```

### 8.3 访问地址

- Swagger UI: `/swagger-ui.html`
- Knife4j: `/doc.html`
- OpenAPI JSON: `/v3/api-docs`

## 九、单元测试

> 说明：interfaces 层为"胶水代码"，不强制要求单元测试。核心业务逻辑已在 domain/application 层测试。

### 9.1 测试结构（可选）

- 建议使用 Given-When-Then 结构
- 测试方法命名：`should<ExpectedBehavior>When<Condition>`
- Mock Servlet API（HttpServletRequest、HttpServletResponse）

```java
@Test
void shouldReturnTrueWhenTokenIsValid() {
    // given
    String validToken = generateValidToken("user-123");
    when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);

    // when
    boolean result = authInterceptor.preHandle(request, response, new Object());

    // then
    assertThat(result).isTrue();
    verify(response, never()).setStatus(anyInt());
}
```

**测试范围**（可选）

| 元素 | 测试要求 |
|------|----------|
| 拦截器 | 测试正常流程、异常场景、上下文清理 |
| 异常处理 | 测试各类异常的响应格式 |
| 响应包装 | 测试包装逻辑、双重包装防护 |
| 控制器 | 测试请求映射、参数校验、响应格式 |

## 十、代码审查

### 10.1 审查范围

- **仅审查增量代码**（本次提交/PR 新增或修改的代码）

### 10.2 审查顺序

1. **依赖检查**：是否引入 infrastructure 依赖？有 = P0
2. **架构规范**：是否在控制器中写业务逻辑？有 = P0
3. **代码规范**：命名、注解、日志是否符合规范？不符合 = P1/P2

### 10.3 报告要求

- 必须只列出本次修改涉及的文件和问题
- 报告必须包含"审查结论"章节，明确标注"合格/不合格"

## 十一、问题分级

| 级别 | 标识 | 定义 | 处理方式 |
|------|------|------|----------|
| 严重 | `P0` | 违反核心规范，影响架构正确性 | **必须立即修复**，禁止合并 |
| 警告 | `P1` | 违反一般规范，影响代码质量 | **必须修复**，禁止合并 |
| 建议 | `P2` | 不符合最佳实践，不影响功能 | 建议修复，可合并 |
| 提示 | `P3` | 代码风格、命名建议 | 可选修复 |

**常见问题分级**

| 问题 | 级别 | 说明 |
|------|------|------|
| 依赖 infrastructure 模块 | P0 | 破坏分层架构 |
| 在控制器中写业务逻辑 | P0 | 职责混淆 |
| 控制器中 try-catch 业务异常 | P1 | 应交由全局异常处理 |
| 直接返回领域实体 | P1 | 应转换为 DTO |
| 未清理 AppContext | P1 | 内存泄漏风险 |
| 响应双重包装 | P1 | 数据格式错误 |
| unused import | P2 | 代码整洁问题 |
| 方法命名不贴近业务语义 | P2 | 可读性问题 |

## 十二、禁止事项清单

| 禁止项 | 级别 | 原因 |
|--------|------|------|
| 依赖 infrastructure 模块 | P0 | 破坏依赖方向 |
| 在控制器中写业务逻辑 | P0 | 属于领域层职责 |
| 定义业务异常 | P0 | 一个 DomainException 就够了 |
| 直接返回领域实体 | P1 | 防泄漏原则 |
| 控制器中 try-catch 业务异常 | P1 | 应交由全局异常处理 |
| 未清理 AppContext | P1 | 内存泄漏风险 |
| 响应双重包装 | P1 | 数据格式错误 |
| 使用空标记接口 | P2 | 无实际价值 |

## 十三、编译验证

1. 每次修改后执行 `mvn compile -q` 验证编译通过
2. 检查并移除所有 unused import
3. 执行 `mvn test` 验证测试通过，覆盖率达标
4. 确保无 Spring Bean 命名冲突（同类名在不同包需确认无冲突）

*本文档由团队共创，后续所有接口层代码必须遵守本规范。*
