package com.example.ztna_connector.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import org.example.ztna_connector.protocol.FrameType;

import java.nio.charset.StandardCharsets;

public final class FrameCodec {

    private FrameCodec() {}

    public static ByteBuf encodeConnect(ByteBufAllocator alloc,
                                        long streamId,
                                        String service,
                                        String jwt) {
        byte[] svc = service.getBytes(StandardCharsets.UTF_8);
        byte[] j = jwt.getBytes(StandardCharsets.UTF_8);
        ByteBuf buf = alloc.buffer(1 + 4 + 2 + svc.length + 2 + j.length);
        buf.writeByte(FrameType.CONNECT);
        buf.writeInt((int) streamId);
        buf.writeShort(svc.length);
        buf.writeBytes(svc);
        buf.writeShort(j.length);
        buf.writeBytes(j);
        return buf;
    }

    public static ConnectHeader decodeConnect(ByteBuf buf) {
        buf.readByte();
        long streamId = buf.readUnsignedInt();
        int svcLen = buf.readUnsignedShort();
        byte[] svc = new byte[svcLen];
        buf.readBytes(svc);
        int jwtLen = buf.readUnsignedShort();
        byte[] jwt = new byte[jwtLen];
        buf.readBytes(jwt);
        return new ConnectHeader(streamId,
                new String(svc, StandardCharsets.UTF_8),
                new String(jwt, StandardCharsets.UTF_8));
    }

    public static ByteBuf encodeData(ByteBufAllocator alloc, long streamId, ByteBuf payload) {
        ByteBuf buf = alloc.buffer(1 + 4 + payload.readableBytes());
        buf.writeByte(FrameType.DATA);
        buf.writeInt((int) streamId);
        buf.writeBytes(payload);
        return buf;
    }

    public static ByteBuf encodeClose(ByteBufAllocator alloc, long streamId) {
        ByteBuf buf = alloc.buffer(1 + 4);
        buf.writeByte(FrameType.CLOSE);
        buf.writeInt((int) streamId);
        return buf;
    }

    public static byte peekType(ByteBuf buf) {
        return buf.getByte(buf.readerIndex());
    }

    public record ConnectHeader(long streamId, String service, String jwt) {}
}