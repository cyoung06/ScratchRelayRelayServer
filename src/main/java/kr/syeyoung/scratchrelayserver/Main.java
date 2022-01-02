package kr.syeyoung.scratchrelayserver;

import org.java_websocket.server.DefaultSSLWebSocketServerFactory;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;

public class Main {
    public static void main(String[] args) {
        String host = "device-manager.scratch.mit.edu";
        int port = 20110;
        WebSocketServer server = new Server(new InetSocketAddress(host, port));
        server.setWebSocketFactory(new DefaultSSLWebSocketServerFactory(SSLContextGenerator.getSSLContext()));
        server.run();
    }
}
