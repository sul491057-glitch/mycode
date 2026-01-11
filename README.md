# 餐厅智慧管理系统 (Restaurant Management System)
<img width="2560" height="1397" alt="image" src="https://github.com/user-attachments/assets/06790f87-c2fe-4e82-b8e6-18cfb41fac7b" />

本项目是一款基于 Spring Boot 3 和 Vue 3 开发的高性能餐厅管理系统，实现了从用户点餐、预约到后台管理的全流程闭环。

## 🚀 技术亮点

### 1. 安全与会话管理 (JWT + Redis)
* **身份认证**：采用 JWT (Json Web Token) 实现无状态登录校验。
* **高性能会话**：将登录 Token 存入 Redis，实现毫秒级的权限验证，并支持管理员后端强制踢人下线功能。

### 2. 性能优化 (Redis 多级缓存)
* **业务缓存**：对菜品列表 (Product)、订单列表 (Orders) 和预约信息 (Reservation) 进行了 Redis 缓存处理，大幅减少数据库 IO 压力。
* **自动同步**：采用 Cache-Aside 模式，在数据更新时自动清除对应缓存，保证数据一致性。

### 3. 实时交互 (WebSocket)
* **实时通知**：集成 WebSocket 全双工通信，实现用户下单后后台管理系统实时弹窗提醒，无需轮询刷新。

### 4. 架构设计 (MyBatis-Plus)
* **规范开发**：基于 MyBatis-Plus 及其 ServiceImpl 封装，代码结构清晰，严格遵循工业级开发规范。

## 🛠️ 环境依赖
* Java 23 / JDK 21+
* MySQL 8.0
* Redis 6.0+
* Maven 3.8+

## 📦 快速启动
1. 导入数据库脚本 `restaurant_db.sql`。
2. 修改 `application.yml` 中的数据库与 Redis 连接配置。
3. 启动 `ManagementApplication.java` 后端服务。
4. 进入 `frontend` 目录，执行 `npm install` 与 `npm run dev` 启动前端。# mycode
