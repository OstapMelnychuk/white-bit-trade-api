package org.whitebit.tradeapi.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.whitebit.tradeapi.model.CandleDto;
import org.whitebit.tradeapi.service.CandleService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/trade/candle")
@CrossOrigin(origins = {"http://localhost:4200", "https://ostapmelnychuk.github.io"})
public class CandleController {
    private final CandleService candleService;

    @GetMapping("/today")
    public List<CandleDto> getCandlesForToday() {
        return candleService.getCandlesForToday();
    }
}
