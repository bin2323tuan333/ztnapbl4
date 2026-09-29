package com.example.ztna_server.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

import java.nio.charset.StandardCharsets;

public final class Frame {

    private Frame() {}

    public static byte peekType(ByteBuf buf) {
        return buf.getByte(buf.readerIndex());
    }

    // ═══════════════════════════════════════════════════════════════
    // CONNECT — Không có streamId
    // Layout: [type:1][svcLen:2][service:N][jwtLen:2][jwt:M]
    // ═══════════════════════════════════════════════════════════════
    public static ByteBuf encodeConnect(ByteBufAllocator alloc,
                                        String service, String jwt) {
        byte[] svc = service.getBytes(StandardCharsets.UTF_8);
        byte[] j = jwt.getBytes(StandardCharsets.UTF_8);

        ByteBuf buf = alloc.buffer(1 + 2 + svc.length + 2 + j.length);
        buf.writeByte(FrameType.CONNECT);
        buf.writeShort(svc.length);
        buf.writeBytes(svc);
        buf.writeShort(j.length);
        buf.writeBytes(j);
        return buf;
    }

    public static ConnectFrame decodeConnect(ByteBuf buf) {
        buf.readByte();
        int svcLen = buf.readUnsignedShort();
        byte[] svc = new byte[svcLen];
        buf.readBytes(svc);
        int jwtLen = buf.readUnsignedShort();
        byte[] jwt = new byte[jwtLen];
        buf.readBytes(jwt);

        return new ConnectFrame(
                new String(svc, StandardCharsets.UTF_8),
                new String(jwt, StandardCharsets.UTF_8)
        );
    }

    // ═══════════════════════════════════════════════════════════════
    // CONNECT_ACK — Có streamId (server cấp)
    // Layout: [type:1][streamId:4][status:1][reasonLen:2][reason:K]
    // ═══════════════════════════════════════════════════════════════
    public static ByteBuf encodeConnectAck(ByteBufAllocator alloc, int streamId, byte status, String reason) {
        byte[] r = reason == null ? new byte[0] : reason.getBytes(StandardCharsets.UTF_8);

        ByteBuf buf = alloc.buffer(1 + 4 + 1 + 2 + r.length);
        buf.writeByte(FrameType.CONNECT_ACK);
        buf.writeInt(streamId);
        buf.writeByte(status);
        buf.writeShort(r.length);
        buf.writeBytes(r);
        return buf;
    }

    public static ConnectAckFrame decodeConnectAck(ByteBuf buf) {
        buf.readByte();
        int streamId = buf.readInt();
        byte status = buf.readByte();
        int reasonLen = buf.readUnsignedShort();
        String reason = null;
        if (reasonLen > 0) {
            byte[] r = new byte[reasonLen];
            buf.readBytes(r);
            reason = new String(r, StandardCharsets.UTF_8);
        }
        return new ConnectAckFrame(streamId, status, reason);
    }

    // ═══════════════════════════════════════════════════════════════
    // DATA
    // ═══════════════════════════════════════════════════════════════
    public static ByteBuf encodeData(ByteBufAllocator alloc, int streamId, byte[] data) {
        ByteBuf buf = alloc.buffer(1 + 4 + 4 + data.length);
        buf.writeByte(FrameType.DATA);
        buf.writeInt(streamId);
        buf.writeInt(data.length);
        buf.writeBytes(data);
        return buf;
    }

    public static DataFrame decodeData(ByteBuf buf) {
        buf.readByte();
        int streamId = buf.readInt();
        int dataLen = buf.readInt();
        byte[] data = new byte[dataLen];
        buf.readBytes(data);
        return new DataFrame(streamId,dataLen, data);
    }

    // ═══════════════════════════════════════════════════════════════
    // CLOSE
    // ═══════════════════════════════════════════════════════════════
    public static ByteBuf encodeClose(ByteBufAllocator alloc, int streamId) {
        return encodeClose(alloc, streamId, (byte) 0);
    }

    public static ByteBuf encodeClose(ByteBufAllocator alloc, int streamId, byte reason) {
        ByteBuf buf = alloc.buffer(1 + 4 + 1);
        buf.writeByte(FrameType.CLOSE);
        buf.writeInt(streamId);
        buf.writeByte(reason);
        return buf;
    }

    public static CloseFrame decodeClose(ByteBuf buf) {
        buf.readByte();
        int streamId = buf.readInt();
        byte reason = buf.readByte();
        return new CloseFrame(streamId, reason);
    }
}