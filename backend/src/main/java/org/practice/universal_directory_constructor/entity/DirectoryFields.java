package org.practice.universal_directory_constructor.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@AllArgsConstructor
@NoArgsConstructor
@Data
public class DirectoryFields {
    private String name;
    private FieldsType type;
    private Long directoryId;
}
