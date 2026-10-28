package com.example.ztna_server.protocol.frames;

import com.example.ztna_server.protocol.Frame;
import com.example.ztna_server.protocol.FrameType;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class HttpAccessFrame extends Frame {

    private String requestId;
    private String appId;
    private String jwt;

    public HttpAccessFrame() {
        super(FrameType.HTTP_ACCESS);
    }

    public HttpAccessFrame(String requestId, String appId, String jwt) {
        super(FrameType.HTTP_ACCESS);
        this.requestId = (requestId == null ? "" : requestId);
        this.appId = (appId == null ? "" : appId);
        this.jwt = (jwt == null ? "" : jwt);
    }

    @Override
    public byte[] encodePayload() {
        byte[] reqIdBuf = requestId.getBytes(StandardCharsets.UTF_8);
        byte[] appIdBuf = appId.getBytes(StandardCharsets.UTF_8);
        byte[] jwtBuf   = jwt.getBytes(StandardCharsets.UTF_8);

        // Format mới: [reqIdLen 4B][reqId N][appIdLen 4B][appId M][jwtLen 4B][jwt K]
        ByteBuffer bb = ByteBuffer.allocate(4 + reqIdBuf.length + 4 + appIdBuf.length + 4 + jwtBuf.length);

        bb.putInt(reqIdBuf.length);
        bb.put(reqIdBuf);

        bb.putInt(appIdBuf.length);
        bb.put(appIdBuf);

        bb.putInt(jwtBuf.length);
        bb.put(jwtBuf);

        return bb.array();
    }

    @Override
    public void decodePayload(byte[] payload) {
        if (payload.length < 12) {
            throw new IllegalArgumentException("Payload quá ngắn");
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

        // 3. Đọc jwt
        int jwtLen = bb.getInt();
        if (jwtLen < 0 || jwtLen > bb.remaining()) {
            throw new IllegalArgumentException("jwtLen không hợp lệ: " + jwtLen);
        }
        byte[] jwtBuf = new byte[jwtLen];
        bb.get(jwtBuf);
        this.jwt = new String(jwtBuf, StandardCharsets.UTF_8);
    }

    public String getRequestId() { return requestId; }
    public String getAppId()     { return appId; }
    public String getJwt()       { return jwt; }
}