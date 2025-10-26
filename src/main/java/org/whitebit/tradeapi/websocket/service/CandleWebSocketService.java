package org.whitebit.tradeapi.websocket.service;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.whitebit.tradeapi.model.CandleDto;

@Service
@RequiredArgsConstructor
public class CandleWebSocketService {

    private final SimpMessagingTemplate messagingTemplate;

    public void sendCandleUpdate(CandleDto candle) {
        messagingTemplate.convertAndSend("/topic/candles", candle);
    }
}