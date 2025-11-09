package com.debugflow.core.util;

import java.util.concurrent.ThreadLocalRandom;

public final class TraceIds {
    private TraceIds() {}

    public static String randomTraceId() {
        // 16-byte (32 hex chars) ID
        byte[] b = new byte[16];
        ThreadLocalRandom.current().nextBytes(b);
        return toHex(b);
    }

    public static String randomSpanId() {
        // 8-byte (16 hex chars) ID
        byte[] b = new byte[8];
        ThreadLocalRandom.current().nextBytes(b);
        return toHex(b);
    }

    private static String toHex(byte[] b) {
        StringBuilder sb = new StringBuilder(b.length * 2);
        for (byte value : b) sb.append(String.format("%02x", value));
        return sb.toString();
    }
}

