package org.whitebit.tradeapi.model;

public record Candle(
        long timestamp,
        double open,
        double close,
        double high,
        double low,
        double volume,
        double quoteVolume,
        String symbol
) {}