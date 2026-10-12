package com.example.ztna_server.protocol.frames;

import com.example.ztna_server.protocol.Frame;
import com.example.ztna_server.protocol.FrameType;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class OpenResFrame extends Frame {

    // Status codes
    public static final byte STATUS_OK                = 0x00;
    public static final byte STATUS_APP_NOT_FOUND     = 0x01;
    public static final byte STATUS_CONNECTOR_OFFLINE = 0x02;
    public static final byte STATUS_INTERNAL_ERROR    = 0x03;
    public static final byte STATUS_AUTH_FAILED       = 0x04;

    private int streamId;
    private byte status;
    private String reason;

    // Constructor rỗng cho FrameRegistry
    public OpenResFrame() {
        super(FrameType.OPEN_RESPONSE);
    }

    public OpenResFrame(int streamId, byte status, String reason) {
        super(FrameType.OPEN_RESPONSE);
        this.streamId = streamId;
        this.status = status;
        this.reason = (reason == null ? "" : reason);
    }

    // Factory tiện dụng
    public static OpenResFrame ok(int streamId) {
        return new OpenResFrame(streamId, STATUS_OK, "");
    }

    public static OpenResFrame failed(byte status, String reason) {
        return new OpenResFrame(0, status, reason);
    }

    @Override
    public byte[] encodePayload() {
        byte[] reasonBuf = reason.getBytes(StandardCharsets.UTF_8);

        // [streamId 4B][status 1B][reasonLen 4B][reason N]
        ByteBuffer bb = ByteBuffer.allocate(4 + 1 + 4 + reasonBuf.length);
        bb.putInt(streamId);
        bb.put(status);
        bb.putInt(reasonBuf.length);
        bb.put(reasonBuf);
        return bb.array();
    }

    @Override
    public void decodePayload(byte[] payload) {
        if (payload.length < 9) {
            throw new IllegalArgumentException("Payload quá ngắn");
        }

        ByteBuffer bb = ByteBuffer.wrap(payload);
        this.streamId = bb.getInt();
        this.status = bb.get();

        int reasonLen = bb.getInt();
        if (reasonLen < 0 || reasonLen > bb.remaining()) {
            throw new IllegalArgumentException("reasonLen không hợp lệ: " + reasonLen);
        }
        byte[] reasonBuf = new byte[reasonLen];
        bb.get(reasonBuf);
        this.reason = new String(reasonBuf, StandardCharsets.UTF_8);
    }

    public int getStreamId() { return streamId; }
    public byte getStatus()  { return status; }
    public String getReason(){ return reason; }

    public boolean isOk() {
        return status == STATUS_OK;
    }
}