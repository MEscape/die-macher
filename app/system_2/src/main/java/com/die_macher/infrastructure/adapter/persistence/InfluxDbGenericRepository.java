package com.die_macher.infrastructure.adapter.persistence;

import com.die_macher.domain.exception.DataPersistenceException;
import com.die_macher.domain.exception.DataQueryException;
import com.die_macher.infrastructure.adapter.web.exception.DataNotFoundException;
import com.die_macher.infrastructure.config.properties.InfluxDbProperties;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.QueryApi;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.write.Point;
import com.influxdb.query.FluxRecord;
import lombok.Getter;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

@Repository
public class InfluxDbGenericRepository<T> {

    private final InfluxDBClient influxDBClient;

    @Getter
    private final String bucket;

    private final String organization;

    public InfluxDbGenericRepository(InfluxDBClient influxDBClient,
                                     InfluxDbProperties properties) {
        this.influxDBClient = influxDBClient;
        this.bucket = properties.getBucket();
        this.organization = properties.getOrganization();
    }

    public CompletableFuture<Void> save(T entity, Function<T, Point> pointMapper) {
        return CompletableFuture.runAsync(() -> {
            try {
                WriteApiBlocking writeApi = influxDBClient.getWriteApiBlocking();
                Point point = pointMapper.apply(entity);
                writeApi.writePoint(bucket, organization, point);
            } catch (Exception e) {
                throw new DataPersistenceException("Failed to save entity", e);
            }
        });
    }

    public CompletableFuture<Void> saveBatch(List<T> entities, Function<T, Point> pointMapper) {
        return CompletableFuture.runAsync(() -> {
            try {
                WriteApiBlocking writeApi = influxDBClient.getWriteApiBlocking();
                List<Point> points = entities.stream()
                        .map(pointMapper)
                        .toList();
                writeApi.writePoints(bucket, organization, points);
            } catch (Exception e) {
                throw new DataPersistenceException("Failed to save batch", e);
            }
        });
    }

    public CompletableFuture<List<T>> query(String fluxQuery, Function<FluxRecord, T> recordMapper) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                QueryApi queryApi = influxDBClient.getQueryApi();
                return queryApi.query(fluxQuery, organization).stream()
                        .flatMap(table -> table.getRecords().stream())
                        .map(recordMapper)
                        .toList();
            } catch (Exception e) {
                throw new DataQueryException("Query execution failed", e);
            }
        });
    }

    public <T> CompletableFuture<T> querySingle(String fluxQuery, Function<FluxRecord, T> recordMapper) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                QueryApi queryApi = influxDBClient.getQueryApi();
                return queryApi.query(fluxQuery, organization).stream()
                        .flatMap(table -> table.getRecords().stream())
                        .findFirst()
                        .map(recordMapper)
                        .orElseThrow(() -> new DataNotFoundException("No result found for single query"));
            } catch (DataNotFoundException e) {
                throw e;
            } catch (Exception e) {
                throw new DataQueryException("Single query execution failed", e);
            }
        });
    }
}
