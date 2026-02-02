package org.practice.universal_directory_constructor.dto;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.practice.universal_directory_constructor.entity.FieldsType;
import org.practice.universal_directory_constructor.util.validation.DirectoryIdValidationForType;

@DirectoryIdValidationForType
public record DirectoryFieldsDTO(
        @NotBlank String name,
        @NotNull FieldsType type,
        @Nullable Long directoryId
) {
}
