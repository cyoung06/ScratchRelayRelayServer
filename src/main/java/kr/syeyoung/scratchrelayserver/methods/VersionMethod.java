package kr.syeyoung.scratchrelayserver.methods;

import kr.syeyoung.scratchrelayserver.data.ConnectionState;
import kr.syeyoung.scratchrelayserver.utils.RPCJson;
import org.java_websocket.WebSocket;
import org.json.JSONObject;

import java.util.EnumSet;

public class VersionMethod implements MethodHandler {
    @Override
    public void handle(WebSocket webSocket, JSONObject object) {
        webSocket.send(RPCJson.createResponse(object, new JSONObject().put("protocol", "1.2")).toString());
    }

    @Override
    public String getName() {
        return "getVersion";
    }

    @Override
    public EnumSet<ConnectionState> getStates() {
        return EnumSet.allOf(ConnectionState.class);
    }
}
