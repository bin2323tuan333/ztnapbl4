package com.example.ztna_server.protocol.frames;



import com.example.ztna_server.protocol.Frame;
import com.example.ztna_server.protocol.FrameType;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class CloseFrame extends Frame {
    public static final byte REASON_NORMAL              = 0x00;
    public static final byte REASON_CLIENT_CLOSED       = 0x01;
    public static final byte REASON_APP_CLOSED          = 0x02;
    public static final byte REASON_APP_ERROR           = 0x03;
    public static final byte REASON_IDLE_TIMEOUT        = 0x04;
    public static final byte REASON_SERVER_SHUTDOWN     = 0x05;
    public static final byte REASON_CONNECTOR_SHUTDOWN  = 0x06;
    public static final byte REASON_PROTOCOL_ERROR      = 0x07;
    public static final byte REASON_RATE_LIMIT          = 0x08;
    public static final byte REASON_SERVER_CLOSE        = 0x09;

    private String streamId;
    private byte reason;

    public CloseFrame() {
        super(FrameType.CLOSE);
    }

    public CloseFrame(String streamId, byte reason) {
        super(FrameType.CLOSE);
        if (streamId == null || streamId.isEmpty()) {
            throw new IllegalArgumentException("streamId không được null/rỗng");
        }
        this.streamId = streamId;
        this.reason = reason;
    }

    public CloseFrame(String streamId) {
        this(streamId, REASON_NORMAL);
    }

    @Override
    protected byte[] encodePayload() {
        if (streamId == null || streamId.isEmpty()) {
            throw new IllegalStateException("streamId null/rỗng khi encode");
        }

        byte[] idBytes = streamId.getBytes(StandardCharsets.UTF_8);
        if (idBytes.length > 255) {
            throw new IllegalStateException(
                    "streamId quá dài: " + idBytes.length + " bytes (max 255)");
        }

        // [1B idLen][idLen bytes UTF-8][1B reason]
        ByteBuffer bb = ByteBuffer.allocate(1 + idBytes.length + 1);
        bb.put((byte) idBytes.length);
        bb.put(idBytes);
        bb.put(reason);
        return bb.array();
    }

    @Override
    protected void decodePayload(byte[] data) {
        // Tối thiểu: 1(idLen) + 0(id) + 1(reason) = 2 bytes
        if (data == null || data.length < 2) {
            throw new IllegalArgumentException("Payload quá ngắn: "
                    + (data == null ? "null" : data.length));
        }

        ByteBuffer bb = ByteBuffer.wrap(data);

        int idLen = bb.get() & 0xFF;
        if (idLen == 0 || idLen > bb.remaining() - 1) {   // -1 cho reason byte
            throw new IllegalArgumentException("streamIdLen không hợp lệ: " + idLen);
        }
        byte[] idBytes = new byte[idLen];
        bb.get(idBytes);
        this.streamId = new String(idBytes, StandardCharsets.UTF_8);

        this.reason = bb.get();
    }

    public String getStreamId() { return streamId; }
    public byte getReason()     { return reason; }

    public void setReason(byte reason) {
        this.reason = reason;
    }

    public void setStreamId(String streamId) {
        this.streamId = streamId;
    }

    public String getReasonText() {
        return reasonText(reason);
    }

    public static String reasonText(byte code) {
        return switch (code) {
            case REASON_NORMAL              -> "Normal";
            case REASON_CLIENT_CLOSED       -> "Client Close";
            case REASON_APP_CLOSED          -> "App Close";
            case REASON_APP_ERROR           -> "App Error";
            case REASON_IDLE_TIMEOUT        -> "Idle Timeout";
            case REASON_SERVER_SHUTDOWN     -> "Server Shutdown";
            case REASON_CONNECTOR_SHUTDOWN  -> "Connector Shutdown";
            case REASON_PROTOCOL_ERROR      -> "Protocol Error";
            case REASON_RATE_LIMIT          -> "Rate Limit";
            case REASON_SERVER_CLOSE        -> "Server Close";
            default                         -> "Undefined (" + code + ")";
        };
    }
}