package org.practice.universal_directory_constructor.service;

import org.practice.universal_directory_constructor.dto.directoriesCountData.DirectoryAndCount;
import org.practice.universal_directory_constructor.dto.DirectoryDTOCreate;
import org.practice.universal_directory_constructor.dto.DirectoryDTO;

import java.util.List;

public interface DirectoryService {
    DirectoryDTO create(DirectoryDTOCreate dto);
    List<DirectoryAndCount> findAll();
    DirectoryDTO update(Long id, DirectoryDTO dto);
}
