package com.die_macher.pick_and_place.dobot.config;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

@SpringBootTest
class DobotConfigTest {

  @Autowired private ApplicationContext context;

  @Test
  @DisplayName("Should create DobotSerialConnector bean")
  void testDobotSerialConnectorBeanCreation() {
    // Arrange & Act
    DobotSerialConnector connector = context.getBean(DobotSerialConnector.class);

    // Assert
    assertNotNull(connector, "DobotSerialConnector bean should be created");
  }

  @Test
  @DisplayName("Should create a new instance of DobotSerialConnector")
  void testDobotSerialConnectorMethod() {
    // Arrange
    DobotConfig config = new DobotConfig();

    // Act
    DobotSerialConnector connector = config.dobotSerialConnector();

    // Assert
    assertNotNull(connector, "dobotSerialConnector() should return a non-null instance");
    assertTrue(
        connector instanceof DobotSerialConnector,
        "Should return an instance of DobotSerialConnector");
  }

  @Test
  @DisplayName("Should create different instances when called multiple times")
  void testDobotSerialConnectorMultipleInstances() {
    // Arrange
    DobotConfig config = new DobotConfig();

    // Act
    DobotSerialConnector connector1 = config.dobotSerialConnector();
    DobotSerialConnector connector2 = config.dobotSerialConnector();

    // Assert
    assertNotNull(connector1, "First connector instance should not be null");
    assertNotNull(connector2, "Second connector instance should not be null");
    assertNotSame(connector1, connector2, "Should create different instances each time");
  }
}
