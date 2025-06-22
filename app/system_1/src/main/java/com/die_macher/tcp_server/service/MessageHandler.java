
package com.die_macher.tcp_server.service;

import com.die_macher.pick_and_place.model.PickAndPlaceResult;
import com.die_macher.pick_and_place.model.StackInfo;
import com.die_macher.pick_and_place.service.StackTracker;
import com.die_macher.tcp_server.events.MessageReceived;
import com.die_macher.pick_and_place.service.PickAndPlaceOrchestrator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
public class MessageHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(MessageHandler.class);

    private final TcpDataDispatcher tcpDataDispatcher;
    private final PickAndPlaceOrchestrator pickAndPlaceOrchestrator;
    private final StackTracker stackTracker;

    public MessageHandler(TcpDataDispatcher tcpDataDispatcher,
                          PickAndPlaceOrchestrator pickAndPlaceOrchestrator, StackTracker stackTracker) {
        this.tcpDataDispatcher = tcpDataDispatcher;
        this.pickAndPlaceOrchestrator = pickAndPlaceOrchestrator;
        this.stackTracker = stackTracker;
    }

    @EventListener
    void on(MessageReceived event) {
        LOGGER.info("Message received from System 2: {}", event.message());

        Map<String, Object> content = event.message();
        if (content.isEmpty()) {
            LOGGER.warn("No content found in message or invalid format.");
            return;
        }

        String type = (String) content.get("type");
        Map<String, Object> data = extractMap(content);

        switch (type) {
            case "request_awattar_data" -> handleAwattarRequest(data);
            case "pick_and_place" -> handlePickAndPlace(data);
            case "process_single_piece" -> handleSinglePieceProcessing(data);
            default -> LOGGER.warn("Unknown message type received: {}", type);
        }
    }

    private void handleAwattarRequest(Map<String, Object> data) {
        String which = (String) data.get("which");
        switch (which) {
            case "optimal_window" -> tcpDataDispatcher.sendOptimalProductionWindow();
            case "tomorrow_prices" -> tcpDataDispatcher.sendTomorrowMarketData();
            default -> LOGGER.warn("Unknown awattar data request type: {}", which);
        }
    }

    private void handlePickAndPlace(Map<String, Object> data) {
        Integer count = (Integer) data.get("number_of_pieces");
        LOGGER.info("Pick and place request received for {} pieces", count);

        if (count == null || count <= 0) {
            LOGGER.warn("Invalid number of pieces for pick and place: {}", count);
            return;
        }

        try {
            List<PickAndPlaceResult> results = pickAndPlaceOrchestrator.startPickAndPlace(count);
            tcpDataDispatcher.sendResult(results);
            LOGGER.info("Pick and place operation completed and results sent.");
        } catch (Exception e) {
            LOGGER.error("Failed to start pick and place operation for {} pieces", count, e);
        }
    }

    private void handleSinglePieceProcessing(Map<String, Object> data) {
        Color color = (Color) data.get("color");
        LOGGER.info("Request to process single piece with color: {}", color);
        StackInfo stackInfo = stackTracker.removeCube(color);
        LOGGER.info("Stack Info: {}", stackInfo);
    }


    @SuppressWarnings("unchecked")
    private static Map<String, Object> extractMap(Map<String, Object> message) {
        return Optional.ofNullable(message.get("data"))
                .filter(Map.class::isInstance)
                .map(m -> (Map<String, Object>) m)
                .orElseGet(HashMap::new);
    }

}
