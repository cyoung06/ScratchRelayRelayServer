package kr.syeyoung.scratchrelayserver.methods;

import java.util.*;

public class MethodRegistry {
    private static final Map<String, MethodHandler> methods = new HashMap<>();

    static {
        registerMethod(new VersionMethod());
        registerMethod(new DiscoverMethod());
        registerMethod(new ConnectMethod());
        registerMethod(new ReadMethod());
        registerMethod(new SendMethod());
        registerMethod(new NotificationMethod());
        registerMethod(new WriteMethod());
    }
    public static void registerMethod(MethodHandler methodHandler) {
        methods.put(methodHandler.getName(), methodHandler);
    }

    public static Optional<MethodHandler> getMethodHandler(String name) {
        return Optional.ofNullable(methods.get(name));
    }

    public static Collection<MethodHandler> getHandlers() {
        return methods.values();
    }
}
