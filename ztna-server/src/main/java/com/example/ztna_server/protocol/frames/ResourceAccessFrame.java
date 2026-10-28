package com.example.ztna_server.protocol.frames;

import com.example.ztna_server.protocol.Frame;
import com.example.ztna_server.protocol.FrameType;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class ResourceAccessFrame extends Frame {

    private String requestId;
    private String appId;

    public ResourceAccessFrame() {
        super(FrameType.RESOURCE_ACCESS);
    }

    public ResourceAccessFrame(String requestId, String appId) {
        super(FrameType.RESOURCE_ACCESS);
        this.requestId = (requestId == null ? "" : requestId);
        this.appId = (appId == null ? "" : appId);
    }

    @Override
    public byte[] encodePayload() {
        byte[] reqIdBuf = requestId.getBytes(StandardCharsets.UTF_8);
        byte[] appIdBuf = appId.getBytes(StandardCharsets.UTF_8);

        // Format: [reqIdLen 4B][reqId N][appIdLen 4B][appId M]
        ByteBuffer bb = ByteBuffer.allocate(4 + reqIdBuf.length + 4 + appIdBuf.length);

        bb.putInt(reqIdBuf.length);
        bb.put(reqIdBuf);

        bb.putInt(appIdBuf.length);
        bb.put(appIdBuf);

        return bb.array();
    }

    @Override
    public void decodePayload(byte[] payload) {
        if (payload.length < 8) {
            throw new IllegalArgumentException("Payload quá ngắn: " + payload.length);
        }

        ByteBuffer bb = ByteBuffer.wrap(payload);

        // 1. Đọc requestId
        int reqIdLen = bb.getInt();
        if (reqIdLen < 0 || reqIdLen > bb.remaining()) {
            throw new IllegalArgumentException("reqIdLen không hợp lệ: " + reqIdLen);
        }
        byte[] reqIdBuf = new byte[reqIdLen];
        bb.get(reqIdBuf);
        this.requestId = new String(reqIdBuf, StandardCharsets.UTF_8);

        // 2. Đọc appId
        int appIdLen = bb.getInt();
        if (appIdLen < 0 || appIdLen > bb.remaining()) {
            throw new IllegalArgumentException("appIdLen không hợp lệ: " + appIdLen);
        }
        byte[] appIdBuf = new byte[appIdLen];
        bb.get(appIdBuf);
        this.appId = new String(appIdBuf, StandardCharsets.UTF_8);
    }

    public String getRequestId() { return requestId; }
    public String getAppId()     { return appId; }
}