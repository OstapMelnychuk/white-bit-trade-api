package org.whitebit.tradeapi.service.async;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.whitebit.tradeapi.entity.CandleEntity;
import org.whitebit.tradeapi.mapper.CandleMapper;
import org.whitebit.tradeapi.model.Candle;
import org.whitebit.tradeapi.repository.CandleRepository;
import org.whitebit.tradeapi.service.EmaCalculationService;
import org.whitebit.tradeapi.websocket.service.CandleWebSocketService;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AsyncCandleProcessor {
    private final EmaCalculationService calculationService;
    private final CandleRepository candleRepository;
    private final CandleMapper candleMapper;
    private final CandleWebSocketService candleWebSocketService;

    @Async
    public void processCandle(Candle candle) {
        double previousEma = getPreviousEma();
        double ema = calculationService.calculateEma(candle, previousEma);
        log.info("Calculated EMA: {} for candle with the timestamp: {}", ema, candle);

        CandleEntity candleEntity = candleMapper.toEntity(candle);
        candleEntity.setEma(ema);
        candleRepository.save(candleEntity);
        sendCandleUpdate(candleEntity);
    }

    private double getPreviousEma() {
        Optional<CandleEntity> previousCandle = candleRepository.findTopByOrderByIdDesc();
        return previousCandle.map(CandleEntity::getEma).orElse(0.0);
    }

    private void sendCandleUpdate(CandleEntity candleEntity) {
        candleWebSocketService.sendCandleUpdate(candleMapper.toDto(candleEntity));
    }
}
