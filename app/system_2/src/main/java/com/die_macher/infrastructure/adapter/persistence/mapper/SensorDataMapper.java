package com.die_macher.infrastructure.adapter.persistence.mapper;

import com.die_macher.domain.model.SensorData;
import com.die_macher.domain.model.SensorType;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import com.influxdb.query.FluxRecord;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class SensorDataMapper {

  public Point toPoint(SensorData sensorData) {
    Point point =
        Point.measurement("sensor_data")
            .addTag("sensor_id", sensorData.sensorId())
            .addTag("type", sensorData.type().name())
            .addField("value", sensorData.value())
            .addField("unit", sensorData.unit())
            .time(sensorData.timestamp().toEpochMilli(), WritePrecision.MS);

    sensorData.metadata().forEach(point::addTag);

    return point;
  }

  public SensorData fromFluxRecord(FluxRecord record) {
    String sensorId = getStringValue(record, "sensor_id");
    String typeStr = getStringValue(record, "type");
    SensorType type = SensorType.valueOf(typeStr);

    Double value = getDoubleValue(record, "value");
    String unit = getStringValue(record, "unit");
    Instant timestamp = record.getTime();

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
                      "sensor_id",
                      "type",
                      "value",
                      "unit")
                  .contains(key)) {
                metadata.put(key, valueObj != null ? valueObj.toString() : "");
              }
            });

    return SensorData.builder()
        .sensorId(sensorId)
        .type(type)
        .value(value)
        .unit(unit)
        .timestamp(timestamp)
        .metadata(metadata)
        .build();
  }

  private Double getDoubleValue(FluxRecord record, String key) {
    Object value = record.getValueByKey(key);
    if (value instanceof Number number) {
      return number.doubleValue();
    }
    return null;
  }

  private String getStringValue(FluxRecord record, String key) {
    Object value = record.getValueByKey(key);
    return value != null ? value.toString() : "";
  }
}
