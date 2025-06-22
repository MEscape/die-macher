package com.die_macher.application.flow;


import com.die_macher.application.service.PriceDataService;
import com.die_macher.application.transformer.PriceDataTransformer;
import com.die_macher.domain.model.PriceData;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.messaging.MessageChannel;

@Configuration
public class PriceDataFlow {
    private static final String PRICE_FLOW_INPUT = "priceFlowInput";

    private final PriceDataService priceDataService;
    private final PriceDataTransformer priceDataTransformer;

    public PriceDataFlow(PriceDataService priceDataService, PriceDataTransformer priceDataTransformer) {
        this.priceDataService = priceDataService;
        this.priceDataTransformer = priceDataTransformer;
    }

    @Bean
    public MessageChannel priceFlowInput() {
        return new DirectChannel();
    }

    @Bean
    public IntegrationFlow priceFlow() {
        return IntegrationFlow
                .from(PRICE_FLOW_INPUT)
                .transform(priceDataTransformer::transformRawMessage) // PriceData
                .publishSubscribeChannel(c -> c
                        .subscribe(f -> f
                                .<PriceData>handle((payload, headers) -> {
                                    priceDataService.storePriceData(payload);
                                    return null;
                                }))
                        .subscribe(f -> f
                                .<PriceData>handle((payload, headers) -> {
                                    priceDataService.publishToMqtt(payload);
                                    return null;
                                }))
                )
                .get();
    }
}
