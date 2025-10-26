package org.whitebit.tradeapi.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.whitebit.tradeapi.mapper.CandleMapper;
import org.whitebit.tradeapi.model.Candle;
import org.whitebit.tradeapi.model.CandleDto;
import org.whitebit.tradeapi.repository.CandleRepository;
import org.whitebit.tradeapi.service.async.AsyncCandleProcessor;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class CandleService {
    private final AsyncCandleProcessor asyncCandleProcessor;
    private final CandleRepository candleRepository;
    private final CandleMapper candleMapper;

    private final Map<Long, Candle> timestampToCandleMap = new ConcurrentHashMap<>();
    private volatile Long lastTimestamp = null;

    public List<CandleDto> getCandlesForToday() {
        long end = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);
        long start = end - 86400; // last 24 hours

        return candleRepository.findAllByDay(start, end).stream()
                .map(candleMapper::toDto)
                .toList();
    }

    public void handleCandleUpdate(Candle newCandle) {
        if (newCandle == null) return;

        long timestamp = newCandle.timestamp();

        if (lastTimestamp != null && timestamp > lastTimestamp) {
            Candle closed = timestampToCandleMap.remove(lastTimestamp);
            if (closed != null) {
                asyncCandleProcessor.processCandle(closed);
            }
        }

        timestampToCandleMap.put(timestamp, newCandle);
        lastTimestamp = timestamp;
    }
}
