package com.die_macher.infrastructure.adapter.persistence;

import com.die_macher.domain.model.SensorData;
import com.die_macher.domain.model.SensorType;
import com.die_macher.domain.port.outbound.SensorDataRepository;
import com.die_macher.infrastructure.adapter.persistence.mapper.SensorDataMapper;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.springframework.stereotype.Repository;

@Repository
public class InfluxDbSensorDataRepository implements SensorDataRepository {

  private final InfluxDbGenericRepository<SensorData> genericRepository;
  private final SensorDataMapper mapper;
  private static final String MEASUREMENT = "sensor_data";

  public InfluxDbSensorDataRepository(
      InfluxDbGenericRepository<SensorData> genericRepository, SensorDataMapper mapper) {
    this.genericRepository = genericRepository;
    this.mapper = mapper;
  }

  @Override
  public CompletableFuture<Void> save(SensorData sensorData) {
    return genericRepository.save(sensorData, mapper::toPoint);
  }

  @Override
  public CompletableFuture<Void> saveBatch(List<SensorData> sensorDataList) {
    return genericRepository.saveBatch(sensorDataList, mapper::toPoint);
  }

  @Override
  public CompletableFuture<List<SensorData>> findBySensorIdAndTimeRange(
      String sensorId, Instant start, Instant end) {

    String query =
        String.format(
            """
            from(bucket: "%s")
              |> range(start: %s, stop: %s)
              |> filter(fn: (r) => r["_measurement"] == "%s")
              |> filter(fn: (r) => r["sensor_id"] == "%s")
              |> pivot(rowKey:["_time"], columnKey: ["_field"], valueColumn: "_value")
            """,
            genericRepository.getBucket(), start, end, MEASUREMENT, sensorId);

    return genericRepository.query(query, mapper::fromFluxRecord);
  }

  @Override
  public CompletableFuture<List<SensorData>> findByTimeRange(Instant start, Instant end) {
    String query =
        String.format(
            """
            from(bucket: "%s")
              |> range(start: %s, stop: %s)
              |> filter(fn: (r) => r["_measurement"] == "%s")
              |> pivot(rowKey:["_time"], columnKey: ["_field"], valueColumn: "_value")
            """,
            genericRepository.getBucket(), start, end, MEASUREMENT);

    return genericRepository.query(query, mapper::fromFluxRecord);
  }

  @Override
  public CompletableFuture<SensorData> aggregateSensorData(
      String sensorId, SensorType sensorType, Instant start, Instant end, String interval) {

    String query =
        String.format(
            """
        from(bucket: "%s")
          |> range(start: %s, stop: %s)
          |> filter(fn: (r) => r["_measurement"] == "%s")
          |> filter(fn: (r) => r["sensor_id"] == "%s")
          |> filter(fn: (r) => r["type"] == "%s")
          |> filter(fn: (r) => r["_field"] == "value")
          |> aggregateWindow(every: %s, fn: mean, createEmpty: false)
          |> pivot(rowKey:["_time"], columnKey: ["_field"], valueColumn: "_value")
        """,
            genericRepository.getBucket(),
            start,
            end,
            MEASUREMENT,
            sensorId,
            sensorType.name(),
            interval);

    return genericRepository.querySingle(query, mapper::fromFluxRecord);
  }
}
