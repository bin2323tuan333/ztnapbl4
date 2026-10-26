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

    private String streamId;
    private byte status;
    private String reason;

    public ConnectAckFrame() {
        super(FrameType.CONNECT_ACK);
    }

    public ConnectAckFrame(String streamId, byte status, String reason) {
        super(FrameType.CONNECT_ACK);
        if (streamId == null || streamId.isEmpty()) {
            throw new IllegalArgumentException("streamId không được null/rỗng");
        }
        this.streamId = streamId;
        this.status = status;
        this.reason = (reason == null ? "" : reason);
    }

    public static ConnectAckFrame ok(String streamId) {
        return new ConnectAckFrame(streamId, STATUS_OK, "");
    }

    public static ConnectAckFrame failed(String streamId, byte status, String reason) {
        return new ConnectAckFrame(streamId, status, reason);
    }

    @Override
    public byte[] encodePayload() {
        if (streamId == null || streamId.isEmpty()) {
            throw new IllegalStateException("streamId null/rỗng khi encode");
        }

        byte[] idBytes = streamId.getBytes(StandardCharsets.UTF_8);
        if (idBytes.length > 255) {
            throw new IllegalStateException("streamId quá dài: " + idBytes.length + " (max 255)");
        }
        byte[] reasonBytes = reason.getBytes(StandardCharsets.UTF_8);

        // [1B idLen][id][1B status][4B reasonLen][reason]
        ByteBuffer bb = ByteBuffer.allocate(1 + idBytes.length + 1 + 4 + reasonBytes.length);
        bb.put((byte) idBytes.length);
        bb.put(idBytes);
        bb.put(status);
        bb.putInt(reasonBytes.length);
        bb.put(reasonBytes);
        return bb.array();
    }

    @Override
    public void decodePayload(byte[] data) {
        // Min: 1(idLen) + 0(id) + 1(status) + 4(reasonLen) = 6
        if (data == null || data.length < 6) {
            throw new IllegalArgumentException("Payload quá ngắn: "
                    + (data == null ? "null" : data.length));
        }

        ByteBuffer bb = ByteBuffer.wrap(data);

        int idLen = bb.get() & 0xFF;
        if (idLen == 0 || idLen > bb.remaining() - 5) {   // -5 cho status(1) + reasonLen(4)
            throw new IllegalArgumentException("streamIdLen không hợp lệ: " + idLen);
        }
        byte[] idBytes = new byte[idLen];
        bb.get(idBytes);
        this.streamId = new String(idBytes, StandardCharsets.UTF_8);

        this.status = bb.get();

        int len = bb.getInt();
        if (len < 0 || len > bb.remaining()) {
            throw new IllegalArgumentException("Độ dài reason không hợp lệ: " + len);
        }
        byte[] reasonBytes = new byte[len];
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