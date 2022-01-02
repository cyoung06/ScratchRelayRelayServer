package kr.syeyoung.scratchrelayserver.fakeDevice;

import org.java_websocket.WebSocket;
import org.json.JSONObject;

import java.util.function.Predicate;

public interface FakeDevice {
    public boolean matchParam(JSONObject param);

    public WebSocket getConnectedSocket();

    public void onMessage(JSONObject object);

    public int getPeripheralID();

    public void connect(WebSocket webSocket);
    public void disconnect();

    public void setup(byte[] arr);
    public void trigger();
    public int getBytes();
}
