package org.example.protocol.frames;

import org.example.protocol.Frame;
import org.example.protocol.FrameType;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class ResourceResFrame extends Frame {
    public static final byte STATUS_OK                = 0x00;
    public static final byte STATUS_APP_NOT_FOUND     = 0x01;
    public static final byte STATUS_CONNECTOR_OFFLINE = 0x02;
    public static final byte STATUS_INTERNAL_ERROR    = 0x03;
    public static final byte STATUS_AUTH_FAILED       = 0x04;

    private String requestId;
    private String streamId;
    private byte status;
    private String reason;

    public ResourceResFrame() {
        super(FrameType.RESOURCE_RESPONSE);
    }

    // Sửa lại constructor để nhận requestId
    public ResourceResFrame(String requestId, String streamId, byte status, String reason) {
        super(FrameType.RESOURCE_RESPONSE);
        this.requestId = (requestId == null ? "" : requestId);
        this.streamId = (streamId == null ? "" : streamId);
        this.status = status;
        this.reason = (reason == null ? "" : reason);
    }

    public static ResourceResFrame ok(String requestId, String streamId) {
        return new ResourceResFrame(requestId, streamId, STATUS_OK, "");
    }

    public static ResourceResFrame failed(String requestId, byte status, String reason) {
        return new ResourceResFrame(requestId, "", status, reason);
    }

    @Override
    public byte[] encodePayload() {
        byte[] reqIdBytes  = requestId.getBytes(StandardCharsets.UTF_8);
        byte[] idBytes     = streamId.getBytes(StandardCharsets.UTF_8);
        byte[] reasonBytes = reason.getBytes(StandardCharsets.UTF_8);

        // Format mới: [4B reqIdLen][reqId][4B idLen][id][1B status][4B reasonLen][reason]
        int totalLen = 4 + reqIdBytes.length + 4 + idBytes.length + 1 + 4 + reasonBytes.length;
        ByteBuffer bb = ByteBuffer.allocate(totalLen);

        bb.putInt(reqIdBytes.length);
        bb.put(reqIdBytes);

        bb.putInt(idBytes.length);
        bb.put(idBytes);

        bb.put(status);

        bb.putInt(reasonBytes.length);
        bb.put(reasonBytes);

        return bb.array();
    }

    @Override
    public void decodePayload(byte[] payload) {
        // Tối thiểu phải có 4 reqIdLen + 4 idLen + 1 status + 4 reasonLen = 13 bytes
        if (payload.length < 13) {
            throw new IllegalArgumentException("Payload quá ngắn: " + payload.length);
        }

        ByteBuffer bb = ByteBuffer.wrap(payload);

        // 1. Đọc requestId
        int reqIdLen = bb.getInt();
        if (reqIdLen < 0 || reqIdLen > bb.remaining()) {
            throw new IllegalArgumentException("reqIdLen không hợp lệ: " + reqIdLen);
        }
        byte[] reqIdBytes = new byte[reqIdLen];
        bb.get(reqIdBytes);
        this.requestId = new String(reqIdBytes, StandardCharsets.UTF_8);

        // 2. Đọc streamId
        int idLen = bb.getInt();
        if (idLen < 0 || idLen > bb.remaining()) {
            throw new IllegalArgumentException("idLen không hợp lệ: " + idLen);
        }
        byte[] idBytes = new byte[idLen];
        bb.get(idBytes);
        this.streamId = new String(idBytes, StandardCharsets.UTF_8);

        // 3. Đọc status
        this.status = bb.get();

        // 4. Đọc reason
        int reasonLen = bb.getInt();
        if (reasonLen < 0 || reasonLen > bb.remaining()) {
            throw new IllegalArgumentException("reasonLen không hợp lệ: " + reasonLen);
        }
        byte[] reasonBytes = new byte[reasonLen];
        bb.get(reasonBytes);
        this.reason = new String(reasonBytes, StandardCharsets.UTF_8);
    }

    public String getRequestId() { return requestId; }
    public String getStreamId()  { return streamId; }
    public byte getStatus()      { return status; }
    public String getReason()    { return reason; }

    public boolean isOk() {
        return status == STATUS_OK;
    }
}