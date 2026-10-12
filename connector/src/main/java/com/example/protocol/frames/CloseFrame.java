package com.example.protocol.frames;

import com.example.protocol.Frame;
import com.example.protocol.FrameType;

import java.nio.ByteBuffer;

public class CloseFrame extends Frame {
    public static final byte REASON_NORMAL            = 0x00;
    public static final byte REASON_CLIENT_CLOSED     = 0x01;
    public static final byte REASON_APP_CLOSED        = 0x02;
    public static final byte REASON_APP_ERROR         = 0x03;
    public static final byte REASON_IDLE_TIMEOUT      = 0x04;
    public static final byte REASON_SERVER_SHUTDOWN   = 0x05;
    public static final byte REASON_CONNECTOR_SHUTDOWN= 0x06;
    public static final byte REASON_PROTOCOL_ERROR    = 0x07;
    public static final byte REASON_RATE_LIMIT        = 0x08;


    private int streamId;
    private byte reason;

    public CloseFrame() {
        super(FrameType.CLOSE);
    }

    public CloseFrame(int streamId, byte reason) {
        super(FrameType.CLOSE);
        this.streamId = streamId;
        this.reason = reason;
    }

    public CloseFrame(int streamId) {
        this(streamId, REASON_NORMAL);
    }

    @Override
    public byte[] encodePayload() {
        // [streamId 4B][reason 1B]
        ByteBuffer bb = ByteBuffer.allocate(5);
        bb.putInt(streamId);
        bb.put(reason);
        return bb.array();
    }

    @Override
    public void decodePayload(byte[] data) {
        if (data.length < 5) {
            throw new IllegalArgumentException("Payload quá ngắn");
        }

        ByteBuffer bb = ByteBuffer.wrap(data);
        this.streamId = bb.getInt();
        this.reason = bb.get();
    }

    public int getStreamId() { return streamId; }
    public byte getReason() { return reason; }


    public String getReasonText() {
        return reasonText(reason);
    }

    public static String reasonText(byte code) {
        return switch (code) {
            case REASON_NORMAL             -> "Nomal";
            case REASON_CLIENT_CLOSED      -> "Client Close";
            case REASON_APP_CLOSED         -> "App Close";
            case REASON_APP_ERROR          -> "App Error";
            case REASON_IDLE_TIMEOUT       -> "Time out";
            case REASON_SERVER_SHUTDOWN    -> "Server Shutdown";
            case REASON_CONNECTOR_SHUTDOWN -> "Connector Shutdown";
            case REASON_PROTOCOL_ERROR     -> "Protocol Error";
            case REASON_RATE_LIMIT         -> "Rate Limit";
            default                        -> "Undefined (" + code + ")";
        };
    }
}