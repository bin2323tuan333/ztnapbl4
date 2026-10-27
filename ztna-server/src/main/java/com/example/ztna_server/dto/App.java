package com.example.ztna_server.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class App {
    private String id;
    private String name;
    private String host;
    private int port;

    private static String readString(ByteBuffer bb) {
        int len = bb.getInt();
        if (len < 0 || len > bb.remaining()) {
            throw new IllegalArgumentException("Độ dài String không hợp lệ: " + len);
        }
        byte[] bytes = new byte[len];
        bb.get(bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    private static void writeString(ByteBuffer bb, byte[] bytes) {
        bb.putInt(bytes.length);
        bb.put(bytes);
    }

    private static byte[] safeBytes(String s) {
        return (s == null ? "" : s).getBytes(StandardCharsets.UTF_8);
    }

    public void decodeApp(ByteBuffer buffer) {
        this.id   = readString(buffer);
        this.name = readString(buffer);
        this.host = readString(buffer);
        this.port = buffer.getInt();
    }

    public byte[] encodeApp() {
        ByteBuffer buffer = ByteBuffer.allocate(size());
        writeString(buffer, safeBytes(this.id));
        writeString(buffer, safeBytes(this.name));
        writeString(buffer, safeBytes(this.host));
        buffer.putInt(this.port);
        return buffer.array();
    }

    public int size() {
        return 4 + safeBytes(id).length
                + 4 + safeBytes(name).length
                + 4 + safeBytes(host).length
                + 4;
    }
}
