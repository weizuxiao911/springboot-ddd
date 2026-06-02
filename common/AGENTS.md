# Common 层 AI 审查规则

> **优先级顺序**：P0 > P1 > P2
> - P0：核心架构违规，**必须立即修复**，禁止合并
> - P1：代码质量违规，**必须修复**，禁止合并
> - P2：最佳实践违规，建议修复，可合并

> 本文档定义共享内核模块（Common Layer）的代码审核规范与约束。
> 所有新增或修改的 common 层代码必须通过审核方可合并。

---

## 快速索引

- [架构约束](#一架构约束)
- [类设计规范](#二类设计规范)
- [单元测试](#三单元测试)
- [代码审查](#四代码审查)
- [禁止事项](#五禁止事项清单)
- [编译验证](#六编译验证)

---

## 一、架构约束

### 1.1 模块定位

`common` 模块是项目的**共享内核（Shared Kernel）**，提供跨层复用的基础类、接口和注解。
它是整个架构的底座，必须保持纯净、稳定和高内聚。

### 1.2 依赖约束

- **零框架依赖**：禁止引入 Spring、JPA、MyBatis、Feign 等任何框架依赖。
- **纯 Java 实现**：仅允许使用 Java 标准库（`java.*`, `javax.*`）和 Lombok。
- **无业务逻辑**：不包含任何特定业务领域的规则或概念。

### 1.3 包结构

```text
common/
├── base/             # 基础抽象 (AggregateRoot, BaseRepository)
├── dto/              # 通用 DTO (R<T> 统一响应体)
├── event/            # 事件机制 (DomainEvent, DomainEventPublisher, DomainEventHandler)
│   └── annotation/   # 事件注解 (@OnEvent)
├── exception/        # 领域异常 (DomainException)
├── context/          # 上下文管理 (AppContext, UserContext)
├── id/               # ID 类型/生成 (ID)
├── pagination/       # 分页模型 (PageQuery, PageResult)
├── annotation/       # 通用技术注解 (@Cache, @Lock)
└── pbac/             # PBAC 权限模型
    ├── domain/        # 权限领域模型 (PermissionCode, EvaluationResult, AccessContext, UserPermissionContext)
    ├── service/       # 权限服务接口 (PBACService)
    ├── exception/     # 权限异常 (AccessDeniedException)
    └── annotation/    # 权限注解 (@RequirePolicy, @RequirePermission)
```

**包职责定义**

| 包 | 职责 | 禁止放入 |
|---|---|---|
| `base/` | 领域建模基础抽象 | 业务逻辑、具体实现 |
| `dto/` | 跨层通用 DTO（如 R<T>） | 业务特定 DTO |
| `event/` | 事件处理机制 | 事件具体实现、业务逻辑 |
| `event/annotation/` | 事件处理注解 | 业务逻辑 |
| `exception/` | 领域异常 | 业务异常子类 |
| `context/` | 应用上下文 | 业务状态 |
| `id/` | ID 类型定义 | 业务 ID 实现 |
| `pagination/` | 分页模型 | 业务查询逻辑 |
| `annotation/` | 通用技术注解 | 业务注解 |
| `pbac/domain/` | PBAC 领域模型 | 权限评估逻辑、基础设施实现 |
| `pbac/service/` | PBAC 服务接口 | 具体实现（在 infrastructure 层） |
| `pbac/exception/` | PBAC 异常 | 业务异常子类 |
| `pbac/annotation/` | PBAC 注解 | 业务逻辑 |

**规则**：
- 按功能域分包，不按层级分包。
- 包名必须清晰表达意图（如 `exception` 而非 `errors`）。
- 新增包需经团队评审。

---

## 二、类设计规范

### 2.1 异常类

- **单一异常**：仅定义 `DomainException`，不定义子类。
- **工厂方法**：通过静态工厂方法创建异常，禁止直接 `new`。
- **错误码**：必须包含 `code` 字段。

```java
// ✅ 正确
throw DomainException.notFound("User", userId);

// ❌ 错误
throw new DomainException("NOT_FOUND", "User not found");
```

### 2.2 上下文类

- **ThreadLocal 管理**：必须提供 `clear()` 方法，防止内存泄漏。
- **私有构造函数**：工具类禁止实例化。

```java
public class AppContext {
    private static final ThreadLocal<Map<String, String>> CONTEXT = new ThreadLocal<>();

    private AppContext() {}

    public static void clear() {
        CONTEXT.remove();
    }
}
```

### 2.3 注解类

- **元注解**：必须声明 `@Retention(RUNTIME)` 和 `@Target`。
- **无实现**：注解仅用于标记，不包含逻辑。

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Cache {
    String key();
    long expire() default 300;
}
```

### 2.4 基类

- **抽象类**：基类必须声明为 `abstract`。
- **泛型支持**：支持泛型参数（如 `BaseRepository<T, ID>`）。
- **无状态**：基类不包含业务状态，仅提供通用能力。

### 2.5 事件相关类

- **DomainEvent**：必须包含 `eventId` 字段（用于幂等去重）。
- **DomainEventPublisher**：仅定义接口，实现在 infrastructure。
- **DomainEventHandler**：仅定义接口，实现在 application。
- **@OnEvent**：仅用于标记事件处理方法，由 infrastructure 扫描注册。

---

## 三、单元测试

### 3.1 覆盖率要求

- **行覆盖率**：≥ 90%
- **分支覆盖率**：≥ 90%
- 未达标代码禁止合并

### 3.2 测试规范

- **测试位置**：测试类放在 `common/src/test/java`，包结构与主代码一致。
- **无 Mock**：common 模块通常无外部依赖，测试应直接运行真实逻辑。

```java
@Test
void shouldThrowExceptionWhenNotFound() {
    assertThatThrownBy(() -> DomainException.notFound("User", "123"))
            .isInstanceOf(DomainException.class)
            .hasFieldOrPropertyWithValue("code", "NOT_FOUND");
}
```

---

## 四、代码审查

### 4.1 审查顺序

1. **依赖检查**：是否引入框架依赖？有 = P0
2. **业务逻辑**：是否包含业务规则？有 = P0
3. **测试覆盖**：是否有测试且覆盖率达标？无 = P0
4. **内存泄漏**：ThreadLocal 是否提供 clear？无 = P1
5. **代码规范**：命名、注释是否符合规范？不符合 = P2

### 4.2 问题分级

| 级别 | 定义 | 处理方式 |
|---|---|---|
| P0 | 违反核心规范 | **必须立即修复**，禁止合并 |
| P1 | 违反一般规范 | **必须修复**，禁止合并 |
| P2 | 不符合最佳实践 | 建议修复，可合并 |

### 4.3 审查报告要求

- 必须只列出本次修改涉及的文件和问题
- 报告必须明确标注审查范围
- 报告必须包含"审查结论"章节

---

## 五、禁止事项清单

| 禁止项 | 级别 | 原因 |
|---|---|---|
| 引入 Spring/JPA 等框架依赖 | P0 | 破坏共享内核纯粹性 |
| 包含业务逻辑规则 | P0 | 属于 domain 层职责 |
| **无单元测试** | **P0** | **质量不达标，禁止合并** |
| 测试覆盖率 < 90% | P0 | 质量不达标 |
| 定义业务异常子类 | P0 | 错误码已足够 |
| DomainEvent 缺少 eventId | P0 | 无法幂等去重 |
| ThreadLocalContext 未提供 clear | P1 | 内存泄漏风险 |
| 异常类直接 new | P1 | 违反工厂方法规范 |
| 注解未声明 Retention | P1 | 运行时不可见 |
| 未按功能域分包 | P1 | 包结构混乱 |
| unused import | P2 | 代码整洁问题 |

---

## 六、编译验证

1. 执行 `mvn compile -q` 验证编译通过
2. 执行 `mvn test` 验证测试通过
3. 检查并移除所有 unused import
4. 确保 JaCoCo 覆盖率检查通过（行/分支 ≥ 90%）

---

*本文档用于代码审核，所有 common 层代码必须遵守本规范。*
