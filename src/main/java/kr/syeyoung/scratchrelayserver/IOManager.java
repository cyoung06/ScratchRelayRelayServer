package kr.syeyoung.scratchrelayserver;

import kr.syeyoung.scratchrelayserver.fakeDevice.FakeDevice;
import kr.syeyoung.scratchrelayserver.fakeDevice.FakeDeviceRegistry;
import kr.syeyoung.scratchrelayserver.fakeDevice.MicroBit;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Arrays;

public class IOManager {

    private static ByteArrayOutputStream baos = new ByteArrayOutputStream();
    private static int type = 0;
    private static SocketConnection connection = null;

    public static void flagConnect() {
        type = 1;
        System.out.println("Connect Flagged");
    }
    public static void flagSend() {
        type = 2;
        System.out.println("Send Flagged");
    }
    public static void EoD() throws IOException {
        byte[] payload = baos.toByteArray();
        baos = new ByteArrayOutputStream();

        System.out.println("Data end - ty-e"+type);
        if (type == 2) {
            if (connection != null)
                connection.writePayload(payload);
        } else if (type == 1){
            String socket = new String(payload);
            if (connection == null)
                connection = new SocketConnection(new InetSocketAddress(socket.split(":")[0], Integer.parseInt(socket.split(":")[1])), IOManager::sendData, () -> {
                    connection = null;
                    FakeDeviceRegistry.boost.sendDisconnected();
                });
            FakeDeviceRegistry.boost.sendConnected();
        }

        type = 0;
    }
    public static void disconnect() throws IOException {
        System.out.println("disconnect ");
        if (connection != null)
            connection.disconnect();
        connection = null;
        FakeDeviceRegistry.boost.sendDisconnected();
    }

    public static void append(byte[] bytes) {
        try {
            baos.write(bytes);
            System.out.println("appended - total"+new String(baos.toByteArray()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void sendData(byte[] bytes){
        if (!FakeDeviceRegistry.isAllConnected()) return;

        new Thread(new Runnable() {
            @Override
            public void run() {
                int totalBytes = 18;
                int iterations = (int) Math.ceil(bytes.length / (double)totalBytes);

                for (int i = 0; i < iterations; i++) {
                    byte[] currPayload = new byte[totalBytes];
                    System.arraycopy(bytes, i * totalBytes, currPayload, 0, Math.min(totalBytes, bytes.length - totalBytes * i));

                    int idx = 0;
                    MicroBit mb = null;
                    for (FakeDevice fd: Arrays.asList(FakeDeviceRegistry.microBit, FakeDeviceRegistry.ev3, FakeDeviceRegistry.boost)) {
                        byte[] required = new byte[fd.getBytes()];
                        System.arraycopy(currPayload, idx, required, 0, fd.getBytes());
                        fd.setup(required);

                        idx += fd.getBytes();

                        if (fd instanceof MicroBit) {
                            mb = (MicroBit) fd;
                        } else {
                            fd.trigger();
                        }
                    }
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    mb.trigger();

                    while(!received);
                    received = false;

                    try {
                        Thread.sleep(30);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                FakeDeviceRegistry.microBit.trigger(true);
            }
        }).start();
    }
    public static volatile boolean received = false;
}
