package com.example.protocol.frames;

import com.example.model.App;
import com.example.protocol.Frame;
import com.example.protocol.FrameType;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class RoutingResFrame extends Frame {
    private int appCount;
    private List<App> apps = new ArrayList<>();

    public RoutingResFrame() {
        super(FrameType.ROUTING_RESPONSE);
    }

    @Override
    public byte[] encodePayload() {
        int size = 4;
        for (App item : apps) {
            size += item.size();
        }
        ByteBuffer buffer = ByteBuffer.allocate(size);
        buffer.putInt(apps.size());
        for (App item : apps) {
            buffer.put(item.encodeApp());
        }
        return buffer.array();
    }

    @Override
    public void decodePayload(byte[] data) {
        ByteBuffer buffer = ByteBuffer.wrap(data);
        int appCount = buffer.getInt();
        for (int i = 0; i < appCount; i++) {
            App app = new App();
            app.decodeApp(buffer);
            apps.add(app);
        }
    }

    public List<App> getApps() {
        return apps;
    }

    public void setApps(List<App> apps) {
        this.apps = apps;
    }

    public int getAppCount() {
        return appCount;
    }

    public void setAppCount(int appCount) {
        this.appCount = appCount;
    }
}
