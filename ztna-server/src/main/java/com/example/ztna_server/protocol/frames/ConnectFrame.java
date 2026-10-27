package com.example.ztna_server.protocol.frames;



import com.example.ztna_server.protocol.Frame;
import com.example.ztna_server.protocol.FrameType;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;


public class ConnectFrame extends Frame {
    private int streamId;
    private String appId;

    public ConnectFrame() {
        super(FrameType.CONNECT);
    }

    public ConnectFrame(int streamId, String appId) {
        super(FrameType.CONNECT);
        this.streamId = streamId;
        this.appId = appId;
    }

    @Override
    public byte[] encodePayload() {
        byte[] appIdBytes = appId.getBytes(StandardCharsets.UTF_8);
        ByteBuffer bb = ByteBuffer.allocate(4 + 4 + appIdBytes.length);
        bb.putInt(streamId);
        bb.putInt(appIdBytes.length);
        bb.put(appIdBytes);
        return bb.array();
    }

    @Override
    public void decodePayload(byte[] data) {
        ByteBuffer bb = ByteBuffer.wrap(data);
        this.streamId = bb.getInt();
        int len = bb.getInt();
        byte[] appIdBytes = new byte[len];
        bb.get(appIdBytes);
        this.appId = new String(appIdBytes, StandardCharsets.UTF_8);
    }

    public int getStreamId() {
        return streamId;
    }

    public void setStreamId(int streamId) {
        this.streamId = streamId;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }
}
