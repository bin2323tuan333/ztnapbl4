package com.example.protocol.frames;

import com.example.protocol.Frame;
import com.example.protocol.FrameType;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class RoutingReqFrame extends Frame {
    private String connectorId;

    public RoutingReqFrame() {
        super(FrameType.ROUTING_REQUEST);
    }

    public RoutingReqFrame(String connectorId) {
        super(FrameType.ROUTING_REQUEST);
        this.connectorId = connectorId;
    }

    public String getConnectorId() {
        return connectorId;
    }

    public void setConnectorId(String connectorId) {
        this.connectorId = connectorId;
    }


    @Override
    public byte[] encodePayload() {
        byte[] tmp = connectorId.getBytes(StandardCharsets.UTF_8);

        // [len] [connectorID]
        ByteBuffer bb = ByteBuffer.allocate(4 + tmp.length);
        bb.putInt(tmp.length);
        bb.put(tmp);
        return bb.array();
    }

    @Override
    public void decodePayload(byte[] data) {
        ByteBuffer bb = ByteBuffer.wrap(data);
        int len = bb.getInt();
        byte[] tmp = new byte[len];
        bb.get(tmp);
        this.connectorId = new String(tmp, StandardCharsets.UTF_8);
    }


}
