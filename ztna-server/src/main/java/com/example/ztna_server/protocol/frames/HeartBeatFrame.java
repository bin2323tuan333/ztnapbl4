package com.example.ztna_server.protocol.frames;



import com.example.ztna_server.protocol.Frame;
import com.example.ztna_server.protocol.FrameType;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class HeartBeatFrame extends Frame {
    private long timestamp;
    private String connectorId;

    // Constructor rỗng cho FrameRegistry
    public HeartBeatFrame() {
        super(FrameType.HEARTBEAT);
        this.timestamp = System.currentTimeMillis();
    }

    public HeartBeatFrame(String connectorId) {
        super(FrameType.HEARTBEAT);
        this.timestamp = System.currentTimeMillis();
        this.connectorId = connectorId;
    }

    public HeartBeatFrame(String connectorId, long timestamp) {
        super(FrameType.HEARTBEAT);
        this.timestamp = timestamp;
        this.connectorId = connectorId;
    }

    @Override
    public byte[] encodePayload() {
        byte[] idBytes = (connectorId == null ? "" : connectorId)
                .getBytes(StandardCharsets.UTF_8);

        // [timestamp 8B][connectorIdLen 4B][connectorId N byte]
        ByteBuffer bb = ByteBuffer.allocate(8 + 4 + idBytes.length);
        bb.putLong(timestamp);
        bb.putInt(idBytes.length);
        bb.put(idBytes);
        return bb.array();
    }

    @Override
    public void decodePayload(byte[] data) {
        if (data.length < 12) {   // 8 + 4
            throw new IllegalArgumentException("Payload quá ngắn");
        }

        ByteBuffer bb = ByteBuffer.wrap(data);
        this.timestamp = bb.getLong();

        int len = bb.getInt();
        if (len < 0 || len > bb.remaining()) {
            throw new IllegalArgumentException("Độ dài connectorId không hợp lệ: " + len);
        }

        byte[] idBytes = new byte[len];
        bb.get(idBytes);
        this.connectorId = new String(idBytes, StandardCharsets.UTF_8);
    }

    public long getTimestamp() { return timestamp; }
    public String getConnectorId() { return connectorId; }
}
