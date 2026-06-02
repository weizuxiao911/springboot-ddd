# Infrastructure 层 AI 审查规则

> **优先级顺序**：P0 > P1 > P2
> - P0：核心架构违规，**必须立即修复**，禁止合并
> - P1：代码质量违规，**必须修复**，禁止合并
> - P2：最佳实践违规，建议修复，可合并

> 本文档定义基础设施层（Infrastructure Layer）的开发规范、约束和最佳实践。
> 所有新增或修改的基础设施层代码必须遵守本规范。

---

## 快速索引

- [架构约束](#一架构约束)
- [包结构](#12-包结构)
- [技术配置](#二技术配置)
- [业务实现](#三业务实现)
- [外部调用](#四外部调用)
- [单元测试](#五单元测试)
- [代码审查](#六代码审查)
- [问题分级](#七问题分级)
- [禁止事项](#八禁止事项清单)
- [编译验证](#九编译验证)

---

## 目录

- [一、架构约束](#一架构约束)
  - [1.1 分层架构](#11-分层架构)
  - [1.2 包结构](#12-包结构)
- [二、技术配置](#二技术配置)
  - [2.1 技术底座](#21-技术底座)
  - [2.2 自动装配](#22-自动装配)
- [三、业务实现](#三业务实现)
  - [3.1 实体定义](#31-实体定义)
  - [3.2 仓库实现](#32-仓库实现)
  - [3.3 领域服务实现](#33-领域服务实现)
  - [3.4 外部服务接口实现](#34-外部服务接口实现)
- [四、外部调用](#四外部调用)
  - [4.1 Feign 客户端](#41-feign-客户端)
  - [4.2 上下文透传](#42-上下文透传)
- [五、单元测试](#五单元测试)
- [六、代码审查](#六代码审查)
  - [6.1 审查范围](#61-审查范围)
  - [6.2 审查顺序](#62-审查顺序)
  - [6.3 报告要求](#63-报告要求)
- [七、问题分级](#七问题分级)
- [八、禁止事项清单](#八禁止事项清单)
- [九、编译验证](#九编译验证)

---

## 一、架构约束

### 1.1 分层架构

**基础设施层职责**

- 技术底座：提供通用技术封装（JPA、Feign、线程池、上下文透传等）
- 业务实现：实现 `domain` 层定义的接口（Repository、DomainService）
- 数据映射：将领域对象与数据库表/外部系统模型进行转换

**禁止做的事**

- 不写核心业务规则（应由领域层承担）
- 不直接返回外部系统的原始模型到上层
- 不依赖 `application` 模块

**依赖方向**

```
infrastructure → domain
infrastructure → 第三方技术库（Spring Boot、JPA、Feign 等）
```

**Maven 依赖约束**：
- `infrastructure` 的 pom.xml 中仅可声明对 `domain` 的内部依赖
- 禁止声明对 `application` 或 `api` 的依赖
- 第三方技术库依赖由 infrastructure 层统一管理，其他层不直接引入

### 1.2 包结构

**核心原则**：技术封装全部在 `config` 下，业务实现直接放在根包。

```
infrastructure/
├── config/              # 技术底座（所有技术封装都在这里）
│   ├── async/           # 线程池配置
│   ├── context/         # Spring 上下文工具
│   ├── feign/           # Feign 配置
│   └── jpa/             # JPA 基础配置
│       ├── entity/      # BaseEntity
│       ├── repository/  # BaseJpaRepository
│       └── statement/   # SQL 柆截处理器（软删除）
├── persistence/         # 持久化技术实现
│   ├── entity/          # JPA 实体（如 UserEntity）
│   └── repository/      # Spring Data JPA 接口（如 UserJpaRepository）
├── repository/          # 领域仓库实现（实现 domain.repository 接口）
├── service/             # 领域域服务和外部服务接口实现
└── feign/               # Feign 客户端（使用者填充）
```

**职责划分**：
- `persistence/entity/` - JPA 实体，映射数据库表，内部封装与 Domain Entity 的转换逻辑
- `persistence/repository/` - Spring Data JPA 接口，提供数据访问能力
- `repository/` - 领域仓库实现，实现 `domain` 层定义的接口，调用 JPA 接口完成持久化
- `service/` - 领域域服务和外部服务接口实现

**命名规范**：
- 仓库实现：`XxxRepositoryImpl`（如 `UserRepositoryImpl`）
- 领域域服务实现：`XxxDomainServiceImpl`（如 `UserDomainServiceImpl`）
- 外部服务接口实现：`XxxServiceImpl`（如 `UserServiceImpl`）

---

## 二、技术配置

### 2.1 技术底座

`config/` 包下存放所有通用技术封装，与具体业务无关，开箱即用。

**基础组件**

- **BaseEntity**: 包含 `id`, `createTime`, `modifyTime`, `deleted`, `version` 等通用字段
- **BaseJpaRepository**: 继承 `JpaRepository` 和 `JpaSpecificationExecutor`
- **SpringContext**: 获取 Spring Bean 的工具类
- **JpaStatementInspector**: SQL 拦截器，自动实现软删除（DELETE 转 UPDATE，SELECT 加 deleted=0）

**上下文透传组件**：

- **AppContext**: ThreadLocal 存储请求头，支持异步线程透传
- **UserContext**: 强类型封装用户 ID、租户 ID、Trace ID
- **AppContextFilterContextFilter**: 自动提取请求头 + MDC 注入 TraceID
- **ContextTaskDecorator**: 异步线程池上下文 + MDC 透传
- **FeignRequestInterceptor**: Feign 调用自动透传 x- 开头的请求头

### 2.2 自动装配

- 配置类使用 `@Configuration` 注解
- 尽量依赖 Spring Boot 自动配置，避免重复定义
- 按需启用，通过 `@ConditionalOnClass` 或 `@ConditionalOnProperty` 控制

```java
@Configuration
@ConditionalOnClass(ThreadPoolTaskExecutor.class)
public class AsyncConfig {
    @Bean
    public Executor taskExecutor() {
        // ...
    }
}
```

---

## 三、业务实现

### 3.1 实体定义

- 放在 `persistence/entity/` 包下
- 继承 `config.jpa.entity.BaseEntity`
- 使用 JPA 注解映射库表结构
- 内部封装与 Domain Entity 的转换逻辑（`toDomain()` 和 `fromDomain()`）

```java
@Entity
@Table(name = "t_user")
public class UserEntity extends BaseEntity {
    // 业务字段

    public User toDomain() {
        // JPA Entity → Domain Entity
    }

    public static UserEntity fromDomain(User user) {
        // Domain Entity → JPA Entity
    }
}
```

### 3.2 JPA 软删除

Infrastructure 层内置 SQL 拦截器实现自动软删除：

- **DELETE 拦截**：`DELETE FROM table WHERE ...` 自动转为 `UPDATE table SET deleted=1 WHERE ...`
- **SELECT 拦截**：自动追加 `deleted=0` 条件，过滤已软删除数据
- **跳过机制**：SQL 中包含 `ignore_deleted` 或 `force_delete` 关键字时跳过拦截

**启用方式**：
```yaml
spring:
  jpa:
    properties:
      hibernate:
        session_factory:
          statement_inspector: top.archaiharness.framework.infrastructure.config.jpa.statement.JpaStatementInspector
```

### 3.3 仓库实现

- 放在 `repository/` 包下
- 实现 `domain.repository` 中定义的接口
- 使用 `@Service` 注解
- 依赖 `persistence.repository` 中的 JPA 接口
- 转换逻辑委托给 JPA 实体的 `toDomain()` 和 `fromDomain()` 方法

```java
@Service
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {
    private final UserJpaRepository jpaRepository;

    @Override
    public User save(User user) {
        UserEntity entity = UserEntity.fromDomain(user);
        jpaRepository.save(entity);
        return user;
    }

    @Override
    public Optional<User> findById(UserId id) {
        return jpaRepository.findByUserId(id.value()).map(UserEntity::toDomain);
    }
}
```

### 3.4 领域域服务实现

- 放在 `service/` 包下
- 实现 `domain.service` 中定义的接口
- 处理跨聚合的业务逻辑或调用外部系统

### 3.5 外部服务接口实现

- 放在 `service/` 包下
- 实现 `domain.service` 中定义的外部服务接口
- 内部调用 Feign 客户端，完成 DTO → VO 转换
- 必须提供批量方法，禁止 N+1 调用

```java
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    
    private final ProductFeignClient feignClient;
    
    @Override
    public ProductInfo getProductInfo(ProductId productId) {
        ProductDTO dto = feignClient.getById(productId.value()).getData();
        if (dto == null) {
            throw DomainException.notFound("Product", productId.value());
        }
        return new ProductInfo(productId, Money.of(dto.getPrice()), dto.getStock(), dto.getName());
    }
    
    @Override
    public Map<ProductId, ProductInfo> batchGetProductInfo(Set<ProductId> productIds) {
        if (productIds.isEmpty()) {
            return Map.of();
        }
        List<String> ids = productIds.stream().map(ProductId::value).toList();
        Map<String, ProductDTO> dtos = feignClient.batchGetByIds(ids).getData();
        return productIds.stream()
            .collect(Collectors.toMap(
                id -> id,
                id -> {
                    ProductDTO dto = dtos.get(id.value());
                    return dto != null 
                        ? new ProductInfo(id, Money.of(dto.getPrice()), dto.getStock(), dto.getName())
                        : null;
                }
            ));
    }
}
```

---

## 四、外部调用

### 4.1 Feign 客户端

- Feign 客户端定义在 `feign/client/` 包下
- 请求/响应 DTO 定义在 `feign/dto/request/` 和 `feign/dto/response/` 包下
- 降级工厂定义在 `feign/factory/` 包下
- 必须定义降级策略（FallbackFactory）
- 统一超时、重试、熔断配置

```java
@FeignClient(
    name = "user-service",
    url = "${service.user.url}",
    fallbackFactory = UserFeignClientFallbackFactory.class
)
public interface UserFeignClient {
    @GetMapping("/users/{id}")
    R<UserInfoResponse> getUserById(@PathVariable String id);
}
```

### 4.2 上下文透传

Infrastructure 层已内置请求头透传能力：

- **HTTP 请求**：`AppContextFilter` 自动提取所有 Header 到 `AppContext`
- **异步线程**：`ContextTaskDecorator` 自动透传 `AppContext` + MDC
- **Feign 调用**：`FeignRequestInterceptor` 自动透传 x- 开头的请求头
- **日志追踪**：`traceId` 自动注入 MDC，全链路可追踪

无需额外配置，开箱即用。

---

## 五、单元测试

**基础设施层全部免测**。

Infrastructure 层负责技术细节实现（如 JPA 映射、Feign 调用、第三方 SDK 集成），属于纯技术适配层。
- Code Review **不审查**该层的测试覆盖率。
- 业务正确性由 Application/Domain 层的测试保障。
- JaCoCo 覆盖率检查默认跳过（`<skip>true</skip>`）。

---

## 六、代码审查

### 6.1 审查范围

- **仅审查增量代码**（本次提交/PR 新增或修改的代码）
- 历史代码不在审查范围内（除非本次有修改）

### 6.2 审查顺序

审查时必须按以下顺序检查：

1. **依赖检查**：是否引入不必要依赖？是否依赖 `application` 模块？有 = P0
2. **架构规范**：是否在 `config` 外定义技术配置？有 = P1
3. **代码规范**：注释、命名、Lombok 使用是否符合规范？不符合 = P1/P2
4. **防腐层**：是否直接返回外部系统原始模型？有 = P1

### 6.3 报告要求

- **必须只列出本次修改涉及的文件和问题**
- 报告必须包含"审查结论"章节，明确标注"合格/不合格，是否允许合并"

---

## 七、问题分级

| 级别 | 标识 | 定义 | 处理方式 |
|------|------|------|----------|
| 严重 | `P0` | 违反核心规范，影响架构正确性 | **必须立即修复**，禁止合并 |
| 警告 | `P1` | 违反一般规范，影响代码质量 | **必须修复**，禁止合并 |
| 建议 | `P2` | 不符合最佳实践，不影响功能 | 建议修复，可合并 |
| 提示 | `P3` | 代码风格、命名建议 | 可选修复 |

**常见问题分级**

| 问题 | 级别 | 说明 |
|------|------|------|
| 依赖 application 模块 | P0 | 违反 DDD 依赖方向 |
| 编码核心业务规则 | P0 | 应由领域层承担 |
| 在 config 外定义技术配置 | P1 | 保持技术底座统一 |
| 直接返回外部系统原始模型 | P1 | 防腐层原则 |
| Feign 客户端无降级策略 | P1 | 影响系统稳定性 |
| unused import | P2 | 代码整洁问题 |
| 方法命名不贴近业务语义 | P2 | 可读性问题 |

---

## 八、禁止事项清单

| 禁止项 | 级别 | 原因 |
|--------|------|------|
| 依赖 application 模块 | P0 | 违反 DDD 依赖方向 |
| 编码核心业务规则 | P0 | 应由领域层承担 |
| 外部服务接口无批量方法 | P0 | N+1 性能陷阱 |
| DTO → VO 转换遗漏 | P0 | 污染领域层 |
| 在 config 外定义技术配置 | P1 | 保持技术底座统一 |
| 直接返回外部系统原始模型 | P1 | 防腐层原则 |
| Feign 客户端无降级策略 | P1 | 影响系统稳定性 |
| 使用空标记接口 | P2 | 无实际价值 |

---

## 九、编译验证

1. 每次修改后执行 `mvn compile -q` 验证编译通过
2. 检查并移除所有 unused import
3. JaCoCo 覆盖率检查默认跳过

---

*本文档由团队共创，后续所有基础设施层代码必须遵守本规范。*
