package kr.syeyoung.scratchrelayserver.methods;

import kr.syeyoung.scratchrelayserver.data.ConnectionState;
import kr.syeyoung.scratchrelayserver.data.ScratchLinkContext;
import kr.syeyoung.scratchrelayserver.fakeDevice.FakeDevice;
import kr.syeyoung.scratchrelayserver.fakeDevice.FakeDeviceRegistry;
import kr.syeyoung.scratchrelayserver.utils.RPCJson;
import org.java_websocket.WebSocket;
import org.json.JSONObject;

import java.util.EnumSet;

public class DiscoverMethod implements MethodHandler {
    @Override
    public void handle(WebSocket webSocket, JSONObject object) {
        webSocket.<ScratchLinkContext>getAttachment().setState(ConnectionState.DISCOVER);
        webSocket.send(RPCJson.createResponse(object, null).toString());

        // SEND PERIPHERIALS
        for (FakeDevice fd:FakeDeviceRegistry.find(object.getJSONObject("params"))) {
            if (fd.getConnectedSocket() != null)
                webSocket.send(new JSONObject().put("jsonrpc", "2.0").put("method", "didDiscoverPeripheral").put("params", new JSONObject().put("peripheralId", -999).put("name", "이미 연결되었습니다").put("rssi", 0)).toString());
            else
                webSocket.send(new JSONObject().put("jsonrpc", "2.0").put("method", "didDiscoverPeripheral").put("params", new JSONObject().put("peripheralId", fd.getPeripheralID()).put("name", "스크래치 릴레이").put("rssi", 127)).toString());
        }
    }

    @Override
    public String getName() {
        return "discover";
    }

    @Override
    public EnumSet<ConnectionState> getStates() {
        return EnumSet.of(ConnectionState.NONE);
    }
}
