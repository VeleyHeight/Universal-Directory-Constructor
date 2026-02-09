package org.practice.universal_directory_constructor.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.practice.universal_directory_constructor.dto.RecordDTO;
import org.practice.universal_directory_constructor.entity.Directory;
import org.practice.universal_directory_constructor.entity.Record;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface RecordMapper {
    RecordDTO toDTO(Record record);
    @Mapping(target = "id", ignore = true)
    Record toEntity(Directory directory, RecordDTO recordDTO);
}
