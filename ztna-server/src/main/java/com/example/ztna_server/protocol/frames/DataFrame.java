package com.example.ztna_server.protocol.frames;


import com.example.ztna_server.protocol.Frame;
import com.example.ztna_server.protocol.FrameType;
import lombok.Data;

import java.nio.ByteBuffer;

@Data
public class DataFrame extends Frame {
    private int streamId;
    private int dataLen;
    private byte[] payload;

    public DataFrame() {
        super(FrameType.DATA);
    }

    public DataFrame(int streamId, byte[] data) {
        super(FrameType.DATA);
        this.streamId = streamId;
        this.dataLen = data.length;
        this.payload = data;
    }

    @Override
    public void decodePayload(byte[] data) {
        ByteBuffer buffer = ByteBuffer.wrap(data);
        this.streamId = buffer.getInt();
        this.dataLen = buffer.getInt();
        this.payload = new byte[dataLen];
        buffer.get(this.payload);
    }

    @Override
    public byte[] encodePayload() {
        int size = 4 + 4 + payload.length;
        ByteBuffer buffer = ByteBuffer.allocate(size);
        buffer.putInt(streamId);
        buffer.putInt(payload.length);
        buffer.put(payload);
        return  buffer.array();
    }
}
