package com.die_macher.awattar.service;

import com.die_macher.awattar.model.MarketData;
import com.die_macher.awattar.model.OptimalProductionWindow;

public interface AwattarService {
    MarketData fetchCurrentMarketData();
    MarketData fetchTomorrowMarketData();
    OptimalProductionWindow getOptimalProductionWindow();
    double getCurrentPartCost();
}
