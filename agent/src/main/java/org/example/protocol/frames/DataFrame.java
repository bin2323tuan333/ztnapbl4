package org.example.protocol.frames;

import org.example.protocol.Frame;
import org.example.protocol.FrameType;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class DataFrame extends Frame {
    private String streamId;
    private byte[] payload;

    public DataFrame() {
        super(FrameType.DATA);
        this.payload = new byte[0];   // tránh null
    }

    public DataFrame(String streamId, byte[] data) {
        super(FrameType.DATA);
        if (streamId == null || streamId.isEmpty()) {
            throw new IllegalArgumentException("streamId không được null/rỗng");
        }
        this.streamId = streamId;
        this.payload = (data == null ? new byte[0] : data);
    }

    public String getStreamId() { return streamId; }
    public byte[] getData()    { return payload; }

    @Override
    public void decodePayload(byte[] data) {
        if (data == null || data.length < 5) {   // min: 1(idLen) + 0(id) + 4(dataLen)
            throw new IllegalArgumentException("Payload quá ngắn: "
                    + (data == null ? "null" : data.length));
        }

        ByteBuffer buffer = ByteBuffer.wrap(data);

        // --- streamId: [1B length][length bytes UTF-8] ---
        int streamIdLen = buffer.get() & 0xFF;
        if (streamIdLen == 0 || streamIdLen > buffer.remaining()) {
            throw new IllegalArgumentException("streamIdLen không hợp lệ: " + streamIdLen);
        }
        byte[] streamIdBytes = new byte[streamIdLen];
        buffer.get(streamIdBytes);
        this.streamId = new String(streamIdBytes, StandardCharsets.UTF_8);

        // --- data: [4B length][length bytes] ---
        int dataLen = buffer.getInt();
        if (dataLen < 0 || dataLen > buffer.remaining()) {
            throw new IllegalArgumentException(
                    "dataLen không hợp lệ: " + dataLen + " (remaining=" + buffer.remaining() + ")");
        }
        this.payload = new byte[dataLen];
        buffer.get(this.payload);
    }

    @Override
    public byte[] encodePayload() {
        if (streamId == null || streamId.isEmpty()) {
            throw new IllegalStateException("streamId null/rỗng khi encode");
        }

        byte[] streamIdBytes = streamId.getBytes(StandardCharsets.UTF_8);
        if (streamIdBytes.length > 255) {
            throw new IllegalStateException(
                    "streamId quá dài: " + streamIdBytes.length + " bytes (max 255)");
        }

        int size = 1 + streamIdBytes.length + 4 + payload.length;
        ByteBuffer buffer = ByteBuffer.allocate(size);
        buffer.put((byte) streamIdBytes.length);
        buffer.put(streamIdBytes);
        buffer.putInt(payload.length);
        buffer.put(payload);
        return buffer.array();
    }
}