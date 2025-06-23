package com.die_macher.infrastructure.adapter.web.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TomorrowDataRequest {

    @NotNull
    private Which which;

    public enum Which {
        OPTIMAL_WINDOW("optimal_window"),
        TOMORROW_PRICES("tomorrow_prices");

        private final String value;

        Which(String value) {
            this.value = value;
        }

        @JsonValue
        public String getValue() {
            return value;
        }

        @JsonCreator
        public static Which fromValue(String value) {
            for (Which w : Which.values()) {
                if (w.value.equalsIgnoreCase(value)) {
                    return w;
                }
            }
            throw new IllegalArgumentException("Unknown value: " + value);
        }
    }
}
