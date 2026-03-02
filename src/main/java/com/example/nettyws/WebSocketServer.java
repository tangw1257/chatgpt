package com.example.nettyws;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class WebSocketServer {

    static final int DEFAULT_PORT = 8080;
    static final String DEFAULT_PATH = "/ws";

    public static void main(String[] args) throws InterruptedException {
        int port = resolvePort(args);
        String path = resolvePath(args);

        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new WebSocketServerInitializer(path));

            Channel channel = bootstrap.bind(port).sync().channel();
            System.out.printf("Netty WebSocket server started at ws://127.0.0.1:%d%s%n", port, path);
            channel.closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully().sync();
            workerGroup.shutdownGracefully().sync();
        }
    }

    static int resolvePort(String[] args) {
        if (args.length == 0 || args[0].isBlank()) {
            return DEFAULT_PORT;
        }
        return Integer.parseInt(args[0]);
    }

    static String resolvePath(String[] args) {
        if (args.length < 2 || args[1].isBlank()) {
            return DEFAULT_PATH;
        }
        String path = args[1];
        return path.startsWith("/") ? path : "/" + path;
    }
}
