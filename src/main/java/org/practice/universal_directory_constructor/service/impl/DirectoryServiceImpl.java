package org.practice.universal_directory_constructor.service.impl;

import lombok.RequiredArgsConstructor;
import org.practice.universal_directory_constructor.dto.DirectoryDTOCreate;
import org.practice.universal_directory_constructor.dto.DirectoryDTO;
import org.practice.universal_directory_constructor.dto.DirectoryFieldsDTO;
import org.practice.universal_directory_constructor.entity.FieldsType;
import org.practice.universal_directory_constructor.mapper.DirectoryMapper;
import org.practice.universal_directory_constructor.repository.DirectoryRepository;
import org.practice.universal_directory_constructor.service.DirectoryService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class DirectoryServiceImpl implements DirectoryService {
    private final DirectoryRepository repository;
    private final DirectoryMapper mapper;

    @Override
    public DirectoryDTO create(DirectoryDTOCreate dto) {
        existsDirectoriesInFields(dto.fields());
        var directory = mapper.toEntity(dto);
        var code = UUID.randomUUID().toString();
        directory.setCode(code);
        var result = repository.save(directory);
        return mapper.toDTO(result);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DirectoryDTO> findAll() {
        return repository.findAll().stream().map(mapper::toDTO).toList();
    }

    @Override
    public DirectoryDTO update(Long id, DirectoryDTO dto) {
        var directory = repository.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Directory with id: %s not found".formatted(id))
        );
        existsDirectoriesInFields(dto.fields());
        entityNotSelfReference(id, dto.fields());
        mapper.updateWithNull(dto, directory);
        var result = repository.save(directory);
        return mapper.toDTO(result);
    }

    private void existsDirectoriesInFields(List<DirectoryFieldsDTO> fields) {
        Set<Long> directoryIds = fields.stream()
                .filter(field -> field.type() == FieldsType.DIRECTORY_REFERENCE)
                .map(DirectoryFieldsDTO::directoryId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // Проверка на существование справочника параметров
        if (!directoryIds.isEmpty()) {
            List<Long> existingId = repository.findExistingByIds(directoryIds);
            if (existingId.size() != directoryIds.size()) {
                Set<Long> existingSet = new HashSet<>(existingId);
                List<Long> missingIds = directoryIds.stream()
                        .filter(id -> !existingSet.contains(id))
                        .toList();
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Directory with ids: %s not exists".formatted(missingIds));
            }
        }
    }

    private void entityNotSelfReference(Long id, List<DirectoryFieldsDTO> fields) {
        boolean referencesSelf = fields.stream()
                .filter(field -> field.type() == FieldsType.DIRECTORY_REFERENCE)
                .anyMatch(field -> field.directoryId() != null && field.directoryId().equals(id));
        if (referencesSelf) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Directory id cannot reference on self");
        }
    }
}
