package org.example.ztna_connector.protocol;

public final class FrameType {
    public static final byte CONNECT = 0x01;
    public static final byte DATA    = 0x02;
    public static final byte CLOSE   = 0x03;

    private FrameType() {}
}