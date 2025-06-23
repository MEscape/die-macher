package com.die_macher.infrastructure.adapter.persistence;

import com.die_macher.domain.model.price.PriceData;
import com.die_macher.domain.port.outbound.PriceDataRepository;
import com.die_macher.infrastructure.adapter.persistence.mapper.PriceDataMapper;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.springframework.stereotype.Repository;

@Repository
public class InfluxDbPriceDataRepository implements PriceDataRepository {

  private final InfluxDbGenericRepository<PriceData> genericRepository;
  private final PriceDataMapper mapper;
  private static final String MEASUREMENT = "price_data";

  public InfluxDbPriceDataRepository(
      InfluxDbGenericRepository<PriceData> genericRepository, PriceDataMapper mapper) {
    this.genericRepository = genericRepository;
    this.mapper = mapper;
  }

  @Override
  public CompletableFuture<Void> save(PriceData priceData) {
    return genericRepository.save(priceData, mapper::toPoint);
  }

  @Override
  public CompletableFuture<List<PriceData>> findByTimeRange(Instant start, Instant end) {
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
  public CompletableFuture<PriceData> aggregatePriceData(
      String field, Instant start, Instant end, String interval) {

    String query =
        String.format(
            """
        from(bucket: "%s")
          |> range(start: %s, stop: %s)
          |> filter(fn: (r) => r["_measurement"] == "%s")
          |> filter(fn: (r) => r["_field"] == "%s")
          |> aggregateWindow(every: %s, fn: mean, createEmpty: false)
          |> pivot(rowKey:["_time"], columnKey: ["_field"], valueColumn: "_value")
        """,
            genericRepository.getBucket(), start, end, MEASUREMENT, field, interval);

    return genericRepository.querySingle(query, mapper::fromFluxRecord);
  }
}
