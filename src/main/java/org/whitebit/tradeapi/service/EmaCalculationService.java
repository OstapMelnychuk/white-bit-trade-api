package org.whitebit.tradeapi.service;

import org.springframework.stereotype.Service;
import org.whitebit.tradeapi.model.Candle;

@Service
public class EmaCalculationService {
    private static final int PERIOD = 10;
    private static final double SMOOTHING_FACTOR = 2d / (PERIOD + 1);

    public double calculateEma(Candle currentCandle, double previousEma) {
        if (previousEma == 0) {
            return currentCandle.close();
        }
        return SMOOTHING_FACTOR * currentCandle.close() + previousEma * (1 - SMOOTHING_FACTOR);
    }
}
