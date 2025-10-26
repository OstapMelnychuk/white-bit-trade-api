package org.whitebit.tradeapi.model;

import lombok.Builder;

@Builder
public record CandleDto(
        long timestamp,
        double open,
        double close,
        double high,
        double low,
        String symbol,
        double ema
) {}
