package org.whitebit.tradeapi.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.whitebit.tradeapi.model.Candle;

import static org.junit.jupiter.api.Assertions.*;

class EmaCalculationServiceTest {

    private EmaCalculationService emaCalculationService;

    @BeforeEach
    void setUp() {
        emaCalculationService = new EmaCalculationService();
    }

    @Test
    void testCalculateEma_previousEmaIsZero_returnsClosePrice() {
        Candle candle = new Candle(1L, 10, 50, 60, 5, 100, 120, "BTC_USDT");
        double result = emaCalculationService.calculateEma(candle, 0);

        assertEquals(50, result, 0.0001); // should return close price
    }

    @Test
    void testCalculateEma_withPreviousEma_calculatesCorrectly() {
        Candle candle = new Candle(1L, 10, 50, 60, 5, 100, 120, "BTC_USDT");
        double previousEma = 40;

        double expected = 0.18181818181818182 * 50 + 0.8181818181818182 * 40;

        double result = emaCalculationService.calculateEma(candle, previousEma);

        assertEquals(expected, result, 0.0001);
    }

    @Test
    void testCalculateEma_multipleCalculations() {
        double previousEma = 0;
        double[] closes = {10, 12, 11, 13, 12};
        double result = 0;

        for (double close : closes) {
            Candle candle = new Candle(1L, 0, close, 0, 0, 0, 0, "BTC_USDT");
            result = emaCalculationService.calculateEma(candle, previousEma);
            previousEma = result;
        }

        Candle lastCandle = new Candle(1L, 0, 12, 0, 0, 0, 0, "BTC_USDT");
        double finalEma = emaCalculationService.calculateEma(lastCandle, previousEma);
        assertTrue(finalEma > 0);
    }
}
