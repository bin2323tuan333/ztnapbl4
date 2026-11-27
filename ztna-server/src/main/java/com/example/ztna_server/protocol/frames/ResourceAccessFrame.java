package com.example.ztna_server.protocol.frames;

import com.example.ztna_server.protocol.Frame;
import com.example.ztna_server.protocol.FrameType;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * RESOURCE_ACCESS: Agent xin mở một stream TCP tới ứng dụng.
 *
 * Format payload: [reqIdLen 4B][reqId][appIdLen 4B][appId][ticketLen 4B][ticket]
 * ticket = vé Server cấp trong HTTP_RESPONSE, cho biết ai đang xin truy cập
 * (một Agent có thể được nhiều người dùng chung). Phần ticket là tuỳ chọn khi decode
 * để không vỡ khi nhận frame kiểu cũ; Server sẽ từ chối nếu thiếu vé.
 */
public class ResourceAccessFrame extends Frame {

    private String requestId;
    private String appId;
    private String ticket = "";

    public ResourceAccessFrame() {
        super(FrameType.RESOURCE_ACCESS);
    }

    public ResourceAccessFrame(String requestId, String appId) {
        this(requestId, appId, "");
    }

    public ResourceAccessFrame(String requestId, String appId, String ticket) {
        super(FrameType.RESOURCE_ACCESS);
        this.requestId = (requestId == null ? "" : requestId);
        this.appId = (appId == null ? "" : appId);
        this.ticket = (ticket == null ? "" : ticket);
    }

    @Override
    public byte[] encodePayload() {
        byte[] reqIdBuf = requestId.getBytes(StandardCharsets.UTF_8);
        byte[] appIdBuf = appId.getBytes(StandardCharsets.UTF_8);
        byte[] ticketBuf = ticket.getBytes(StandardCharsets.UTF_8);

        ByteBuffer bb = ByteBuffer.allocate(4 + reqIdBuf.length + 4 + appIdBuf.length + 4 + ticketBuf.length);

        bb.putInt(reqIdBuf.length);
        bb.put(reqIdBuf);

        bb.putInt(appIdBuf.length);
        bb.put(appIdBuf);

        bb.putInt(ticketBuf.length);
        bb.put(ticketBuf);

        return bb.array();
    }

    @Override
    public void decodePayload(byte[] payload) {
        if (payload.length < 8) {
            throw new IllegalArgumentException("Payload quá ngắn: " + payload.length);
        }

        ByteBuffer bb = ByteBuffer.wrap(payload);
        this.requestId = readString(bb, "reqIdLen");
        this.appId = readString(bb, "appIdLen");
        // Frame kiểu cũ không có ticket
        this.ticket = bb.remaining() >= 4 ? readString(bb, "ticketLen") : "";
    }

    private static String readString(ByteBuffer bb, String name) {
        int len = bb.getInt();
        if (len < 0 || len > bb.remaining()) {
            throw new IllegalArgumentException(name + " không hợp lệ: " + len);
        }
        byte[] buf = new byte[len];
        bb.get(buf);
        return new String(buf, StandardCharsets.UTF_8);
    }

    public String getRequestId() { return requestId; }
    public String getAppId()     { return appId; }
    public String getTicket()    { return ticket; }
}
