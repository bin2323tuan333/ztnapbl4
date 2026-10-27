package com.example.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.nio.charset.StandardCharsets;


public  abstract class Frame {
    protected byte header;

    public Frame(byte header) {
        this.header = header;
    }

    public byte getHeader() {
        return header;
    }

    public void decode(byte[] data) {
        this.header = data[0];
        byte[] tmp = new byte[data.length - 1];
        for (int i = 1; i < data.length; i++)
            tmp[i-1] = data[i];
        this.decodePayload(tmp);
    }

    public byte[] encode() {
        byte[] payload = this.encodePayload();
        byte[] tmp = new byte[payload.length + 1];
        tmp[0] = this.header;
        for (int i = 0; i < payload.length; i++)
            tmp[i + 1] = payload[i];
        return tmp;
    }

    public abstract void decodePayload(byte[] data);
    public abstract byte[] encodePayload();



//    public static byte peekType(ByteBuf buf) {
//        return buf.getByte(buf.readerIndex());
//    }
//
//    // CONNECT: [type:1][streamId:4][svcLen:2][service:N][jwtLen:2][jwt:M]
//    public static ByteBuf encodeConnect(ByteBufAllocator alloc, int streamId, String service, String jwt) {
//        byte[] svc = service.getBytes(StandardCharsets.UTF_8);
//        byte[] j = jwt == null ? new byte[0] : jwt.getBytes(StandardCharsets.UTF_8);
//
//        ByteBuf buf = alloc.buffer(1 + 4 + 2 + svc.length + 2 + j.length);
//        buf.writeByte(FrameType.CONNECT);
//        buf.writeInt(streamId);
//        buf.writeShort(svc.length);
//        buf.writeBytes(svc);
//        buf.writeShort(j.length);
//        buf.writeBytes(j);
//        return buf;
//    }
//
//    public static ConnectFrame decodeConnect(ByteBuf buf) {
//        buf.readByte();
//        int streamId = buf.readInt();
//        int svcLen = buf.readUnsignedShort();
//        byte[] svc = new byte[svcLen];
//        buf.readBytes(svc);
//        int jwtLen = buf.readUnsignedShort();
//        byte[] jwt = new byte[jwtLen];
//        buf.readBytes(jwt);
//
//        return new ConnectFrame(
//                streamId,
//                new String(svc, StandardCharsets.UTF_8),
//                new String(jwt, StandardCharsets.UTF_8)
//        );
//    }
//
//    // CONNECT_ACK: [type:1][streamId:4][status:1][reasonLen:2][reason:K]
//    public static ByteBuf encodeConnectAck(ByteBufAllocator alloc, int streamId,
//                                           byte status, String reason) {
//        byte[] r = reason == null ? new byte[0] : reason.getBytes(StandardCharsets.UTF_8);
//
//        ByteBuf buf = alloc.buffer(1 + 4 + 1 + 2 + r.length);
//        buf.writeByte(FrameType.CONNECT_ACK);
//        buf.writeInt(streamId);
//        buf.writeByte(status);
//        buf.writeShort(r.length);
//        buf.writeBytes(r);
//        return buf;
//    }
//
//    public static ConnectAckFrame decodeConnectAck(ByteBuf buf) {
//        buf.readByte();
//        int streamId = buf.readInt();
//        byte status = buf.readByte();
//        int reasonLen = buf.readUnsignedShort();
//        String reason = null;
//        if (reasonLen > 0) {
//            byte[] r = new byte[reasonLen];
//            buf.readBytes(r);
//            reason = new String(r, StandardCharsets.UTF_8);
//        }
//        return new ConnectAckFrame(streamId, status, reason);
//    }
//
//    // DATA — [type:1][streamId:4][dataLen:4][data:N]
//    public static ByteBuf encodeData(ByteBufAllocator alloc, int streamId, byte[] data) {
//        ByteBuf buf = alloc.buffer(1 + 4 + 4 + data.length);
//        buf.writeByte(FrameType.DATA);
//        buf.writeInt(streamId);
//        buf.writeInt(data.length);
//        buf.writeBytes(data);
//        return buf;
//    }
//
//    public static DataFrame decodeData(ByteBuf buf) {
//        buf.readByte();
//        int streamId = buf.readInt();
//        int dataLen = buf.readInt();
//        byte[] data = new byte[dataLen];
//        buf.readBytes(data);
//        return new DataFrame(streamId, dataLen, data);
//    }
//
//    // CLOSE — [type:1][streamId:4][reason:1]
//    public static ByteBuf encodeClose(ByteBufAllocator alloc, int streamId) {
//        return encodeClose(alloc, streamId, (byte) 0);
//    }
//
//    public static ByteBuf encodeClose(ByteBufAllocator alloc, int streamId, byte reason) {
//        ByteBuf buf = alloc.buffer(1 + 4 + 1);
//        buf.writeByte(FrameType.CLOSE);
//        buf.writeInt(streamId);
//        buf.writeByte(reason);
//        return buf;
//    }
//
//    public static CloseFrame decodeClose(ByteBuf buf) {
//        buf.readByte();
//        int streamId = buf.readInt();
//        byte reason = buf.readByte();
//        return new CloseFrame(streamId, reason);
//    }
}