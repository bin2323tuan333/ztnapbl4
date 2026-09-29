package com.example.protocol;

public class FrameType {
    public static final byte CONNECT = 0x01;
    public static final byte DATA = 0x02;
    public static final byte CLOSE = 0x03;
    public static final byte CONNECT_ACK = 0x04;
    public static final byte PING = 0x05;
    public static final byte PONG = 0x06;
    public static final byte ERROR = 0x07;

    // Status cho CONNECT_ACK
    public static final byte STATUS_OK                = 0x00;
    public static final byte STATUS_AUTH_FAIL         = 0x01;
    public static final byte STATUS_SERVICE_NOT_FOUND = 0x02;
    public static final byte STATUS_REFUSED           = 0x03;

    private FrameType() {}

    public static String getName(byte code) {
        return switch (code) {
            case CONNECT -> "CONNECT";
            case DATA        -> "DATA";
            case CLOSE       -> "CLOSE";
            case CONNECT_ACK -> "CONNECT_ACK";
            case PING        -> "PING";
            case PONG        -> "PONG";
            case ERROR       -> "ERROR";
            default -> "UNKNOWN(0x" + String.format("%02X", code) + ")";
        };
    }

    public static boolean isValid(byte code) {
        return switch (code) {
            case CONNECT, DATA, CLOSE, CONNECT_ACK, PING, PONG, ERROR -> true;
            default -> false;
        };
    }
}
