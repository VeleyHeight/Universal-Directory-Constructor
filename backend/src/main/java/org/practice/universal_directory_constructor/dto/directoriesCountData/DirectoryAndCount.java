package org.practice.universal_directory_constructor.dto.directoriesCountData;

import org.practice.universal_directory_constructor.dto.DirectoryFieldsDTO;

import java.util.List;

public record DirectoryAndCount(
        Long id,
        String name,
        String code,
        List<DirectoryFieldsDTO> fields,
        Long fieldCount,
        Long recordCount
) {
}
