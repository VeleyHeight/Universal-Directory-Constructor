package org.practice.universal_directory_constructor.service.impl;

import lombok.RequiredArgsConstructor;

import org.practice.universal_directory_constructor.dto.DirectoryFieldsDTO;
import org.practice.universal_directory_constructor.dto.recordCheckReference.ReferenceCheckDTO;
import org.practice.universal_directory_constructor.dto.recordCheckReference.RepositoryResultDTO;
import org.practice.universal_directory_constructor.entity.DirectoryFields;
import org.practice.universal_directory_constructor.entity.FieldsType;
import org.practice.universal_directory_constructor.entity.Record;
import org.practice.universal_directory_constructor.dto.RecordDTO;
import org.practice.universal_directory_constructor.filter.RecordFilter;
import org.practice.universal_directory_constructor.mapper.RecordMapper;
import org.practice.universal_directory_constructor.repository.DirectoryRepository;
import org.practice.universal_directory_constructor.repository.RecordRepository;
import org.practice.universal_directory_constructor.service.RecordService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class RecordServiceImpl implements RecordService {
    private final RecordRepository recordRepository;
    private final DirectoryRepository directoryRepository;
    private final RecordMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public PagedModel<RecordDTO> findAllPagination(Long id, Pageable pageable, String search) {
        var directory = directoryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Directory with ID " + id + " not found"));
        var filter = new RecordFilter(id, search,directory.getFields());
        return new PagedModel<>(recordRepository.findAll(filter.toSpecification(), pageable).map(mapper::toDTO));
    }

    @Override
    @Transactional(readOnly = true)
    public List<RecordDTO> findAllForDirectory(Long id, String search) {
        var directory = directoryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Directory with ID " + id + " not found"));
        var filter = new RecordFilter(id, search,directory.getFields());
        var result = recordRepository.findAll(filter.toSpecification());
        return result.stream().map(mapper::toDTO).toList();
    }

    @Override
    public RecordDTO save(Long id, RecordDTO dto) {
        if (dto.id() != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    String.format("Record with ID %d already exists", dto.id()));
        }
        var directory = directoryRepository.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Directory with ID " + id + " not found"));
        existReference(directory.getFields(), dto.values());
        var record = mapper.toEntity(directory, dto);
        var result = recordRepository.save(record);
        return mapper.toDTO(result);
    }

    @Override
    public void delete(Long id) {
        if (!recordRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("Record with ID %d does not exist", id));
        }
        recordRepository.deleteById(id);
    }

    private void existReference(List<DirectoryFields> fields, Map<String, Object> values) {
        List<ReferenceCheckDTO> checks = fields.stream()
                .filter(f -> f.getType() == FieldsType.DIRECTORY_REFERENCE)
                .map(f -> new ReferenceCheckDTO(
                        f.getName(),
                        f.getDirectoryId(),
                        checkLongType(values.get(f.getName()), f.getName())
                ))
                .filter(c -> c.referenceId() != null)
                .toList();
        if (checks.isEmpty()) return;
        Set<Long> refIds = checks.stream().map(ReferenceCheckDTO::referenceId).collect(Collectors.toSet());
        var allRecords = recordRepository.findIdAndDirIdByRecordIds(refIds);
        var dirAndRecordIds = allRecords.stream()
                .collect(Collectors.toMap(RepositoryResultDTO::recordId, RepositoryResultDTO::directoryId));
        for (ReferenceCheckDTO result : checks) {
            Long actualDirId = dirAndRecordIds.get(result.referenceId());
            if (actualDirId == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Invalid reference in field '%s': record %d not found"
                                .formatted(result.name(), result.referenceId()));
            }
            if (!Objects.equals(actualDirId, result.directoryId())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Invalid reference in field '%s': record %d belongs to directory %d, expected %d"
                                .formatted(result.name(), result.referenceId(), actualDirId, result.directoryId()));
            }
        }

    }

    private Long checkLongType(Object value, String fieldName) {
        if (value == null) return null;
        if (value instanceof Number n) return n.longValue();
        if (value instanceof String s && !s.isBlank()) {
            try { return Long.parseLong(s); }
            catch (NumberFormatException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Field '%s' must be a number (record id)".formatted(fieldName));
            }
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Field '%s' must be a number (record id)".formatted(fieldName));
    }

}