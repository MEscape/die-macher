package com.die_macher.infrastructure.adapter.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

@Data
public class CubeManipulationRequest {

  @JsonProperty("color")
  @NotBlank
  private String color;

  @JsonProperty("action")
  @NotNull
  private Action action;

  public enum Action {
    ADD,
    REMOVE
  }
}
