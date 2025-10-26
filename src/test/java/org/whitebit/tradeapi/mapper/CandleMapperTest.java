package org.whitebit.tradeapi.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.whitebit.tradeapi.entity.CandleEntity;
import org.whitebit.tradeapi.model.Candle;
import org.whitebit.tradeapi.model.CandleDto;

import static org.junit.jupiter.api.Assertions.*;

class CandleMapperTest {

    private CandleMapper candleMapper;

    @BeforeEach
    void setUp() {
        candleMapper = new CandleMapper();
    }

    @Test
    void testToEntity_mapsAllFields() {
        Candle candle = new Candle(
                123L,   // timestamp
                10.5,   // open
                11.0,   // close
                12.0,   // high
                9.5,    // low
                100.0,  // volume
                105.0,  // quoteVolume
                "BTC_USDT"
        );

        CandleEntity entity = candleMapper.toEntity(candle);

        assertEquals(candle.timestamp(), entity.getTimestamp());
        assertEquals(candle.open(), entity.getOpen());
        assertEquals(candle.close(), entity.getClose());
        assertEquals(candle.high(), entity.getHigh());
        assertEquals(candle.low(), entity.getLow());
        assertEquals(candle.volume(), entity.getVolume());
        assertEquals(candle.quoteVolume(), entity.getQuoteVolume());
        assertEquals(candle.symbol(), entity.getSymbol());
    }

    @Test
    void testToDto_mapsAllFields() {
        CandleEntity entity = CandleEntity.builder()
                .timestamp(123L)
                .open(10.5)
                .close(11.0)
                .high(12.0)
                .low(9.5)
                .volume(100.0)
                .quoteVolume(105.0)
                .symbol("BTC_USDT")
                .ema(10.75)
                .build();

        CandleDto dto = candleMapper.toDto(entity);

        assertEquals(entity.getTimestamp(), dto.timestamp());
        assertEquals(entity.getOpen(), dto.open());
        assertEquals(entity.getClose(), dto.close());
        assertEquals(entity.getHigh(), dto.high());
        assertEquals(entity.getLow(), dto.low());
        assertEquals(entity.getSymbol(), dto.symbol());
        assertEquals(entity.getEma(), dto.ema());
    }
}
