package com.example.taskplatform.client.service;

import com.example.taskplatform.client.config.ClientProperties;
import com.example.taskplatform.common.AgentMessage;
import com.example.taskplatform.common.MessageType;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientProtocolHandler;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class AgentNettyClientService {

    private final ObjectMapper objectMapper;
    private final CommandExecutorService executorService;
    private final ClientProperties properties;
    private final EventLoopGroup group = new NioEventLoopGroup();
    private final AtomicReference<Channel> channelRef = new AtomicReference<>();

    public AgentNettyClientService(ObjectMapper objectMapper, CommandExecutorService executorService, ClientProperties properties) {
        this.objectMapper = objectMapper;
        this.executorService = executorService;
        this.properties = properties;
    }

    public void connect() {
        if (isConnected()) {
            return;
        }
        try {
            URI uri = URI.create(properties.serverWsUrl());
            Bootstrap bootstrap = new Bootstrap()
                    .group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ch.pipeline().addLast(new HttpClientCodec());
                            ch.pipeline().addLast(new HttpObjectAggregator(65536));
                            ch.pipeline().addLast(new WebSocketClientProtocolHandler(
                                    uri,
                                    WebSocketVersion.V13,
                                    null,
                                    true,
                                    new DefaultHttpHeaders(),
                                    65536));
                            ch.pipeline().addLast(new AgentNettyWsInboundHandler(AgentNettyClientService.this));
                        }
                    });
            Channel channel = bootstrap.connect(uri.getHost(), uri.getPort()).sync().channel();
            channelRef.set(channel);
        } catch (Exception ignored) {
        }
    }

    public void onConnected() throws Exception {
        AgentMessage register = new AgentMessage(
                MessageType.REGISTER,
                properties.id(),
                null,
                "register",
                0,
                Map.of(
                        "host", InetAddress.getLocalHost().getHostName(),
                        "os", System.getProperty("os.name"),
                        "version", properties.version()));
        send(register);
    }

    public void handleInbound(String payload) throws Exception {
        AgentMessage incoming = objectMapper.readValue(payload, AgentMessage.class);
        if (incoming.type() != MessageType.TASK_ASSIGN) {
            return;
        }
        String os = System.getProperty("os.name").toLowerCase();
        String command = os.contains("win") ? incoming.metadata().get("windowsCommand") : incoming.metadata().get("linuxCommand");
        sendProgress(incoming.taskId(), 20, "received command: " + command);
        String result = executorService.execute(command);
        sendProgress(incoming.taskId(), 80, "command executed");
        send(new AgentMessage(MessageType.TASK_RESULT, properties.id(), incoming.taskId(), result, 100, Map.of()));
    }

    public void sendProgress(String taskId, int progress, String text) throws Exception {
        send(new AgentMessage(MessageType.TASK_PROGRESS, properties.id(), taskId, text, progress, Map.of()));
    }

    public void sendCustomProgress(String taskId, String text, Integer progress) throws Exception {
        int resolvedProgress = progress == null ? 0 : progress;
        String resolvedTaskId = (taskId == null || taskId.isBlank()) ? "manual" : taskId;
        sendProgress(resolvedTaskId, resolvedProgress, text == null ? "" : text);
    }

    public boolean isConnected() {
        Channel channel = channelRef.get();
        return channel != null && channel.isActive();
    }

    private void send(AgentMessage message) throws Exception {
        Channel channel = channelRef.get();
        if (channel == null || !channel.isActive()) {
            throw new IllegalStateException("websocket not connected");
        }
        channel.writeAndFlush(new TextWebSocketFrame(objectMapper.writeValueAsString(message)));
    }

    @PreDestroy
    public void shutdown() {
        Channel channel = channelRef.get();
        if (channel != null) {
            channel.close();
        }
        group.shutdownGracefully();
    }
}
