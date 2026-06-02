# 领域层（Domain Layer）

> 代码审核规则见 rule.md，所有代码必须遵守。

## 定位与职责

领域层承载核心业务规则与不变式（Invariants），是业务价值的真正所在。

**职责**：

- 定义聚合（Aggregate）、实体（Entity）、值对象（Value Object）及其行为
- 定义仓库接口（Repository），由基础设施层实现
- 定义领域事件（Domain Event），表达业务事实
- 暴露领域服务（Domain Service）与工厂（Factory）
- 校验业务规则，确保不变式成立

**不做的职责**：

- 不依赖框架（Spring、JPA、MyBatis 等）
- 不承担用例编排与事务边界（应用层职责）
- 不暴露内部结构给外部层

***

## 依赖方向

```
domain → common
```

domain 模块依赖 common 模块，使用 common 中定义的共享组件。

***

## 包结构

```
domain/
├── {aggregate}/              # 聚合包（根据业务命名）
│   ├── entity/              # 实体（继承 AggregateRoot 即为聚合根）
│   ├── event/              # 领域事件
│   ├── repository/        # 仓库接口
│   ├── service/           # 领域服务
│   └── vo/                 # 值对象和枚举
```

| 包           | 职责                         | 命名规则                                                  |
| ----------- | -------------------------- | ----------------------------------------------------- |
| entity/     | 实体（继承 AggregateRoot 即为聚合根） | 业务名词，如 `User`、`Order`                                 |
| event/      | 领域事件定义                     | 实体名 + 动作 + Event，如 `UserCreatedEvent`                 |
| repository/ | 仓库接口定义（由基础设施实现）            | 实体名 + Repository，如 `UserRepository`                   |
| service/    | 跨聚合或复杂业务规则                 | 实体名 + DomainService，如 `UserDomainService`             |
| vo/         | 值对象、枚举、ID 类型               | ID：实体名 + Id，如 `UserId`；枚举：实体名 + Status，如 `UserStatus` |

***

## 实体

实体有唯一标识，随生命周期变化而状态变更。

**聚合根**：继承 `AggregateRoot` 的实体，封装业务不变式，管理领域事件。

**强制约束**：
- 必须使用私有构造函数 + 工厂方法创建，禁止 public 构造函数
- 实体 ID 必须定义值对象类型（如 `UserId`），禁止 `String`/`Long`

```java
@Getter
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class XxxEntity extends AggregateRoot {  // 聚合根

    @EqualsAndHashCode.Include
    private XxxId id;

    private String name;

    private XxxStatus status;

    // 私有构造函数，通过工厂方法创建
    private XxxEntity(...) { ... }

    // 工厂方法，确保创建时满足不变式
    public static XxxEntity create(...) {
        if (invalid) {
            throw DomainException.invalidState("reason");
        }
        return new XxxEntity(...);
    }

    // 或使用 @Builder（必须重写 build() 方法）
    @Builder
    public static class Builder {
        public XxxEntity build() {
            if (invalid) {
                throw DomainException.invalidState("reason");
            }
            XxxEntityXxxEntity = new XxxEntity();
            // ... 设置字段
            entity.registerEvent(new XxxCreatedEvent(entity.getId()));
            return entity;
        }
    }
}
```

***

## 值对象

值对象是不可变的，无唯一标识，用于表达概念。

**推荐使用 `record`（Java 17+）**，代码更简洁且天然不可变。

```java
public record UserId(String value) {

    public UserId {
        if (value == null || value.isBlank()) {
            throw DomainException.invalidState("UserId cannot be empty");
        }
    }

    public static UserId of(String value) {
        return new UserId(value);
    }

    public static UserId generate() {
        return new UserId(IdGenerator.generate());
    }
}
```

**枚举**：

```java
public enum XxxStatus {
    ACTIVE,
    INACTIVE;

    public static XxxStatus of(String value) {
        return Arrays.stream(values())
            .filter(s -> s.name().equalsIgnoreCase(value))
            .orElseThrow(() -> DomainException.invalidState("Unknown XxxStatus: " + value));
    }
}
```

***

## 仓库接口

领域层定义仓库接口，由基础设施层实现。

**必须继承 `BaseRepository<T, ID>`**。

```java
public interface XxxRepository extends BaseRepository<XxxEntity, XxxId> {

    Optional<XxxEntity> findById(XxxId id);

    PageResult<XxxEntity> findAll(PageQuery pageQuery);

    XxxEntity save(XxxEntity entity);

    void deleteById(XxxId id);

    boolean existsById(XxxId id);
}
```

***

## 领域服务

当业务规则不适合放入单一实体时（如跨聚合交互），使用领域服务。

**跨聚合交互示例**：

```java
public class OrderDomainService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    @Transactional // 注意：领域服务通常不定义事务，事务应在 Application 层
    public void createOrder(CreateOrderCommand cmd) {
        // 1. 获取产品聚合根
        Product product = productRepository.findById(cmd.getProductId())
            .orElseThrow(() -> DomainException.notFound("Product", cmd.getProductId()));

        // 2. 检查库存（产品聚合根内部行为）
        product.deductStock(cmd.getQuantity());

        // 3. 创建订单聚合根
        Order order = Order.create(product, cmd);

        // 4. 保存（由 Application 层调用 Repository 保存，或在此处调用）
        // 注意：通常领域服务返回聚合根，由 Application 层负责持久化
    }
}
```

***

## 领域事件

领域事件表达业务事实，**必须包含 `eventId`**（用于消费者幂等去重）。

实体通过 `registerEvent` 收集事件，**不直接发布**，由应用层在事务提交后发布。

**禁止跨聚合直接引用**，应通过领域服务协调。

```java
@Getter
public class XxxCreatedEvent extends DomainEvent {

    private final XxxId entityId;

    public XxxCreatedEvent(XxxId entityId) {
        this.entityId = entityId;
    }
}
```

**在实体中使用**：

```java
public static XxxEntity create(...) {
    XxxEntity entity = new XxxEntity(...);
    entity.registerEvent(new XxxCreatedEvent(entity.getId()));
    return entity;
}
```

***

## 异常规范

使用 `DomainException`，通过工厂方法抛出，**禁止直接** **`new`**。

```java
// ✅ 正确
throw DomainException.invalidState("cannot be empty");
throw DomainException.notFound("Xxx", id);
throw DomainException.alreadyExists("Xxx", identifier);
throw DomainException.of("CUSTOM_CODE", "custom message");

// ❌ 禁止
throw new DomainException("NOT_FOUND", "message");
throw new RuntimeException("message");
throw new IllegalArgumentException("message");
```

**禁止事项**：

- 禁止定义 `XxxNotFoundException` 等异常子类
- 禁止直接 `new DomainException(...)`
- 禁止使用 `IllegalArgumentException`/`RuntimeException` 表达业务错误
- 禁止在领域层定义事务边界

***

## 基类与共享组件

**基类**

| 基类                      | 用途          |
| ----------------------- | ----------- |
| `AggregateRoot`         | 实体基类，封装领域事件 |
| `BaseRepository<T, ID>` | 仓库基础接口      |

**共享组件**

| 组件                         | 定义位置                |
| -------------------------- | ------------------- |
| `DomainException`          | `common.exception`  |
| `DomainEvent`              | `common.event`      |
| `DomainEventPublisher`     | `common.event`      |
| `PageQuery` / `PageResult` | `common.pagination` |
| `AppContext`               | `common.context`    |
| `ID`                       | `common.id`         |

***

## 代码风格

- 实体：有唯一标识，使用 `@Getter`，`id` 使用 `@EqualsAndHashCode.Include`
- 聚合根：特殊的实体，必须继承 `AggregateRoot`，使用私有构造函数 + 工厂方法或 `@Builder`（但必须重写 build() 方法做强校验和事件注册）
- 实体 ID：必须定义值对象类型（如 `UserId`），禁止使用 `String` 或 `Long`
- 值对象：推荐 `record`，字段 `final`，使用 `@EqualsAndHashCode`

***

## 单元测试

- 测试类必须以 `Test` 结尾（如 `UserTest`）
- **覆盖率要求**：行覆盖率 ≥ 90%，分支覆盖率 ≥ 90%
- 实体测试：验证不变式、业务方法、领域事件
- 值对象测试：验证构造、相等性
- 领域服务测试：验证复杂规则
- 仓库测试由基础设施层负责

```java
@ExtendWith(MockitoExtension.class)
class XxxEntityTest {

    @Test
    void should_throw_when_create_with_invalid_input() {
        assertThatThrownBy(() -> XxxEntity.create(invalidInput))
            .isInstanceOf(DomainException.class);
    }

    @Test
    void should_raise_event_when_create() {
        XxxEntity entity = XxxEntity.create(validInput);
        assertThat(entity.getDomainEvents())
            .hasSize(1)
            .first()
            .isInstanceOf(XxxCreatedEvent.class);
    }
}
```

