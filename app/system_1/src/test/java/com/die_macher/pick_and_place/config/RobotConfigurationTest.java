package com.die_macher.pick_and_place.config;

import static org.junit.jupiter.api.Assertions.*;

import com.die_macher.pick_and_place.model.Position;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class RobotConfigurationTest {

  private Validator validator;
  private RobotConfiguration.MovementProfile validFastMovement;
  private RobotConfiguration.MovementProfile validSlowMovement;
  private RobotConfiguration.RobotPositions validPositions;
  private RobotConfiguration.PhysicalConstants validPhysicalConstants;

  @BeforeEach
  void setUp() {
    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    validator = factory.getValidator();

    validFastMovement = new RobotConfiguration.MovementProfile(500, 400, 300, 200);
    validSlowMovement = new RobotConfiguration.MovementProfile(100, 80, 60, 40);

    validPositions =
        new RobotConfiguration.RobotPositions(
            new Position(0f, 0f, 0f, 0f), // startPoint
            new Position(10f, 10f, 5f, 0f), // pickupPoint
            new Position(20f, 20f, 15f, 0f), // camera
            new Position(30f, 30f, 5f, 0f), // red
            new Position(40f, 40f, 5f, 0f), // green
            new Position(50f, 50f, 5f, 0f), // blue
            new Position(60f, 60f, 5f, 0f) // yellow
            );

    validPhysicalConstants = new RobotConfiguration.PhysicalConstants(0f, 2.5f, 1f, 20f);
  }

  @Nested
  @DisplayName("RobotConfiguration Tests")
  class RobotConfigurationTests {

    @Test
    @DisplayName("Should create valid RobotConfiguration")
    void shouldCreateValidRobotConfiguration() {
      RobotConfiguration config =
          new RobotConfiguration(
              validFastMovement, validSlowMovement, validPositions, validPhysicalConstants);

      Set<ConstraintViolation<RobotConfiguration>> violations = validator.validate(config);
      assertTrue(violations.isEmpty());

      assertEquals(validFastMovement, config.fastMovement());
      assertEquals(validSlowMovement, config.slowMovement());
      assertEquals(validPositions, config.positions());
      assertEquals(validPhysicalConstants, config.physicalConstants());
    }

    @Test
    @DisplayName("Should fail validation when fastMovement is null")
    void shouldFailValidationWhenFastMovementIsNull() {
      RobotConfiguration config =
          new RobotConfiguration(null, validSlowMovement, validPositions, validPhysicalConstants);

      Set<ConstraintViolation<RobotConfiguration>> violations = validator.validate(config);
      assertFalse(violations.isEmpty());
    }

    @Test
    @DisplayName("Should fail validation when slowMovement is null")
    void shouldFailValidationWhenSlowMovementIsNull() {
      RobotConfiguration config =
          new RobotConfiguration(validFastMovement, null, validPositions, validPhysicalConstants);

      Set<ConstraintViolation<RobotConfiguration>> violations = validator.validate(config);
      assertFalse(violations.isEmpty());
    }

    @Test
    @DisplayName("Should fail validation when positions is null")
    void shouldFailValidationWhenPositionsIsNull() {
      RobotConfiguration config =
          new RobotConfiguration(
              validFastMovement, validSlowMovement, null, validPhysicalConstants);

      Set<ConstraintViolation<RobotConfiguration>> violations = validator.validate(config);
      assertFalse(violations.isEmpty());
    }

    @Test
    @DisplayName("Should fail validation when physicalConstants is null")
    void shouldFailValidationWhenPhysicalConstantsIsNull() {
      RobotConfiguration config =
          new RobotConfiguration(validFastMovement, validSlowMovement, validPositions, null);

      Set<ConstraintViolation<RobotConfiguration>> violations = validator.validate(config);
      assertFalse(violations.isEmpty());
    }
  }

  @Nested
  @DisplayName("MovementProfile Tests")
  class MovementProfileTests {

    @Test
    @DisplayName("Should create valid MovementProfile")
    void shouldCreateValidMovementProfile() {
      RobotConfiguration.MovementProfile profile =
          new RobotConfiguration.MovementProfile(100, 200, 300, 400);

      Set<ConstraintViolation<RobotConfiguration.MovementProfile>> violations =
          validator.validate(profile);
      assertTrue(violations.isEmpty());

      assertEquals(100, profile.xyzVelocity());
      assertEquals(200, profile.rVelocity());
      assertEquals(300, profile.xyzAcceleration());
      assertEquals(400, profile.rAcceleration());
    }

    @Test
    @DisplayName("Should accept minimum valid values")
    void shouldAcceptMinimumValidValues() {
      RobotConfiguration.MovementProfile profile =
          new RobotConfiguration.MovementProfile(1, 1, 1, 1);

      Set<ConstraintViolation<RobotConfiguration.MovementProfile>> violations =
          validator.validate(profile);
      assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("Should accept maximum valid values")
    void shouldAcceptMaximumValidValues() {
      RobotConfiguration.MovementProfile profile =
          new RobotConfiguration.MovementProfile(1000, 1000, 1000, 1000);

      Set<ConstraintViolation<RobotConfiguration.MovementProfile>> violations =
          validator.validate(profile);
      assertTrue(violations.isEmpty());
    }
  }

  @Nested
  @DisplayName("RobotPositions Tests")
  class RobotPositionsTests {

    @Test
    @DisplayName("Should create valid RobotPositions")
    void shouldCreateValidRobotPositions() {
      Set<ConstraintViolation<RobotConfiguration.RobotPositions>> violations =
          validator.validate(validPositions);
      assertTrue(violations.isEmpty());

      assertNotNull(validPositions.startPoint());
      assertNotNull(validPositions.pickupPoint());
      assertNotNull(validPositions.camera());
      assertNotNull(validPositions.red());
      assertNotNull(validPositions.green());
      assertNotNull(validPositions.blue());
      assertNotNull(validPositions.yellow());
    }

    @Test
    @DisplayName("Should fail validation when startPoint is null")
    void shouldFailValidationWhenStartPointIsNull() {
      RobotConfiguration.RobotPositions positions =
          new RobotConfiguration.RobotPositions(
              null,
              validPositions.pickupPoint(),
              validPositions.camera(),
              validPositions.red(),
              validPositions.green(),
              validPositions.blue(),
              validPositions.yellow());

      Set<ConstraintViolation<RobotConfiguration.RobotPositions>> violations =
          validator.validate(positions);
      assertFalse(violations.isEmpty());
      assertTrue(
          violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("startPoint")));
    }

    @Test
    @DisplayName("Should fail validation when pickupPoint is null")
    void shouldFailValidationWhenPickupPointIsNull() {
      RobotConfiguration.RobotPositions positions =
          new RobotConfiguration.RobotPositions(
              validPositions.startPoint(),
              null,
              validPositions.camera(),
              validPositions.red(),
              validPositions.green(),
              validPositions.blue(),
              validPositions.yellow());

      Set<ConstraintViolation<RobotConfiguration.RobotPositions>> violations =
          validator.validate(positions);
      assertFalse(violations.isEmpty());
      assertTrue(
          violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("pickupPoint")));
    }

    @Test
    @DisplayName("Should fail validation when camera is null")
    void shouldFailValidationWhenCameraIsNull() {
      RobotConfiguration.RobotPositions positions =
          new RobotConfiguration.RobotPositions(
              validPositions.startPoint(),
              validPositions.pickupPoint(),
              null,
              validPositions.red(),
              validPositions.green(),
              validPositions.blue(),
              validPositions.yellow());

      Set<ConstraintViolation<RobotConfiguration.RobotPositions>> violations =
          validator.validate(positions);
      assertFalse(violations.isEmpty());
      assertTrue(
          violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("camera")));
    }

    @Test
    @DisplayName("Should fail validation when red is null")
    void shouldFailValidationWhenRedIsNull() {
      RobotConfiguration.RobotPositions positions =
          new RobotConfiguration.RobotPositions(
              validPositions.startPoint(),
              validPositions.pickupPoint(),
              validPositions.camera(),
              null,
              validPositions.green(),
              validPositions.blue(),
              validPositions.yellow());

      Set<ConstraintViolation<RobotConfiguration.RobotPositions>> violations =
          validator.validate(positions);
      assertFalse(violations.isEmpty());
      assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("red")));
    }

    @Test
    @DisplayName("Should fail validation when green is null")
    void shouldFailValidationWhenGreenIsNull() {
      RobotConfiguration.RobotPositions positions =
          new RobotConfiguration.RobotPositions(
              validPositions.startPoint(),
              validPositions.pickupPoint(),
              validPositions.camera(),
              validPositions.red(),
              null,
              validPositions.blue(),
              validPositions.yellow());

      Set<ConstraintViolation<RobotConfiguration.RobotPositions>> violations =
          validator.validate(positions);
      assertFalse(violations.isEmpty());
      assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("green")));
    }

    @Test
    @DisplayName("Should fail validation when blue is null")
    void shouldFailValidationWhenBlueIsNull() {
      RobotConfiguration.RobotPositions positions =
          new RobotConfiguration.RobotPositions(
              validPositions.startPoint(),
              validPositions.pickupPoint(),
              validPositions.camera(),
              validPositions.red(),
              validPositions.green(),
              null,
              validPositions.yellow());

      Set<ConstraintViolation<RobotConfiguration.RobotPositions>> violations =
          validator.validate(positions);
      assertFalse(violations.isEmpty());
      assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("blue")));
    }

    @Test
    @DisplayName("Should fail validation when yellow is null")
    void shouldFailValidationWhenYellowIsNull() {
      RobotConfiguration.RobotPositions positions =
          new RobotConfiguration.RobotPositions(
              validPositions.startPoint(),
              validPositions.pickupPoint(),
              validPositions.camera(),
              validPositions.red(),
              validPositions.green(),
              validPositions.blue(),
              null);

      Set<ConstraintViolation<RobotConfiguration.RobotPositions>> violations =
          validator.validate(positions);
      assertFalse(violations.isEmpty());
      assertTrue(
          violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("yellow")));
    }
  }

  @Nested
  @DisplayName("PhysicalConstants Tests")
  class PhysicalConstantsTests {

    @Test
    @DisplayName("Should create valid PhysicalConstants")
    void shouldCreateValidPhysicalConstants() {
      Set<ConstraintViolation<RobotConfiguration.PhysicalConstants>> violations =
          validator.validate(validPhysicalConstants);
      assertTrue(violations.isEmpty());

      assertEquals(0f, validPhysicalConstants.absoluteFloor());
      assertEquals(2.5f, validPhysicalConstants.cubeHeight());
      assertEquals(1f, validPhysicalConstants.offset());
    }

    @Test
    @DisplayName("Should create PhysicalConstants with different values")
    void shouldCreatePhysicalConstantsWithDifferentValues() {
      RobotConfiguration.PhysicalConstants constants =
          new RobotConfiguration.PhysicalConstants(-5.5f, 10.25f, 0.75f, 20f);

      Set<ConstraintViolation<RobotConfiguration.PhysicalConstants>> violations =
          validator.validate(constants);
      assertTrue(violations.isEmpty());

      assertEquals(-5.5f, constants.absoluteFloor());
      assertEquals(10.25f, constants.cubeHeight());
      assertEquals(0.75f, constants.offset());
    }
  }

  @Nested
  @DisplayName("Record Equality and HashCode Tests")
  class RecordEqualityTests {

    @Test
    @DisplayName("Should have proper equality for RobotConfiguration")
    void shouldHaveProperEqualityForRobotConfiguration() {
      RobotConfiguration config1 =
          new RobotConfiguration(
              validFastMovement, validSlowMovement, validPositions, validPhysicalConstants);
      RobotConfiguration config2 =
          new RobotConfiguration(
              validFastMovement, validSlowMovement, validPositions, validPhysicalConstants);

      assertEquals(config1, config2);
      assertEquals(config1.hashCode(), config2.hashCode());
    }

    @Test
    @DisplayName("Should have proper equality for MovementProfile")
    void shouldHaveProperEqualityForMovementProfile() {
      RobotConfiguration.MovementProfile profile1 =
          new RobotConfiguration.MovementProfile(100, 200, 300, 400);
      RobotConfiguration.MovementProfile profile2 =
          new RobotConfiguration.MovementProfile(100, 200, 300, 400);

      assertEquals(profile1, profile2);
      assertEquals(profile1.hashCode(), profile2.hashCode());
    }

    @Test
    @DisplayName("Should have proper toString for all records")
    void shouldHaveProperToStringForAllRecords() {
      assertNotNull(validFastMovement.toString());
      assertNotNull(validPositions.toString());
      assertNotNull(validPhysicalConstants.toString());

      RobotConfiguration config =
          new RobotConfiguration(
              validFastMovement, validSlowMovement, validPositions, validPhysicalConstants);
      assertNotNull(config.toString());
    }
  }
}
