package org.example.protocol;

public abstract class Frame {
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

    protected abstract void decodePayload(byte[] data);
    protected abstract byte[] encodePayload();
}