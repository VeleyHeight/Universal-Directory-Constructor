package org.practice.universal_directory_constructor.dto.recordCheckReference;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public record ReferenceCheckDTO(
        String name,
        Long directoryId,
        Long referenceId
) {
}
