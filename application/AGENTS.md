# Application 层 AI 审查规则

> **优先级顺序**：P0 > P1 > P2
> - P0：核心架构违规，**必须立即修复**，禁止合并
> - P1：代码质量违规，**必须修复**，禁止合并
> - P2：最佳实践违规，建议修复，可合并

> 本文档定义应用层（Application Layer）的代码审核规范与约束。
> 所有新增或修改的应用层代码必须通过审核方可合并。

---

## 快速索引

- [架构约束](#一架构约束)
- [包结构](#二包结构)
- [应用服务规范](#三应用服务规范)
- [DTO 规范](#四dto-规范)
- [领域事件处理](#五领域事件处理)
- [异常规范](#六异常规范)
- [禁止事项](#七禁止事项清单)
- [测试规范](#八测试规范)
- [代码审查](#九代码审查)
- [编译验证](#十编译验证)

---

## 一、架构约束

### 1.1 分层架构

**严格依赖约束**

- **仅允许依赖**：`domain`、`common`
- **绝对禁止依赖**：`infrastructure`、`interfaces`、`bootstrap` 或其他任何非授权模块
- 违反此约束 = **P0 严重问题，禁止合并**

**依赖方向**

```
application → domain
application → common
```

**应用层职责**

- 协调用例：封装业务场景的应用服务，驱动领域模型
- 事务边界：`@Transactional` 定义事务边界
- DTO 转换：接收 Command/Query，返回 Response
- 事件发布：在事务提交后发布领域事件

**禁止做的事**

- 不写核心业务规则（应由领域层承担）
- 不直接做持久化或第三方调用细节（应由基础设施层承担）
- 不定义应用层异常，不转换 `DomainException`

---

## 二、包结构

### 项目结构

```
application/
├── service/           # 应用服务（必要）
│   └── <Aggregate>Service.java
├── event/             # 事件消费者（需要时定义）
│   └── <Aggregate>EventHandler.java
├── dto/
│   ├── command/       # 命令 DTO（需要时定义）
│   ├── query/         # 查询 DTO（需要时定义）
│   └── response/      # 响应 DTO（需要时定义）
└── converter/         # MapStruct 转换器（需要时定义）
```

**必要才定义，勿过度设计**

| 包 | 职责责 | 命名规则 |
|---|---|---|
| service/ | 应用服务（用例入口） | 实体名 + AppService，如 `UserAppService` |
| event/ | 事件消费者 | 实体名 + EventHandler，如 `UserCreatedEventHandler` |
| dto/command/ | 写操作输入 | 动作 + 实体名 + Command，如 `CreateUserCommand` |
| dto/query/ | 读操作输入 | 动作 + 实体名 + Query，如 `GetUserQuery` |
| dto/response/ | 操作输出 | 实体名 + Response，如 `UserResponse` |
| converter/ | DTO 转换器 | 实体名 + Converter，如 `UserConverter` |

**禁止定义**

| 禁止项 | 级别 |
|---|---|
| handler/（未使用） | P1 |
| enums/（未使用） | P1 |
| utils/（未使用） | P1 |

**空目录必须加 .gitkeep，非空不加**

---

## 三、应用服务规范

### 定义方式

- 使用 `@Service` + `@RequiredArgsConstructor`
- **必须**使用构造器注入，**禁止**使用 `@Autowired` 字段注入
- 通过构造函数注入依赖的 Repository、DomainService、DomainEventPublisher
- 一个用例一个方法，用例原子化
- 方法返回值必须是 `Response`、`PageResult` 或 `void`，**禁止**返回领域实体

### 方法命名规范

| 操作类型 | 命名格式 | 示例 |
|---|---|---|
| 创建 | `create<Aggregate>` | `createUser` |
| 更新 | `update<Aggregate>` | `updateUser` |
| 删除 | `delete<Aggregate>` | `deleteUser` |
| 查询单个 | `get<Aggregate>` | `getUser` |
| 查询列表 | `list<Aggregate>` | `listUsers` |
| 分页查询 | `page<Aggregate>` | `pageUsers` |
| 业务操作 | `<动词><Aggregate>` | `activateUser`、`changeEmail` |

**规则**：
- 方法名必须清晰表达业务意图
- 禁止使用 `handle`、`process`、`do` 等模糊动词
- 写操作返回 Response，读操作返回 Response 或 PageResult

```java
@Service
@RequiredArgsConstructor
public class UserAppService {

    private final UserRepository userRepository;
    private final UserDomainService userDomainService;
    private final DomainEventPublisher eventPublisher;

    @Transactional
    public UserResponse createUser(CreateUserCommand command) {
        // 1. 校验业务规则
        if (!userDomainService.isUsernameUnique(command.getUsername(), null)) {
            throw DomainException.of("USERNAME_TAKEN", "用户名已被占用");
        }

        // 2. 创建聚合
        User user = User.create(command.getUsername(), command.getEmail());

        // 3. 保存
        userRepository.save(user);

        // 4. 发布事件
        eventPublisher.publish(user.getDomainEvents());
        user.clearDomainEvents();

        // 5. 返回响应
        return UserResponse.from(user);
    }
}
```

### 架构模式：CQRS (命令查询职责分离)

- **写操作 (Command)**：必须经过 Domain 层，通过聚合根和领域服务保证业务一致性。
- **读操作 (Query)**：**严禁**经过 Domain 层。直接由 Application 层查询数据（Repository 或外部服务）并组装 Response，避免不必要的领域对象创建开销。

**核心原则**：应用层只做编排，不做数据处理。

**禁止**：使用 `Stream` / `Optional` 做业务逻辑（过滤、聚合、复杂判断）。
**允许**：DTO 转换（`map`）、简单判空（`orElseThrow`）。

---

## 四、DTO 规范

### 命令 DTO（Command）

- 放在 `dto/command/` 包下
- 使用 `@Data` + 校验注解（`@NotBlank`、`@Email`、`@Size` 等）
- **禁止**在 DTO 上使用 JPA 注解
- 命名格式：`<Action><Aggregate>Command`

```java
@Data
public class CreateUserCommand {
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度必须在 3-50 之间")
    private String username;

    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    @NotBlank(message = "租户 ID 不能为空")
    private String tenantId;
}
```

### 查询 DTO（Query）

- 放在 `dto/query/` 包下
- 仅包含查询条件，**禁止**包含业务逻辑
- 命名格式：`<Action><Aggregate>Query`

```java
@Data
public class GetUserQuery {
    @NotBlank(message = "用户 ID 不能为空")
    private String userId;
}
```

### DTO 验证失败处理

- **验证入口**：由 `interfaces` 层 Controller 使用 `@Valid` 触发
- **验证失败**：抛出 `MethodArgumentNotValidException`，由 `GlobalExceptionAdvice` 统一捕获返回 400
- **application 层职责**：仅定义校验注解，不主动触发验证
- **非 HTTP 场景**（如事件消费）：由调用方负责验证或信任已验证数据
- ****禁止**在 application 层手动校验 DTO 字段（如 `if (command.getName() == null)`）

### 响应 DTO（Response）

- 放在 `dto/response/` 包下
- 只暴露必要字段，**禁止**暴露领域实体所有字段
- **禁止**在 Response 中包含敏感信息（如密码、内部 ID）
- **必须**内置静态工厂方法 `from(Aggregate aggregate)` 进行转换

```java
@Data
public class UserResponse {
    private String userId;
    private String username;
    private String email;
    private String status;

    public static UserResponse from(User user) {
        UserResponse response = new UserResponse();
        response.setUserId(user.getId().value());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setStatus(user.getStatus().getValue());
        return response;
    }
}
```

---

## 五、领域事件处理

### 发布端

- 应用服务保存聚合后调用 `eventPublisher.publish()`
- 事件发布由基础设施层 `@TransactionalEventListener` 保证在事务提交后执行
- 必须调用 `clearDomainEvents()` 清空事件

```java
userRepository.save(user);
eventPublisher.publish(user.getDomainEvents());
user.clearDomainEvents();
```

### 消费端

- 事件消费者放在 `application/event/` 包下
- 使用 `@OnEvent` 注解标记事件处理方法
- **只能**注入应用服务（Application Service），**禁止**直接注入 Repository 或 DomainService
- **必须幂等**：通过 `eventId` 去重
- **必须捕获异常**：避免事件处理失败导致消息丢失
- **必须记录日志**：处理开始、成功、失败均需记录
- **职责**：幂等检查 → 委托给应用服务 → 异常处理和日志

### 幂等实现方案

**由 infrastructure 层自动处理，开发无需手写去重逻辑。**

- `IdempotentAspect` 切面自动拦截所有 `@OnEvent` 方法
- 自动检查 `eventId` 是否已处理
- 处理成功后自动记录到 `event_idempotent` 表
- 重复事件直接跳过，不执行目标方法

**EventHandler 示例**（无需手写幂等检查）：
```java
@Component
public class UserCreatedEventHandler {

    private final UserAppService userAppService;

    public UserCreatedEventHandler(UserAppService userAppService) {
        this.userAppService = userAppService;
    }

    @OnEvent(UserCreatedEvent.class)
    public void handle(UserCreatedEvent event) {
        // 无需手写幂等检查，切面已自动处理
        userAppService.activateUser(new ActivateUserCommand(event.getUserId()));
        log.info("UserCreatedEvent handled: eventId={}", event.getEventId());
    }
}
```

---

## 六、异常规范

**核心原则**

- **一个 `DomainException` 就够了**，不定义应用层异常
- **不做** `DomainException` → 其他异常转换
- **直接让** `DomainException` 向上冒泡到 API 层

```java
@Transactional
public UserResponse createUser(CreateUserCommand command) {
    // 不需要 try-catch，DomainException 直接向上冒泡
    if (!userDomainService.isUsernameUnique(command.getUsername(), null)) {
        throw DomainException.of("USERNAME_TAKEN", "用户名已被占用");
    }
    // ...
}
```

**禁止事项**

| 禁止项 | 级别 |
|---|---|
| 定义应用层异常（如 `ApplicationException`） | P0 |
| 捕获并转换 `DomainException` | P0 |
| 在应用层定义事务边界（应在应用服务方法上） | P0 |

---

## 七、禁止事项清单

| 禁止项 | 级别 | 原因 |
|---|---|---|
| **依赖未授权模块**（如 infrastructure, interfaces） | **P0** | **破坏分层架构，绝对禁止** |
| 定义应用层异常 | P0 | 一个 DomainException 就够了 |
| 转换 DomainException | P0 | 直接冒泡即可 |
| **无单元测试** | **P0** | **质量不达标，禁止合并** |
| 测试覆盖率 < 90% | P0 | 质量不达标 |
| 分支覆盖率 < 90% | P0 | 质量不达标 |
| 在应用层写核心业务规则 | P0 | 应由领域层承担 |
| 在 DTO 上使用 JPA 注rao | P0 | 污染领域层，破坏分层 |
| 事件处理器不幂等 | P0 | 导致数据重复处理 |
| 在 DTO 转换中写业务规则 | P0 | 业务逻辑泄漏 |
| 事务注解放在类级别 | P0 | 事务边界不清晰 |
| Response 缺少 from() 方法 | P0 | 违反 DTO 转换规范 |
| EventHandler 直接注入 Repository/DomainService | P0 | 应委托给应用服务，实现复用 |
| EventHandler 写业务规则 | P0 | 应由领域层承担 |
| 循环中调用外部服务接口 | P0 | 性能陷阱，必须使用批量方法 |
| 在应用层直接调用 Feign/RPC | P0 | 必须通过 Domain 层接口 |
| 方法命名不符合规范 | P1 | 降低可读性，必须使用 `createXxx`/`updateXxx`/`getXxx` |
| 方法返回领域实体 | P0 | 必须返回 Response 或 void，禁止直接暴露实体 |
| 在应用层使用 Stream/Optional 做业务逻辑 | P0 | 业务逻辑应在领域层处理 |
| 使用 `@Autowired` 字段注入 | P0 | 必须使用构造器注入（`@RequiredArgsConstructor`） |
| 在应用层定义常量/枚举 | P1 | 常量/枚举应放在 domain 或 common 层 |
| Mock 实体或 DTO | P1 | 无法验证真实领域行为 |
| DTO 暴露领域实体所有字段 | P1 | 防泄漏原则 |
| 非必要目录定义 | P1 | 勿过度设计 |
| 用例不原子化 | P1 | 单一职责 |
| 使用空标记接口 | P2 | 无实际价值 |
| unused import | P2 | 代码整洁问题 |

---

## 八、测试规范

### 覆盖率要求

- **行覆盖率**：≥ 90%
- **分支覆盖率**：≥ 90%
- 未达标禁止合并

### 测试要求

- 每个 public 类必须有对应测试类（`<ClassName>Test`）
- 无测试 = P0 严重问题，禁止合并
- **强制使用 Given-When-Then 结构**
- 测试方法命名：`should<ExpectedBehavior>When<Condition>`

### 异常场景测试

**必须测试以下异常分支**：

| 场景 | 测试用例 |
|---|---|
| 业务规则校验失败 | `shouldThrowDomainExceptionWhen<Rule>Violated` |
| 实体不存在 | `shouldThrowDomainExceptionWhenNotFound` |
| 唯一性冲突 | `shouldThrowDomainExceptionWhenAlreadyExists` |
| 状态非法 | `shouldThrowDomainExceptionWhenInvalidState` |

```java
@Test
void shouldThrowDomainExceptionWhenUsernameTaken() {
    // given
    when(userDomainService.isUsernameUnique("zhangsan", null)).thenReturn(false);

    // when & then
    assertThatThrownBy(() -> userAppService.createUser(command))
        .isInstanceOf(DomainException.class)
        .hasFieldOrPropertyWithValue("code", "USERNAME_TAKEN");
}
```

```java
@Test
void shouldCreateUserSuccessfully() {
    // given
    when(userDomainService.isUsernameUnique("zhangsan", null)).thenReturn(true);
    when(userDomainService.isEmailUnique("zhangsan@example.com", null)).thenReturn(true);
    when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

    // when
    UserResponse response = userAppService.createUser(command);

    // then
    assertThat(response.getUsername()).isEqualTo("zhangsan");
    verify(userRepository).save(any(User.class));
    verify(eventPublisher).publish(any());
}
```

### Mock 边界

| 元素 | 测试要求 |
|---|---|
| Repository | 必须 Mock（接口） |
| DomainService | 必须 Mock（接口或类） |
| EventPublisher | 必须 Mock（接口） |
| 外部服务接口（Domain 层） | 必须 Mock（接口） |
| Entity | **禁止 Mock**，应真实创建 |
| Command/Query | **禁止 Mock**，应真实构造 |

```java
// ✅ 正确：Mock 外部服务接口
@Mock
private ProductService productService;

when(productService.getProductInfo(any())).thenReturn(new ProductInfo(...));

// ✅ 正确：真实创建实体
User user = User.create("zhangsan", "zhangsan@example.com");
when(userDomainService.getUserOrThrow(user.getId())).thenReturn(user);

// ❌ 错误：Mock 实体
User mockUser = mock(User.class);
when(mockUser.getUsername()).thenReturn("zhangsan");
```

---

## 九、代码审查

### 审查顺序

1. **测试存在性**：新增/修改的 public 类是否有对应 `*Test.java`？
2. **覆盖率**：增量代码行覆盖率 ≥ 90%，分支覆盖率 ≥ 90%？
3. **依赖检查**：是否直接调用基础设施实现类？
4. **架构规范**：是否定义应用层异常？是否转换 DomainException？
5. **代码规范**：注释、命名、Lombok 使用是否符合规范？

### 问题分级

| 级别 | 定义 | 处理方式 |
|---|---|---|
| P0 | 违反核心规范 | **必须立即修复**，禁止合并 |
| P1 | 违反一般规范 | **必须修复**，禁止合并 |
| P2 | 不符合最佳实践 | 建议修复，可合并 |

### 审查报告要求

- 必须只列出本次修改涉及的文件和问题
- 历史代码问题不在本次审查范围内，不应出现在报告中
- 报告必须明确标注审查范围（如：新增 3 个文件，修改 2 个文件）
- 若本次无代码变更，报告应标注"无增量代码，无需审查"
- 报告必须包含"审查结论"章节，明确标注"合格/不合格，是否允许合并"

---

## 十、编译验证

1. 执行 `mvn compile -q` 验证编译通过
2. 执行 `mvn test` 验证测试通过
3. 检查并移除所有 unused import
4. 确保 JaCoCo 覆盖率检查通过（行/分支 ≥ 90%）

---

*本文档用于代码审核，所有应用层代码必须遵守本规范。*
