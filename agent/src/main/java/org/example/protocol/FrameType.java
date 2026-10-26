package org.example.protocol;

public class FrameType {
    public static final byte CONNECT = 0x01;
    public static final byte DATA = 0x02;
    public static final byte CLOSE = 0x03;
    public static final byte CONNECT_ACK = 0x04;
    public static final byte HEARTBEAT = 0x05;
    public static final byte HEARTBEAT_ACK = 0x06;
    public static final byte ROUTING_REQUEST = 0x07;
    public static final byte ROUTING_RESPONSE = 0x08;
    public static final byte OPEN_REQUEST = 0x09;
    public static final byte OPEN_RESPONSE = 0x0A;


    private FrameType() {}

    public static String getName(byte code) {
        return switch (code) {
            case ROUTING_REQUEST  -> "ROUTING_REQUEST";
            case ROUTING_RESPONSE -> "ROUTING_RESPONSE";
            case HEARTBEAT        -> "HEARTBEAT";
            case HEARTBEAT_ACK    -> "HEARTBEAT_ACK";
            case CONNECT          -> "CONNECT";
            case CONNECT_ACK      -> "CONNECT_ACK";
            case DATA             -> "DATA";
            case CLOSE            -> "CLOSE";
            case OPEN_REQUEST     -> "OPEN_REQUEST";
            case OPEN_RESPONSE    -> "OPEN_RESPONSE";
            default -> "UNKNOWN (0x" + String.format("%02X", code) + ")";
        };
    }

    public static boolean isValid(byte code) {
        return switch (code) {
            case ROUTING_REQUEST, ROUTING_RESPONSE,
                 HEARTBEAT, HEARTBEAT_ACK,
                 CONNECT, CONNECT_ACK,
                 DATA, CLOSE,
                 OPEN_REQUEST, OPEN_RESPONSE -> true;
            default -> false;
        };
    }
}

