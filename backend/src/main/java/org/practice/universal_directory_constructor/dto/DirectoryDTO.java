package org.practice.universal_directory_constructor.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record DirectoryDTO(
        Long id,
        @NotBlank String name,
        @NotBlank String code,
        @NotNull @Valid List<DirectoryFieldsDTO> fields
) {
}
