package com.die_macher.infrastructure.adapter.persistence;

import com.die_macher.domain.model.robot.RobotData;
import com.die_macher.domain.port.outbound.RobotDataRepository;
import com.die_macher.infrastructure.adapter.persistence.mapper.RobotDataMapper;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.springframework.stereotype.Repository;

@Repository
public class InfluxDbRobotDataRepository implements RobotDataRepository {

  private final InfluxDbGenericRepository<RobotData> genericRepository;
  private final RobotDataMapper mapper;
  private static final String MEASUREMENT = "robot_data";

  public InfluxDbRobotDataRepository(
      InfluxDbGenericRepository<RobotData> genericRepository, RobotDataMapper mapper) {
    this.genericRepository = genericRepository;
    this.mapper = mapper;
  }

  @Override
  public CompletableFuture<Void> save(RobotData robotData) {
    return genericRepository.save(robotData, mapper::toPoint);
  }

  @Override
  public CompletableFuture<List<RobotData>> findByTimeRange(Instant start, Instant end) {
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
}
