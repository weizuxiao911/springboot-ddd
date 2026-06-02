<div align="center">

# ArchAIHarness · Framework

### 人架构 · AI 编码 · DDD 多租户 企业级脚手架

**一套让 AI 在架构师定义的秩序中安全生长的 Spring Boot 底座**

[![MIT License](https://img.shields.io/badge/License-MIT-success.svg?style=flat-square)](LICENSE)
[![Java 17](https://img.shields.io/badge/Java-17-orange?style=flat-square&logo=openjdk)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3-6DB33F?style=flat-square&logo=spring)](https://spring.io/projects/spring-boot)
[![DDD](https://img.shields.io/badge/Paradigm-DDD-blue?style=flat-square)](https://github.com/ArchAIHarness/docs)
[![AI Friendly](https://img.shields.io/badge/AI-AGENTS.md-9333EA?style=flat-square)](./AGENTS.md)
[![Organization](https://img.shields.io/badge/Org-ArchAIHarness-181717?style=flat-square&logo=github)](https://github.com/ArchAIHarness)

</div>

---

## 它解决什么

> 当 AI 编码能力突飞猛进、却频繁失控时,什么才是真正的护栏?
>
> **答案是:架构师的秩序 —— 用工程化的约束,让 AI 永远在边界内生长。**

`framework` 是 ArchAIHarness 生态的**核心底座**,在每一个新服务诞生的第一天,就把以下能力固化进去:

- 🧱 **严格的 DDD 六模块分层**(common · domain · application · infrastructure · interfaces · bootstrap)
- 🛡 **内置 `AGENTS.md` 强约束规范** —— AI Agent 编码时自动遵守
- 🎯 **PBAC 权限模型** —— 资源/操作/上下文三维权限决策
- 🔔 **统一领域事件机制** —— 本地 + Kafka 双通道,纯 JSON 跨服务通信
- 🏢 **多租户基础架构** —— Gateway Header 透传(`x-tenant-id` / `x-tenant-permissions`)
- 📊 **统一响应/异常/分页/审计** —— 出厂即可用,杜绝重复造轮子

---

## 1 分钟看懂分层

```text
bootstrap        ← 启动入口(@SpringBootApplication)
    ↓
interfaces       ← HTTP/RPC 接口层(Controller, GlobalExceptionAdvice, PBACAspect)
    ↓
application      ← 应用层(AppService, Command/Query/Response DTO)
    ↓
domain           ← 领域层 ⭐ 零框架依赖(AggregateRoot, DomainService, Repository 接口)
    ↓
common           ← 共享内核(R<T>, DomainEvent, PBAC 模型, 注解)
                 ↑
infrastructure   ← 基础设施(JPA 实体, Feign, Kafka, RepositoryImpl) 实现 domain 接口
```

**铁律**:`domain` 与 `common` 零 Spring 依赖,可被任意框架替换、可独立测试。

---

## 快速开始

### 前置要求

| 工具 | 版本 |
| --- | --- |
| JDK | 17+ |
| Maven | 3.9+ |
| MySQL | 8.0+ (可选,可改 H2 跑通) |
| Kafka | 3.x (可选,事件机制需要) |
| Redis | 7.x (可选) |

### 运行

```bash
# 克隆
git clone https://github.com/ArchAIHarness/framework.git
cd framework

# 编译 + 测试(零外部依赖)
mvn clean test

# 启动(需要 MySQL/Kafka/Redis,或注释相关配置)
mvn -pl bootstrap spring-boot:run
```

### 输出验证

启动后访问:

- 📘 **API 文档**: http://localhost:8080/swagger-ui.html
- 💚 **健康检查**: http://localhost:8080/actuator/health
- 📊 **指标**: http://localhost:8080/actuator/metrics

---

## 用作自己项目的脚手架

```bash
# 1. 克隆并改名
git clone https://github.com/ArchAIHarness/framework.git my-service
cd my-service

# 2. 全局替换包名(macOS / Linux)
find . -type f \( -name "*.java" -o -name "*.xml" -o -name "*.yml" \) \
    -exec sed -i '' 's|top.archaiharness.framework|com.your.service|g' {} +

# 3. 移动目录
for m in common domain application infrastructure interfaces bootstrap; do
  for s in main test; do
    [ -d "$m/src/$s/java/top/archaiharness/framework" ] && \
      mkdir -p "$m/src/$s/java/com/your/service" && \
      mv "$m/src/$s/java/top/archaiharness/framework"/* "$m/src/$s/java/com/your/service/" && \
      rm -rf "$m/src/$s/java/top"
  done
done

# 4. 编译验证
mvn clean test
```

---

## 与 AI Agent 协同

本仓库内置 [`AGENTS.md`](./AGENTS.md),作为 AI 编码代理(OpenCode / Claude Code / Cursor / Copilot)的**强制约束**。

### AI 必须遵守的核心规则

```text
✅ 允许:
  bootstrap → interfaces → application → domain → common
              infrastructure → domain → common

❌ 禁止:
  domain → infrastructure / interfaces / Spring 框架
  application → infrastructure
  common → 任何框架
```

> 💡 **当你让 AI 在本框架内开发新功能时,AI 会自动:**
>
> - 把聚合根放到 `domain/`,Repository 接口放到 `domain/`,实现放到 `infrastructure/`
> - 使用 `BigDecimal` 处理金额,使用 `SLF4J` 而非 `System.out`
> - 给所有公开方法添加 Javadoc
> - 通过 `@OnEvent` 注册事件处理器,而非散落的监听代码

各层有独立的 `AGENTS.md`,提供更细颗粒度的约束。

---

## 项目结构

```text
framework/
├── bootstrap/              # 启动模块 @SpringBootApplication
├── interfaces/             # 接口层 Controller / ExceptionAdvice / Aspect
├── application/            # 应用层 AppService / Command / Query
├── domain/                 # 领域层 AggregateRoot / Repository 接口  ⭐ 零依赖
├── infrastructure/         # 基础设施 JPA / Feign / Kafka 实现
├── common/                 # 共享内核 R<T> / Event / PBAC
│
├── AGENTS.md               # 🤖 AI 编码代理约束
├── readme.md               # 📐 详细架构与开发规范(给人看)
├── DOCS.md                 # 双文档体系说明
├── code-review.md          # 代码审查清单
└── pbac-test.http          # PBAC 权限测试用例
```

---

## 核心特性

| 特性 | 说明 |
| --- | --- |
| **DDD 六模块分层** | 严格依赖方向,零跨层污染 |
| **领域事件机制** | 本地 `@OnEvent` + Kafka 双通道 |
| **PBAC 权限** | `@RequirePermission("USER:DELETE")` 注解 + SpEL |
| **统一响应体** | `R<T>` 包装 + `ResponseBodyWrapper` 自动包装 |
| **统一异常** | `GlobalExceptionAdvice` 兜底 + `DomainException` 体系 |
| **多租户上下文** | Gateway Header 透传(`x-tenant-id`, `x-user-id`) |
| **代码质量门禁** | Checkstyle + 编译时层级约束 |

> 详细机制说明见 [readme.md](./readme.md)。

---

## 在生态中的位置

```text
                   ┌──────────────────────────────────┐
                   │  ArchAIHarness/docs              │  哲学 + 方法论
                   │  (philosophy · methodology)      │
                   └────────────────┬─────────────────┘
                                    │ 指导
                                    ▼
        ┌───────────────────────────────────────────────────┐
        │  ArchAIHarness/framework          ← 你在这里       │
        │  (DDD 脚手架 + AGENTS.md 约束 + 多租户 + PBAC)     │
        └─────────┬─────────────────────────────┬───────────┘
                  │                             │
                  ▼                             ▼
         ┌──────────────────┐         ┌──────────────────┐
         │  mcp-sdk         │         │  skill-market    │
         │  (Agent 通信)     │         │  (AI 技能包)      │
         └──────────────────┘         └──────────────────┘
```

---

## 落地数据

| 指标 | 效果 |
| --- | --- |
| 🚀 新服务搭建时间 | **从天级压缩到小时级** |
| ⚡ 整体交付周期 | **缩短 30% ~ 40%** |
| 🛡 架构腐化率 | **由 AGENTS.md 强制约束接近 0** |
| 🧪 领域层测试覆盖 | **≥ 90%(JaCoCo 强制)** |

---

## 适用场景

- ✅ **多租户 SaaS 平台** —— 内置租户上下文与权限基座
- ✅ **企业级业务中台** —— DDD 限界上下文与中台业务域天然映射
- ✅ **金融 / 政企系统** —— 强业务领域 + 严格合规审计
- ✅ **云原生微服务** —— 与 Spring Cloud 生态深度契合
- ⚠️ **不适合**:简单 CRUD、原型验证、单租户工具

---

## 深入阅读

- 📐 [**详细架构与开发规范**](./readme.md) —— 六层职责、命名约定、核心机制(必读)
- 🤖 [**AI Agent 约束规范**](./AGENTS.md) —— AI 编码必读
- 📚 [**双文档体系说明**](./DOCS.md) —— 为什么有 readme + AGENTS 双轨
- ✅ [**代码审查清单**](./code-review.md) —— PR 评审标准
- 🌟 [**ArchAIHarness 方法论**](https://github.com/ArchAIHarness/docs) —— 框架背后的思想体系

---

## 共建社区

本项目以 **MIT 协议**全面开源。无论你是架构师、领域专家、AI 工程师,
我们都欢迎你通过 [Issue](https://github.com/ArchAIHarness/framework/issues) / PR / 案例分享共建生态。

<div align="center">

—— **Engineered by Architects · Empowered by AI** ——

</div>
