package com.example.ztna_server.protocol.frames;

import com.example.ztna_server.protocol.Frame;
import com.example.ztna_server.protocol.FrameType;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class OpenResFrame extends Frame {
    public static final byte STATUS_OK                = 0x00;
    public static final byte STATUS_APP_NOT_FOUND     = 0x01;
    public static final byte STATUS_CONNECTOR_OFFLINE = 0x02;
    public static final byte STATUS_INTERNAL_ERROR    = 0x03;
    public static final byte STATUS_AUTH_FAILED       = 0x04;

    private String streamId;
    private byte status;
    private String reason;

    public OpenResFrame() {
        super(FrameType.OPEN_RESPONSE);
    }

    public OpenResFrame(String streamId, byte status, String reason) {
        super(FrameType.OPEN_RESPONSE);
        this.streamId = streamId;
        this.status = status;
        this.reason = (reason == null ? "" : reason);
    }

    public static OpenResFrame ok(String streamId) {
        return new OpenResFrame(streamId, STATUS_OK, "");
    }

    /** Fail vẫn phải mang streamId để Agent biết đóng stream nào */
    public static OpenResFrame failed(String streamId, byte status, String reason) {
        return new OpenResFrame(streamId, status, reason);
    }

    @Override
    public byte[] encodePayload() {
        byte[] idBytes = streamId.getBytes(StandardCharsets.UTF_8);
        if (idBytes.length > 255) {
            throw new IllegalStateException(
                    "streamId quá dài: " + idBytes.length + " bytes (max 255)");
        }
        byte[] reasonBytes = reason.getBytes(StandardCharsets.UTF_8);

        // [1B idLen][idLen][1B status][4B reasonLen][reasonLen]
        ByteBuffer bb = ByteBuffer.allocate(1 + idBytes.length + 1 + 4 + reasonBytes.length);
        bb.put((byte) idBytes.length);
        bb.put(idBytes);
        bb.put(status);
        bb.putInt(reasonBytes.length);
        bb.put(reasonBytes);
        return bb.array();
    }

    @Override
    public void decodePayload(byte[] payload) {
        // Tối thiểu: 1B idLen + 0B id + 1B status + 4B reasonLen = 6 bytes
        if (payload.length < 6) {
            throw new IllegalArgumentException("Payload quá ngắn: " + payload.length);
        }

        ByteBuffer bb = ByteBuffer.wrap(payload);

        int idLen = bb.get() & 0xFF;
        if (idLen > bb.remaining()) {
            throw new IllegalArgumentException("idLen không hợp lệ: " + idLen);
        }
        byte[] idBytes = new byte[idLen];
        bb.get(idBytes);
        this.streamId = new String(idBytes, StandardCharsets.UTF_8);

        this.status = bb.get();

        int reasonLen = bb.getInt();
        if (reasonLen < 0 || reasonLen > bb.remaining()) {
            throw new IllegalArgumentException("reasonLen không hợp lệ: " + reasonLen);
        }
        byte[] reasonBytes = new byte[reasonLen];
        bb.get(reasonBytes);
        this.reason = new String(reasonBytes, StandardCharsets.UTF_8);
    }

    public String getStreamId() { return streamId; }
    public byte getStatus()     { return status; }
    public String getReason()   { return reason; }

    public boolean isOk() {
        return status == STATUS_OK;
    }
}