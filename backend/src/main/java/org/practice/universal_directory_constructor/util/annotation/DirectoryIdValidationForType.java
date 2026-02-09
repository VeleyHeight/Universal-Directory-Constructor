package org.practice.universal_directory_constructor.util.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;
import java.lang.annotation.Retention;

@Constraint(validatedBy = DirectoryIdValidationForTypeValidator.class)
@Target( { ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface DirectoryIdValidationForType {
    String message() default "Invalid directory id value for directory type";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
