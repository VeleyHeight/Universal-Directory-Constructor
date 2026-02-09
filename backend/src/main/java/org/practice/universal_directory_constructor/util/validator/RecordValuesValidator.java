package org.practice.universal_directory_constructor.util.validator;

import org.practice.universal_directory_constructor.entity.DirectoryFields;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.practice.universal_directory_constructor.entity.FieldsType.*;

@Component
public class RecordValuesValidator {

    public void validateAndType(List<DirectoryFields> fields, Map<String, Object> values) {
        if (values == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "values must not be null");
        }

        fields.forEach(f -> {
            Object raw = values.get(f.getName());

            if (f.getType() == DIRECTORY_REFERENCE && f.getDirectoryId() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Field '%s': directoryId is not set for DIRECTORY_REFERENCE".formatted(f.getName()));
            }

            Object normalized = switch (f.getType()) {
                case STRING -> checkNonBlankString(raw, f.getName());
                case NUMBER -> checkBigDecimal(raw, f.getName());
                case DIRECTORY_REFERENCE -> checkLongSelected(raw, f.getName());
            };

            values.put(f.getName(), normalized);
        });
    }

    private String checkNonBlankString(Object value, String fieldName) {
        if (value == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Field '%s' must not be empty".formatted(fieldName));
        String s = String.valueOf(value).trim();
        if (s.isEmpty())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Field '%s' must not be empty".formatted(fieldName));
        return s;
    }

    private BigDecimal checkBigDecimal(Object value, String fieldName) {
        if (value == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Field '%s' must be a number".formatted(fieldName));

        if (value instanceof Number n)
            return new BigDecimal(n.toString());

        if (value instanceof String s) {
            String t = s.trim();
            if (t.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Field '%s' must be a number".formatted(fieldName));
            }
            try {
                return new BigDecimal(t);
            }
            catch (NumberFormatException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Field '%s' must be a number".formatted(fieldName));
            }
        }

        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Field '%s' must be a number".formatted(fieldName));
    }

    private Long checkLongSelected(Object value, String fieldName) {
        if (value == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Field '%s' must be selected".formatted(fieldName));
        }
        if (value instanceof Number n)
            return n.longValue();

        if (value instanceof String s) {
            String t = s.trim();
            if (t.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Field '%s' must be selected".formatted(fieldName));
            }
            try {
                return Long.parseLong(t);
            }
            catch (NumberFormatException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Field '%s' must be a number (record id)".formatted(fieldName));
            }
        }

        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Field '%s' must be a number (record id)".formatted(fieldName));
    }
}
