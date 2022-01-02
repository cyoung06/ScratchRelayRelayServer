package kr.syeyoung.scratchrelayserver.fakeDevice;

import kr.syeyoung.scratchrelayserver.IOManager;
import kr.syeyoung.scratchrelayserver.utils.RPCJson;
import org.java_websocket.WebSocket;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Base64;
import java.util.Random;

public class MicroBit extends Thread implements FakeDevice  {

    @Override
    public boolean matchParam(JSONObject param) {
        if (!param.has("filters")) return false;
        JSONObject filter = param.getJSONArray("filters").getJSONObject(0);
        if (!filter.has("services")) return false;
        return filter.getJSONArray("services").getInt(0) == 61445;
    }

    private WebSocket connected;

    @Override
    public WebSocket getConnectedSocket() {
        return connected;
    }

    @Override
    public void onMessage(JSONObject object) {
        // ㅁㄴㅇㄹ ㅁㄴㅇㄹ
        if (object.getString("method").equals("write")) {
            byte[] bytearr = Base64.getDecoder().decode(object.getJSONObject("params").getString("message"));
            byte command = bytearr[0];
            byte[] payload = new byte[bytearr.length - 1];
            System.arraycopy(bytearr, 1, payload, 0, payload.length);
            if (command == (byte)0x81) {
                IOManager.append(payload);
            } else if (command == (byte)0x82) {
                byte[] total = new byte[3];
                int matrix = 0;
                boolean lastBit = false;
                for (int i = 0; i < payload.length; i++) {
                    byte b = payload[i];
                    for (int j = 0; j < 5; j++) {
                        boolean turnedOn = (b & (1 << j)) > 0;
                        if (i == 4 && j == 4) {
                            lastBit = turnedOn;
                        } else {
                            matrix <<= 1;
                            matrix += turnedOn ? 1 : 0;
                        }
                    }
                }
                total[0] = (byte) ((matrix >> 16) & 0xFF);
                total[1] = (byte) ((matrix >> 8) & 0xFF);
                total[2] = (byte) (matrix & 0xFF);

                if (lastBit) {
                    try {
                        IOManager.EoD();
                    } catch (IOException e) {
                        e.printStackTrace();
                        FakeDeviceRegistry.boost.sendError();
                        byte[] msg = e.getMessage().getBytes();
                        byte[] payload1 = new byte[msg.length + 6];
                        payload1[0] = (byte) 0xFF;
                        payload1[1] = (byte) 0xFF;
                        payload1[2] = (byte) ((msg.length >> 24) & 0xFF);
                        payload1[3] = (byte) ((msg.length >> 16) & 0xFF);
                        payload1[4] = (byte) ((msg.length >> 8) & 0xFF);
                        payload1[5] = (byte) ((msg.length) & 0xFF);
                        System.arraycopy(payload1, 0, msg, 6, msg.length);
                        IOManager.sendData(payload1);
                    }
                } else {
                    IOManager.append(total);
                }
            }
//            Random r = new Random();
//            byte[] randoms = new byte[100];
//            r.nextBytes(randoms);
//            try {
//                IOManager.sendData(randoms);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//            for (byte b:randoms) {
//                System.out.print((Byte.toUnsignedInt(b))+" ");
//            }
//            System.out.println();
        }
    }

    @Override
    public int getPeripheralID() {
        return 0;
    }

    private Thread t;
    @Override
    public void connect(WebSocket webSocket) {
        if (this.connected != null) throw new IllegalStateException();
        this.connected = webSocket;
        setup(new byte[] {0,0,0});
        t = new Thread(new Runnable() {
            @Override
            public void run() {
                while(!isInterrupted()) {
                    trigger();
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                    }
                }
            }
        });
        t.start();
    }

    @Override
    public void disconnect() {
        this.connected = null;
        t.interrupt();
    }

    private boolean lastBtn = false;

    private byte[] payload = new byte[10];
    @Override
    public void setup(byte[] arr) {
        int together = ((arr[0] << 24) & 0xFF000000) | ((arr[1] << 16) & 0xFF0000) | ((arr[2] << 8) & 0xFF00);
        together = Integer.reverse(together) & 0xFFFFFF;

        short high = (short) ((together / 6555 - 3277) * 10);
        short low = (short) ((together % 6555 - 3277) * 10);

        payload[0] = (byte) ((high >> 8) & 0xFF);
        payload[1] = (byte) (high & 0xFF);

        payload[2] = (byte) ((low >> 8) & 0xFF);
        payload[3] = (byte) (low & 0xFF);

        payload[4] = (byte) (lastBtn ? 0 : 1); // BTN A
        payload[5] = (byte) (lastBtn ? 1 : 0); // BTN B
        lastBtn = !lastBtn;

        payload[6] = 1; // PIN 0~2 // CONNECTION INDICATOR
        payload[7] = 0; // DATA END
        payload[8] = 0; // DISCONNECTED

        payload[9] = 0; // STATE
    }

    public void trigger(boolean de) {
        payload[8] = (byte) (de ? 1 : 0);
        String base64 = Base64.getEncoder().encodeToString(payload);
        connected.send(RPCJson.createRequest("characteristicDidChange", new JSONObject().put("message", base64).put("encoding", "base64")).toString());
    }

    @Override
    public void trigger() {
        trigger(false);
    }

    @Override
    public int getBytes() {
        return 3;
    }
}
