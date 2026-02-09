package org.practice.universal_directory_constructor.util.validator;

import lombok.RequiredArgsConstructor;
import org.practice.universal_directory_constructor.dto.DirectoryFieldsDTO;
import org.practice.universal_directory_constructor.entity.FieldsType;
import org.practice.universal_directory_constructor.repository.DirectoryRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class DirectoryReferencesValidator {

    private final DirectoryRepository directoryRepository;

    public void validateReferencedDirectoriesExist(List<DirectoryFieldsDTO> fields) {
        Set<Long> directoryIds = fields.stream()
                .filter(f -> f.type() == FieldsType.DIRECTORY_REFERENCE)
                .map(DirectoryFieldsDTO::directoryId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (directoryIds.isEmpty()) return;

        List<Long> existing = directoryRepository.findExistingByIds(directoryIds);

        if (existing.size() != directoryIds.size()) {
            Set<Long> existingSet = new HashSet<>(existing);
            List<Long> missing = directoryIds.stream()
                    .filter(id -> !existingSet.contains(id))
                    .toList();

            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Directory with ids %s not exists".formatted(missing));
        }
    }

    public void validateNotSelfReference(Long directoryId, List<DirectoryFieldsDTO> fields) {
        boolean selfRef = fields.stream()
                .filter(f -> f.type() == FieldsType.DIRECTORY_REFERENCE)
                .map(DirectoryFieldsDTO::directoryId)
                .filter(Objects::nonNull)
                .anyMatch(refId -> refId.equals(directoryId));

        if (selfRef) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Directory id cannot reference on self");
        }
    }
}
