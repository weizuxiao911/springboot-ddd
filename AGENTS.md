# Service 模块通用规则

本文档定义 Service 模块的通用代码审查规则，适用于所有层。

---

## 1. 通用规则

- 代码无警告级别的编译问题
- 无未使用的 import、变量、方法
- 无 `System.out.println`，统一使用 SLF4J (`@Slf4j`)
- 无硬编码的魔法值，常量提取到类或配置文件
- 异常不吞咽（空 catch），必须处理或向上抛出
- 公开方法有 Javadoc（类和 public 方法）

---

## 2. 分层依赖检查

```
✅ 允许的依赖方向：
  bootstrap → interfaces → application → domain → common
                infrastructure → domain → common

❌ 禁止的依赖：
  domain → infrastructure / interfaces / Spring 框架
  application → infrastructure / interfaces
  common → 任何框架（Spring、JPA、Feign 等）
```

- `common` 层零框架依赖（纯 Java + Lombok + jackson-annotations）
- `domain` 层无 Spring 注解（`@Service`、`@Component` 等）
- `domain` 仓储为接口，实现在 `infrastructure`
- `application` 层不直接操作数据源

---

## 3. 事件机制检查

- 事件处理器使用 `@OnEvent(XxxEvent.class)` 注解
- 事件处理器幂等（通过 `EventIdempotent` 记录去重）
- Kafka 消息使用 JSON 格式，含 `eventType`、`eventId`、`occurredAt`、`payload` 字段
- `EventDispatcher` 不在 `@PostConstruct` 中调用 `getBean()`（避免循环依赖）

---

## 4. PBAC 权限检查

- 权限码命名规范：`领域:操作`，如 `USER:DELETE`、`ORDER:VIEW`
- SpEL 表达达式中不含 `:`（冒号），固定权限码直接写字符串
- `@RequirePermission` 的 `resource + action` 自动组合为 `RESOURCE:ACTION`
- Gateway 透传 Headers：`x-user-id`、`x-tenant-id`、`x-accessible-tenants`、`x-tenant-permissions`
- `AccessDeniedException` 由 `GlobalExceptionAdvice` 统统一捕获返回 403

---

## 5. 数据库检查

- DDL 维护在 `infrastructure/ddl.sql`
- 表名 `t_` 前缀，字段名下划线命名
- 公共字段：`id`(自增)、`create_time`、`modify_time`、`deleted`、`version`
- 索引命名：`uk_` 唯一索引、`idx_` 普通索引

---

## 6. 测试检查

- 领域层单元测试覆盖率 ≥ 90%（JaCoCo）
- 测试使用 JUnit 5 + Mockito，不依赖 Spring 上下文
- 测试方法命名：`shouldXxxWhenYyy` 或 `方法名_场景_预期`

---

## 7. 安全检查

- 无明文密码、密钥提交到代码库
- `.env`、`credentials.json` 等文件在 `.gitignore` 中
- 日志中不打印敏感信息（密码、Token、身份证号）
- SQL 使用参数化查询，无字符串拼接

---

## 各层规范文件

| 层 | 规范文件 |
|---|---------|
| Common | common/AGENTS.md |
| Domain | domain/AGENTS.md |
| Application | application/AGENTS.md |
| Infrastructure | infrastructure/AGENTS.md |
| Interfaces | interfaces/AGENTS.md |
