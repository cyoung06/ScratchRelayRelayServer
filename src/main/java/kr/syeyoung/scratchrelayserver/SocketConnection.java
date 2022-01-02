package kr.syeyoung.scratchrelayserver;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.function.Consumer;

public class SocketConnection extends Thread {
    private Socket s;
    private Consumer<byte[]> dataListener;
    private DataOutputStream bos;
    private DataInputStream bis;
    private Runnable onDisconnect;
    public SocketConnection(InetSocketAddress socketAddress, Consumer<byte[]> onData, Runnable onDisconnect) throws IOException {
        s = new Socket();
        s.connect(socketAddress);
        bos = new DataOutputStream(s.getOutputStream());
        bis = new DataInputStream(s.getInputStream());
        this.onDisconnect = onDisconnect;
    }
    public void disconnect() throws IOException {
        s.close();
    }

    public void writePayload(byte[] payload) throws IOException {
        bos.writeInt(payload.length);
        bos.write(payload);
    }

    private int length = 0;
    private ByteBuffer payload;
    @Override
    public void run() {
        while(!s.isClosed()) {
            try {
                length = bis.readInt();
            } catch (IOException e) {
                continue;
            }
            payload = ByteBuffer.allocate(length);
            byte[] buffer = new byte[1024];
            while(length > 0 && !s.isClosed()) {
                try {
                    int read = bis.read(buffer, 0, Math.min(length, buffer.length));
                    payload.put(buffer, 0 ,read);
                    length -= read;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (!s.isClosed()) {
                payload.flip();
                dataListener.accept(payload.array());
            }
        }

        onDisconnect.run();
    }
}
