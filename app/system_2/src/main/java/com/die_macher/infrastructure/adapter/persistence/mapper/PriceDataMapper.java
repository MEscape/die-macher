package com.die_macher.infrastructure.adapter.persistence.mapper;

import com.die_macher.domain.model.price.PriceData;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import com.influxdb.query.FluxRecord;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class PriceDataMapper {

  // Field name constants
  private static final String FIELD_MARKETPRICE = "marketprice";
  private static final String FIELD_PRICE_PER_KWH = "price_per_kwh";
  private static final String FIELD_START_TIME_FORMATTED = "start_time_formatted";
  private static final String FIELD_END_TIME_FORMATTED = "end_time_formatted";
  private static final String FIELD_END_TIMESTAMP = "end_timestamp";

  // Influx system field constants
  private static final String FIELD_VALUE = "_value";
  private static final String FIELD_FIELD = "_field";
  private static final String FIELD_MEASUREMENT = "_measurement";
  private static final String FIELD_TIME = "_time";

  public Point toPoint(PriceData priceData) {
    Point point =
            Point.measurement("price_data")
                    .addField(FIELD_MARKETPRICE, priceData.marketprice())
                    .addField(FIELD_PRICE_PER_KWH, priceData.priceInEurPerKwh())
                    .addField(FIELD_START_TIME_FORMATTED, priceData.startTimeFormatted())
                    .addField(FIELD_END_TIME_FORMATTED, priceData.endTimeFormatted())
                    .time(priceData.startTimestamp().toEpochMilli(), WritePrecision.MS);

    priceData.metadata().forEach(point::addTag);

    return point;
  }

  public PriceData fromFluxRecord(FluxRecord fluxRecord) {
    Instant startTimestamp = fluxRecord.getTime();
    Instant endTimestamp = parseInstantField(fluxRecord, FIELD_END_TIMESTAMP);

    double marketprice = getDoubleValue(fluxRecord, FIELD_MARKETPRICE);
    double pricePerKwh = getDoubleValue(fluxRecord, FIELD_PRICE_PER_KWH);

    String startFormatted = getStringValue(fluxRecord, FIELD_START_TIME_FORMATTED);
    String endFormatted = getStringValue(fluxRecord, FIELD_END_TIME_FORMATTED);

    Map<String, String> metadata = new HashMap<>();
    fluxRecord.getValues().forEach((key, valueObj) -> {
      if (!Set.of(
              FIELD_VALUE,
              FIELD_FIELD,
              FIELD_MEASUREMENT,
              FIELD_TIME,
              FIELD_MARKETPRICE,
              FIELD_PRICE_PER_KWH,
              FIELD_START_TIME_FORMATTED,
              FIELD_END_TIME_FORMATTED,
              FIELD_END_TIMESTAMP
      ).contains(key)) {
        metadata.put(key, valueObj != null ? valueObj.toString() : "");
      }
    });

    return PriceData.builder()
            .startTimestamp(startTimestamp)
            .endTimestamp(endTimestamp != null ? endTimestamp : startTimestamp)
            .marketprice(marketprice)
            .priceInEurPerKwh(pricePerKwh)
            .startTimeFormatted(startFormatted != null ? startFormatted : "")
            .endTimeFormatted(endFormatted != null ? endFormatted : "")
            .metadata(metadata)
            .build();
  }

  private Double getDoubleValue(FluxRecord fluxRecord, String key) {
    Object value = fluxRecord.getValueByKey(key);
    return (value instanceof Number number) ? number.doubleValue() : 0.0;
  }

  private String getStringValue(FluxRecord fluxRecord, String key) {
    Object value = fluxRecord.getValueByKey(key);
    return value != null ? value.toString() : "";
  }

  private Instant parseInstantField(FluxRecord fluxRecord, String key) {
    Object value = fluxRecord.getValueByKey(key);
    if (value instanceof String str) return Instant.parse(str);
    return null;
  }
}
