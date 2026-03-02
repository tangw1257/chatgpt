# Task Platform Demo (Java 21 + Spring Boot + WebSocket)

这是一个按照你描述模式实现的 Demo：

- `server`：带页面的服务端（任务编辑、Client 登记、任务下发、进度查看、更新发布接口）
- `client`：执行端（通过 WebSocket 接收任务，执行命令，实时上报进度，轮询自动更新）
- `common`：服务端和客户端共享消息模型

## 为什么拆成多子模块

建议拆分为多模块：

1. `common` 独立出来，避免 server/client 双端重复定义协议对象。
2. `server` 与 `client` 生命周期和打包目标不同，分离后更方便独立发布。
3. 后续扩展（比如增加 agent 插件、任务类型）更清晰。

## 环境要求

- JDK 21
- Maven 3.9+

## 构建

```bash
mvn clean package
```

## 运行

```bash
# 启动服务端（默认 8080）
mvn -pl server spring-boot:run

# 启动客户端（默认 8090，仅作为进程保活）
mvn -pl client spring-boot:run
```

打开服务端界面：

- http://localhost:8080

## Demo 功能映射

1. 服务端编辑任务：Web 页面可新增任务（Linux / Windows 命令）。
2. 服务端登记 Client：Client 连接后自动 REGISTER，页面显示 client 信息。
3. 服务端发布最新版本：`GET /api/client/update/latest` + `POST /api/client/update/publish`。
4. Client 执行任务并反馈：收到 `TASK_ASSIGN` 后执行命令并上报 `TASK_PROGRESS` / `TASK_RESULT`。
5. Client 自动更新：定时查询更新接口，发现新版本后下载到 `updates/` 目录。

> 当前自动更新是 Demo 形态（下载新包），未实现“替换当前进程并重启”。真实生产可加 launcher 或守护进程完成原地升级。
