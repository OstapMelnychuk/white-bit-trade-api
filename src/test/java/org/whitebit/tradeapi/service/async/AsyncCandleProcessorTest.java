package org.whitebit.tradeapi.service.async;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.whitebit.tradeapi.entity.CandleEntity;
import org.whitebit.tradeapi.mapper.CandleMapper;
import org.whitebit.tradeapi.model.Candle;
import org.whitebit.tradeapi.model.CandleDto;
import org.whitebit.tradeapi.repository.CandleRepository;
import org.whitebit.tradeapi.service.EmaCalculationService;
import org.whitebit.tradeapi.websocket.service.CandleWebSocketService;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AsyncCandleProcessorTest {

    @Mock
    private EmaCalculationService calculationService;

    @Mock
    private CandleRepository candleRepository;

    @Mock
    private CandleMapper candleMapper;

    @Mock
    private CandleWebSocketService candleWebSocketService;

    @InjectMocks
    private AsyncCandleProcessor asyncCandleProcessor;

    @Test
    void testProcessCandle_withPreviousEma() {
        // Prepare test data
        Candle candle = new Candle(1L, 10, 12, 15, 8, 100, 120, "BTC_USDT");
        CandleEntity candleEntity = new CandleEntity();
        CandleEntity previousCandle = new CandleEntity();
        previousCandle.setEma(50.0);

        // Expected DTO
        CandleDto candleDto = new CandleDto(
                candle.timestamp(),
                candle.open(),
                candle.close(),
                candle.high(),
                candle.low(),
                candle.symbol(),
                55.0 // EMA after calculation
        );

        // Mock repository to return previous candle
        when(candleRepository.findTopByOrderByIdDesc()).thenReturn(Optional.of(previousCandle));

        // Mock EMA calculation
        when(calculationService.calculateEma(candle, 50.0)).thenReturn(55.0);

        // Mock mapper conversions
        when(candleMapper.toEntity(candle)).thenReturn(candleEntity);
        when(candleMapper.toDto(candleEntity)).thenReturn(candleDto);

        // Call the method under test
        asyncCandleProcessor.processCandle(candle);

        // Verify repository save
        ArgumentCaptor<CandleEntity> entityCaptor = ArgumentCaptor.forClass(CandleEntity.class);
        verify(candleRepository).save(entityCaptor.capture());
        assertEquals(55.0, entityCaptor.getValue().getEma());

        // Verify WebSocket update
        verify(candleWebSocketService).sendCandleUpdate(candleDto);
    }

    @Test
    void testProcessCandle_withoutPreviousEma() {
        // Prepare test data
        Candle candle = new Candle(2L, 20, 22, 25, 18, 200, 220, "BTC_USDT");
        CandleEntity candleEntity = new CandleEntity();

        // Expected DTO
        CandleDto candleDto = new CandleDto(
                candle.timestamp(),
                candle.open(),
                candle.close(),
                candle.high(),
                candle.low(),
                candle.symbol(),
                10.0
        );

        // No previous candle
        when(candleRepository.findTopByOrderByIdDesc()).thenReturn(Optional.empty());

        // Mock EMA calculation (previousEma = 0.0)
        when(calculationService.calculateEma(candle, 0.0)).thenReturn(10.0);

        // Mock mapper conversions
        when(candleMapper.toEntity(candle)).thenReturn(candleEntity);
        when(candleMapper.toDto(candleEntity)).thenReturn(candleDto);

        // Call the method under test
        asyncCandleProcessor.processCandle(candle);

        // Verify repository save
        ArgumentCaptor<CandleEntity> entityCaptor = ArgumentCaptor.forClass(CandleEntity.class);
        verify(candleRepository).save(entityCaptor.capture());
        assertEquals(10.0, entityCaptor.getValue().getEma());

        // Verify WebSocket update
        verify(candleWebSocketService).sendCandleUpdate(candleDto);
    }
}
