package org.practice.universal_directory_constructor.dto.recordCheckReference;

public record ReferenceCheckDTO(
        String name,
        Long directoryId,
        Long referenceId
) {
}
