package com.die_macher.awattar.dto;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

/** DTO representing the raw market data response from the aWATTar API. */
@Getter
@Setter
public class MarketDataDto {
  private String object;
  private List<MarketPriceDto> data;
}
