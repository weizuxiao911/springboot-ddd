# Domain 层 AI 审查规则

> **优先级顺序**：P0 > P1 > P2
> - P0：核心架构违规，**必须立即修复**，禁止合并
> - P1：代码质量违规，**必须修复**，禁止合并
> - P2：最佳实践违规，建议修复，可合并

> 本文档定义领域层（Domain Layer）的代码审核规范与约束。
> 所有新增或修改的领域层代码必须通过审核方可合并。

---

## 快速索引

- [架构约束](#一架构约束)
- [包结构](#二包结构)
- [异常规范](#三异常规范)
- [建模规范](#四建模规范)
- [禁止事项](#五禁止事项清单)
- [测试规范](#六测试规范)
- [代码审查](#七代码审查)
- [编译验证](#八编译验证)

---

## 一、架构约束

### 1.1 分层架构

**领域层不依赖任何框架**

- 禁止引入 Spring、JPA、MyBatis 等框架依赖
- 禁止使用 `@Component`、`@Service`、`@Repository`、`@Entity` 等框架注解
- 仅允许使用 Lombok、Java 标准库等纯 Java 工具

**依赖方向**

```
domain → common
```

domain 模块依赖 common 模块，使用共享组件。

**领域层职责**

- 承载核心业务规则与不变式
- 定义聚合（Aggregate）、实体（Entity）、值对象（Value Object）及其行为
- 定义仓库接口（由 infrastructure 实现）
- 定义外部服务接口（由 infrastructure 实现）
- 产生领域事件

---

## 二、包结构

### 项目结构

```
domain/
├── {aggregate}/              # 聚合包（根据业务命名）
│   ├── entity/             # 实体，继承 AggregateRoot 即为聚合根
│   ├── event/               # 领域事件
│   ├── repository/         # 仓库接口
│   ├── service/            # 领域服务和外部服务接口
│   └── vo/                  # 值对象和枚举
```

### 外部服务接口（service/）

**用途**：定义跨服务交互的业务接口，由 infrastructure 层实现。

**核心原则**：
- 接口定义在 Domain 层，是**业务概念**，不是 RPC 接口
- 返回值必须是**领域值对象**（VO），禁止返回 DTO
- 接口命名格式：`<业务>Service`（如 `ProductService`、`TenantService`）
- **必须提供批量方法**，禁止只定义单条查询接口

**定义规范**：
```java
// ✅ 正确：业务接口，返回领域值对象
public interface ProductService {
    ProductInfo getProductInfo(ProductId productId);
    Map<ProductId, ProductInfo> batchGetProductInfo(Set<ProductId> productIds);
}

// ❌ 错误：定义 RPC 接口
public interface ProductFeignClient {
    @GetMapping("/products/{id}")
    R<ProductDTO> getById(@PathVariable String id);
}
```

**值对象定义**（vo/）：
```java
// 外部服务返回的领域值对象
public record ProductInfo(
    ProductId productId,
    Money price,
    Integer stock,
    String productName
) {}
```

---

## 三、异常规范

**核心原则**

- **一个 `DomainException` 就够了**，不定义子类异常
- 通过错误码区分场景，而不是通过异常类型
- `DomainException` 定义在 `common.exception` 模块

**工厂方法**

```java
throw DomainException.invalidState("cannot be empty");
throw DomainException.notFound("Xxx", id);
throw DomainException.alreadyExists("Xxx", identifier);
throw DomainException.of("CUSTOM_CODE", "message");
```

**禁止事项**

| 禁止项 | 级别 |
|---|---|
| 直接 `new DomainException(...)` | P0 |
| 定义异常子类（如 `XxxNotFoundException`） | P0 |
| 使用 `IllegalArgumentException` 表达业务错误 | P1 |
| 使用 `RuntimeException` 表达业务错误 | P1 |

---

## 四、建模规范

### 命名规则

| 类型 | 命名规则 | 示例 |
|---|---|---|
| 实体 | 业务名词 | `User`、`Order` |
| 值对象 ID | 实体名 + Id | `UserId`、`OrderId` |
| 状态枚举 | 实体名 + Status | `UserStatus` |
| 领域事件 | 实体名 + 动作 + Event | `UserCreatedEvent` |
| 仓库接口 | 实体名 + Repository | `UserRepository` |
| 领域服务 | 实体名 + DomainService | `UserDomainService` |
| 外部服务接口 | 业务名 + Service | `ProductService`、`TenantService` |
| 工厂方法 | create / of | `User.create(...)` |
| 业务方法 | 动词开头 | `activate()`、`changeEmail()` |

### 实体（entity/）

- 继承 `AggregateRoot`（即为聚合根）
- 使用 `@Getter` 暴露只读属性
- 使用 `@NoArgsConstructor(access = AccessLevel.PROTECTED)`
- `id` 字段使用 `@EqualsAndHashCode.Include`
- **禁止**实体基类使用 `@EqualsAndHashCode`

### 值对象（vo/）

- **推荐使用 `record`**（Java 17+），天然不可变且代码简洁。
- 若使用类：使用 `@Getter` + `@EqualsAndHashCode`，字段声明为 `final`。
- **禁止**继承任何基类。

### ID 值对象

- **必须**使用值对象类型（如 `UserId`），禁止使用 `String` 或 `Long`
- 提供 `of(String)` 工厂方法和 `generate()` 生成方法

---

## 五、禁止事项清单

| 禁止项 | 级别 | 原因 |
|---|---|---|
| 引入框架依赖（Spring、JPA等） | P0 | 破坏领域纯粹性 |
| 使用 `@Builder` 但未重写 `build()` 方法 | P0 | 必须在 build() 中做强校验和事件注册 |
| 定义异常子类 | P0 | 错误码已足够 |
| 跨聚合直接引用 | P0 | 破坏聚合边界 |
| 在领域层定义事务 | P0 | 属于应用层职责 |
| 直接 `new DomainException(...)` | P0 | 必须使用工厂方法 |
| 实体使用 public 构造函数 | P0 | 必须使用工厂方法 |
| 领域事件缺少 `eventId` | P0 | 无法幂等去重 |
| 仓库接口不继承 `BaseRepository` | P0 | 违反基础接口规范 |
| 外部服务接口返回 DTO | P0 | 污染领域层，必须返回值对象 |
| 在领域层定义 RPC 接口（@FeignClient） | P0 | 破坏领域纯粹性 |
| **无单元测试** | **P0** | 质量不达标 |
| 测试覆盖率 < 90% | P0 | 质量不达标 |
| 使用 `IllegalArgumentException` 表达业务错误 | P1 | 无法区分错误类型 |
| 值对象继承基类 | P1 | 限制设计 |
| 实体无注释 | P1 | 降低可读性 |
| 使用空标记接口 | P2 | 无实际价值 |

---

## 六、测试规范

### 覆盖率要求

- **行覆盖率**：≥ 90%
- **分支覆盖率**：≥ 90%
- 未达标禁止合并

### 测试要求

- 每个 public 类必须有对应测试类（`<ClassName>Test`）
- 无测试 = P0 严重问题，禁止合并

### 测试范围

| 元素 | 测试要求 |
|---|---|
| 实体行为方法 | 必须测试正常流程、边界条件、异常场景 |
| 工厂方法 | 必须测试创建结果、默认值、事件注册 |
| 值对象 | 必须测试相等性、校验逻辑 |

---

## 七、代码审查

### 审查顺序

1. **测试存在性**：新增/修改的 public 类是否有对应 `*Test.java`？
2. **覆盖率**：增量代码行覆盖率 ≥ 90%，分支覆盖率 ≥ 90%？
3. **依赖检查**：是否引入框架依赖？
4. **架构规范**：是否使用 `@Builder` 创建实体？是否定义异常子类？
5. **代码规范**：注释、命名是否符合规范？

### 问题分级

| 级别 | 定义 | 处理方式 |
|---|---|---|
| P0 | 违反核心规范 | **必须立即修复**，禁止合并 |
| P1 | 违反一般规范 | **必须修复**，禁止合并 |
| P2 | 不符合最佳实践 | 建议修复，可合并 |

### 审查报告要求

- 必须只列出本次修改涉及的文件和问题
- 报告必须明确标注审查范围
- 报告必须包含"审查结论"章节

---

## 八、编译验证

1. 执行 `mvn compile -q` 验证编译通过
2. 执行 `mvn test` 验证测试通过
3. 检查并移除所有 unused import

---

*本文档用于代码审核，所有领域层代码必须遵守本规范。*
