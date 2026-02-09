package org.practice.universal_directory_constructor.service.impl;

import lombok.RequiredArgsConstructor;
import org.practice.universal_directory_constructor.dto.directoriesCountData.CountsForDirectory;
import org.practice.universal_directory_constructor.dto.directoriesCountData.DirectoryAndCount;
import org.practice.universal_directory_constructor.dto.DirectoryDTOCreate;
import org.practice.universal_directory_constructor.dto.DirectoryDTO;
import org.practice.universal_directory_constructor.entity.Directory;
import org.practice.universal_directory_constructor.mapper.DirectoryMapper;
import org.practice.universal_directory_constructor.repository.DirectoryRepository;
import org.practice.universal_directory_constructor.repository.RecordRepository;
import org.practice.universal_directory_constructor.service.DirectoryService;
import org.practice.universal_directory_constructor.util.validator.DirectoryReferencesValidator;
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
    private final RecordRepository recordRepository;
    private final DirectoryReferencesValidator referencesValidator;

    @Override
    public DirectoryDTO create(DirectoryDTOCreate dto) {
        referencesValidator.validateReferencedDirectoriesExist(dto.fields());
        var directory = mapper.toEntity(dto);
        var code = UUID.randomUUID().toString();
        directory.setCode(code);
        var result = repository.save(directory);
        return mapper.toDTO(result);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DirectoryAndCount> findAll() {
        var directories = repository.findAll();
        Set<Long> ids = directories.stream().map(Directory::getId).collect(Collectors.toSet());
        Map<Long, Long> directoryAndCount = recordRepository.findCountForDirectory(ids)
                .stream().collect(Collectors.toMap(CountsForDirectory::dirId, CountsForDirectory::recCount));
        return directories.stream().map(directory ->
                mapper.directoryAndCounts(directory, (long) directory.getFields().size(),
                        directoryAndCount.getOrDefault(directory.getId(),0L))).toList();
    }

    @Override
    public DirectoryDTO update(Long id, DirectoryDTO dto) {
        var directory = repository.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Directory with id: %s not found".formatted(id))
        );
        referencesValidator.validateReferencedDirectoriesExist(dto.fields());
        referencesValidator.validateNotSelfReference(id, dto.fields());
        mapper.updateWithNull(dto, directory);
        var result = repository.save(directory);
        return mapper.toDTO(result);
    }

}
