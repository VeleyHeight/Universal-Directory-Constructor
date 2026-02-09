package org.practice.universal_directory_constructor.util.validator;

import lombok.RequiredArgsConstructor;
import org.practice.universal_directory_constructor.dto.recordCheckReference.ReferenceCheckDTO;
import org.practice.universal_directory_constructor.dto.recordCheckReference.RepositoryResultDTO;
import org.practice.universal_directory_constructor.entity.DirectoryFields;
import org.practice.universal_directory_constructor.entity.FieldsType;
import org.practice.universal_directory_constructor.repository.RecordRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RecordReferenceValidator {

    private final RecordRepository recordRepository;

    public void validateReferences(List<DirectoryFields> fields, Map<String, Object> typedValues) {
        List<ReferenceCheckDTO> checks = fields.stream()
                .filter(f -> f.getType() == FieldsType.DIRECTORY_REFERENCE)
                .map(f -> new ReferenceCheckDTO(
                        f.getName(),
                        f.getDirectoryId(),
                        (Long) typedValues.get(f.getName())
                ))
                .toList();

        if (checks.isEmpty()) return;

        Set<Long> refIds = checks.stream().map(ReferenceCheckDTO::referenceId).collect(Collectors.toSet());

        var allRecords = recordRepository.findIdAndDirIdByRecordIds(refIds);
        var dirByRecordId = allRecords.stream()
                .collect(Collectors.toMap(RepositoryResultDTO::recordId, RepositoryResultDTO::directoryId));

        for (ReferenceCheckDTO c : checks) {
            Long actualDirId = dirByRecordId.get(c.referenceId());
            if (actualDirId == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Invalid reference in field '%s': record %d not found"
                                .formatted(c.name(), c.referenceId()));
            }
            if (!Objects.equals(actualDirId, c.directoryId())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Invalid reference in field '%s': record %d belongs to directory %d, expected %d"
                                .formatted(c.name(), c.referenceId(), actualDirId, c.directoryId()));
            }
        }
    }
}
