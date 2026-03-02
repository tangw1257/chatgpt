package com.example.nettyws;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.util.CharsetUtil;

public class WebSocketFrameHandler extends SimpleChannelInboundHandler<WebSocketFrame> {

    @Override
    public void channelRead0(ChannelHandlerContext ctx, WebSocketFrame frame) {
        if (frame instanceof TextWebSocketFrame textFrame) {
            String requestText = textFrame.text();
            String responseText = "echo: " + requestText;
            ctx.channel().writeAndFlush(new TextWebSocketFrame(responseText));
            return;
        }

        if (frame instanceof PingWebSocketFrame pingFrame) {
            ctx.channel().writeAndFlush(new PongWebSocketFrame(pingFrame.content().retain()));
            return;
        }

        if (frame instanceof CloseWebSocketFrame) {
            ctx.channel().writeAndFlush(frame.retain()).addListener(ChannelFutureListener.CLOSE);
            return;
        }

        throw new UnsupportedOperationException(
                "Unsupported frame type: " + frame.getClass().getName() + " value=" + frame.toString(CharsetUtil.UTF_8));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
