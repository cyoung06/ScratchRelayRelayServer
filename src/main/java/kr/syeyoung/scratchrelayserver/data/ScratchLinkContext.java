package kr.syeyoung.scratchrelayserver.data;

import kr.syeyoung.scratchrelayserver.fakeDevice.FakeDevice;

public class ScratchLinkContext {
    private ConnectionState state = ConnectionState.NONE;
    private FakeDevice connectedDevice;

    public ConnectionState getState() {
        return state;
    }

    public void setState(ConnectionState state) {
        this.state = state;
    }

    public FakeDevice getConnectedDevice() {
        return connectedDevice;
    }

    public void setConnectedDevice(FakeDevice connectedDevice) {
        this.connectedDevice = connectedDevice;
    }
}
