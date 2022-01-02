package kr.syeyoung.scratchrelayserver.methods;

import kr.syeyoung.scratchrelayserver.data.ConnectionState;
import kr.syeyoung.scratchrelayserver.data.ScratchLinkContext;
import kr.syeyoung.scratchrelayserver.fakeDevice.FakeDevice;
import kr.syeyoung.scratchrelayserver.utils.RPCJson;
import org.java_websocket.WebSocket;
import org.json.JSONObject;

import java.util.EnumSet;

public class SendMethod implements MethodHandler {
    @Override
    public void handle(WebSocket webSocket, JSONObject object) {
        webSocket.send(RPCJson.createResponse(object, null).toString());

        FakeDevice fd = webSocket.<ScratchLinkContext>getAttachment().getConnectedDevice();
        fd.onMessage(object);
    }

    @Override
    public String getName() {
        return "send";
    }

    @Override
    public EnumSet<ConnectionState> getStates() {
        return EnumSet.of(ConnectionState.CONNECTED);
    }
}
