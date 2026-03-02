# Netty WebSocket Server (Java)

一个基于 **Java + Netty + WebSocket** 的最小可运行示例。

## 运行

```bash
mvn clean package
java -jar target/netty-websocket-server-1.0-SNAPSHOT.jar
```

默认地址：

- `ws://127.0.0.1:8080/ws`

可选参数：

```bash
java -jar target/netty-websocket-server-1.0-SNAPSHOT.jar <port> <path>
# 例子
java -jar target/netty-websocket-server-1.0-SNAPSHOT.jar 9090 /chat
```

## 行为

- 文本消息：回显 `echo: <message>`
- `Ping` 帧：返回 `Pong`
- `Close` 帧：优雅关闭连接
