package org.whitebit.tradeapi.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.websocket.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.whitebit.tradeapi.model.Candle;
import org.whitebit.tradeapi.model.CandleUpdateResponse;
import org.whitebit.tradeapi.service.CandleService;

import java.net.URI;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@ClientEndpoint
@RequiredArgsConstructor
public class WhiteBitWsClient {
    @Autowired
    private CandleService candleService;
    @Autowired
    private final ObjectMapper mapper;

    private Session session;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    private static final String WS_URL = "wss://api.whitebit.com/ws";
    private static final String CANDLES_UPDATE = "candles_update";
    private volatile boolean reconnecting = false;

    @EventListener(ApplicationReadyEvent.class)
    public void startWebSocketClient() {
        try {
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            container.setDefaultMaxTextMessageBufferSize(1024 * 1024 * 5);
            container.connectToServer(this, URI.create(WS_URL));
            log.info("Successfully connected to WhiteBit WebSocket Server");
        } catch (Exception e) {
            log.error("Cannot connect to WhiteBit WebSocket", e);
        }
    }

    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
        log.info("Connected to WhiteBit WebSocket");
        scheduler.scheduleAtFixedRate(this::sendPing, 0, 50, TimeUnit.SECONDS);

        subscribe();
    }

    @OnMessage
    public void onMessage(String message) {
        try {
            Map<String, Object> response = mapper.readValue(message, Map.class);
            String method = (String) response.get("method");

            if (Objects.isNull(method) || !method.equals(CANDLES_UPDATE)) {
                log.info("Received: {}", message);
                return;
            }

            CandleUpdateResponse candleUpdateResponse = mapper.readValue(message, CandleUpdateResponse.class);
            Candle candle = candleUpdateResponse.toCandle();
            log.info("Received candle update: {}", candle);
            candleService.handleCandleUpdate(candle);
        } catch (Exception e) {
            log.error("Cannot parse message {}", message, e);
        }
    }


    @OnError
    public void onError(Session session, Throwable thr) {
        log.error("WebSocket error", thr);
        reconnectWithDelay(5);
    }

    @OnClose
    public void onClose(Session session, CloseReason reason) {
        log.warn("Connection closed: {}", reason);
        reconnectWithDelay(5);
    }

    private void sendPing() {
        if (session != null && session.isOpen()) {
            session.getAsyncRemote().sendText("{\"id\":1,\"method\":\"ping\",\"params\":[]}");
            log.info("Sent ping");
        }
    }

    public void subscribe() {
        if (session != null && session.isOpen()) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                Map<String, Object> payload = Map.of(
                        "id", 3,
                        "method", "candles_subscribe",
                        "params", new Object[]{"BTC_USDT", 300}
                );
                String json = mapper.writeValueAsString(payload);
                session.getAsyncRemote().sendText(json);
                log.info("📤 Sent candles_subscribe: {}", json);
            } catch (Exception e) {
                log.error("Cannot send candles_subscribe", e);
            }
        }
    }

    private void reconnectWithDelay(long delaySeconds) {
        if (reconnecting) {
            return;
        }
        reconnecting = true;

        scheduler.schedule(() -> {
            try {
                log.info("Attempting to reconnect to WhiteBit WebSocket...");
                WebSocketContainer container = ContainerProvider.getWebSocketContainer();
                container.setDefaultMaxTextMessageBufferSize(1024 * 1024 * 5);
                container.connectToServer(this, URI.create(WS_URL));
                reconnecting = false; // reset if successful
            } catch (Exception e) {
                log.error("Reconnect failed, will retry in {} seconds", delaySeconds, e);
                reconnecting = false;
                reconnectWithDelay(Math.min(delaySeconds * 2, 60)); // exponential backoff
            }
        }, delaySeconds, TimeUnit.SECONDS);
    }
}
