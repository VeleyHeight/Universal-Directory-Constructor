package org.practice.universal_directory_constructor.util.annotation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.practice.universal_directory_constructor.dto.DirectoryFieldsDTO;
import org.practice.universal_directory_constructor.entity.FieldsType;

public class DirectoryIdValidationForTypeValidator implements ConstraintValidator<DirectoryIdValidationForType, DirectoryFieldsDTO> {

    @Override
    public boolean isValid(DirectoryFieldsDTO fields, ConstraintValidatorContext ctx) {
        if (fields == null) {
            return true;
        }
        ctx.disableDefaultConstraintViolation();

        if (fields.type() == FieldsType.DIRECTORY_REFERENCE) {
            if (fields.directoryId() == null) {
                ctx.buildConstraintViolationWithTemplate("directoryId is required for DIRECTORY_REFERENCE")
                        .addPropertyNode("directoryId")
                        .addConstraintViolation();
                return false;
            }
            return true;
        }

        if (fields.directoryId() != null) {
            ctx.buildConstraintViolationWithTemplate("directoryId must be null unless type is DIRECTORY_REFERENCE")
                    .addPropertyNode("directoryId")
                    .addConstraintViolation();
            return false;
        }

        return true;
    }
}
