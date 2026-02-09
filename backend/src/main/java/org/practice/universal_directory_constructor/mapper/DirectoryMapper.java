package org.practice.universal_directory_constructor.mapper;

import org.mapstruct.*;
import org.practice.universal_directory_constructor.dto.DirectoryDTOCreate;
import org.practice.universal_directory_constructor.dto.DirectoryDTO;
import org.practice.universal_directory_constructor.dto.directoriesCountData.DirectoryAndCount;
import org.practice.universal_directory_constructor.entity.Directory;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface DirectoryMapper {
    DirectoryDTO toDTO(Directory directory);
    Directory toEntity(DirectoryDTOCreate dto);
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "code", ignore = true)
    void updateWithNull(DirectoryDTO dto, @MappingTarget Directory directory);
    DirectoryAndCount directoryAndCounts(Directory directory, Long fieldCount, Long recordCount);
}
