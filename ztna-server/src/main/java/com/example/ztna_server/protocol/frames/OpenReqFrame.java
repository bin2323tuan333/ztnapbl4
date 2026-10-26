package com.example.ztna_server.protocol.frames;

import com.example.ztna_server.protocol.Frame;
import com.example.ztna_server.protocol.FrameType;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class OpenReqFrame extends Frame {

    private String appId;
    private String jwt;

    public OpenReqFrame() {
        super(FrameType.OPEN_REQUEST);
    }

    public OpenReqFrame(String appId, String jwt) {
        super(FrameType.OPEN_REQUEST);
        this.appId = appId;
        this.jwt = (jwt == null ? "" : jwt);
    }

    @Override
    public byte[] encodePayload() {
        byte[] appIdBuf = (appId == null ? "" : appId).getBytes(StandardCharsets.UTF_8);
        byte[] jwtBuf   = (jwt == null ? "" : jwt).getBytes(StandardCharsets.UTF_8);

        // [appIdLen 4B][appId N][jwtLen 4B][jwt M]
        ByteBuffer bb = ByteBuffer.allocate(4 + appIdBuf.length + 4 + jwtBuf.length);
        bb.putInt(appIdBuf.length);
        bb.put(appIdBuf);
        bb.putInt(jwtBuf.length);
        bb.put(jwtBuf);
        return bb.array();
    }

    @Override
    public void decodePayload(byte[] payload) {
        if (payload.length < 8) {
            throw new IllegalArgumentException("Payload quá ngắn");
        }

        ByteBuffer bb = ByteBuffer.wrap(payload);

        // appId
        int appIdLen = bb.getInt();
        if (appIdLen < 0 || appIdLen > bb.remaining()) {
            throw new IllegalArgumentException("appIdLen không hợp lệ: " + appIdLen);
        }
        byte[] appIdBuf = new byte[appIdLen];
        bb.get(appIdBuf);
        this.appId = new String(appIdBuf, StandardCharsets.UTF_8);

        // jwt
        if (bb.remaining() < 4) {
            throw new IllegalArgumentException("Thiếu jwtLen");
        }
        int jwtLen = bb.getInt();
        if (jwtLen < 0 || jwtLen > bb.remaining()) {
            throw new IllegalArgumentException("jwtLen không hợp lệ: " + jwtLen);
        }
        byte[] jwtBuf = new byte[jwtLen];
        bb.get(jwtBuf);
        this.jwt = new String(jwtBuf, StandardCharsets.UTF_8);
    }

    public String getAppId() { return appId; }
    public String getJwt()   { return jwt; }
}