package com.die_macher.awattar.dto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * DTO representing an optimal production window for client responses.
 */
@Getter
@Setter
public class OptimalProductionWindowDto {
    private long startTimestamp;
    private long endTimestamp;
    private List<MarketPriceDto> prices;
    private double totalCost;
    private double priceInEurPerKwh;
    private String startTimeFormatted;
    private String endTimeFormatted;
}