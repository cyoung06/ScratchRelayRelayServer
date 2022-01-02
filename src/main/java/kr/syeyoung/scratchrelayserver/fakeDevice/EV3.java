package kr.syeyoung.scratchrelayserver.fakeDevice;

import kr.syeyoung.scratchrelayserver.utils.RPCJson;
import org.java_websocket.WebSocket;
import org.json.JSONObject;

import java.util.Base64;

public class EV3 implements FakeDevice {

    @Override
    public boolean matchParam(JSONObject param) {
        return param.has("majorDeviceClass") && param.has("minorDeviceClass");
    }

    private WebSocket connected;

    @Override
    public WebSocket getConnectedSocket() {
        return connected;
    }

    @Override
    public void onMessage(JSONObject object) {
        byte[] array = Base64.getDecoder().decode(object.getJSONObject("params").getString("message"));
        if (array.length == 13 && array[7] == -104) {
            // SEND UPDATE!!
            byte[] bytes = new byte[25];
            bytes[4] = 0x02;
            bytes[5] = 29; // COLOR
            bytes[6] = 30; // DIST
            bytes[7] = 16; // BTN
            bytes[8] = 16; // BTN - All connected?
            bytes[21] = 7; // BIG MOTOR
            bytes[22] = 7; // BIG MOTOR
            bytes[23] = 7; // BIG MOTOR
            bytes[24] = 7; // BIG MOTOR

            connected.send(RPCJson.createRequest("didReceiveMessage", new JSONObject().put("message", Base64.getEncoder().encodeToString(bytes)).put("encoding", "base64")).toString());
        }
        trigger();
    }

    @Override
    public int getPeripheralID() {
        return 1;
    }

    @Override
    public void connect(WebSocket webSocket) {
        if (this.connected != null) throw new IllegalStateException();
        this.connected = webSocket;
        setup(new byte[] {0,0,0,0,0,0,0,0,0});
    }

    @Override
    public void disconnect() {
        this.connected = null;
    }


    byte[] payload = new byte[38];
    @Override
    public void setup(byte[] arr2) {
        byte[] arr = new byte[arr2.length];
        for (int i = 0; i< arr2.length; i++) {
            arr[arr2.length - i - 1] = (byte) Integer.reverse(arr2[i] << 24);
        }



        payload[4] = 0x02;
        fillInt(encode24bit(arr[0], arr[1], arr[2]), 5); // SENSORs 1~4 // BRIGHTNESS just a float
        long reallylong = (((long)arr[3]<< 40L)  & 0xFF0000000000L)
                | (((long)arr[4]<< 32L)   & 0xFF00000000L)
                | (((long)arr[5] << 24L)  & 0xFF000000L)
                | (((long)arr[6] << 16L) & 0xFF0000L)
                | (((long)arr[7] << 8L) & 0xFF00L)
                | ((long)arr[8] & 0xFFL);
        int[] motors = new int[5];
        int asdf = (int) ((reallylong >> 47L) & 0x1);
        reallylong = reallylong & 0x7FFFFFFFFFFFL;

        fillInt(Float.floatToIntBits((motors[0] = (int) (reallylong % 10001)) / 100.0f), 9); // DISTANCE float 0 ~ 10000 / 100
        reallylong /= 10001;

        fillInt(Float.floatToIntBits((float) asdf), 13); // BTN
        fillInt(Float.floatToIntBits(FakeDeviceRegistry.isAllConnected() ? 1 : 0), 17); // HEYYY

        fillInt(motors[1] = (int) (reallylong % 361), 21); reallylong /= 361; // MOTORS 1~4. has to be 0~360
        fillInt(motors[2] = (int) (reallylong % 361), 25); reallylong /= 361;
        fillInt(motors[3] = (int) (reallylong % 361), 29); reallylong /= 361;
        fillInt(motors[4] = (int) (reallylong % 361), 33);reallylong /= 361;
    }

    public int encode24bit(byte bit1, byte bit2, byte bit3) {
        int base = (((bit1 << 24) & 0x80000000) | (0x4B000000));
        bit1 &= 0x7F;
        return base | ((bit1 << 16) & 0xFF0000) | ((bit2 << 8) & 0xFF00) | (bit3 & 0xFF);
    }

    private void fillInt(int val, int startidx) {
        int bits = Integer.reverseBytes( val);
        payload[startidx] = (byte) ((bits >> 24) & 0xFF);
        payload[startidx + 1] = (byte) ((bits >> 16) & 0xFF);
        payload[startidx + 2] = (byte) ((bits >> 8) & 0xFF);
        payload[startidx + 3] = (byte) (bits & 0xFF);
    }

    @Override
    public void trigger() {
        fillInt(Float.floatToIntBits(FakeDeviceRegistry.isAllConnected() ? 1 : 0), 17); // HEYYY
        String base64 = Base64.getEncoder().encodeToString(payload);
        connected.send(RPCJson.createRequest("didReceiveMessage", new JSONObject().put("message", base64).put("encoding", "base64")).toString());
    }

    @Override
    public int getBytes() {
        return 9;
    }
}
