package com.die_macher.tcp_server.context;

public class FlowTypeContextHolder {
    private static final ThreadLocal<Byte> flowTypeHolder = new ThreadLocal<>();

    public static void set(byte flowType) {
        flowTypeHolder.set(flowType);
    }

    public static Byte get() {
        return flowTypeHolder.get();
    }

    public static void clear() {
        flowTypeHolder.remove();
    }
}
