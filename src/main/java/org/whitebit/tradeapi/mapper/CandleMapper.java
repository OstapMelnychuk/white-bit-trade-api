package org.whitebit.tradeapi.mapper;

import org.springframework.stereotype.Component;
import org.whitebit.tradeapi.entity.CandleEntity;
import org.whitebit.tradeapi.model.Candle;
import org.whitebit.tradeapi.model.CandleDto;

@Component
public class CandleMapper {
    public CandleEntity toEntity(Candle candle) {
        return CandleEntity.builder()
                .timestamp(candle.timestamp())
                .open(candle.open())
                .close(candle.close())
                .high(candle.high())
                .low(candle.low())
                .quoteVolume(candle.quoteVolume())
                .volume(candle.volume())
                .symbol(candle.symbol())
                .build();
    }

    public CandleDto toDto(CandleEntity candleEntity) {
        return CandleDto.builder()
                .timestamp(candleEntity.getTimestamp())
                .open(candleEntity.getOpen())
                .close(candleEntity.getClose())
                .high(candleEntity.getHigh())
                .low(candleEntity.getLow())
                .symbol(candleEntity.getSymbol())
                .ema(candleEntity.getEma())
                .build();
    }
}
