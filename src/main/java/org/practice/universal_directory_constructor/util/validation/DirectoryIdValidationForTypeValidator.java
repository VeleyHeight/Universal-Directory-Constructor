package org.practice.universal_directory_constructor.util.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.practice.universal_directory_constructor.dto.DirectoryFieldsDTO;
import org.practice.universal_directory_constructor.entity.DirectoryFields;
import org.practice.universal_directory_constructor.entity.FieldsType;

public class DirectoryIdValidationForTypeValidator implements ConstraintValidator<DirectoryIdValidationForType, DirectoryFieldsDTO> {

    @Override
    public boolean isValid(DirectoryFieldsDTO fields, ConstraintValidatorContext constraintValidatorContext) {
        if (fields == null) {
            return true;
        }
        FieldsType type = fields.type();
        if (type == FieldsType.DIRECTORY_REFERENCE) {
            return !(fields.directoryId() == null);
        }
        else {
            return fields.directoryId() == null;
        }
    }
}
