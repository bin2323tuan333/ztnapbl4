package org.example.protocol;

public class FrameType {
    public static final byte CONNECT = 0x01;
    public static final byte DATA = 0x02;
    public static final byte CLOSE = 0x03;
    public static final byte CONNECT_ACK = 0x04;

    // Sửa lại tên cho đúng chính tả
    public static final byte RESOURCE_ACCESS = 0x05;
    public static final byte RESOURCE_RESPONSE = 0x06;

    public static final byte ROUTING_REQUEST = 0x07;
    public static final byte ROUTING_RESPONSE = 0x08;

    public static final byte HTTP_ACCESS = 0x09;
    public static final byte HTTP_RESPONSE = 0x0A;

    private FrameType() {}

    public static String getName(byte code) {
        return switch (code) {
            case ROUTING_REQUEST   -> "ROUTING_REQUEST";
            case ROUTING_RESPONSE  -> "ROUTING_RESPONSE";
            case CONNECT           -> "CONNECT";
            case CONNECT_ACK       -> "CONNECT_ACK";
            case DATA              -> "DATA";
            case CLOSE             -> "CLOSE";
            case RESOURCE_ACCESS   -> "RESOURCE_ACCESS";
            case RESOURCE_RESPONSE -> "RESOURCE_RESPONSE";
            case HTTP_ACCESS       -> "HTTP_ACCESS";
            case HTTP_RESPONSE     -> "HTTP_RESPONSE";
            default -> "UNKNOWN (0x" + String.format("%02X", code) + ")";
        };
    }

    public static boolean isValid(byte code) {
        return switch (code) {
            case ROUTING_REQUEST, ROUTING_RESPONSE,
                 CONNECT, CONNECT_ACK,
                 DATA, CLOSE,
                 RESOURCE_ACCESS, RESOURCE_RESPONSE,
                 HTTP_ACCESS, HTTP_RESPONSE -> true;
            default -> false;
        };
    }
}