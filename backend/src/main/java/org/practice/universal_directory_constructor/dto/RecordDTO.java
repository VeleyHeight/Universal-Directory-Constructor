package org.practice.universal_directory_constructor.dto;

import jakarta.validation.constraints.NotNull;

import java.util.Map;

public record RecordDTO(
        Long id,
        @NotNull
        Map<String, Object> values
) {
}
