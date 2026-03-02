package com.example.nettyws;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WebSocketServerTest {

    @Test
    void shouldUseDefaultsWhenNoArgumentsProvided() {
        assertEquals(8080, WebSocketServer.resolvePort(new String[]{}));
        assertEquals("/ws", WebSocketServer.resolvePath(new String[]{}));
    }

    @Test
    void shouldParseProvidedArguments() {
        String[] args = new String[]{"9090", "chat"};
        assertEquals(9090, WebSocketServer.resolvePort(args));
        assertEquals("/chat", WebSocketServer.resolvePath(args));
    }
}
