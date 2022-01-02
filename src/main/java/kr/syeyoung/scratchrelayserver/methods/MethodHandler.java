package kr.syeyoung.scratchrelayserver.methods;

import kr.syeyoung.scratchrelayserver.data.ConnectionState;
import org.java_websocket.WebSocket;
import org.json.JSONObject;

import java.util.EnumSet;

public interface MethodHandler {
    public void handle(WebSocket webSocket, JSONObject object);

    public String getName();

    public EnumSet<ConnectionState> getStates();
}
