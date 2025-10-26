package org.whitebit.tradeapi.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.whitebit.tradeapi.entity.CandleEntity;
import org.whitebit.tradeapi.mapper.CandleMapper;
import org.whitebit.tradeapi.model.Candle;
import org.whitebit.tradeapi.model.CandleDto;
import org.whitebit.tradeapi.repository.CandleRepository;
import org.whitebit.tradeapi.service.async.AsyncCandleProcessor;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CandleServiceTest {

    @Mock
    private AsyncCandleProcessor asyncCandleProcessor;

    @Mock
    private CandleRepository candleRepository;

    @Spy
    private CandleMapper candleMapper;

    @InjectMocks
    private CandleService candleService;

    @Test
    void testGetCandlesForToday_returnsMappedDtos() {
        // Create CandleEntity instances, because repository returns CandleEntity
        CandleEntity entity1 = new CandleEntity();
        entity1.setTimestamp(1L);
        entity1.setOpen(10);
        entity1.setClose(12);
        entity1.setHigh(15);
        entity1.setLow(8);
        entity1.setSymbol("BTC_USDT");

        CandleEntity entity2 = new CandleEntity();
        entity2.setTimestamp(2L);
        entity2.setOpen(20);
        entity2.setClose(22);
        entity2.setHigh(25);
        entity2.setLow(18);
        entity2.setSymbol("BTC_USDT");

        // Mock repository
        when(candleRepository.findAllByDay(anyLong(), anyLong())).thenReturn(List.of(entity1, entity2));

        // Call service
        List<CandleDto> result = candleService.getCandlesForToday();

        assertEquals(2, result.size());

        CandleDto dto1 = result.get(0);
        assertEquals(entity1.getTimestamp(), dto1.timestamp());
        assertEquals(entity1.getOpen(), dto1.open());
        assertEquals(entity1.getClose(), dto1.close());
        assertEquals(entity1.getHigh(), dto1.high());
        assertEquals(entity1.getLow(), dto1.low());
        assertEquals(entity1.getSymbol(), dto1.symbol());

        CandleDto dto2 = result.get(1);
        assertEquals(entity2.getTimestamp(), dto2.timestamp());
        assertEquals(entity2.getOpen(), dto2.open());
        assertEquals(entity2.getClose(), dto2.close());
        assertEquals(entity2.getHigh(), dto2.high());
        assertEquals(entity2.getLow(), dto2.low());
        assertEquals(entity2.getSymbol(), dto2.symbol());

        // Verify mapper called
        verify(candleMapper, times(1)).toDto(entity1);
        verify(candleMapper, times(1)).toDto(entity2);
    }

    @Test
    void testHandleCandleUpdate_newCandle_noPrevious() {
        Candle newCandle = new Candle(100L, 10, 12, 15, 8, 100, 120, "BTC_USDT");

        candleService.handleCandleUpdate(newCandle);

        Map<Long, Candle> map = (Map<Long, Candle>) getField(candleService, "timestampToCandleMap");
        assertEquals(newCandle, map.get(100L));

        verify(asyncCandleProcessor, never()).processCandle(any());
    }

    @Test
    void testHandleCandleUpdate_newCandle_closesPrevious() {
        Candle firstCandle = new Candle(100L, 10, 12, 15, 8, 100, 120, "BTC_USDT");
        Candle secondCandle = new Candle(200L, 20, 22, 25, 18, 200, 220, "BTC_USDT");

        candleService.handleCandleUpdate(firstCandle);
        candleService.handleCandleUpdate(secondCandle);

        Map<Long, Candle> map = (Map<Long, Candle>) getField(candleService, "timestampToCandleMap");
        assertEquals(1, map.size());
        assertEquals(secondCandle, map.get(200L));

        verify(asyncCandleProcessor, times(1)).processCandle(firstCandle);
    }

    @Test
    void testHandleCandleUpdate_nullCandle() {
        candleService.handleCandleUpdate(null);
        verifyNoInteractions(asyncCandleProcessor);
    }

    @SuppressWarnings("unchecked")
    private Object getField(Object obj, String fieldName) {
        try {
            var field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
