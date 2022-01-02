package kr.syeyoung.scratchrelayserver.methods;

import kr.syeyoung.scratchrelayserver.data.ConnectionState;
import kr.syeyoung.scratchrelayserver.data.ScratchLinkContext;
import kr.syeyoung.scratchrelayserver.fakeDevice.FakeDevice;
import kr.syeyoung.scratchrelayserver.fakeDevice.FakeDeviceRegistry;
import kr.syeyoung.scratchrelayserver.utils.RPCJson;
import org.java_websocket.WebSocket;
import org.java_websocket.framing.CloseFrame;
import org.json.JSONObject;

import java.util.EnumSet;
import java.util.Optional;

public class ConnectMethod implements MethodHandler {

    @Override
    public void handle(WebSocket webSocket, JSONObject object) {

        int peripheralId = object.getJSONObject("params").getInt("peripheralId");

        if (peripheralId == -999) {
            webSocket.send(RPCJson.createResponse(object, null).put("result", (Object)null).put("error", new JSONObject()).toString());
            return;
        }

        Optional<FakeDevice> foundDevice = FakeDeviceRegistry.find(peripheralId);

        if (!foundDevice.isPresent()) {
            webSocket.close(CloseFrame.POLICY_VALIDATION, "Not found peripheral");
            return;
        }

        webSocket.<ScratchLinkContext>getAttachment().setState(ConnectionState.CONNECTED);
        try {
            FakeDevice fd = foundDevice.get();
            fd.connect(webSocket);
            webSocket.<ScratchLinkContext>getAttachment().setConnectedDevice(fd);
        } catch (IllegalStateException e) {
            webSocket.close(CloseFrame.POLICY_VALIDATION, "already connected peripheral");
            return;
        }

        webSocket.send(RPCJson.createResponse(object, null).toString());
    }

    @Override
    public String getName() {
        return "connect";
    }

    @Override
    public EnumSet<ConnectionState> getStates() {
        return EnumSet.of(ConnectionState.DISCOVER);
    }
}
