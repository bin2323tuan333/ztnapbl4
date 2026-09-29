package com.example.ztna_server.netty;

import com.example.ztna_server.core.ConnectorSession;
import com.example.ztna_server.core.ServiceRegistry;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.util.AttributeKey;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@ChannelHandler.Sharable
public class ConnectorWsHandler extends SimpleChannelInboundHandler<WebSocketFrame> {

    private static final AttributeKey<ConnectorSession> SESSION =
            AttributeKey.valueOf("ztna.connector.session");

    private final ServiceRegistry registry;
    private final ObjectMapper mapper = new ObjectMapper();

    public ConnectorWsHandler(ServiceRegistry registry) {
        this.registry = registry;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame frame) {
        if (!(frame instanceof TextWebSocketFrame text)) {
            log.warn("Connector sent non-text frame: {}", frame.getClass().getSimpleName());
            return;
        }

        // Đã đăng ký rồi → bỏ qua frame thừa
        if (ctx.channel().hasAttr(SESSION)) {
            log.debug("Connector sent extra frame, ignoring");
            return;
        }

        try {
            List<String> services = mapper.readValue(
                    text.text(), new TypeReference<List<String>>() {});

            String id = UUID.randomUUID().toString().substring(0, 8);
            ConnectorSession session = new ConnectorSession(id, services, ctx.channel());

            registry.register(session);
            ctx.channel().attr(SESSION).set(session);

            log.info("Connector {} registered with services: {}", id, services);

        } catch (Exception e) {
            log.error("Failed to parse connector services", e);
            ctx.close();
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        ConnectorSession s = ctx.channel().attr(SESSION).get();
        if (s != null) {
            registry.unregister(s);
        }
        super.channelInactive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("Connector handler error", cause);
        ctx.close();
    }
}