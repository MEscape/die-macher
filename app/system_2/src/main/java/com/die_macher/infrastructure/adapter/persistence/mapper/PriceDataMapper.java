package com.die_macher.infrastructure.adapter.persistence.mapper;

import com.die_macher.domain.model.PriceData;
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

  public Point toPoint(PriceData priceData) {
    Point point =
        Point.measurement("price_data")
            .addField("total_cost", priceData.totalCost())
            .addField("price_per_kwh", priceData.priceInEurPerKwh())
            .addField("start_time_formatted", priceData.startTimeFormatted())
            .addField("end_time_formatted", priceData.endTimeFormatted())
            .time(priceData.startTimestamp().toEpochMilli(), WritePrecision.MS);

    priceData.metadata().forEach(point::addTag);

    return point;
  }

  public PriceData fromFluxRecord(FluxRecord record) {
    Instant startTimestamp = record.getTime(); // usually mapped to `_time`
    Instant endTimestamp = parseInstantField(record, "end_timestamp");

    double totalCost = getDoubleValue(record, "total_cost");
    double pricePerKwh = getDoubleValue(record, "price_per_kwh");

    String startFormatted = getStringValue(record, "start_time_formatted");
    String endFormatted = getStringValue(record, "end_time_formatted");

    Map<String, String> metadata = new HashMap<>();
    record
        .getValues()
        .forEach(
            (key, valueObj) -> {
              if (!Set.of(
                      "_value",
                      "_field",
                      "_measurement",
                      "_time",
                      "total_cost",
                      "price_per_kwh",
                      "start_time_formatted",
                      "end_time_formatted",
                      "end_timestamp")
                  .contains(key)) {
                metadata.put(key, valueObj != null ? valueObj.toString() : "");
              }
            });

    return PriceData.builder()
        .startTimestamp(startTimestamp)
        .endTimestamp(endTimestamp != null ? endTimestamp : startTimestamp)
        .totalCost(totalCost)
        .priceInEurPerKwh(pricePerKwh)
        .startTimeFormatted(startFormatted != null ? startFormatted : "")
        .endTimeFormatted(endFormatted != null ? endFormatted : "")
        .metadata(metadata)
        .build();
  }

  private Double getDoubleValue(FluxRecord record, String key) {
    Object value = record.getValueByKey(key);
    return (value instanceof Number number) ? number.doubleValue() : 0.0;
  }

  private String getStringValue(FluxRecord record, String key) {
    Object value = record.getValueByKey(key);
    return value != null ? value.toString() : "";
  }

  private Instant parseInstantField(FluxRecord record, String key) {
    Object value = record.getValueByKey(key);
    if (value instanceof String str) {
      try {
        return Instant.parse(str);
      } catch (Exception ignored) {
      }
    }
    return null;
  }
}
