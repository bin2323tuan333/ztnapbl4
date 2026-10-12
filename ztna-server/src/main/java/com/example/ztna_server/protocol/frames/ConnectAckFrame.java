package com.example.ztna_server.protocol.frames;



import com.example.ztna_server.protocol.Frame;
import com.example.ztna_server.protocol.FrameType;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;


public class ConnectAckFrame extends Frame {
    public static final byte STATUS_OK              = 0x00;
    public static final byte STATUS_APP_NOT_FOUND   = 0x01;
    public static final byte STATUS_CONNECT_FAILED  = 0x02;
    public static final byte STATUS_APP_REFUSED     = 0x03;
    public static final byte STATUS_TIMEOUT         = 0x04;


    private int streamId;
    private byte status;
    private String reason;

    public ConnectAckFrame() {
        super(FrameType.CONNECT_ACK);
    }

    public ConnectAckFrame(int streamId, byte status, String reason) {
        super(FrameType.CONNECT_ACK);
        this.streamId = streamId;
        this.status = status;
        this.reason = (reason == null ? "" : reason);
    }

    // Factory method tiện dụng
    public static ConnectAckFrame ok(int streamId) {
        return new ConnectAckFrame(streamId, STATUS_OK, "");
    }

    public static ConnectAckFrame failed(int streamId, byte status, String reason) {
        return new ConnectAckFrame(streamId, status, reason);
    }

    @Override
    public byte[] encodePayload() {
        byte[] reasonBytes = reason.getBytes(StandardCharsets.UTF_8);

        // [streamId 4B][status 1B][reasonLen 4B][reason N byte]
        ByteBuffer bb = ByteBuffer.allocate(4 + 1 + 4 + reasonBytes.length);
        bb.putInt(streamId);
        bb.put(status);
        bb.putInt(reasonBytes.length);
        bb.put(reasonBytes);
        return bb.array();
    }

    @Override
    public void decodePayload(byte[] data) {
        if (data.length < 9) {
            throw new IllegalArgumentException("Payload quá ngắn");
        }

        ByteBuffer bb = ByteBuffer.wrap(data);
        this.streamId = bb.getInt();
        this.status = bb.get();

        int len = bb.getInt();
        if (len < 0 || len > bb.remaining()) {
            throw new IllegalArgumentException("Độ dài reason không hợp lệ: " + len);
        }

        byte[] reasonBytes = new byte[len];
        bb.get(reasonBytes);
        this.reason = new String(reasonBytes, StandardCharsets.UTF_8);
    }

    public int getStreamId() { return streamId; }
    public byte getStatus() { return status; }
    public String getReason() { return reason; }

    public boolean isOk() {
        return status == STATUS_OK;
    }
}
