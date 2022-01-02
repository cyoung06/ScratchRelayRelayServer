package kr.syeyoung.scratchrelayserver.fakeDevice;

import org.json.JSONObject;

import java.awt.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class FakeDeviceRegistry {
    private static final Set<FakeDevice> fakeDeviceSet = new HashSet<>();
    public static MicroBit microBit;
    public static EV3 ev3;
    public static Boost boost;
    static {
        registerDevice(microBit = new MicroBit());
        registerDevice(ev3 = new EV3());
        registerDevice(boost = new Boost());
    }

    public static void registerDevice(FakeDevice fd) {
        fakeDeviceSet.add(fd);
    }

    public static Set<FakeDevice> find(JSONObject param) {
        Set<FakeDevice> found = fakeDeviceSet.stream().filter(fd ->{
          try {
              return fd.matchParam(param);
          }catch (Exception e) {
              return false;
          }
        } ).collect(Collectors.toSet());
        return found;
    }

    public static Set<FakeDevice> getFakeDeviceSet() {
        return fakeDeviceSet;
    }

    public static Optional<FakeDevice> find(int peripheralId) {
        return fakeDeviceSet.stream().filter(fd -> fd.getPeripheralID() == peripheralId).findFirst();
    }


    public static boolean isAllConnected() {
        return fakeDeviceSet.stream().noneMatch(f -> f.getConnectedSocket() == null);
    }


}
