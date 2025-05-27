package com.die_macher.awattar.mapper;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.die_macher.awattar.dto.MarketDataDto;
import com.die_macher.awattar.dto.MarketPriceDto;
import com.die_macher.awattar.dto.OptimalProductionWindowDto;
import com.die_macher.awattar.model.MarketData;
import com.die_macher.awattar.model.OptimalProductionWindow;

/**
 * Mapper class for converting between DTOs and domain models.
 */
@Component
public class AwattarMapper {

    /**
     * Converts a MarketData domain model to a MarketDataDto.
     */
    public MarketDataDto toDto(MarketData marketData) {
        if (marketData == null) {
            return null;
        }
        
        MarketDataDto dto = new MarketDataDto();
        dto.setObject(marketData.getObject());
        
        if (marketData.getData() != null) {
            List<MarketPriceDto> priceDtos = marketData.getData().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
            dto.setData(priceDtos);
        }
        
        return dto;
    }
    
    /**
     * Converts a MarketPrice domain model to a MarketPriceDto.
     */
    public MarketPriceDto toDto(MarketData.MarketPrice marketPrice) {
        if (marketPrice == null) {
            return null;
        }
        
        MarketPriceDto dto = new MarketPriceDto();
        dto.setStart_timestamp(marketPrice.getStartTimestamp());
        dto.setEnd_timestamp(marketPrice.getEndTimestamp());
        dto.setMarketprice(marketPrice.getMarketprice());
        dto.setUnit(marketPrice.getUnit());
        
        return dto;
    }
    
    /**
     * Converts a MarketDataDto to a MarketData domain model.
     */
    public MarketData toModel(MarketDataDto dto) {
        if (dto == null) {
            return null;
        }
        
        MarketData model = new MarketData();
        model.setObject(dto.getObject());
        
        if (dto.getData() != null) {
            List<MarketData.MarketPrice> prices = dto.getData().stream()
                .map(this::toModel)
                .collect(Collectors.toList());
            model.setData(prices);
        }
        
        return model;
    }
    
    /**
     * Converts a MarketPriceDto to a MarketPrice domain model.
     */
    public MarketData.MarketPrice toModel(MarketPriceDto dto) {
        if (dto == null) {
            return null;
        }
        
        MarketData.MarketPrice model = new MarketData.MarketPrice();
        model.setStartTimestamp(dto.getStart_timestamp());
        model.setEndTimestamp(dto.getEnd_timestamp());
        model.setMarketprice(dto.getMarketprice());
        model.setUnit(dto.getUnit());
        
        return model;
    }
    
    /**
     * Converts an OptimalProductionWindow domain model to an OptimalProductionWindowDto.
     */
    public OptimalProductionWindowDto toDto(OptimalProductionWindow window) {
        if (window == null) {
            return null;
        }
        
        OptimalProductionWindowDto dto = new OptimalProductionWindowDto();
        dto.setStartTimestamp(window.getStartTimestamp());
        dto.setEndTimestamp(window.getEndTimestamp());
        dto.setTotalCost(window.getTotalCost());
        dto.setPriceInEurPerKwh(window.getPriceInEurPerKwh());
        dto.setStartTimeFormatted(window.getStartTimeFormatted());
        dto.setEndTimeFormatted(window.getEndTimeFormatted());
        
        if (window.getPrices() != null) {
            List<MarketPriceDto> priceDtos = window.getPrices().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
            dto.setPrices(priceDtos);
        }
        
        return dto;
    }
}