package kr.syeyoung.scratchrelayserver;

import kr.syeyoung.scratchrelayserver.data.ScratchLinkContext;
import kr.syeyoung.scratchrelayserver.fakeDevice.FakeDevice;
import kr.syeyoung.scratchrelayserver.methods.MethodHandler;
import kr.syeyoung.scratchrelayserver.methods.MethodRegistry;
import org.java_websocket.WebSocket;
import org.java_websocket.drafts.Draft;
import org.java_websocket.exceptions.InvalidDataException;
import org.java_websocket.framing.CloseFrame;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.handshake.ServerHandshakeBuilder;
import org.java_websocket.server.WebSocketServer;
import org.json.JSONObject;

import java.net.InetSocketAddress;
import java.util.Optional;

public class Server extends WebSocketServer {
    public Server(InetSocketAddress inetSocketAddress) {
        super(inetSocketAddress);
    }

    public void onOpen(WebSocket webSocket, ClientHandshake clientHandshake) {
        System.out.println(webSocket +" Connected " + webSocket.getResourceDescriptor());
        webSocket.setAttachment(new ScratchLinkContext());
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        System.out.println("closed " + conn.getRemoteSocketAddress() + " with exit code " + code + " additional info: " + reason);
        if (conn.<ScratchLinkContext>getAttachment() != null) {
            FakeDevice fd = conn.<ScratchLinkContext>getAttachment().getConnectedDevice();
            if (fd != null) fd.disconnect();
            conn.<ScratchLinkContext>getAttachment().setConnectedDevice(null);
        }
    }

    public void onMessage(WebSocket webSocket, String message) {
//        System.out.println("Message received from "+webSocket.getRemoteSocketAddress()+": "+message);
        JSONObject json = new JSONObject(message);
        if (!json.has("method")) {
            webSocket.close(CloseFrame.POLICY_VALIDATION, "method not found");
            return;
        }
        Optional<MethodHandler> mh = MethodRegistry.getMethodHandler(json.getString("method"));
        if (!mh.isPresent()) {
//            webSocket.close(CloseFrame.POLICY_VALIDATION, "method not found");
            return;
        }
        MethodHandler handler = mh.get();
        if (!handler.getStates().contains(webSocket.<ScratchLinkContext>getAttachment().getState())) {
            webSocket.close(CloseFrame.POLICY_VALIDATION, "not valid state for that method");
            return;
        }

        handler.handle(webSocket, json);
    }

    public void onError(WebSocket webSocket, Exception e) {
        System.out.println("onError "+webSocket.getRemoteSocketAddress()+": "+e);
        e.printStackTrace();
    }

    public void onStart() {
        System.out.println("Server started");
    }

    @Override
    public ServerHandshakeBuilder onWebsocketHandshakeReceivedAsServer(WebSocket conn, Draft draft, ClientHandshake request) throws InvalidDataException {
        ServerHandshakeBuilder shb = super.onWebsocketHandshakeReceivedAsServer(conn, draft, request);
        if (!request.getResourceDescriptor().startsWith("/scratch/")) throw new InvalidDataException(CloseFrame.POLICY_VALIDATION);
        return shb;
    }
}
