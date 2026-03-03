package com.example.taskplatform.client.service;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AgentNettyWsInboundHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    private static final Logger log = LoggerFactory.getLogger(AgentNettyWsInboundHandler.class);

    private final AgentNettyClientService nettyClientService;

    public AgentNettyWsInboundHandler(AgentNettyClientService nettyClientService) {
        this.nettyClientService = nettyClientService;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        nettyClientService.onConnected();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws Exception {
        nettyClientService.handleInbound(msg.text());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.warn("agent websocket error", cause);
        ctx.close();
    }
}
