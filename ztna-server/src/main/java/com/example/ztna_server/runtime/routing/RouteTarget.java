package com.example.ztna_server.runtime.routing;

public class RouteTarget {
    private final String resourceId;
    private final String resourceName;
    private final String host;
    private final int port;
    private final String connectorId;

    public RouteTarget(String resourceId, String resourceName, String host, int port, String connectorId) {
        if (resourceId == null || resourceId.isBlank())
            throw new IllegalArgumentException("resourceId is required");
        if (connectorId == null || connectorId.isBlank())
            throw new IllegalArgumentException("connectorId is required");
        if (host == null || host.isBlank())
            throw new IllegalArgumentException("host is required");
        if (port < 1 || port > 65535)
            throw new IllegalArgumentException("port out of range: " + port);

        this.resourceId   = resourceId;
        this.resourceName = resourceName;
        this.host         = host;
        this.port         = port;
        this.connectorId  = connectorId;
    }

    public String resourceId()   { return resourceId; }
    public String resourceName() { return resourceName; }
    public String host()         { return host; }
    public int    port()         { return port; }
    public String connectorId()  { return connectorId; }
}
