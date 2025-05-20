package com.die_macher.awattar.service;

import com.die_macher.awattar.model.MarketData;
import com.die_macher.awattar.model.MarketData.MarketPrice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.die_macher.awattar.model.OptimalProductionWindow;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
public class AwattarService {
    private static final Logger logger = LoggerFactory.getLogger(AwattarService.class);
    private static final double ENERGY_PER_PART = 0.2; // kWh pro Bauteil
    private static final int PARTS_PER_HOUR = 5; // Bauteile pro Stunde
    private static final int PRODUCTION_HOURS = 3; // Benötigte Produktionszeit in Stunden
    
    private final RestTemplate restTemplate;
    MarketData cachedMarketData;
    private OptimalProductionWindow optimalWindow;

    @Autowired
    public AwattarService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Ruft die Marktdaten für einen beliebigen Startzeitpunkt ab (immer für 24h)
     */
    public MarketData fetchMarketDataFor(long startMillis) {
        try {
            long endMillis = startMillis + 24 * 60 * 60 * 1000; // 24 hours later
            String url = "https://api.awattar.at/v1/marketdata?start=" + startMillis + "&end=" + endMillis;
            logger.info("Rufe aWATTar API ab: {}", url);
            ResponseEntity<MarketData> response = restTemplate.getForEntity(url, MarketData.class);
            return response.getBody();
        } catch (Exception e) {
            logger.error("Fehler beim Abrufen der Marktdaten: {}", e.getMessage(), e);
            return null;
        }
    }

    public MarketData fetchCurrentMarketData() {
        ZonedDateTime nowHour = ZonedDateTime.now(ZoneId.of("Europe/Vienna")).truncatedTo(ChronoUnit.HOURS);
        long startMillis = nowHour.toInstant().toEpochMilli();
        return fetchMarketDataFor(startMillis);
    }

    /**
     * Holt die Strompreise für morgen (ab Mitternacht)
     */
    public MarketData fetchTomorrowMarketData() {
        ZonedDateTime tomorrowMidnight = ZonedDateTime.now(ZoneId.of("Europe/Vienna"))
                .plusDays(1)
                .truncatedTo(ChronoUnit.DAYS);
        long tomorrowMillis = tomorrowMidnight.toInstant().toEpochMilli();
        return fetchMarketDataFor(tomorrowMillis);
    }

    /**
     * Aktualisiert die Marktdaten einmal täglich um 14 Uhr
     */
    @Scheduled(cron = "0 0 14 * * *")
    public void updateMarketData() {
        logger.info("Aktualisiere Marktdaten für morgen...");
        cachedMarketData = fetchTomorrowMarketData();
        calculateOptimalProductionWindow();
    }

    /**
     * Berechnet das optimale Produktionsfenster (3 aufeinanderfolgende Stunden mit den niedrigsten Stromkosten)
     */
    private void calculateOptimalProductionWindow() {
        if (cachedMarketData == null || cachedMarketData.getData() == null || cachedMarketData.getData().size() < PRODUCTION_HOURS) {
            logger.warn("Keine ausreichenden Marktdaten für die Berechnung des optimalen Produktionsfensters");
            return;
        }

        List<MarketPrice> prices = cachedMarketData.getData();
        double lowestCost = Double.MAX_VALUE;
        int bestStartIndex = 0;

        // Finde das günstigste Zeitfenster von PRODUCTION_HOURS aufeinanderfolgenden Stunden
        for (int i = 0; i <= prices.size() - PRODUCTION_HOURS; i++) {
            double windowCost = 0;
            for (int j = i; j < i + PRODUCTION_HOURS; j++) {
                windowCost += prices.get(j).getMarketprice();
            }

            if (windowCost < lowestCost) {
                lowestCost = windowCost;
                bestStartIndex = i;
            }
        }

        // Erstelle das optimale Produktionsfenster
        MarketPrice startPrice = prices.get(bestStartIndex);
        MarketPrice endPrice = prices.get(bestStartIndex + PRODUCTION_HOURS - 1);
        
        List<MarketPrice> windowPrices = new ArrayList<>();
        for (int i = bestStartIndex; i < bestStartIndex + PRODUCTION_HOURS; i++) {
            windowPrices.add(prices.get(i));
        }
        
        double totalCost = calculateProductionCost(windowPrices);
        
        optimalWindow = new OptimalProductionWindow(
                startPrice.getStart_timestamp(),
                endPrice.getEnd_timestamp(),
                windowPrices,
                totalCost
        );
        
        logger.info("Optimales Produktionsfenster berechnet: {} bis {}, Kosten: {} EUR",
                startPrice.getStartTimeFormatted(),
                endPrice.getEndTimeFormatted(),
                String.format("%.2f", totalCost));
    }

    /**
     * Berechnet die Produktionskosten für ein gegebenes Zeitfenster
     * @param prices Liste der Marktpreise im Zeitfenster
     * @return Gesamtkosten in EUR
     */
    private double calculateProductionCost(List<MarketPrice> prices) {
        double totalCost = 0;
        int totalParts = PARTS_PER_HOUR * PRODUCTION_HOURS;
        double totalEnergy = ENERGY_PER_PART * totalParts;
        
        // Berechne den Durchschnittspreis pro kWh im Zeitfenster
        double avgPricePerKwh = prices.stream()
                .mapToDouble(MarketPrice::getPriceInEurPerKwh)
                .average()
                .orElse(0);
        
        // Gesamtkosten = Durchschnittspreis pro kWh * Gesamtenergieverbrauch
        totalCost = avgPricePerKwh * totalEnergy;
        
        return totalCost;
    }

    /**
     * Gibt das aktuell berechnete optimale Produktionsfenster zurück
     * @return OptimalProductionWindow oder null, wenn noch nicht berechnet
     */
    public OptimalProductionWindow getOptimalProductionWindow() {
        if (optimalWindow == null) {
            // Zuerst Marktdaten laden, dann berechnen!
            cachedMarketData = fetchTomorrowMarketData();
            calculateOptimalProductionWindow();
        }
        return optimalWindow;
    }

 
}