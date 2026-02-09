package org.practice.universal_directory_constructor.service.impl;

import lombok.RequiredArgsConstructor;

import org.practice.universal_directory_constructor.dto.recordCheckReference.ReferenceCheckDTO;
import org.practice.universal_directory_constructor.dto.recordCheckReference.RepositoryResultDTO;
import org.practice.universal_directory_constructor.entity.DirectoryFields;
import org.practice.universal_directory_constructor.entity.FieldsType;
import org.practice.universal_directory_constructor.dto.RecordDTO;
import org.practice.universal_directory_constructor.filter.RecordFilter;
import org.practice.universal_directory_constructor.mapper.RecordMapper;
import org.practice.universal_directory_constructor.repository.DirectoryRepository;
import org.practice.universal_directory_constructor.repository.RecordRepository;
import org.practice.universal_directory_constructor.service.RecordService;
import org.practice.universal_directory_constructor.util.validator.RecordReferenceValidator;
import org.practice.universal_directory_constructor.util.validator.RecordValuesValidator;
import org.springframework.data.domain.Pageable;
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
    private final RecordValuesValidator valuesValidator;
    private final RecordReferenceValidator referenceValidator;

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

        valuesValidator.validateAndType(directory.getFields(), dto.values());
        referenceValidator.validateReferences(directory.getFields(), dto.values());
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

}