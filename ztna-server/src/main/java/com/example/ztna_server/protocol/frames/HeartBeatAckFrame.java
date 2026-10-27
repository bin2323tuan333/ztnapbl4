package com.example.ztna_server.protocol.frames;



import com.example.ztna_server.protocol.Frame;
import com.example.ztna_server.protocol.FrameType;

import java.nio.ByteBuffer;

public class HeartBeatAckFrame extends Frame {
    private long clientTimestamp;
    private long serverTimestamp;

    public HeartBeatAckFrame() {
        super(FrameType.HEARTBEAT_ACK);
        this.serverTimestamp = System.currentTimeMillis();
    }

    public HeartBeatAckFrame(long clientTimestamp) {
        super(FrameType.HEARTBEAT_ACK);
        this.clientTimestamp = clientTimestamp;
        this.serverTimestamp = System.currentTimeMillis();
    }

    public HeartBeatAckFrame(long clientTimestamp, long serverTimestamp) {
        super(FrameType.HEARTBEAT_ACK);
        this.clientTimestamp = clientTimestamp;
        this.serverTimestamp = serverTimestamp;
    }

    @Override
    public byte[] encodePayload() {
        // [clientTimestamp 8B][serverTimestamp 8B]
        ByteBuffer bb = ByteBuffer.allocate(16);
        bb.putLong(clientTimestamp);
        bb.putLong(serverTimestamp);
        return bb.array();
    }

    @Override
    public void decodePayload(byte[] data) {
        if (data.length < 16) {
            throw new IllegalArgumentException("Payload quá ngắn");
        }

        ByteBuffer bb = ByteBuffer.wrap(data);
        this.clientTimestamp = bb.getLong();
        this.serverTimestamp = bb.getLong();
    }

    public long getClientTimestamp() { return clientTimestamp; }
    public long getServerTimestamp() { return serverTimestamp; }


    public long getRtt() {
        return System.currentTimeMillis() - clientTimestamp;
    }
}
