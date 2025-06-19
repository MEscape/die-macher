package com.die_macher.tcp_server.service;

import com.die_macher.awattar.model.MarketData;
import com.die_macher.awattar.model.OptimalProductionWindow;
import com.die_macher.awattar.service.AwattarService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AwattarDataSender {
    private static final Logger LOGGER = LoggerFactory.getLogger(AwattarDataSender.class);

    private final AwattarService awattarService;
    private final TcpSender tcpSender;

    public AwattarDataSender(AwattarService awattarService, TcpSender tcpSender) {
        this.awattarService = awattarService;
        this.tcpSender = tcpSender;
    }

    /**
     * Send only tomorrow's market data to a specific client
     */
    public void sendTomorrowMarketData() {
        MarketData tomorrowData = awattarService.fetchTomorrowMarketData();
        try {
            Map<String, Object> dataPacket = new HashMap<>();
            dataPacket.put("type", "tomorrow_market_data");
            dataPacket.put("data", tomorrowData);

            tcpSender.send(dataPacket);
            LOGGER.debug("Sent tomorrow's market data");
        } catch (Exception e) {
            LOGGER.error("Failed to send tomorrow market data to {}", e.getMessage(), e);
        }
    }

    /**
     * Send only the optimal production window to a specific client
     */
    public void sendOptimalProductionWindow() {
        OptimalProductionWindow window = awattarService.getOptimalProductionWindow();
        try {
            if (window != null) {
                Map<String, Object> dataPacket = new HashMap<>();
                dataPacket.put("type", "optimal_production_window");
                dataPacket.put("data", window);

                tcpSender.send(dataPacket);
                LOGGER.debug("Sent optimal production window");
            }
        } catch (Exception e) {
            LOGGER.error("Failed to send optimal production window to {}", e.getMessage(), e);
        }
    }

    /**
     * Send a "data not available" message to one client
     */
    public void sendDataNotAvailableMessage() {
        try {
            Map<String, Object> notificationPacket = new HashMap<>();
            notificationPacket.put("type", "data_not_available");
            notificationPacket.put("message", "Awattar data for tomorrow is not yet available. Data arrives daily between 13:00 and 14:00.");

            tcpSender.send(notificationPacket);
            LOGGER.debug("Sent data not available message ");
        } catch (Exception e) {
            LOGGER.error("Failed to send data not available message to {}",  e.getMessage(), e);
        }
    }


}
