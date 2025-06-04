package com.die_macher.awattar.dto;

import lombok.Getter;
import lombok.Setter;

/** DTO representing a single market price entry from the aWATTar API. */
@Getter
@Setter
public class MarketPriceDto {
  private long start_timestamp;
  private long end_timestamp;
  private double marketprice;
  private String unit;
}
