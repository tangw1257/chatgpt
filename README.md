# Task Platform Demo (Java 21 + Spring Boot + Netty WebSocket)

这是一个按照你描述模式实现的 Demo：

- `server`：带页面的服务端（任务编辑、Client 登记、任务下发、进度查看、更新发布接口）
- `client`：执行端（通过 **Netty WebSocket** 接收任务，执行命令，实时上报进度，轮询自动更新）
- `common`：服务端和客户端共享消息模型

## 环境要求

- JDK 21
- Maven 3.9+

## 构建

```bash
mvn clean package
```

## 运行

```bash
# 启动服务端（HTTP 控制台 8080，Netty WS 9001）
mvn -pl server spring-boot:run

# 启动客户端（HTTP 接口 8090，TCP 接口 19090）
mvn -pl client spring-boot:run
```

打开服务端界面：

- http://localhost:8080

## 关键能力

1. **Netty WebSocket 通道**：server/client 均使用 Netty 实现 WS 通信（`ws://localhost:9001/ws/agent`）。
2. **指定执行机下发任务**：服务端在控制台按 `clientId` 指定执行机。
3. **执行机发送接口（HTTP）**：`POST http://localhost:8090/api/send`
   ```json
   {"taskId":"manual-1","text":"hello from http","progress":30}
   ```
4. **执行机发送接口（TCP）**：连接 `localhost:19090`，发送一行 JSON：
   ```json
   {"taskId":"manual-2","text":"hello from tcp","progress":40}
   ```

> TCP 接口采用“一行一个请求”的简单协议，便于脚本或系统对接。
