package org.example.protocol.frames;

import org.example.protocol.Frame;
import org.example.protocol.FrameType;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class ConnectFrame extends Frame {
    private String streamId;
    private String appId;      // = resourceId

    public ConnectFrame() {
        super(FrameType.CONNECT);
    }

    public ConnectFrame(String streamId, String appId) {
        super(FrameType.CONNECT);
        if (streamId == null || streamId.isEmpty()) {
            throw new IllegalArgumentException("streamId không được null/rỗng");
        }
        if (appId == null || appId.isEmpty()) {
            throw new IllegalArgumentException("appId không được null/rỗng");
        }
        this.streamId = streamId;
        this.appId = appId;
    }

    @Override
    public byte[] encodePayload() {
        if (streamId == null || streamId.isEmpty()) {
            throw new IllegalStateException("streamId null/rỗng khi encode");
        }
        if (appId == null || appId.isEmpty()) {
            throw new IllegalStateException("appId null/rỗng khi encode");
        }

        byte[] idBytes  = streamId.getBytes(StandardCharsets.UTF_8);
        byte[] appBytes = appId.getBytes(StandardCharsets.UTF_8);

        if (idBytes.length > 255) {
            throw new IllegalStateException("streamId quá dài: " + idBytes.length + " (max 255)");
        }
        if (appBytes.length > 255) {
            throw new IllegalStateException("appId quá dài: " + appBytes.length + " (max 255)");
        }

        // [1B idLen][id][1B appLen][app]
        ByteBuffer bb = ByteBuffer.allocate(1 + idBytes.length + 1 + appBytes.length);
        bb.put((byte) idBytes.length);
        bb.put(idBytes);
        bb.put((byte) appBytes.length);
        bb.put(appBytes);
        return bb.array();
    }

    @Override
    public void decodePayload(byte[] data) {
        if (data == null || data.length < 2) {
            throw new IllegalArgumentException("Payload quá ngắn: "
                    + (data == null ? "null" : data.length));
        }

        ByteBuffer bb = ByteBuffer.wrap(data);

        // streamId
        int idLen = bb.get() & 0xFF;
        if (idLen == 0 || idLen > bb.remaining()) {
            throw new IllegalArgumentException("streamIdLen không hợp lệ: " + idLen);
        }
        byte[] idBytes = new byte[idLen];
        bb.get(idBytes);
        this.streamId = new String(idBytes, StandardCharsets.UTF_8);

        // appId
        if (bb.remaining() < 1) {
            throw new IllegalArgumentException("Thiếu appIdLen");
        }
        int appLen = bb.get() & 0xFF;
        if (appLen == 0 || appLen > bb.remaining()) {
            throw new IllegalArgumentException("appIdLen không hợp lệ: " + appLen);
        }
        byte[] appBytes = new byte[appLen];
        bb.get(appBytes);
        this.appId = new String(appBytes, StandardCharsets.UTF_8);
    }

    public String getStreamId() { return streamId; }
    public String getAppId()    { return appId; }

    public void setStreamId(String streamId) { this.streamId = streamId; }
    public void setAppId(String appId)       { this.appId = appId; }
}