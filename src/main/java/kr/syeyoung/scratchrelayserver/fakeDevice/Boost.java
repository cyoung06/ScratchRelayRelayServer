package kr.syeyoung.scratchrelayserver.fakeDevice;

import kr.syeyoung.scratchrelayserver.IOManager;
import kr.syeyoung.scratchrelayserver.utils.RPCJson;
import org.java_websocket.WebSocket;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Base64;

public class Boost implements FakeDevice {

    @Override
    public boolean matchParam(JSONObject param) {
        if (!param.has("filters")) return false;
        JSONObject filter = param.getJSONArray("filters").getJSONObject(0);
        if (!filter.has("services")) return false;
        return filter.getJSONArray("services").getString(0).equals("00001623-1212-efde-1623-785feabcd123");
    }

    private WebSocket connected;

    @Override
    public WebSocket getConnectedSocket() {
        return connected;
    }

    @Override
    public void onMessage(JSONObject object) {
        if (object.getString("method").equals("startNotification") || object.getString("method").equals("read")) {
            // SEND HUB / PORT EVENTS
            onAttached();
        } else if (object.getString("method").equals("write") && object.getJSONObject("params").getString("message").equals("CQCBABAHMjwA")) {
            IOManager.received = true;
        } else if (object.getString("method").equals("write") && object.getJSONObject("params").getString("message").equals("CQCBARAHMjwA")) {
            IOManager.flagConnect();
        } else if (object.getString("method").equals("write") && object.getJSONObject("params").getString("message").equals("CQCBAhAHMjwA")) {
            try {
                IOManager.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (object.getString("method").equals("write") && object.getJSONObject("params").getString("message").equals("CQCBAxAHMjwA")) {
            IOManager.flagSend();
        }
    }

    byte color = (byte) 255;
    public void sendDisconnected() {
        color = 9;
        trigger();
        trigger();
        trigger();
        trigger();
        trigger();
    }
    public void sendConnected() {
        color = 5;
        trigger();
        trigger();
        trigger();
        trigger();
        trigger();
    }
    public void sendError() {
        color = 7;
        trigger();
        trigger();
        trigger();
        trigger();
        trigger();
    }


    private void onAttached() {
        byte[] payload = new byte[9];
        payload[2] = 0x01; // HUB PROP
        payload[3] = 0x03; // FIRMWARE VER
        payload[5] = 0x24;
        payload[6] = 0x02;
        payload[7] = 0x00;
        payload[8] = 0x10;
        sendCommand(payload);
        attachEvent(0x00, 0x27); // A REVERSED 0~360
        attachEvent(0x01, 0x27); // B 0~360
        attachEvent(0x02, 0x27); // C 0~360
        attachEvent(0x03, 0x27); // D 0~360

        attachEvent(0x04, 0x25); // COLOR / ACTION CMD
        attachEvent(0x05, 0x28); // TILT / 2BYTES
    }

    private void sendCommand(byte[] payload) {
        connected.send(RPCJson.createRequest("characteristicDidChange", new JSONObject().put("message", Base64.getEncoder().encodeToString(payload)).put("encoding", "base64")).toString());
    }
    private void attachEvent(int portID, int typeID) {
        byte[] payload = new byte[6];
        payload[2] = 0x04;// HUB ATTACHED
        payload[3] = (byte) portID;// PORT ID
        payload[4] = 0x01;// EVENT
        payload[5] = (byte) typeID;// TYPE ID;
        sendCommand(payload);
    }
    private void sendData(int portID, byte[] payload2) {
        byte[] payload = new byte[4 + payload2.length];
        payload[2] = 0x45;// HUB ATTACHED
        payload[3] = (byte) portID;// PORT I
        System.arraycopy(payload2, 0, payload, 4, payload2.length);
        sendCommand(payload);
    }

    @Override
    public int getPeripheralID() {
        return 2;
    }

    @Override
    public void connect(WebSocket webSocket) {
        if (this.connected != null) throw new IllegalStateException();
        this.connected = webSocket;
        setup(new byte[] {0,0,0,0,0,0});
    }

    @Override
    public void disconnect() {
        this.connected = null;
    }


    private int[] motors = new int[4];
    private byte[] tilts = new byte[2];
    @Override
    public void setup(byte[] arr2) {
        byte[] arr = new byte[arr2.length];
        for (int i = 0; i< arr2.length; i++) {
            arr[arr2.length - i - 1] = (byte) Integer.reverse(arr2[i] << 24);
        }

        tilts[0] = arr[0];
        tilts[1] = arr[1];

        long integer = (((long)arr[2] << 24L) & 0xFF000000L)
                | (((long)arr[3] << 16L) & 0xFF0000L)
                | (((long)arr[4] << 8L) & 0xFF00L)
                | (((long)arr[5]) & 0xFFL);

        motors[0] = (int) -(integer % 361); integer /= 361;
        motors[1] = (int) integer % 361; integer /= 361;
        motors[2] = (int) integer % 361; integer /= 361;
        motors[3] = (int) integer % 361;
    }

    private byte[] fillInt(int val) {
        int bits = Integer.reverseBytes( val);
        byte[] payload = new byte[4];
        payload[0] = (byte) ((bits >> 24) & 0xFF);
        payload[1] = (byte) ((bits >> 16) & 0xFF);
        payload[2] = (byte) ((bits >> 8) & 0xFF);
        payload[3] = (byte) (bits & 0xFF);
        return payload;
    }

    @Override
    public void trigger() {
        sendData(0x00, fillInt(motors[0]));
        sendData(0x01, fillInt(motors[1]));
        sendData(0x02, fillInt(motors[2]));
        sendData(0x03, fillInt(motors[3]));
        sendData(0x04, new byte[] {color});
        sendData(0x05, tilts);
    }

    @Override
    public int getBytes() {
        return 6;
    }
}
