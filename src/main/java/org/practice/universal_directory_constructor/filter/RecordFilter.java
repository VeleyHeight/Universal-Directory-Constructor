package org.practice.universal_directory_constructor.filter;

import org.practice.universal_directory_constructor.entity.DirectoryFields;
import org.practice.universal_directory_constructor.entity.Record;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;

import java.util.ArrayList;
import java.util.Locale;

import java.util.List;

public record RecordFilter(Long id, String search, List<DirectoryFields> schemaFields) {
    public Specification<Record> toSpecification() {
        return Specification.where(byDirectoryId()).and(searchRecordForDirectory());
    }

    private Specification<Record> searchRecordForDirectory() {
        return (root, query, cb) -> {
            if (search == null || search.isBlank()) {
                return cb.conjunction();
            }
            if (schemaFields == null || schemaFields.isEmpty()) {
                return cb.conjunction();
            }

            String term = "%" + search.toLowerCase(Locale.ROOT) + "%";
            List<Predicate> orPredicates = new ArrayList<>();

            for (DirectoryFields f : schemaFields) {
                // values ->> 'fieldName' (аналог через функцию)
                Expression<String> jsonValue = cb.function(
                        "jsonb_extract_path_text",
                        String.class,
                        root.get("values"),
                        cb.literal(f.getName())
                );

                // чтобы null не ломал like
                Expression<String> safeValue = cb.coalesce(jsonValue, "");

                orPredicates.add(
                        cb.like(cb.lower(safeValue), term)
                );
            }

            return cb.or(orPredicates.toArray(Predicate[]::new));
        };
    }

    private Specification<Record> byDirectoryId() {
        return (root, query, cb) -> cb.equal(root.get("directory").get("id"), id);
    }
}
