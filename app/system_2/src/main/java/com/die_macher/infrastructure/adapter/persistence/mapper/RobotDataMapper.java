package com.die_macher.infrastructure.adapter.persistence.mapper;

import com.die_macher.domain.model.robot.RobotData;
import com.die_macher.domain.model.robot.RobotStatus;
import com.die_macher.domain.model.robot.RobotTask;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import com.influxdb.query.FluxRecord;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class RobotDataMapper {

  public Point toPoint(RobotData robotData) {
    Point point =
        Point.measurement("robot_data")
            .addField("robot_status", robotData.robotStatus().name())
            .addField("robot_task", robotData.robotTask().name())
            .addField("color", robotData.color())
            .time(robotData.timestamp().toEpochMilli(), WritePrecision.MS);

    robotData.metadata().forEach(point::addTag);

    return point;
  }

  public RobotData fromFluxRecord(FluxRecord record) {
    String robotStatusStr = getStringValue(record, "robot_status");
    RobotStatus robotStatus = RobotStatus.valueOf(robotStatusStr);

    String robotTaskStr = getStringValue(record, "robot_task");
    RobotTask robotTask = RobotTask.valueOf(robotTaskStr);

    String color = getStringValue(record, "color");

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
                      "robot_status",
                      "robot_task",
                      "color")
                  .contains(key)) {
                metadata.put(key, valueObj != null ? valueObj.toString() : "");
              }
            });

    return RobotData.builder()
        .robotTask(robotTask)
        .robotStatus(robotStatus)
        .color(color)
        .timestamp(timestamp)
        .metadata(metadata)
        .build();
  }

  private String getStringValue(FluxRecord record, String key) {
    Object value = record.getValueByKey(key);
    return value != null ? value.toString() : "";
  }
}
